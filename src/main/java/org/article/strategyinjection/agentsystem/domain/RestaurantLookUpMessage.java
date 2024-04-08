package org.article.strategyinjection.agentsystem.domain;

import org.immutables.value.Value;
import org.jrba.environment.domain.ExternalMessage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableRestaurantLookUpMessage.class)
@JsonDeserialize(as = ImmutableRestaurantLookUpMessage.class)
@Value.Immutable
public interface RestaurantLookUpMessage extends ExternalMessage {

	String getCuisine();

	String getDish();

	String getAdditionalInstructions();

	double getPrice();
}
