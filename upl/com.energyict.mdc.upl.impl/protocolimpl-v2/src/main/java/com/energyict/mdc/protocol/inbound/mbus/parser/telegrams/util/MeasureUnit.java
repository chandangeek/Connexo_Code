package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


public enum MeasureUnit {
    WH("WH"),
    J("J"),
    M3("m3"),
    KG("kg"),
    W("W"),
    J_H("J/h"),
    M3_H("m3/h"),
    M3_MIN("m3/min"),
    M3_S("m3/s"),
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

    private MeasureUnit(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}