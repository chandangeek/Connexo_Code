package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


public enum Measure_Unit {
    WH("WH"),
    J("J"),
    M3("m^3"),
    KG("kg"),
    W("W"),
    J_H("J/h"),
    M3_H("m^3/h"),
    M3_MIN("m^3/min"),
    M3_S("m^3/s"),
    KG_H("kg/h"),
    C("C"),
    K("K"),
    BAR("bar"),
    DATE("date"),
    TIME("time"),
    DATE_TIME("date time"),
    DATE_TIME_S("date time to second"),
    SECONDS("seconds"),
    MINUTES("minutes"),
    HOURS("hours"),
    DAYS("days"),
    NONE("none"),
    EPOCH_TIME("epoch time"),;

    private String value;

    private Measure_Unit(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}