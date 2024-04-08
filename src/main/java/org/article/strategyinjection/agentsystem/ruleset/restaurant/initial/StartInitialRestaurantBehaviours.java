package org.article.strategyinjection.agentsystem.ruleset.restaurant.initial;

import static org.jrba.rulesengine.constants.RuleTypeConstants.DEFAULT_MESSAGE_LISTENER_RULE;

import java.util.Set;

import org.article.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.listen.ListenForMessages;
import org.jrba.rulesengine.rule.simple.AgentBehaviourRule;

import jade.core.behaviours.Behaviour;

public class StartInitialRestaurantBehaviours
		extends AgentBehaviourRule<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> {

	public StartInitialRestaurantBehaviours(
			final RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	@Override
	protected Set<Behaviour> initializeBehaviours() {
		return Set.of(
				ListenForMessages.create(agent, DEFAULT_MESSAGE_LISTENER_RULE, controller, true)
		);
	}
}
