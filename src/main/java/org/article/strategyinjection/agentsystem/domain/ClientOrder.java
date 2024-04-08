package org.article.strategyinjection.agentsystem.domain;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableClientOrder.class)
@JsonDeserialize(as = ImmutableClientOrder.class)
@Value.Immutable
public interface ClientOrder {

	int getOrderId();

	CuisineType getCuisine();

	String getDish();

	Double getMaxPrice();

	String getAdditionalInstructions();
}
