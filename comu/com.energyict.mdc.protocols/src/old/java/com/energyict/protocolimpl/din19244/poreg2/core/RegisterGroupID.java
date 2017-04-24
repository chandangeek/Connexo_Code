/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.core;

public enum RegisterGroupID {

    DateTime(3),
    SerialNumber(1),
    Events(8),
    MeasuringPeriod(6),
    DstSettings(7),
    Level0Result(10),
    Level3Result(13),
    BillingDataCurrentPeriod(20),
    BillingDataLastPeriod(21),
    BillingDataCurrentPeriodTimeStamp(22),
    BillingDataLastPeriodTimeStamp(23),
    BillingCounter(70),
    Level0Parameters(110),
    Level3Parameters(113),
    BillingParameters(120),
    AlarmLinks(170),
    AlarmParam(195),
    ProfileParameters(210);

    private int id;

    RegisterGroupID(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}