/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.outbound.rest;

/**
 * Outbound REST service; DEMO BUNDLE
 */
public interface WeatherService {
    /**
     * Get the current weather at a certain destination.
     *
     * @param city The city to get the current weather from
     * @return The current weather
     */
    WeatherInfo getWeather(String city);
}
