package com.elster.jupiter.webservices.outbound.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherInfo {
    public List<Weather> weathers;
    public Main main;
    public Wind wind;
    public Clounds clouds;

    public class Coord {
        public double lon, lat;
    }

    public class Weather {
        public long id;
        public String main, description, icon;
    }

    public class Main {
        public double temp, pressure, humidity, temp_min, temp_max, sea_level, grnd_level;
    }

    public class Wind {
        public double speed, deg;
    }

    public class Clounds {
        public double all;
    }
}
