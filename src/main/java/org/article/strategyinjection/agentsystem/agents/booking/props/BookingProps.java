package org.article.strategyinjection.agentsystem.agents.booking.props;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jrba.agentmodel.domain.props.AgentProps;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import lombok.Getter;

/**
 * Properties of booking agent
 */
@Getter
public class BookingProps extends AgentProps {

	private final List<AID> restaurants;
	private final Map<Integer, ACLMessage> restaurantForOrder;
	private final ConcurrentMap<String, Integer> strategyForOrder;

	/**
	 * Default constructor that sets the type of the agent
	 *
	 * @param agentName name of the agent
	 */
	public BookingProps(final String agentName) {
		super("BOOKING", agentName);
		this.restaurants = new ArrayList<>();
		this.restaurantForOrder = new HashMap<>();
		this.strategyForOrder = new ConcurrentHashMap<>();
	}
}
