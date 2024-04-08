package org.article.strategyinjection.agentsystem.ruleset.restaurant.service;

import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static org.jrba.rulesengine.constants.RuleTypeConstants.DEFAULT_MESSAGE_LISTENER_RULE;

import org.article.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.article.strategyinjection.agentsystem.domain.ClientOrder;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

import jade.lang.acl.MessageTemplate;

public class ListenForNewClientOrdersRule extends
		AgentMessageListenerRule<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> {

	private static final MessageTemplate ORDER_TEMPLATE =
			and(MatchProtocol("NEW_CLIENT_ORDER_PROTOCOL"), MatchPerformative(CFP));

	public ListenForNewClientOrdersRule(
			final RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, ClientOrder.class, ORDER_TEMPLATE, 1, "BASIC_LISTENER_HANDLER");
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(DEFAULT_MESSAGE_LISTENER_RULE,
				"listen for new client orders",
				"listening for new client restaurant look-up orders");
	}
}