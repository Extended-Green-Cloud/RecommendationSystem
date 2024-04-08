package org.article.strategyinjection.agentsystem.agents.booking;

import org.article.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.article.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.jrba.agentmodel.domain.AbstractAgent;
import org.jrba.rulesengine.RulesController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent representing service for restaurant booking
 */
@SuppressWarnings("unchecked")
public class BookingAgent extends AbstractAgent<BookingNode, BookingProps> {

	private static final Logger logger = LoggerFactory.getLogger(BookingAgent.class);

	@Override
	protected void setup() {
		logger.info("Setting up Agent {}", getName());
		final Object[] arguments = getArguments();

		this.rulesController = (RulesController<BookingProps, BookingNode>) arguments[0];
		this.properties = new BookingProps(getName());
		try {
			this.agentNode = new BookingNode(getLocalName());
		} catch (InterruptedException e) {
			logger.error("Couldn't initialize agent node.");
		}

		setRulesController(rulesController);
	}

	@Override
	protected void takeDown() {
		logger.info("I'm finished. Bye!");
		super.takeDown();
	}
}
