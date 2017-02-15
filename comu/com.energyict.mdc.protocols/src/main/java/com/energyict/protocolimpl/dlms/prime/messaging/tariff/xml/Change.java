/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;

public class Change implements Serializable {

    protected String hour;

    protected int tariffRate;

    /**
     * Gets the value of the hour property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHour() {
        return hour;
    }

    /**
     * Sets the value of the hour property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHour(String value) {
        this.hour = value;
    }

    /**
     * Gets the value of the tariffRate property.
     */
    public int getTariffRate() {
        return tariffRate;
    }

    /**
     * Sets the value of the tariffRate property.
     */
    public void setTariffRate(int value) {
        this.tariffRate = value;
    }

}
