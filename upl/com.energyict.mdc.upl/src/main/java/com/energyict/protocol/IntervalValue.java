/*
 * IntervalValue.java
 *
 * Created on 15 juli 2003, 10:24
 */

package com.energyict.protocol;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Koen
 */
@XmlRootElement
public class IntervalValue implements Serializable {

    Number number;
    int protocolStatus;
    int eiStatus;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private IntervalValue() {
    }

    /**
     * Creates a new instance of IntervalValue
     */
    protected IntervalValue(Number number) {
        this(number, 0, 0);
    }

    public IntervalValue(Number number, int protocolStatus, int eiStatus) {
        this.number = number;
        this.protocolStatus = protocolStatus;
        this.eiStatus = eiStatus;
    }

    @XmlAttribute
    public Number getNumber() {
        return number;
    }

    public void setNumber(Number number) {
        this.number = number;
    }

    @XmlAttribute
    public int getProtocolStatus() {
        return protocolStatus;
    }

    @XmlAttribute
    public int getEiStatus() {
        return eiStatus;
    }

    // KV 25082004
    protected void setEiStatus(int eiStatus) {
        this.eiStatus = eiStatus;
    }

    protected void setProtocolStatus(int protocolStatus) {
        this.protocolStatus = protocolStatus;
    }

    public String toString() {
        return number + " " + protocolStatus + " " + eiStatus;
    }

}