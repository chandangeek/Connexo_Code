/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;

public class Week implements Serializable {

    protected String name;

    protected String week;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the week property.
     */
    public String getWeek() {
        return week;
    }

    /**
     * Sets the value of the week property.
     */
    public void setWeek(String value) {
        this.week = value;
    }

}
