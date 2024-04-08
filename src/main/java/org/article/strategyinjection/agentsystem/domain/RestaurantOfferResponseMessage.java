package org.article.strategyinjection.agentsystem.domain;

import org.immutables.value.Value;
import org.jrba.environment.domain.ExternalMessage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableRestaurantOfferResponseMessage.class)
@JsonDeserialize(as = ImmutableRestaurantOfferResponseMessage.class)
@Value.Immutable
public interface RestaurantOfferResponseMessage extends ExternalMessage {

	int getOrderId();

	boolean getAccepted();
}
