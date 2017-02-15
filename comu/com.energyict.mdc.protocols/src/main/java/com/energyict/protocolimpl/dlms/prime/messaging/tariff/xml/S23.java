/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;

public class S23  implements Serializable {

    protected PCact pCact;

    protected PCLatent pcLatent;

    protected ActiveCalendars activeCalendars;

    protected LatentCalendars latentCalendars;

    protected String fh;

    /**
     * Gets the value of the pCact property.
     *
     * @return possible object is
     *         {@link PCact }
     */
    public PCact getPCact() {
        return pCact;
    }

    /**
     * Sets the value of the pCact property.
     *
     * @param value allowed object is
     *              {@link PCact }
     */
    public void setPCact(PCact value) {
        this.pCact = value;
    }

    /**
     * Gets the value of the pcLatent property.
     *
     * @return possible object is
     *         {@link PCLatent }
     */
    public PCLatent getPCLatent() {
        return pcLatent;
    }

    /**
     * Sets the value of the pcLatent property.
     *
     * @param value allowed object is
     *              {@link PCLatent }
     */
    public void setPCLatent(PCLatent value) {
        this.pcLatent = value;
    }

    /**
     * Gets the value of the activeCalendars property.
     *
     * @return possible object is
     *         {@link ActiveCalendars }
     */
    public ActiveCalendars getActiveCalendars() {
        return activeCalendars;
    }

    /**
     * Sets the value of the activeCalendars property.
     *
     * @param value allowed object is
     *              {@link ActiveCalendars }
     */
    public void setActiveCalendars(ActiveCalendars value) {
        this.activeCalendars = value;
    }

    /**
     * Gets the value of the latentCalendars property.
     *
     * @return possible object is
     *         {@link LatentCalendars }
     */
    public LatentCalendars getLatentCalendars() {
        return latentCalendars;
    }

    /**
     * Sets the value of the latentCalendars property.
     *
     * @param value allowed object is
     *              {@link LatentCalendars }
     */
    public void setLatentCalendars(LatentCalendars value) {
        this.latentCalendars = value;
    }

    /**
     * Gets the value of the fh property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getFh() {
        return fh;
    }

    /**
     * Sets the value of the fh property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFh(String value) {
        this.fh = value;
    }

}
