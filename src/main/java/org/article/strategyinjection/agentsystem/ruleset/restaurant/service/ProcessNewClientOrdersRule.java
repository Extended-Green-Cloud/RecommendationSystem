package org.article.strategyinjection.agentsystem.ruleset.restaurant.service;

import static jade.lang.acl.ACLMessage.REFUSE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.RuleTypeConstants.DEFAULT_PROPOSAL_RULE;

import org.article.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.article.strategyinjection.agentsystem.domain.ClientOrder;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateProposal;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.lang.acl.ACLMessage;

public class ProcessNewClientOrdersRule extends AgentBasicRule<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> {

	private static final Logger logger = LoggerFactory.getLogger(ProcessNewClientOrdersRule.class);

	public ProcessNewClientOrdersRule(
			final RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("BASIC_LISTENER_HANDLER",
				"process new client orders",
				"processing new client restaurant look-up orders");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientOrder clientOrder = facts.get(MESSAGE_CONTENT);

		if (agentProps.canFulfillOrder(clientOrder)) {
			return true;
		}
		final ACLMessage msg = facts.get(MESSAGE);
		agent.send(MessageBuilder.builder(msg.getOntology(), REFUSE)
				.copy(msg.createReply())
				.withStringContent("REFUSE")
				.build());
		return false;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Sending proposal for order {} to booking agent!",
				((ClientOrder) facts.get(MESSAGE_CONTENT)).getOrderId());
		agent.addBehaviour(InitiateProposal.create(agent, facts, DEFAULT_PROPOSAL_RULE, controller));
	}
}