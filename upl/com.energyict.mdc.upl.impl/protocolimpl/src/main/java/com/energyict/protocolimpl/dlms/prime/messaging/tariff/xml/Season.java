package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;

public class Season implements Serializable {

    protected String name;

    protected String start;

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
     * Gets the value of the start property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStart(String value) {
        this.start = value;
    }

    /**
     * Gets the value of the week property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getWeek() {
        return week;
    }

    /**
     * Sets the value of the week property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWeek(String value) {
        this.week = value;
    }

}
