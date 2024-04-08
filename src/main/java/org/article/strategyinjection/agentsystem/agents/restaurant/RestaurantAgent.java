package org.article.strategyinjection.agentsystem.agents.restaurant;

import static org.jrba.utils.yellowpages.YellowPagesRegister.register;

import java.util.Map;

import org.article.strategyinjection.agentsystem.agents.restaurant.props.RestaurantAgentProps;
import org.article.strategyinjection.agentsystem.domain.CuisineType;
import org.jrba.agentmodel.domain.AbstractAgent;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.rulesengine.RulesController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent representing a restaurant
 */
@SuppressWarnings("unchecked")
public class RestaurantAgent extends AbstractAgent<AgentNode<RestaurantAgentProps>, RestaurantAgentProps> {

	private static final Logger logger = LoggerFactory.getLogger(RestaurantAgent.class);

	@Override
	protected void setup() {
		logger.info("Setting up Agent {}", getName());
		final Object[] arguments = getArguments();

		this.rulesController = (RulesController<RestaurantAgentProps, AgentNode<RestaurantAgentProps>>) arguments[0];
		this.properties = new RestaurantAgentProps(getName(), (CuisineType) arguments[1],
				(Map<String, Double>) arguments[2], (Map<String, Object>) arguments[3]);

		register(this, this.getDefaultDF(), "RESTAURANT", "RESTAURANT");
		setRulesController(rulesController);
	}

	@Override
	protected void takeDown() {
		logger.info("I'm finished. Bye!");
		super.takeDown();
	}
}
