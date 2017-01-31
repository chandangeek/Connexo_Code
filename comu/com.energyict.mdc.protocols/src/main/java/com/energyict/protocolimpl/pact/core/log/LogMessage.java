/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LogEvent.java
 *
 * Created on 30 maart 2004, 9:27
 */

package com.energyict.protocolimpl.pact.core.log;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class LogMessage {

    private static int DEBUG=0;

    private static List list = new ArrayList();
    static {
        list.add(new LogMessage(254, "Not an event", -1, -1, "", -1));
        list.add(new LogMessage(1, "NVM failure", 1, -1, "On an E200 meter: MS byte of NF", MeterEvent.ROM_MEMORY_ERROR));
        list.add(new LogMessage(2, "ROM crash", 1, 0, "", MeterEvent.FATAL_ERROR));
        list.add(new LogMessage(3, "CLEM crash", 1, -1, "On a E200 meter: Byte 2 of G4 at the crash", MeterEvent.FATAL_ERROR));
        list.add(new LogMessage(4, "RTC failure", 1, 0, "Non specific failure", MeterEvent.HARDWARE_ERROR));
        list.add(new LogMessage(4, "RTC failure", 1, 1, "Battery at end of expected life time", MeterEvent.HARDWARE_ERROR));
        list.add(new LogMessage(4, "RTC failure", 1, 2, "Suspect time on power up", MeterEvent.HARDWARE_ERROR));
        list.add(new LogMessage(4, "RTC failure", 1, 3, "Failure to read the RTC", MeterEvent.HARDWARE_ERROR));
        list.add(new LogMessage(5, "CLEM start", 1, 0, "Non specific page address", MeterEvent.OTHER));
        list.add(new LogMessage(5, "CLEM start", 1, -1, "Specific page address", MeterEvent.OTHER));
        list.add(new LogMessage(6, "Voltage failure", 2, 0, "Start, no specific phase", MeterEvent.METER_ALARM));
        list.add(new LogMessage(6, "Voltage failure", 2, 1, "Start, on phase 1", MeterEvent.METER_ALARM));
        list.add(new LogMessage(6, "Voltage failure", 2, 2, "Start, on phase 2", MeterEvent.METER_ALARM));
        list.add(new LogMessage(6, "Voltage failure", 2, 3, "Start, on phase 3", MeterEvent.METER_ALARM));
        list.add(new LogMessage(6, "Voltage failure", 2, 0x80, "End, no specific phase", MeterEvent.METER_ALARM));
        list.add(new LogMessage(6, "Voltage failure", 2, 0x81, "End, on phase 1", MeterEvent.METER_ALARM));
        list.add(new LogMessage(6, "Voltage failure", 2, 0x82, "End, on phase 2", MeterEvent.METER_ALARM));
        list.add(new LogMessage(6, "Voltage failure", 2, 0x83, "End, on phase 3", MeterEvent.METER_ALARM));
        list.add(new LogMessage(7, "Voltage imbalance", 2, 0, "Start", MeterEvent.METER_ALARM));
        list.add(new LogMessage(7, "Voltage imbalance", 2, 0x80, "End", MeterEvent.METER_ALARM));
        list.add(new LogMessage(8, "Over current", 2, 0, "Start, no specific phase", MeterEvent.METER_ALARM));
        list.add(new LogMessage(8, "Over current", 2, 1, "Start, on phase 1", MeterEvent.METER_ALARM));
        list.add(new LogMessage(8, "Over current", 2, 2, "Start, on phase 2", MeterEvent.METER_ALARM));
        list.add(new LogMessage(8, "Over current", 2, 3, "Start, on phase 3", MeterEvent.METER_ALARM));
        list.add(new LogMessage(8, "Over current", 2, 0x80, "End, no specific phase", MeterEvent.METER_ALARM));
        list.add(new LogMessage(8, "Over current", 2, 0x81, "End, on phase 1", MeterEvent.METER_ALARM));
        list.add(new LogMessage(8, "Over current", 2, 0x82, "End, on phase 2", MeterEvent.METER_ALARM));
        list.add(new LogMessage(8, "Over current", 2, 0x83, "End, on phase 3", MeterEvent.METER_ALARM));
        list.add(new LogMessage(9, "Reverse current", 2, 0, "Start, no specific phase", MeterEvent.METER_ALARM));
        list.add(new LogMessage(9, "Reverse current", 2, 1, "Start, on phase 1", MeterEvent.METER_ALARM));
        list.add(new LogMessage(9, "Reverse current", 2, 2, "Start, on phase 2", MeterEvent.METER_ALARM));
        list.add(new LogMessage(9, "Reverse current", 2, 3, "Start, on phase 3", MeterEvent.METER_ALARM));
        list.add(new LogMessage(9, "Reverse current", 2, 0x80, "End, no specific phase", MeterEvent.METER_ALARM));
        list.add(new LogMessage(9, "Reverse current", 2, 0x81, "End, on phase 1", MeterEvent.METER_ALARM));
        list.add(new LogMessage(9, "Reverse current", 2, 0x82, "End, on phase 2", MeterEvent.METER_ALARM));
        list.add(new LogMessage(9, "Reverse current", 2, 0x83, "End, on phase 3", MeterEvent.METER_ALARM));
        list.add(new LogMessage(10, "Brownout", 2, 0, "Start", MeterEvent.METER_ALARM));
        list.add(new LogMessage(10, "Brownout", 2, 0x80, "End", MeterEvent.METER_ALARM));
        list.add(new LogMessage(11, "Meter reading", 3, 0, "", MeterEvent.OTHER));
        list.add(new LogMessage(12, "Survey reading", 3, 0, "", MeterEvent.OTHER));
        list.add(new LogMessage(13, "Tariff download", 3, 0, "Unspecified type of tariff", MeterEvent.OTHER));
        list.add(new LogMessage(13, "Tariff download", 3, -1, "Type of the tariff", MeterEvent.OTHER));
        list.add(new LogMessage(14, "Tariff reading", 3, 0, "Unspecified type of tariff", MeterEvent.OTHER));
        list.add(new LogMessage(14, "Tariff reading", 3, -1, "Type of the tariff", MeterEvent.OTHER));
        list.add(new LogMessage(15, "Event log reading", 3, 0, "All event logs", MeterEvent.OTHER));
        list.add(new LogMessage(15, "Event log reading", 3, -1, "Specific event log", MeterEvent.OTHER));
        list.add(new LogMessage(16, "Time set request", 3, 0, "", MeterEvent.SETCLOCK));
        list.add(new LogMessage(17, "MD reset request", 3, 0, "", MeterEvent.MAXIMUM_DEMAND_RESET));
        list.add(new LogMessage(18, "Billing action", 4, 0, "", MeterEvent.BILLING_ACTION));
        list.add(new LogMessage(19, "Tariff invocation", 4, 0, "", MeterEvent.OTHER));
        list.add(new LogMessage(20, "Auxiliary power fail", 2, 0, "Start", MeterEvent.METER_ALARM));
        list.add(new LogMessage(20, "Auxiliary power fail", 2, 0x80, "End", MeterEvent.METER_ALARM));
        list.add(new LogMessage(21, "Loss of neutral", 2, 0, "Start", MeterEvent.METER_ALARM));
        list.add(new LogMessage(21, "Loss of neutral", 2, 0x80, "End", MeterEvent.METER_ALARM));
        list.add(new LogMessage(22, "Current without volts", 2, 0, "Start, no specific phase", MeterEvent.METER_ALARM));
        list.add(new LogMessage(22, "Current without volts", 2, 1, "Start, on phase 1", MeterEvent.METER_ALARM));
        list.add(new LogMessage(22, "Current without volts", 2, 2, "Start, on phase 2", MeterEvent.METER_ALARM));
        list.add(new LogMessage(22, "Current without volts", 2, 3, "Start, on phase 3", MeterEvent.METER_ALARM));
        list.add(new LogMessage(22, "Current without volts", 2, 0x80, "End, no specific phase", MeterEvent.METER_ALARM));
        list.add(new LogMessage(22, "Current without volts", 2, 0x81, "End, on phase 1", MeterEvent.METER_ALARM));
        list.add(new LogMessage(22, "Current without volts", 2, 0x82, "End, on phase 2", MeterEvent.METER_ALARM));
        list.add(new LogMessage(22, "Current without volts", 2, 0x83, "End, on phase 3", MeterEvent.METER_ALARM));
        list.add(new LogMessage(23, "Meter power off", 2, 0, "Start", MeterEvent.POWERDOWN));
        list.add(new LogMessage(23, "Meter power off", 2, 0x80, "End", MeterEvent.POWERUP));
        list.add(new LogMessage(24, "Concecutive dates", 2, 0, "Start, no specific reason", MeterEvent.OTHER));
        list.add(new LogMessage(24, "Concecutive dates", 2, 1, "Start, after timechange", MeterEvent.OTHER));
        list.add(new LogMessage(24, "Concecutive dates", 2, 2, "Start, after powerfail", MeterEvent.OTHER));
        list.add(new LogMessage(24, "Concecutive dates", 2, 3, "Start, after change of parameters", MeterEvent.OTHER));
        list.add(new LogMessage(24, "Concecutive dates", 2, 4, "Start, after rescaling", MeterEvent.OTHER));
        list.add(new LogMessage(24, "Concecutive dates", 2, 5, "Start, after initialization", MeterEvent.OTHER));
        list.add(new LogMessage(24, "Concecutive dates", 2, 0x80, "End, no specific reason", MeterEvent.OTHER));
        list.add(new LogMessage(24, "Concecutive dates", 2, 0x81, "End, before timechange", MeterEvent.OTHER));
        list.add(new LogMessage(24, "Concecutive dates", 2, 0x82, "End, before powerfail", MeterEvent.OTHER));
        list.add(new LogMessage(24, "Concecutive dates", 2, 0x83, "End, before change of parameters", MeterEvent.OTHER));
        list.add(new LogMessage(24, "Concecutive dates", 2, 0x84, "End, before rescaling", MeterEvent.OTHER));
        list.add(new LogMessage(25, "Current imbalance", 2, 0x80, "Start", MeterEvent.METER_ALARM));
        list.add(new LogMessage(25, "Current imbalance", 2, 0x80, "end", MeterEvent.METER_ALARM));
        list.add(new LogMessage(26, "Low PF", 2, 0, "Start", MeterEvent.METER_ALARM));
        list.add(new LogMessage(26, "Low PF", 2, 0x80, "End", MeterEvent.METER_ALARM));
        list.add(new LogMessage(27, "Reset request", 3, 0, "Non specific", MeterEvent.OTHER));
        list.add(new LogMessage(27, "Reset request", 3, 1, "Battery on time", MeterEvent.OTHER));
        list.add(new LogMessage(28, "Battery failure", 1, 0, "Start", MeterEvent.HARDWARE_ERROR));
        list.add(new LogMessage(28, "Battery failure", 1, 0x80, "End", MeterEvent.HARDWARE_ERROR));
        list.add(new LogMessage(29, "Phase disturbance", 2, 0, "Start, no particular phase", MeterEvent.METER_ALARM));
        list.add(new LogMessage(29, "Phase disturbance", 2, 1, "Start, phase R,Y,B, or neutral respectively",MeterEvent.METER_ALARM));
        list.add(new LogMessage(29, "Phase disturbance", 2, 2, "Start, phase R,Y,B, or neutral respectively",MeterEvent.METER_ALARM));
        list.add(new LogMessage(29, "Phase disturbance", 2, 3, "Start, phase R,Y,B, or neutral respectively",MeterEvent.METER_ALARM));
        list.add(new LogMessage(29, "Phase disturbance", 2, 4, "Start, phase R,Y,B, or neutral respectively",MeterEvent.METER_ALARM));
        list.add(new LogMessage(29, "Phase disturbance", 2, 0x80, "End, no particular phase",MeterEvent.METER_ALARM));
        list.add(new LogMessage(29, "Phase disturbance", 2, 0x81, "End, phase R,Y,B, or neutral respectively", MeterEvent.METER_ALARM));
        list.add(new LogMessage(29, "Phase disturbance", 2, 0x82, "End, phase R,Y,B, or neutral respectively", MeterEvent.METER_ALARM));
        list.add(new LogMessage(29, "Phase disturbance", 2, 0x83, "End, phase R,Y,B, or neutral respectively", MeterEvent.METER_ALARM));
        list.add(new LogMessage(29, "Phase disturbance", 2, 0x84, "End, phase R,Y,B, or neutral respectively", MeterEvent.METER_ALARM));
        list.add(new LogMessage(30, "Magnetic tamper", 2, 0, "Start", MeterEvent.OTHER));
        list.add(new LogMessage(30, "Magnetic tamper", 2, 0x80, "End", MeterEvent.OTHER));
        list.add(new LogMessage(31, "Non zero current sum", 2, 0, "Start", MeterEvent.METER_ALARM));
        list.add(new LogMessage(31, "Non zero current sum", 2, 0x80, "End", MeterEvent.METER_ALARM));
    }

    private int mainCode;
    private String description;
    private int logId;
    private int subCode;
    private String MeaningOfSubcode;
    private int eiMeterEventCode;

    /** Creates a new instance of LogEvent */
    private LogMessage(int mainCode, String description, int logId, int subCode, String MeaningOfSubcode, int eiMeterEventCode) {
        this.mainCode=mainCode;
        this.description=description;
        this.logId=logId;
        this.subCode=subCode;
        this.MeaningOfSubcode=MeaningOfSubcode;
        this.eiMeterEventCode=eiMeterEventCode;
    }

    public static MeterEvent getMeterEvent(LogHeader lh, LogEvent le) {
        if (le.getMain() == 0xFE) {
            if (DEBUG>=1) {
				System.out.println("KV_DEBUG> No MeterEvent");
			}
            return null;
        }
        else {
            Iterator it = list.iterator();
            while(it.hasNext()) {
                LogMessage lm = (LogMessage)it.next();
                if ((lm.getLogId() == lh.getLogId()) &&
                (lm.getSubCode() == le.getSub()) &&
                (lm.getMainCode() == le.getMain())) {
                    if (DEBUG>=1) {
						System.out.println("KV_DEBUG> MeterEvent "+lm.getDescription()+", "+lm.getMeaningOfSubcode());
					}
                    if (le.getFutureDate() != null) {
						return new MeterEvent(le.getDate(),lm.getEiMeterEventCode(),lm.getDescription()+", "+lm.getMeaningOfSubcode()+", "+le.getMore()+", future event at "+le.getFutureDate());
					} else {
						return new MeterEvent(le.getDate(),lm.getEiMeterEventCode(),lm.getDescription()+", "+lm.getMeaningOfSubcode()+", "+le.getMore());
					}
                }
            }
        }
        return null;
    } // static public MeterEvent getMeterEvent(LogHeader lh, LogEvent le)

    /** Getter for property mainCode.
     * @return Value of property mainCode.
     *
     */
    public int getMainCode() {
        return mainCode;
    }

    /** Setter for property mainCode.
     * @param mainCode New value of property mainCode.
     *
     */
    public void setMainCode(int mainCode) {
        this.mainCode = mainCode;
    }

    /** Getter for property description.
     * @return Value of property description.
     *
     */
    public java.lang.String getDescription() {
        return description;
    }

    /** Setter for property description.
     * @param description New value of property description.
     *
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }

    /** Getter for property logId.
     * @return Value of property logId.
     *
     */
    public int getLogId() {
        return logId;
    }

    /** Setter for property logId.
     * @param logId New value of property logId.
     *
     */
    public void setLogId(int logId) {
        this.logId = logId;
    }

    /** Getter for property subCode.
     * @return Value of property subCode.
     *
     */
    public int getSubCode() {
        return subCode;
    }

    /** Setter for property subCode.
     * @param subCode New value of property subCode.
     *
     */
    public void setSubCode(int subCode) {
        this.subCode = subCode;
    }

    /** Getter for property MeaningOfSubcode.
     * @return Value of property MeaningOfSubcode.
     *
     */
    public java.lang.String getMeaningOfSubcode() {
        return MeaningOfSubcode;
    }

    /** Setter for property MeaningOfSubcode.
     * @param MeaningOfSubcode New value of property MeaningOfSubcode.
     *
     */
    public void setMeaningOfSubcode(java.lang.String MeaningOfSubcode) {
        this.MeaningOfSubcode = MeaningOfSubcode;
    }


    /** Getter for property eiMeterEventCode.
     * @return Value of property eiMeterEventCode.
     *
     */
    public int getEiMeterEventCode() {
        return eiMeterEventCode;
    }

    /** Setter for property eiMeterEventCode.
     * @param eiMeterEventCode New value of property eiMeterEventCode.
     *
     */
    public void setEiMeterEventCode(int eiMeterEventCode) {
        this.eiMeterEventCode = eiMeterEventCode;
    }

}
