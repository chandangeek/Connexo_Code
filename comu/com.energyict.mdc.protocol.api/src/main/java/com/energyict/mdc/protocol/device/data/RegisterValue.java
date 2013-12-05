/*
 * RegisterValue.java
 *
 * Created on 27 mei 2004, 16:47
 */

package com.energyict.mdc.protocol.device.data;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.device.offline.OfflineRegister;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Immutable object
 *
 * @author Koen
 */
public class RegisterValue implements Serializable {


    ObisCode obisCode; // as support for the toString()
    int rtuRegisterId; // to find back the Register to which this RegisterValue belongs

    Quantity quantity;
    Date readTime;
    Date fromTime;
    Date toTime; // billing timestamp
    Date eventTime; // Maximum demand timestamp
    String text;

    private String rtuSerialNumber;

    // this constructor is used when a register is unsupported!

    public RegisterValue(ObisCode obisCode) {
        this(obisCode, null, null, null, null);
    }

    public RegisterValue(Register register) {
        this(register, null, null, null, null);
    }

    public RegisterValue(OfflineRegister offlineRegister) {
        this(offlineRegister, null, null, null, null);
    }

    public RegisterValue(ObisCode obisCode, String value) {
        this(obisCode, null, null, null, null, new Date(), 0, value);
    }

    public RegisterValue(Register register, String value) {
        this(register, null, null, null, null, new Date(), 0, value);
    }

    public RegisterValue(OfflineRegister offlineRegister, String value) {
        this(offlineRegister, null, null, null, null, new Date(), 0, value);
    }

    public RegisterValue(ObisCode obisCode, Date eventTime) {
        this(obisCode, new Quantity(BigDecimal.valueOf(eventTime.getTime() / 1000), Unit.get(255)), eventTime, null, null);
    }

    public RegisterValue(Register register, Date eventTime) {
        this(register, new Quantity(BigDecimal.valueOf(eventTime.getTime() / 1000), Unit.get(255)), eventTime, null, null);
    }

    public RegisterValue(OfflineRegister offlineRegister, Date eventTime) {
        this(offlineRegister, new Quantity(BigDecimal.valueOf(eventTime.getTime() / 1000), Unit.get(255)), eventTime, null, null);
    }

    public RegisterValue(ObisCode obisCode, Quantity quantity) {
        this(obisCode, quantity, null, null, null);
    }

    public RegisterValue(Register register, Quantity quantity) {
        this(register, quantity, null, null, null);
    }

    public RegisterValue(OfflineRegister offlineRegister, Quantity quantity) {
        this(offlineRegister, quantity, null, null, null);
    }

    public RegisterValue(ObisCode obisCode, Quantity quantity, Date eventTime) {
        this(obisCode, quantity, eventTime, null, null);
    }

    public RegisterValue(Register register, Quantity quantity, Date eventTime) {
        this(register, quantity, eventTime, null, null);
    }

    public RegisterValue(OfflineRegister offlineRegister, Quantity quantity, Date eventTime) {
        this(offlineRegister, quantity, eventTime, null, null);
    }

    public RegisterValue(ObisCode obisCode, Quantity quantity, Date eventTime, Date toTime) {
        this(obisCode, quantity, eventTime, null, toTime);
    }

    public RegisterValue(Register register, Quantity quantity, Date eventTime, Date toTime) {
        this(register, quantity, eventTime, null, toTime);
    }

    public RegisterValue(OfflineRegister offlineRegister, Quantity quantity, Date eventTime, Date toTime) {
        this(offlineRegister, quantity, eventTime, null, toTime);
    }

    public RegisterValue(ObisCode obisCode, Quantity quantity, Date eventTime, Date fromTime, Date toTime) {
        this(obisCode, quantity, eventTime, fromTime, toTime, new Date());
    }

    public RegisterValue(Register register, Quantity quantity, Date eventTime, Date fromTime, Date toTime) {
        this(register, quantity, eventTime, fromTime, toTime, new Date());
    }

    public RegisterValue(OfflineRegister offlineRegister, Quantity quantity, Date eventTime, Date fromTime, Date toTime) {
        this(offlineRegister, quantity, eventTime, fromTime, toTime, new Date());
    }

    public RegisterValue(ObisCode obisCode, Quantity quantity, Date eventTime, Date fromTime, Date toTime, Date readTime) {
        this(obisCode, quantity, eventTime, fromTime, toTime, readTime, 0);
    }

    public RegisterValue(Register register, Quantity quantity, Date eventTime, Date fromTime, Date toTime, Date readTime) {
        this(register, quantity, eventTime, fromTime, toTime, readTime, 0);
    }

    public RegisterValue(OfflineRegister offlineRegister, Quantity quantity, Date eventTime, Date fromTime, Date toTime, Date readTime) {
        this(offlineRegister, quantity, eventTime, fromTime, toTime, readTime, 0);
    }

    public RegisterValue(ObisCode obisCode, Quantity quantity, Date eventTime, Date fromTime, Date toTime, Date readTime, int rtuRegisterId) {
        this(obisCode, quantity, eventTime, fromTime, toTime, readTime, rtuRegisterId, null);
    }

    public RegisterValue(Register register, Quantity quantity, Date eventTime, Date fromTime, Date toTime, Date readTime, int rtuRegisterId) {
        this(register, quantity, eventTime, fromTime, toTime, readTime, rtuRegisterId, null);
    }

    public RegisterValue(OfflineRegister offlineRegister, Quantity quantity, Date eventTime, Date fromTime, Date toTime, Date readTime, int rtuRegisterId) {
        this(offlineRegister, quantity, eventTime, fromTime, toTime, readTime, rtuRegisterId, null);
    }

    public RegisterValue(ObisCode obisCode, Quantity quantity, Date eventTime, Date fromTime, Date toTime, Date readTime, int rtuRegisterId, String text) {
        this(new Register(rtuRegisterId, obisCode, null), quantity, eventTime, fromTime, toTime, readTime, rtuRegisterId, text);
    }

    public RegisterValue(OfflineRegister register, Quantity quantity, Date eventTime, Date fromTime, Date toTime, Date readTime, int rtuRegisterId, String text) {
        this.obisCode = register.getObisCode();
        this.rtuSerialNumber = register.getSerialNumber();
        this.quantity = quantity;
        this.eventTime = eventTime;
        this.fromTime = fromTime;
        this.readTime = readTime;
        this.toTime = (toTime == null ? readTime : toTime);
        this.rtuRegisterId = rtuRegisterId;
        this.text = text;
    }

    public RegisterValue(Register register, Quantity quantity, Date eventTime, Date fromTime, Date toTime, Date readTime, int rtuRegisterId, String text) {
        this.obisCode = register.getObisCode();
        this.rtuSerialNumber = register.getSerialNumber();
        this.quantity = quantity;
        this.eventTime = eventTime;
        this.fromTime = fromTime;
        this.readTime = readTime;
        this.toTime = (toTime == null ? readTime : toTime);
        this.rtuRegisterId = rtuRegisterId;
        this.text = text;
    }

    /*
     *   If quantity and text is null, register was unsupported for the meter configuration!
     */

    public boolean isSupported() {
        return ((getQuantity() != null) || (getText() != null));
    }

    public String toString() {
        if ((quantity == null) && (text == null)) {
            return obisCode.toString() + " NOT SUPPORTED!";
        } else {
            return obisCode.toString() +
                    (quantity == null ? "" : ", quantity=" + quantity.toString()) +
                    (text == null ? "" : ", text=" + text) +
                    (readTime == null ? "" : ", readTime=" + readTime.toString()) +
                    (eventTime == null ? "" : ", eventTime=" + eventTime.toString()) +
                    (fromTime == null ? "" : ", fromTime=" + fromTime.toString()) +
                    (toTime == null ? "" : ", toTime=" + toTime.toString());
        }
    }

    /**
     * Getter for property obisCode.
     *
     * @return Value of property obisCode.
     */
    public ObisCode getObisCode() {
        return obisCode;
    }

    /**
     * Getter for property quantity.
     *
     * @return Value of property quantity.
     */
    public Quantity getQuantity() {
        return quantity;
    }

    /**
     * Setter for property quantity.
     *
     * @param quantity New value of property quantity.
     */
    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    /**
     * Getter for property readTime.
     *
     * @return Value of property readTime.
     */
    public Date getReadTime() {
        return readTime;
    }

    /**
     * Getter for property fromTime.
     *
     * @return Value of property fromTime.
     */
    public Date getFromTime() {
        return fromTime;
    }

    /**
     * Getter for property toTime.
     *
     * @return Value of property toTime.
     */
    public Date getToTime() {
        return toTime;
    }

    /**
     * Getter for property eventTime.
     *
     * @return Value of property eventTime.
     */
    public Date getEventTime() {
        return eventTime;
    }

    /**
     * Getter for property rtuRegisterId.
     *
     * @return Value of property rtuRegisterId.
     */
    public int getRtuRegisterId() {
        return rtuRegisterId;
    }

    /**
     * Setter for property rtuRegisterId.
     *
     * @param rtuRegisterId New value of property rtuRegisterId.
     */
    public void setRtuRegisterId(int rtuRegisterId) {
        this.rtuRegisterId = rtuRegisterId;
    }

    /**
     * Getter for property text.
     *
     * @return Value of property text.
     */
    public String getText() {
        return text;
    }

    public String getSerialNumber() {
        return rtuSerialNumber;
    }

}