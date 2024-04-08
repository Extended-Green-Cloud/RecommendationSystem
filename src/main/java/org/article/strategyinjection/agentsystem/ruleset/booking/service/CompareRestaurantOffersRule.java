package org.article.strategyinjection.agentsystem.ruleset.booking.service;
import static org.jrba.rulesengine.constants.FactTypeConstants.CFP_BEST_MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.utils.messages.MessageReader.readMessageContent;

import java.util.Comparator;

import org.article.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.article.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.article.strategyinjection.agentsystem.domain.RestaurantData;
import org.jrba.exception.IncorrectMessageContentException;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.lang.acl.ACLMessage;

public class CompareRestaurantOffersRule extends AgentBasicRule<BookingProps, BookingNode> {

	public CompareRestaurantOffersRule(final RulesController<BookingProps, BookingNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("BASIC_COMPARATOR_RULE",
				"compare offers received from restaurants",
				"compare offers received from restaurants");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage bestProposal = facts.get(CFP_BEST_MESSAGE);
		final ACLMessage newProposal = facts.get(MESSAGE);

		final Comparator<RestaurantData> comparator = (msg1, msg2) -> {
			final double priceDiff = msg2.getPrice() - msg1.getPrice();
			return (int) priceDiff;
		};

		facts.put(RESULT, compareMessages(bestProposal, newProposal, comparator));
	}

	private static int compareMessages(final ACLMessage message1, final ACLMessage message2,
			final Comparator<RestaurantData> comparator) {
		try {
			final RestaurantData message1Content = readMessageContent(message1, RestaurantData.class);
			final RestaurantData message2Content = readMessageContent(message2, RestaurantData.class);

			return comparator.compare(message1Content, message2Content);
		} catch (IncorrectMessageContentException e) {
			e.printStackTrace();
			return Integer.MAX_VALUE;
		}
	}
}
