package org.article.strategyinjection.agentsystem.domain;

import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableRestaurantData.class)
@JsonDeserialize(as = ImmutableRestaurantData.class)
@Value.Immutable
public interface RestaurantData {

	double getPrice();
	Map<String, Object> getRestaurantInformation();
}
