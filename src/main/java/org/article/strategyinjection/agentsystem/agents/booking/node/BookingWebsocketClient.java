package org.article.strategyinjection.agentsystem.agents.booking.node;

import static io.micrometer.common.util.StringUtils.isNotEmpty;
import static java.lang.String.format;
import static org.jrba.rulesengine.constants.RuleTypeConstants.DEFAULT_CFP_RULE;
import static org.jrba.rulesengine.enums.rulesteptype.RuleStepTypeEnum.CFP_COMPARE_MESSAGES_STEP;
import static org.jrba.rulesengine.enums.rulesteptype.RuleStepTypeEnum.CFP_HANDLE_SELECTED_PROPOSAL_STEP;
import static org.jrba.rulesengine.enums.ruletype.AgentRuleTypeEnum.BASIC;
import static org.jrba.utils.mapper.JsonMapper.getMapper;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.article.strategyinjection.agentsystem.domain.ClientOrder;
import org.article.strategyinjection.agentsystem.domain.CuisineType;
import org.article.strategyinjection.agentsystem.domain.ImmutableClientOrder;
import org.article.strategyinjection.agentsystem.domain.RestaurantLookUpMessage;
import org.article.strategyinjection.agentsystem.domain.RestaurantOfferResponseMessage;
import org.jrba.environment.websocket.GuiWebSocketClient;
import org.jrba.exception.IncorrectMessageContentException;
import org.jrba.rulesengine.rest.domain.RuleRest;
import org.jrba.rulesengine.rest.domain.RuleSetRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import kotlin.Pair;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookingWebsocketClient extends GuiWebSocketClient {

	private static final Logger logger = LoggerFactory.getLogger(BookingWebsocketClient.class);
	final BookingNode node;

	public BookingWebsocketClient(final BookingNode node) {
		super(URI.create(("ws://localhost:8080/")));
		this.node = node;
	}

	@Override
	public void onMessage(final String message) {
		if (!message.equals("Welcoming message!")) {
			if (message.contains("RESTAURANT_LOOK_UP")) {
				handleNewClientOrder(message);
			}
			if (message.contains("ACCEPT_ORDER")) {
				handleOrderResponse(message);
			}
		}
	}

	private void handleNewClientOrder(final String message) {
		try {
			final RestaurantLookUpMessage receivedMessage = getMapper().readValue(message,
					RestaurantLookUpMessage.class);
			final int orderId = node.getLatestId().incrementAndGet();

			String additionalInstructionStrategy = "";

			if (isNotEmpty(receivedMessage.getAdditionalInstructions())) {
				additionalInstructionStrategy = createStrategyForAdditionalInstructions(
						receivedMessage.getAdditionalInstructions(), orderId);
			}

			final ClientOrder order = ImmutableClientOrder.builder()
					.orderId(orderId)
					.cuisine(CuisineType.valueOf(receivedMessage.getCuisine()))
					.dish(receivedMessage.getDish())
					.maxPrice(receivedMessage.getPrice())
					.additionalInstructions(additionalInstructionStrategy)
					.build();
			node.getClientEvents().add(new Pair<>("RESTAURANT_LOOK_UP", order));
			getConnection().send(format("Order was assigned with id: %d and is being processed!", orderId));
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	private void handleOrderResponse(final String message) {
		try {
			final RestaurantOfferResponseMessage receivedMessage = getMapper().readValue(message,
					RestaurantOfferResponseMessage.class);
			node.getClientEvents().add(new Pair<>("ACCEPT_ORDER", receivedMessage));
		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		}
	}

	private String createStrategyForAdditionalInstructions(final String instructions, final int orderId) {
		final String strategyName = "CUSTOM_INSTRUCTIONS_ORDER_" + orderId;
		final RuleRest handleProposalsRule = createProposeHandlerRule(instructions);
		final RuleRest compareProposalsRule = createComparatorRule(instructions);

		final RuleSetRest strategyRest = new RuleSetRest();
		strategyRest.setName(strategyName);
		strategyRest.setRules(new ArrayList<>(List.of(handleProposalsRule, compareProposalsRule)));

		try {
			final String json = getMapper().writeValueAsString(strategyRest);
			final RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
			final Request request = new Request.Builder()
					.url("http://localhost:5000/ruleSet")
					.post(body)
					.build();
			final OkHttpClient client = new OkHttpClient();
			final Call call = client.newCall(request);
			final Response response = call.execute();
			logger.info("Personalized client rule set was sent to server! Response: {}", response);

		} catch (JsonProcessingException e) {
			throw new IncorrectMessageContentException();
		} catch (IOException e) {
			throw new InvalidParameterException("Could not send rule set to REST!");
		}

		return strategyName;
	}

	private RuleRest createComparatorRule(final String instructions) {
		final RuleRest handleProposalsRule = new RuleRest();
		handleProposalsRule.setAgentRuleType(BASIC.getType());
		handleProposalsRule.setAgentType("BOOKING");
		handleProposalsRule.setType(DEFAULT_CFP_RULE);
		handleProposalsRule.setStepType(CFP_COMPARE_MESSAGES_STEP.getType());
		handleProposalsRule.setName("compare restaurant offers with custom filter");
		handleProposalsRule.setDescription("comparing restaurant offers with custom filter");
		handleProposalsRule.setImports(List.of(
				"import org.jrba.rulesengine.constants.FactTypeConstants;",
				"import org.jrba.utils.messages.MessageReader;",
				"import java.lang.Class;"
		));
		handleProposalsRule.setExecute("""
				bestProposal = facts.get(FactTypeConstants.CFP_BEST_MESSAGE);
				newProposal = facts.get(FactTypeConstants.CFP_NEW_PROPOSAL);
				restaurantData = MessageReader.readMessageContent(newProposal, Class.forName("org.article.strategyinjection.agentsystem.domain.RestaurantData"));
				restaurantDataBest = MessageReader.readMessageContent(bestProposal, Class.forName("org.article.strategyinjection.agentsystem.domain.RestaurantData"));
								
				if($instruction) {
				restaurantDataNewPrice = restaurantData.getPrice();
				restaurantData = restaurantDataBest;
								
				if($instruction) {
				result = (int) (restaurantDataBest.getPrice() - restaurantDataNewPrice);
				facts.put(FactTypeConstants.CFP_RESULT, result);
				}
				else {
				facts.put(FactTypeConstants.CFP_RESULT, -1);
				}
				}
				else { facts.put(FactTypeConstants.CFP_RESULT, 1); }
				""".replace("$instruction", instructions));
		return handleProposalsRule;
	}

	private RuleRest createProposeHandlerRule(final String instructions) {
		final String responseMsgLiteral = """
				There is a restaurant which fulfills the criteria.\s
				Strategy used in processing: $strategy\s
				Restaurant information:\s
				price $price,\s
				additional information $additionalInfo
				""";

		final RuleRest handleProposalsRule = new RuleRest();
		handleProposalsRule.setAgentRuleType(BASIC.getType());
		handleProposalsRule.setAgentType("BOOKING");
		handleProposalsRule.setType(DEFAULT_CFP_RULE);
		handleProposalsRule.setStepType(CFP_HANDLE_SELECTED_PROPOSAL_STEP.getType());
		handleProposalsRule.setName("look for restaurant to complete client order with custom filter");
		handleProposalsRule.setDescription(
				"process looking for restaurant to complete client order with custom filter");
		handleProposalsRule.setImports(List.of(
				"import org.jrba.rulesengine.constants.FactTypeConstants;",
				"import org.jrba.utils.messages.MessageReader;",
				"import java.lang.Class;",
				"import jade.lang.acl.ACLMessage;",
				"import org.jrba.utils.messages.MessageBuilder;"
		));
		handleProposalsRule.setExecute("""
				order = facts.get(FactTypeConstants.RESULT);
				bestProposal = facts.get(FactTypeConstants.CFP_BEST_MESSAGE);
				restaurantData = MessageReader.readMessageContent(bestProposal, Class.forName("org.article.strategyinjection.agentsystem.domain.RestaurantData"));
				if($instruction) {
				agentNode.passRestaurantMessageToClient("$msgLiteral".replace("$strategy", controller.getRuleSets().get((int) facts.get(FactTypeConstants.RULE_SET_IDX)).getName()).replace("$price", restaurantData.getPrice().toString()).replace("$additionalInfo", restaurantData.getRestaurantInformation().toString()));
				agentProps.getRestaurantForOrder().put(order.getOrderId(), bestProposal);}
				else { agent.send(MessageBuilder.builder(bestProposal.getOntology(), ACLMessage.REJECT_PROPOSAL).copy(proposalToReject.createReply()).withPerformative(ACLMessage.REJECT_PROPOSAL).withObjectContent(ACLMessage.REJECT_PROPOSAL).build()); agentNode.passRestaurantMessageToClient("No restaurants that fulfill additional instructions were found!");}
				""".replace("$instruction", instructions).replace("$msgLiteral", responseMsgLiteral));
		return handleProposalsRule;
	}
}

