package org.article.strategyinjection.agentsystem.agents.booking.node;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.article.strategyinjection.agentsystem.agents.booking.props.BookingProps;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.environment.websocket.GuiWebSocketClient;

import kotlin.Pair;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingNode extends AgentNode<BookingProps> {

	private ConcurrentLinkedQueue<Pair<String, Object>> clientEvents;
	private AtomicInteger latestId;

	public BookingNode(final String name) throws InterruptedException {
		super(name, "BOOKING");
		this.clientEvents = new ConcurrentLinkedQueue<>();
		this.latestId = new AtomicInteger(0);
		connectSocket(null);
	}

	/**
	 * Method sends message to client informing about restaurant response
	 *
	 * @param messageToClient message content
	 */
	public void passRestaurantMessageToClient(final String messageToClient) {
		mainWebSocket.send(messageToClient);
	}

	@Override
	public GuiWebSocketClient initializeSocket(final String url) {
		return new BookingWebsocketClient(this);
	}

	@Override
	public void updateGUI(final BookingProps props) {
		// nothing should happen here
	}

	@Override
	public void saveMonitoringData(final BookingProps props) {
		// nothing should happen here
	}
}
