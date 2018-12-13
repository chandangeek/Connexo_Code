/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.outbound.rest.impl;

import com.elster.jupiter.webservices.outbound.rest.WeatherInfo;
import com.elster.jupiter.webservices.outbound.rest.WeatherService;

import javax.ws.rs.client.WebTarget;

/**
 * Created by bvn on 7/5/16.
 */
public class WeatherServiceImpl implements WeatherService {
    private final String apiKey = "7c05c886001bcc728e4bc1ca8f19c2b0";
    private final WebTarget target;

    public WeatherServiceImpl(WebTarget target) {
        this.target = target;
    }

    @Override
    public WeatherInfo getWeather(String city) {
        WeatherInfo weatherInfo = target
                .path("/weather")
                .queryParam("q", city)
                .queryParam("APPID", apiKey)
                .request().get(WeatherInfo.class);
        return weatherInfo;
    }
}
