package org.article.strategyinjection.agentsystem.ruleset;

import static org.jrba.rulesengine.constants.RuleSetTypeConstants.DEFAULT_RULE_SET;

import java.util.ArrayList;
import java.util.List;

import org.article.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.article.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.article.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.article.strategyinjection.agentsystem.ruleset.booking.df.SearchForRestaurantsRule;
import org.article.strategyinjection.agentsystem.ruleset.booking.initial.StartInitialBookingBehaviours;
import org.article.strategyinjection.agentsystem.ruleset.booking.sensor.ListenForExternalOrdersRule;
import org.article.strategyinjection.agentsystem.ruleset.booking.service.CompareRestaurantOffersRule;
import org.article.strategyinjection.agentsystem.ruleset.booking.service.LookForRestaurantForClientOrderRule;
import org.article.strategyinjection.agentsystem.ruleset.restaurant.initial.StartInitialRestaurantBehaviours;
import org.article.strategyinjection.agentsystem.ruleset.restaurant.service.ListenForNewClientOrdersRule;
import org.article.strategyinjection.agentsystem.ruleset.restaurant.service.ProcessNewClientOrdersRule;
import org.article.strategyinjection.agentsystem.ruleset.restaurant.service.ProposeToBookingServiceRule;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Default rule set applied in the restaurant testing system
 */
@SuppressWarnings("unchecked")
public class DefaultRestaurantRuleSet extends RuleSet {

	public DefaultRestaurantRuleSet() {
		super(DEFAULT_RULE_SET);
	}

	@Override
	protected List<AgentRule> initializeRules(RulesController<?, ?> rulesController) {
		return new ArrayList<>(switch (rulesController.getAgentProps().getAgentType()) {
			case "BOOKING" -> getBookingRules((RulesController<BookingProps, BookingNode>) rulesController);
			case "RESTAURANT" -> getRestaurantRules(
					(RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>>) rulesController);
			default -> new ArrayList<AgentRule>();
		});
	}

	protected List<AgentRule> getBookingRules(RulesController<BookingProps, BookingNode> rulesController) {
		return List.of(
				new StartInitialBookingBehaviours(rulesController),
				new SearchForRestaurantsRule(rulesController),
				new ListenForExternalOrdersRule(rulesController),
				new LookForRestaurantForClientOrderRule(rulesController),
				new CompareRestaurantOffersRule(rulesController)
		);
	}

	protected List<AgentRule> getRestaurantRules(
			RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>> rulesController) {
		return List.of(
				new StartInitialRestaurantBehaviours(rulesController),
				new ListenForNewClientOrdersRule(rulesController, this),
				new ProcessNewClientOrdersRule(rulesController),
				new ProposeToBookingServiceRule(rulesController)
		);
	}

}
