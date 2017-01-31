/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SerialCommunicationSettings.java
 *
 * Created on 17 april 2003, 13:17
 */

package com.energyict.mdc.protocol.api;

import java.io.Serializable;

/**
 * Immutable object holding information about serial communication settings.
 *
 * @author Karel
 */
public class SerialCommunicationSettings implements Serializable {

    private static final long serialVersionUID = 7830830557365505498L;

    /**
     * no parity
     */
    public static final char NO_PARITY = 'N';
    /**
     * odd parity
     */
    public static final char ODD_PARITY = 'O';
    /**
     * even parity
     */
    public static final char EVEN_PARITY = 'E';
    static final SerialCommunicationSettings DEFAULT =
            new SerialCommunicationSettings(9600, 8, 'N', 1);
    static final SerialCommunicationSettings MULTIEDIT_DEFAULT =
            new SerialCommunicationSettings(-1, 8, 'N', 1);

    private int speed;
    private int dataBits;
    private char parity;
    private int stopBits;

    /**
     * Returns the default serial communication settings.
     * 9600 bps , 8 databits , no parity , 1 stopbit.
     *
     * @return the default serial communication settings.
     */
    public static SerialCommunicationSettings getDefault() {
        return DEFAULT;
    }

    /**
     * Returns the default (don't change) serial communication settings in case of multi-edit
     * -1 bps , 8 databits , no parity , 1 stopbit.
     *
     * @return the default serial communication settings in case of multi-edit
     */
    public static SerialCommunicationSettings getMultiEditDefault() {
        return MULTIEDIT_DEFAULT;
    }

    /**
     * Creates a new instance of SerialCommunicationSettings.
     *
     * @param speed    baud rate.
     * @param dataBits number of data bits per character (7 or 8).
     * @param parity   parity:
     *                 <UL>
     *                 <LI>'N': none</LI>
     *                 <LI>'E': even</LI>
     *                 <LI>'O': odd<LI>
     *                 </UL>.
     * @param stopBits number of stopbits (1 or 2).
     */
    public SerialCommunicationSettings(int speed, int dataBits, char parity, int stopBits) {

        this.speed = speed;
        switch (dataBits) {
            case 7:
            case 8:
                this.dataBits = dataBits;
                break;
            default:
                throw new IllegalArgumentException("Invalid dataBits argument: " + dataBits);
        }
        switch (parity) {
            case NO_PARITY:
            case ODD_PARITY:
            case EVEN_PARITY:
                this.parity = parity;
                break;
            default:
                throw new IllegalArgumentException("Invalid parity argument: " + parity);
        }
        switch (stopBits) {
            case 1:
            case 2:
                this.stopBits = stopBits;
                break;
            default:
                throw new IllegalArgumentException("Invalid stopBits argument: " + stopBits);
        }
    }

    /**
     * Creates a new instance of SerialCommunicationSettings.
     *
     * @param speed baud rate.
     * @param other SerialCommunicationSettings object to copy the
     *              data bits, parity and stop bits from
     */
    public SerialCommunicationSettings(int speed, SerialCommunicationSettings other) {
        this.speed = speed;
        this.dataBits = other.dataBits;
        this.parity = other.parity;
        this.stopBits = other.stopBits;
    }

    /**
     * Getter for property dataBits.
     *
     * @return Value of property dataBits.
     */
    public int getDataBits() {
        return dataBits;
    }

    /**
     * Getter for property parity.
     *
     * @return Value of property parity.
     */
    public char getParity() {
        return parity;
    }

    /**
     * Getter for property speed.
     *
     * @return Value of property speed.
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Getter for property stopBits.
     *
     * @return Value of property stopBits.
     */
    public int getStopBits() {
        return stopBits;
    }

    /**
     * Returns a string representation of the receiver.
     *
     * @return the string representation.
     */
    public String toString() {
        return "" + speed + "," + dataBits + "," + parity + "," + stopBits;
    }

    /**
     * tests if the receiver is equal to the argument.
     *
     * @param other to test for equality.
     * @return true if the receiver is equal to the argument, false otherwise.
     */
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        try {
            SerialCommunicationSettings test = (SerialCommunicationSettings) other;
            return
                    this.speed == test.speed &&
                            this.dataBits == test.dataBits &&
                            this.parity == test.parity &&
                            this.stopBits == test.stopBits;
        } catch (ClassCastException ex) {
            return false;
        }
    }

    /**
     * returns the receiver's hashCode.
     *
     * @return the receiver's hashCode.
     */
    public int hashCode() {
        return this.speed & this.dataBits & this.parity & this.stopBits;
    }

    // A line added to com.energyict.mdw.xml.MdwPersistentDelegates
    // to nicely serialize this one (audit)
}
