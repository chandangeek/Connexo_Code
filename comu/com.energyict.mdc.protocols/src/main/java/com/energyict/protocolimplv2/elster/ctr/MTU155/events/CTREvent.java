/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.events;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CTREvent implements Comparable<CTREvent> {

    private static final int NUMBER_OF_VALUES = 11;
    private final CTRAbstractValue[] rawEvent;
    private final TimeZone timeZone;

    public CTREvent(CTRAbstractValue[] rawEvent, TimeZone timeZone) {
        this.rawEvent = rawEvent;
        this.timeZone = timeZone;
    }

    public Date getTimeStamp() {
        if (isIndexSave()) {

            int year = rawEvent[0].getIntValue() + 2000;
            int month = rawEvent[1].getIntValue() - 1;
            int day = rawEvent[2].getIntValue();
            int hour = rawEvent[3].getIntValue();
            int min = rawEvent[4].getIntValue();

            hour = hour >= 30 ? hour - 30 : hour;
            min = min >= 60 ? min - 60 : min;

            Calendar cal = Calendar.getInstance(getTimeZone());
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, min);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return cal.getTime();
        } else {
            return new Date(0);
        }
    }

    public int getProgressiveEventNumber() {
        return isIndexSave() ? rawEvent[5].getIntValue() : -1;
    }

    public int getType() {
        return isIndexSave() ? rawEvent[6].getIntValue() : -1;
    }

    public String getTypeDescription() {
        String description = "";
        int start = getType() & 0x03;
        int channel = (getType() >> 2) & 0x3F;
        switch (start) {
            case 1: description += "START, "; break;
            case 2: description += "END, "; break;
        }
        return description + "Channel " + channel;
    }

    public int getCode() {
        return isIndexSave() ? rawEvent[7].getIntValue() : -1;
    }

    public String getCodeDescription() {
        switch (getCode()) {
            case 0x30: return "Generic";
            case 0x31: return "Over Limit";
            case 0x32: return "Out of range";
            case 0x33: return "Programming";
            case 0x34: return "Modification of a relevant parameter";
            case 0x35: return "General fault";
            case 0x36: return "Primary supply OFF";
            case 0x37: return "Battery low";
            case 0x38: return "Modify date & time";
            case 0x3A: return "Calculation error";
            case 0x3B: return "Memories reset";
            case 0x3C: return "Relevant seal deactivated";
            case 0x3D: return "Synchronization error";
            case 0x3E: return "Reset event queue";
            case 0x3F: return "Day light saving time programming";
            case 0x40: return "Event buffer full";
            case 0x41: return "Tariff scheme configuration";
            case 0x42: return "Activation of a new tariff scheme";
            case 0x43: return "Download of new software";
            case 0x44: return "Activation of new software";
            case 0x46: return "Fraud attempt";
            case 0x47: return "Change of status";
            case 0x48: return "Programming failed";
            case 0x49: return "Flow cut-off";
            case 0x4A: return "Pressure cut-off";
            case 0x4B: return "Halt volume calculation at standard therm. cond.";
            case 0x4C: return "Modification of security parameters";
            case 0x4D: return "Replace batteries";
            default: return "Unknown event code ["+getCode()+"]";
        }
    }

    public boolean isValid() {
        return isIndexSave() && isValidTimeStamp();
    }

    public boolean isValidTimeStamp() {
        Date min = ProtocolTools.createCalendar(1999, 1, 1, 0, 0, 0, 0).getTime();
        Date max = ProtocolTools.createCalendar(2050, 1, 1, 0, 0, 0, 0).getTime();
        return min.before(getTimeStamp()) && max.after(getTimeStamp());
    }

    private boolean isIndexSave() {
        return (rawEvent != null) && (rawEvent.length == NUMBER_OF_VALUES);
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isSetTimeEvent() {
        return isValid() && (getCode() == 0x38);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CTREvent{");
        sb.append("valid=").append(isValid()).append(", ");
        sb.append("time=").append(getTimeStamp()).append(", ");
        sb.append("progressiveEventNr=").append(getProgressiveEventNumber()).append(", ");
        sb.append("type=").append(getType()).append(", ");
        sb.append("typeDescription=").append(getTypeDescription()).append(", ");
        sb.append("code=").append(getCode()).append(", ");
        sb.append("codeDescription=").append(getCodeDescription()).append(", ");
        sb.append("}");
        return sb.toString();
    }

    public int compareTo(CTREvent o) {
        if (getProgressiveEventNumber() > o.getProgressiveEventNumber()) {
            return  1;
        } else if (getProgressiveEventNumber() < o.getProgressiveEventNumber()) {
            return -1;
        } else {
            return 0;
        }
    }

}
