/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.outbound.client.impl;

import com.elster.jupiter.webservices.outbound.rest.WeatherInfo;
import com.elster.jupiter.webservices.outbound.rest.WeatherService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.rest.weather.client.gogo",
        service = WeatherGogoCommands.class,
        property = {"osgi.command.scope=wea",
                "osgi.command.function=weather"},
        immediate = true)
public class WeatherGogoCommands {

    private volatile WeatherService weatherService;

    public void weather() {
        System.out.println("Get the current weather for a certain city");
        System.out.println("usage: weather <city>");
        System.out.println("   where <city> denotes a city name, e.g. London, Brussels");
        System.out.println("example: wea.weather London");
    }

    public void weather(String city) {
        WeatherInfo result = weatherService.getWeather(city);
        if (result.main == null || result.clouds == null) {
            System.out.println("No weather information was found for " + city);
        } else {
            System.out.println("The temperature in " + city + " is currently " + result.main.temp + "K with " + result.clouds.all + "% cloud coverage");
        }
    }

    @Reference
    public void setWeatherService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
}
