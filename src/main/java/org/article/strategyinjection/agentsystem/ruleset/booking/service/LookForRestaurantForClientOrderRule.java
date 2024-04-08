package org.article.strategyinjection.agentsystem.ruleset.booking.service;

import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.lang.String.format;
import static org.jrba.rulesengine.constants.FactTypeConstants.CFP_BEST_MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.RuleTypeConstants.DEFAULT_CFP_RULE;
import static org.jrba.utils.messages.MessageReader.readMessageContent;

import java.util.Collection;

import org.article.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.article.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.article.strategyinjection.agentsystem.domain.ClientOrder;
import org.article.strategyinjection.agentsystem.domain.RestaurantData;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentCFPRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.lang.acl.ACLMessage;

public class LookForRestaurantForClientOrderRule extends AgentCFPRule<BookingProps, BookingNode> {

	private static final Logger logger = LoggerFactory.getLogger(LookForRestaurantForClientOrderRule.class);

	public LookForRestaurantForClientOrderRule(
			final RulesController<BookingProps, BookingNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(DEFAULT_CFP_RULE,
				"look for restaurant to complete client order",
				"process looking for restaurant to complete client order");
	}

	@Override
	protected ACLMessage createCFPMessage(final RuleSetFacts facts) {
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), CFP)
				.withMessageProtocol("NEW_CLIENT_ORDER_PROTOCOL")
				.withObjectContent(facts.get(RESULT))
				.withReceivers(agentProps.getRestaurants())
				.build();
	}

	@Override
	protected int compareProposals(final RuleSetFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		final RuleSetFacts comparatorFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		comparatorFacts.put(CFP_BEST_MESSAGE, bestProposal);
		comparatorFacts.put(MESSAGE, newProposal);
		comparatorFacts.put(RULE_TYPE, "BASIC_COMPARATOR_RULE");
		controller.fire(comparatorFacts);

		return comparatorFacts.get(RESULT);
	}

	@Override
	protected void handleRejectProposal(final ACLMessage proposalToReject, final RuleSetFacts facts) {
		agent.send(MessageBuilder.builder(proposalToReject.getOntology(), REJECT_PROPOSAL)
						.copy(proposalToReject.createReply())
						.withPerformative(REJECT_PROPOSAL)
						.withObjectContent(REJECT_PROPOSAL)
						.build());
	}

	@Override
	protected void handleNoResponses(final RuleSetFacts facts) {
		logger.info("No restaurants found!");
		agentNode.passRestaurantMessageToClient("No restaurants that fulfill the criteria were found!");
	}

	@Override
	protected void handleNoProposals(final RuleSetFacts facts) {
		logger.info("No restaurants found!");
		agentNode.passRestaurantMessageToClient("No restaurants that fulfill the criteria were found!");
	}

	@Override
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final RuleSetFacts facts) {
		final ClientOrder order = facts.get(RESULT);
		final RestaurantData restaurantData = readMessageContent(bestProposal, RestaurantData.class);
		agentNode.passRestaurantMessageToClient(
				format("""
						There is a restaurant which fulfills the criteria.\s
						Strategy used in processing: %s\s
						Restaurant information:\s
						price %f,\s
						additional information %s
						""",
						controller.getRuleSets().get((int) facts.get(RULE_SET_IDX)).getName(),
						restaurantData.getPrice(),
						restaurantData.getRestaurantInformation()));
		agentProps.getRestaurantForOrder().put(order.getOrderId(), bestProposal);
	}
}
