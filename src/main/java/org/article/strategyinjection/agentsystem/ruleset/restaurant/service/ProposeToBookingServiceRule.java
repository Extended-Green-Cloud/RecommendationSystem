package org.article.strategyinjection.agentsystem.ruleset.restaurant.service;

import static jade.lang.acl.ACLMessage.PROPOSE;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.util.Optional.ofNullable;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.RuleTypeConstants.DEFAULT_PROPOSAL_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;

import org.article.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.article.strategyinjection.agentsystem.domain.ClientOrder;
import org.article.strategyinjection.agentsystem.domain.ImmutableRestaurantData;
import org.article.strategyinjection.agentsystem.domain.RestaurantData;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentProposalRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.slf4j.Logger;

import jade.lang.acl.ACLMessage;

public class ProposeToBookingServiceRule
		extends AgentProposalRule<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> {

	private static final Logger logger = getLogger(ProposeToBookingServiceRule.class);

	public ProposeToBookingServiceRule(
			final RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(DEFAULT_PROPOSAL_RULE,
				"propose offer with dish price and restaurant information",
				"rule sends proposal message to Booking Agent and handles the response");
	}

	@Override
	protected ACLMessage createProposalMessage(final RuleSetFacts facts) {
		final ClientOrder clientOrder = facts.get(MESSAGE_CONTENT);
		final double price = agentProps.getDishWithPrice().get(clientOrder.getDish());
		final Map<String, Object> restaurantInfo =
				ofNullable(agentProps.getAdditionalInformation()).orElse(new HashMap<>());
		restaurantInfo.put("cuisine", agentProps.getCuisineType().name());
		restaurantInfo.put("dish", clientOrder.getDish());

		final RestaurantData responseData = ImmutableRestaurantData.builder()
				.restaurantInformation(restaurantInfo)
				.price(price)
				.build();
		final ACLMessage msg = facts.get(MESSAGE);

		return MessageBuilder.builder(msg.getOntology(), PROPOSE)
				.copy(msg.createReply())
				.withPerformative(PROPOSE)
				.withObjectContent(responseData)
				.build();
	}

	@Override
	protected void handleAcceptProposal(final ACLMessage accept, final RuleSetFacts facts) {
		logger.info("Booking Agent accepted my offer!");
	}

	@Override
	protected void handleRejectProposal(final ACLMessage reject, final RuleSetFacts facts) {
		logger.info("Booking Agent rejected my offer!");
	}
}
