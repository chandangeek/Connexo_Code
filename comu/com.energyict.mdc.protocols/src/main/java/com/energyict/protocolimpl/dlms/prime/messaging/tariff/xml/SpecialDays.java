package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;

public class SpecialDays implements Serializable {

    protected String dt;

    protected String dtCard;

    protected int dayID;

    /**
     * Gets the value of the dt property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDT() {
        return dt;
    }

    /**
     * Sets the value of the dt property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDT(String value) {
        this.dt = value;
    }

    /**
     * Gets the value of the dtCard property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getDTCard() {
        return dtCard;
    }

    /**
     * Sets the value of the dtCard property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDTCard(String value) {
        this.dtCard = value;
    }

    /**
     * Gets the value of the dayID property.
     */
    public int getDayID() {
        return dayID;
    }

    /**
     * Sets the value of the dayID property.
     */
    public void setDayID(int value) {
        this.dayID = value;
    }

}
