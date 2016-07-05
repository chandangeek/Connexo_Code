package com.elster.jupiter.webservices.outbound.rest.impl;

import com.elster.jupiter.webservices.outbound.rest.WeatherInfo;
import com.elster.jupiter.webservices.outbound.rest.WeatherService;

import javax.ws.rs.client.WebTarget;

/**
 * Created by bvn on 7/5/16.
 */
public class WeatherServiceImpl implements WeatherService {
    private final String apiKey = "ba24738bc368fce207fc7cbc9061525e";
    private final WebTarget target;

    public WeatherServiceImpl(WebTarget target) {
        this.target = target;
    }

    @Override
    public WeatherInfo getWeather(String city) {
        WeatherInfo weatherInfo = target.queryParam("q", city)
                .queryParam("APPID", apiKey)
                .request().get(WeatherInfo.class);
        return weatherInfo;
    }
}
