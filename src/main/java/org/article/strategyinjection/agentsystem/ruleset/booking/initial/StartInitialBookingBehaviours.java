package org.article.strategyinjection.agentsystem.ruleset.booking.initial;

import java.util.Set;

import org.article.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.article.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.schedule.SchedulePeriodically;
import org.jrba.rulesengine.behaviour.search.SearchForAgents;
import org.jrba.rulesengine.rule.simple.AgentBehaviourRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.core.behaviours.Behaviour;

public class StartInitialBookingBehaviours extends AgentBehaviourRule<BookingProps, BookingNode> {

	public StartInitialBookingBehaviours(final RulesController<BookingProps, BookingNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	@Override
	protected Set<Behaviour> initializeBehaviours() {
		return Set.of(
				SearchForAgents.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						"SEARCH_OWNED_AGENTS_RULE", controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						"SENSE_EVENTS_RULE", controller)
		);
	}
}
