package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;

public class PCact implements Serializable {

    protected Contrato1 contrato1;

    protected PResidual pResidual;

    protected String actDate;

    /**
     * Gets the value of the contrato1 property.
     *
     * @return possible object is
     *         {@link Contrato1 }
     */
    public Contrato1 getContrato1() {
        return contrato1;
    }

    /**
     * Sets the value of the contrato1 property.
     *
     * @param value allowed object is
     *              {@link Contrato1 }
     */
    public void setContrato1(Contrato1 value) {
        this.contrato1 = value;
    }

    /**
     * Gets the value of the pResidual property.
     *
     * @return possible object is
     *         {@link PResidual }
     */
    public PResidual getPResidual() {
        return pResidual;
    }

    /**
     * Sets the value of the pResidual property.
     *
     * @param value allowed object is
     *              {@link PResidual }
     */
    public void setPResidual(PResidual value) {
        this.pResidual = value;
    }

    /**
     * Gets the value of the actDate property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getActDate() {
        return actDate;
    }

    /**
     * Sets the value of the actDate property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setActDate(String value) {
        this.actDate = value;
    }

}
