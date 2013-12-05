/*
 * IntervalValue.java
 *
 * Created on 15 juli 2003, 10:24
 */

package com.energyict.mdc.protocol.device.data;


/**
 * @author Koen
 */
public class IntervalValue implements java.io.Serializable {

    Number number;
    int protocolStatus;
    int eiStatus;

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

    public Number getNumber() {
        return number;
    }

    public void setNumber(Number number) {
        this.number = number;
    }

    public int getProtocolStatus() {
        return protocolStatus;
    }

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
