package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 1-apr-2011
 * Time: 16:28:00
 */
public class Event {

    private int status;
    private int cause;
    private Date eventDate;

    private static final String HIGH = "high";
    private static final String LOW = "low";
    private static final String UNKNOWN = "unknown";

    public Event(int cause, Date eventDate, int status) {
        this.cause = cause;
        this.eventDate = eventDate;
        this.status = status;
    }

    public String getInputStateDescription(int input) {
        switch (input) {
            case 1:
                return (getLogicalStateReadInput1() == 1) ? HIGH : LOW;
            case 2:
                return (getLogicalStateReadInput2() == 1) ? HIGH : LOW;
            case 3:
                return (getLogicalStateReadInput3() == 1) ? HIGH : LOW;
            case 4:
                return (getLogicalStateReadInput4() == 1) ? HIGH : LOW;
            default:
                return UNKNOWN;
        }
    }

    public String getInputDescription() {
        switch ((cause & 0xF0) >> 4) {
            case 1:
                return " on input 1";
            case 2:
                return " on input 2";
            case 4:
                return " on input 3";
            case 8:
                return " on input 4";
            default:
                return "";
        }
    }

    public String getCauseDescription() {
        if (isOpeningDetection()) {
            return "Opening detection" + getInputDescription();
        }
        if (isClosingDetection()) {
            return "Closing detection" + getInputDescription();
        }
        if (isTimeExpiryDetectionOnOpenedContact()) {
            return "Detection of a time expiry on an opened contact" + getInputDescription();
        }
        if (isTimeExpiryDetectionOnClosedContact()) {
            return "Detection of a time expiry on a closed contact" + getInputDescription();
        }
        return UNKNOWN;
    }

    public int getCause() {
        return cause;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public int getStatus() {
        return status;
    }

    public int getLogicalStateReadInput1() {
        return (status & 0x01);
    }

    public int getLogicalStateReadInput2() {
        return (status & 0x02) >> 1;
    }

    public int getLogicalStateReadInput3() {
        return (status & 0x04) >> 2;
    }

    public int getLogicalStateReadInput4() {
        return (status & 0x08) >> 3;
    }

    public boolean isTransmittedOnEventDetection() {
        return (status & 0x40) == 0x40;
    }

    public boolean isReceptionOfAck() {
        return (status & 0x80) == 0x80;
    }


    public boolean isOpeningDetection() {
        return (cause & 0x01) == 0x01;
    }

    public boolean isClosingDetection() {
        return (cause & 0x02) == 0x02;
    }

    public boolean isTimeExpiryDetectionOnOpenedContact() {
        return (cause & 0x04) == 0x04;
    }

    public boolean isTimeExpiryDetectionOnClosedContact() {
        return (cause & 0x08) == 0x08;
    }
}