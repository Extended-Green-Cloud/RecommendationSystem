package org.article.strategyinjection.agentsystem.ruleset.booking.sensor;

import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.RuleSetTypeConstants.DEFAULT_RULE_SET;
import static org.jrba.rulesengine.constants.RuleTypeConstants.DEFAULT_CFP_RULE;

import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.article.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.article.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.article.strategyinjection.agentsystem.domain.ClientOrder;
import org.article.strategyinjection.agentsystem.domain.RestaurantOfferResponseMessage;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateCallForProposal;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.lang.acl.ACLMessage;
import kotlin.Pair;

public class ListenForExternalOrdersRule extends AgentPeriodicRule<BookingProps, BookingNode> {

	private static final Logger logger = LoggerFactory.getLogger(ListenForExternalOrdersRule.class);
	private static final long TIMEOUT = 100;

	public ListenForExternalOrdersRule(final RulesController<BookingProps, BookingNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("SENSE_EVENTS_RULE",
				"listen for external client orders",
				"rule listens for external client orders");
	}

	@Override
	protected long specifyPeriod() {
		return TIMEOUT;
	}

	@Override
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		return nonNull(agentNode);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final Optional<Pair<String, Object>> latestEvent = ofNullable(agentNode.getClientEvents().poll());

		latestEvent.ifPresent(event -> {
			if (event.getFirst().equals("RESTAURANT_LOOK_UP")) {
				final ClientOrder order = (ClientOrder) event.getSecond();

				if (Strings.isNotBlank(order.getAdditionalInstructions())) {
					controller.addModifiedRuleSet(order.getAdditionalInstructions(),
							controller.getLatestLongTermRuleSetIdx().incrementAndGet());
					logger.info("Customer added personalized search instructions! Changing rule set to {}.",
							order.getAdditionalInstructions());
				}

				facts.put(RULE_SET_IDX, controller.getLatestLongTermRuleSetIdx().get());
				agentProps.getStrategyForOrder()
						.put(Integer.toString(order.getOrderId()), controller.getLatestLongTermRuleSetIdx().get());
				logger.info("New client order with id {} was received. Looking for restaurants with rule set {}.",
						order.getOrderId(),
						controller.getRuleSets().get(controller.getLatestLongTermRuleSetIdx().get()).getName());
				facts.put(RESULT, order);
				agent.addBehaviour(InitiateCallForProposal.create(agent, facts, DEFAULT_CFP_RULE, controller));
			} else {
				final RestaurantOfferResponseMessage response = (RestaurantOfferResponseMessage) event.getSecond();
				final ACLMessage restaurantMsg = agentProps.getRestaurantForOrder().get(response.getOrderId());
				agentProps.getRestaurantForOrder().remove(response.getOrderId());

				final int strategyIdx = agentProps.getStrategyForOrder()
						.remove(Integer.toString(response.getOrderId()));
				controller.removeRuleSet(agentProps.getStrategyForOrder(), strategyIdx);
				controller.addNewRuleSet(DEFAULT_RULE_SET, controller.getLatestLongTermRuleSetIdx().incrementAndGet());

				final ACLMessage message = response.getAccepted() ?
						prepareStringReply(restaurantMsg, "ACCEPT", ACCEPT_PROPOSAL) :
						prepareStringReply(restaurantMsg, "REJECT", REJECT_PROPOSAL);
				agent.send(message);
			}
		});
	}

	private static ACLMessage prepareStringReply(final ACLMessage msg, final String content,
			final Integer performative) {
		return MessageBuilder.builder(msg.getOntology(), performative)
				.copy(msg.createReply())
				.withPerformative(performative)
				.withStringContent(content)
				.build();
	}
}
