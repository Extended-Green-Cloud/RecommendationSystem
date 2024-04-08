package org.article.strategyinjection.agentsystem.agents.restaurant.props;

import java.util.HashMap;
import java.util.Map;

import org.article.strategyinjection.agentsystem.domain.ClientOrder;
import org.article.strategyinjection.agentsystem.domain.CuisineType;
import org.jrba.agentmodel.domain.props.AgentProps;

import lombok.Getter;

/**
 * Properties of restaurant agent
 */
@Getter
public class RestaurantAgentProps extends AgentProps {

	final CuisineType cuisineType;
	final Map<String, Double> dishWithPrice;
	final Map<String, Object> additionalInformation;

	/**
	 * Default constructor that sets the type of the agent
	 *
	 * @param agentName name of the agent
	 */
	public RestaurantAgentProps(final String agentName,
			final CuisineType cuisineType,
			final Map<String, Double> dishWithPrice,
			final Map<String, Object> additionalInformation) {
		super("RESTAURANT", agentName);
		this.cuisineType = cuisineType;
		this.dishWithPrice = dishWithPrice;
		this.additionalInformation = new HashMap<>(additionalInformation);
	}

	/**
	 * Method verifies if restaurant can fulfill client order
	 *
	 * @param clientOrder client order information
	 */
	public boolean canFulfillOrder(final ClientOrder clientOrder) {
		return cuisineType.equals(clientOrder.getCuisine())
				&& dishWithPrice.containsKey(clientOrder.getDish())
				&& dishWithPrice.get(clientOrder.getDish()) <= clientOrder.getMaxPrice();
	}

}
