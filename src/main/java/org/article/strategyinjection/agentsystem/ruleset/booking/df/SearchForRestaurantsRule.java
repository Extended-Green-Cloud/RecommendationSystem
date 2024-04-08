package org.article.strategyinjection.agentsystem.ruleset.booking.df;

import static org.jrba.utils.yellowpages.YellowPagesRegister.search;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Set;

import org.article.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.article.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentSearchRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;

public class SearchForRestaurantsRule extends AgentSearchRule<BookingProps, BookingNode> {

	private static final Logger logger = getLogger(SearchForRestaurantsRule.class);

	public SearchForRestaurantsRule(final RulesController<BookingProps, BookingNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("SEARCH_OWNED_AGENTS_RULE",
				"searching for Restaurants",
				"handle search for Restaurants Agents");
	}

	@Override
	protected Set<AID> searchAgents(final RuleSetFacts facts) {
		return search(agent, agent.getDefaultDF(), "RESTAURANT");
	}

	@Override
	protected void handleNoResults(final RuleSetFacts facts) {
		logger.info("No restaurants found!");
		agent.doDelete();
	}

	@Override
	protected void handleResults(final Set<AID> dfResults, final RuleSetFacts facts) {
		logger.info("Found {} restaurants!", (long) dfResults.size());
		agentProps.getRestaurants().addAll(dfResults);
	}
}
