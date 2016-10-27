/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a single interval record.
 *
 * @author Karel
 */
@XmlRootElement
public class IntervalData implements java.io.Externalizable, Comparable, IntervalStateBits {

    private static final int THIRTY_SECONDS = 30_000;
    private static final int SIXTY_SECONDS = 60_000;

    private Date endTime;
    private int eiStatus = 0;
    private int protocolStatus = 0;
    private int tariffCode = 0;
    private List<IntervalValue> intervalValues = new ArrayList<>();

    /**
     * Return a list of String, one for each IntervalStateBit in the IntervalData
     *
     * @param flag int IntervalStateBit flags
     * @return List
     */
    public static List<String> getDescriptions(int flag) {
        List<String> result = new ArrayList<>();
        if ((flag & POWERDOWN) != 0) {
            result.add("POWERDOWN");
        }
        if ((flag & POWERUP) != 0) {
            result.add("POWERUP");
        }
        if ((flag & SHORTLONG) != 0) {
            result.add("SHORTLONG");
        }
        if ((flag & WATCHDOGRESET) != 0) {
            result.add("WATCHDOGRESET");
        }
        if ((flag & CONFIGURATIONCHANGE) != 0) {
            result.add("CONFIGURATIONCHANGE");
        }
        if ((flag & CORRUPTED) != 0) {
            result.add("CORRUPTED");
        }
        if ((flag & OVERFLOW) != 0) {
            result.add("OVERFLOW");
        }
        if ((flag & ESTIMATED) != 0) {
            result.add("ESTIMATED");
        }
        if ((flag & MISSING) != 0) {
            result.add("MISSING");
        }
        if ((flag & MODIFIED) != 0) {
            result.add("MODIFIED");
        }
        if ((flag & REVISED) != 0) {
            result.add("REVISED");
        }
        if ((flag & OTHER) != 0) {
            result.add("OTHER");
        }
        if ((flag & REVERSERUN) != 0) {
            result.add("REVERSERUN");
        }
        if ((flag & PHASEFAILURE) != 0) {
            result.add("PHASEFAILURE");
        }
        if ((flag & BADTIME) != 0) {
            result.add("BADTIME");
        }
        if ((flag & INITIALFAILVALIDATION) != 0) {
            result.add("INITIALFAILVALIDATION");
        }
        if ((flag & CURRENTFAILVALIDATION) != 0) {
            result.add("CURRENTFAILVALIDATION");
        }
        if ((flag & DEVICE_ERROR) != 0) {
            result.add("DEVICE_ERROR");
        }
        if ((flag & BATTERY_LOW) != 0) {
            result.add("BATTERY_LOW");
        }
        if ((flag & TEST) != 0) {
            result.add("TEST");
        }
        return result;
    }


    public IntervalData() {
    }

    /**
     * <p></p>
     *
     * @param endTime end of interval in UTC
     */
    public IntervalData(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * <p></p>
     *
     * @param endTime  end of interval in UTC
     * @param eiStatus generic interval status
     */
    public IntervalData(Date endTime, int eiStatus) {
        this(endTime);
        this.eiStatus = eiStatus;
    }

    /**
     * <p></p>
     *
     * @param endTime        end of interval in UTC
     * @param eiStatus       generic interval status
     * @param protocolStatus protocol specific interval status
     */
    public IntervalData(Date endTime, int eiStatus, int protocolStatus) {
        this(endTime, eiStatus);
        this.protocolStatus = protocolStatus;
    }

    /**
     * <p></p>
     *
     * @param endTime        end of interval in UTC
     * @param eiStatus       generic interval status
     * @param protocolStatus protocol specific interval status
     * @param tariffCode     tariff code
     */
    public IntervalData(Date endTime, int eiStatus, int protocolStatus, int tariffCode) {
        this(endTime, eiStatus, protocolStatus);
        this.tariffCode = tariffCode;
    }

    /**
     * <p></p>
     *
     * @param endTime        end of interval in UTC
     * @param eiStatus       generic interval status
     * @param protocolStatus protocol specific interval status
     * @param tariffCode     tariff code
     * @param intervalValues List of IntervalValue
     */
    public IntervalData(Date endTime, int eiStatus, int protocolStatus, int tariffCode, List<IntervalValue> intervalValues) {
        this(endTime, eiStatus, protocolStatus, tariffCode);
        this.intervalValues = intervalValues;
    }

    /**
     * <p></p>
     *
     * @return time at the end of interval
     */
    @XmlAttribute
    public Date getEndTime() {
        return endTime;
    }

    /**
     * <p></p>
     *
     * @return the generic interval status
     */
    @XmlAttribute
    public int getEiStatus() {
        return eiStatus;
    }

    /**
     * <p></p>
     *
     * @return the protocol specific interval status
     */
    @XmlAttribute
    public int getProtocolStatus() {
        return protocolStatus;
    }

    /**
     * <p></p>
     *
     * @param index the logical channel index (zero based)
     * @return the protocolStatus for the given logical channel
     */
    public int getProtocolStatus(int index) {
        return intervalValues.get(index).getProtocolStatus();
    }

    /**
     * <p></p>
     *
     * @param index the logical channel index (zero based)
     * @return the eict channelStatus for the given logical channel OR-ed with the global EiStatus
     */
    public int getEiStatus(int index) {
        return intervalValues.get(index).getEiStatus() | getEiStatus();
    }

    /**
     * Returns a String with comma separated IntervalStateBit descriptions for the channel (index) in IntervalData
     *
     * @param index int index
     * @return String
     */
    public String getEiStatusTranslation(int index) {
        int state = intervalValues.get(index).getEiStatus() | getEiStatus();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < IntervalStateBits.states.length; i++) {
            if ((state & (0x00000001 << i)) != 0) {
                stringBuilder.append(IntervalStateBits.states[i]).append(" ");
            }
        }
        return stringBuilder.toString();
    }


    /**
     * Setter for EIStatus IntervalStateBit flags for the IntervalData
     *
     * @param eiStatus int IntervalStateBit flags
     */
    public void setEiStatus(int eiStatus) {
        this.eiStatus = eiStatus;
    }

    /**
     * Setter for ProtocolStatus (manufacturer's meter code) IntervalStateBit flags for the IntervalData. This is for informational purposes only and is not used in the businesslogic of EIServer.
     *
     * @param protocolStatus int
     */
    public void setProtocolStatus(int protocolStatus) {
        this.protocolStatus = protocolStatus;
    }

    // KV 25082004

    /**
     * Setter for EIStatus IntervalStateBit flags for the channel (index) in IntervalData
     *
     * @param index    int index
     * @param eiStatus IntervalStateBit flags
     */
    public void setEiStatus(int index, int eiStatus) {
        intervalValues.get(index).setEiStatus(eiStatus);
    }

    /**
     * Setter for ProtocolStatus for the channel (index) in IntervalData. This is for informational purposes only and is not used in the businesslogic of EIServer.
     *
     * @param index          int
     * @param protocolStatus int
     */
    public void setProtocolStatus(int index, int protocolStatus) {
        intervalValues.get(index).setProtocolStatus(protocolStatus);
    }

    /*
    *  Copy the eiStatus and protocolStatus of all values in intervalValues to this.intervalValues
    */

    /**
     * Setter for the IntervalValue objects List in IntervalData
     *
     * @param values List
     */
    public void setValuesStatus(List values) {
        for (int i = 0; i < intervalValues.size(); i++) {
            intervalValues.get(i).setEiStatus(((IntervalValue) values.get(i)).getEiStatus());
            intervalValues.get(i).setProtocolStatus(((IntervalValue) values.get(i)).getProtocolStatus());
        }
    }

    /**
     * Copy another IntervalData status information into this IntervalData
     *
     * @param intervalData IntervalData to copy from
     */
    public void copyStatus(IntervalData intervalData) {
        setValuesStatus(intervalData.getIntervalValues());
        setEiStatus(intervalData.getEiStatus());
        setProtocolStatus(intervalData.getProtocolStatus());
    }

    /**
     * <p></p>
     *
     * @return the tariff code
     */
    @XmlAttribute
    public int getTariffCode() {
        return tariffCode;
    }


    /* <p></p>
    * @param tariffCode the tariff code
    */

    /**
     * Setter for the tariffCode
     *
     * @param tariffCode int
     */
    public void setTariffCode(int tariffCode) {
        this.tariffCode = tariffCode;
    }


    /**
     * adds a value to the receiver
     *
     * @param number          an Integer or BigDecimal representing the value for the given interval and logical channel
     * @param channelStatus   a status for the logical channel
     * @param eiChannelStatus an eict converted status for the logical channel
     */
    public void addValue(Number number, int channelStatus, int eiChannelStatus) {
        intervalValues.add(new IntervalValue(number, channelStatus, eiChannelStatus));
    }

    /**
     * adds a value to the receiver
     *
     * @param number an Integer or BigDecimal representing the value for the
     *               given interval and logical channel
     */
    public void addValue(Number number) {
        intervalValues.add(new IntervalValue(number));
    }

    /**
     * adds a value to the receiver
     *
     * @param numbers an array of Numbers representing the values for the
     *                given interval
     */
    public void addValues(Number[] numbers) {
        for (int i = 0; i < numbers.length; i++) {
            intervalValues.add(new IntervalValue(numbers[i]));
        }
    }

    /* adds a value to the receiver
     * @param numbers a collection of Numbers representing the value for the
     * given interval and logical channel
     */

    /**
     * add a collection of values
     *
     * @param numbers collection of Number objects
     */
    public void addValues(Collection numbers) {
        for (Object number : numbers) {
            intervalValues.add(new IntervalValue((Number) number));
        }
    }


    /**
     * <p></p>
     *
     * @return the number of values for the interval
     */
    public int getValueCount() {
        return intervalValues.size();
    }

    /**
     * <p></p>
     *
     * @param index the logical channel index (zero based)
     * @return the value for the given index
     */
    public Number get(int index) {
        return intervalValues.get(index).getNumber();
    }

    /**
     * <p></p>
     *
     * @return an iterator over the values for the interval
     */
    public Iterator getValuesIterator() {
        return intervalValues.iterator();
    }

    /**
     * Getter for IntervalValue objects
     *
     * @return List
     */
    @XmlAttribute
    public List getIntervalValues() {
        return intervalValues;
    }

    /**
     * Returns a ListIterator for the IntervalValue objects List
     *
     * @return List Iterator
     */
    public ListIterator getIntervalValueIterator() {
        return intervalValues.listIterator();
    }

    /**
     * Setter for the IntervalValue objects List
     *
     * @param intervalValues List
     */
    public void setIntervalValues(List<IntervalValue> intervalValues) {
        this.intervalValues = intervalValues;
    }

    /**
     * adds the specified status to the current interval status
     *
     * @param status one of
     *               <UL>
     *               <LI>OK</LI>
     *               <LI>POWERDOWN</LI>
     *               <LI>POWERUP</LI>
     *               <LI>SHORTLONG</LI>
     *               <LI>WATCHDOG</LI>
     *               <LI>CONFIGURATION</LI>
     *               </UL>
     */
    public void addStatus(int status) {
        eiStatus |= status;
    }

    /**
     * Add an IntervalStateBit. The IntervalStateBit is OR-ed together with the already present EIStatus.
     *
     * @param eiStatus int IntervalStateBit
     */
    public void addEiStatus(int eiStatus) {
        this.eiStatus |= eiStatus;
    }

    /**
     * Add a ProtocolStatus. The ProtocolStatus is OR-ed together with the already present ProtocolStatus.
     *
     * @param protocolStatus int
     */
    public void addProtocolStatus(int protocolStatus) {
        this.protocolStatus |= protocolStatus;
    }

    /**
     * <p>tests if the date is within the interval</p>
     *
     * @param date     the date to the test
     * @param interval the interval length in minutes
     * @return true if the given date is in the interval
     */
    public boolean includes(Date date, int interval) {
        if (endTime.getTime() < date.getTime()) {
            return false;
        }
        return ((endTime.getTime() - date.getTime()) / SIXTY_SECONDS) < interval;
    }

    /**
     * updates the interval status based on the information in the event.
     * Calls doApply if the event happened during the interval
     *
     * @param event    <br>
     * @param interval interval length in minutes
     */
    public void apply(MeterEvent event, int interval) {
        if (includes(event.getTime(), interval)) {
            doApply(event);
        }
    }

    /**
     * updates the interval status based on the information in the event.
     *
     * @param event <br>
     */
    protected void doApply(MeterEvent event) {
        switch (event.getEiCode()) {

            case MeterEvent.POWERDOWN:
                addStatus(IntervalData.POWERDOWN);
                break;

            case MeterEvent.POWERUP:
                addStatus(IntervalData.POWERUP);
                break;

            case MeterEvent.WATCHDOGRESET:
                addStatus(IntervalData.WATCHDOGRESET);
                break;

            case MeterEvent.SETCLOCK:
            case MeterEvent.SETCLOCK_BEFORE:
            case MeterEvent.SETCLOCK_AFTER:
                addStatus(IntervalData.SHORTLONG);
                break;

            case MeterEvent.CONFIGURATIONCHANGE:
                addStatus(IntervalData.CONFIGURATIONCHANGE);
                break;

            // KV 29082006
            case MeterEvent.HARDWARE_ERROR:
                addStatus(IntervalData.DEVICE_ERROR);
                break;

            // KV 10102003
            case MeterEvent.OTHER:
                addStatus(IntervalData.OTHER);
                break;

            // KV 12082005
            case MeterEvent.VOLTAGE_SAG:
                addStatus(IntervalData.OTHER);
                break;
            case MeterEvent.VOLTAGE_SWELL:
                addStatus(IntervalData.OTHER);
                break;
            case MeterEvent.PHASE_FAILURE:
                addStatus(IntervalData.PHASEFAILURE);
                break;

            case MeterEvent.REVERSE_RUN:
                addStatus(IntervalData.REVERSERUN);
                break;


        }

    }

    /**
     * generate a list of events based on the interval status
     *
     * @return a list of MeterEvents
     */
    public List<MeterEvent> generateEvents() {
        List<MeterEvent> result = new ArrayList<>();
        if (eiStatus == IntervalData.OK) {
            return result;
        }
        // report event as 30 seconds before end of interval
        Date eventTime = new Date(endTime.getTime() - THIRTY_SECONDS);
        if ((eiStatus & IntervalData.POWERDOWN) != 0) {
            result.add(new MeterEvent(eventTime, MeterEvent.POWERDOWN));
        }
        if ((eiStatus & IntervalData.POWERUP) != 0) {
            result.add(new MeterEvent(eventTime, MeterEvent.POWERUP));
        }
        if ((eiStatus & IntervalData.SHORTLONG) != 0) {
            result.add(new MeterEvent(eventTime, MeterEvent.SETCLOCK));
        }
        if ((eiStatus & IntervalData.WATCHDOGRESET) != 0) {
            result.add(new MeterEvent(eventTime, MeterEvent.WATCHDOGRESET));
        }
        if ((eiStatus & IntervalData.CONFIGURATIONCHANGE) != 0) {
            result.add(new MeterEvent(eventTime, MeterEvent.CONFIGURATIONCHANGE));
        }
        // KV 10102003
        if ((eiStatus & IntervalData.OTHER) != 0) {
            result.add(new MeterEvent(eventTime, MeterEvent.OTHER));
        }
        // KV 12082005
        if ((eiStatus & IntervalData.PHASEFAILURE) != 0) {
            result.add(new MeterEvent(eventTime, MeterEvent.PHASE_FAILURE));
        }
        if ((eiStatus & IntervalData.REVERSERUN) != 0) {
            result.add(new MeterEvent(eventTime, MeterEvent.REVERSE_RUN));
        }

        // KV 29082006
        if ((eiStatus & IntervalData.DEVICE_ERROR) != 0) {
            result.add(new MeterEvent(eventTime, MeterEvent.HARDWARE_ERROR));
        }

        return result;
    }


    /**
     * Compare another IntervalData to this IntervalData
     *
     * @param o IntervalData
     * @return comparision result
     */
    public int compareTo(Object o) {
        return (endTime.compareTo(((IntervalData) o).getEndTime()));
    }

    // Implementation of the Externalizable interface
    // This is done to boost the performance in the case of use with the PDA
    // Measurements on PDA IPAQ 3850:
    // Java serialization takes +/- 360 sec to serialize/deserialize an 2880 intervalvalues of 6 channels profiledata object
    // After implementing Externalizable in IntervalData, serialization of the same object takes 51 sec to serialize and 16.3 sec to deserialize

    /**
     * Used by the Serialization interface
     *
     * @param input Object
     * @throws java.io.IOException thrown when something goes wrong
     * @throws java.lang.ClassNotFoundException
     *                             ClassNotFoundException
     */
    public void readExternal(ObjectInput input) throws IOException, java.lang.ClassNotFoundException {
        endTime = new Date(input.readLong());
        eiStatus = input.readInt();
        protocolStatus = input.readInt();
        tariffCode = input.readInt();

        int size = input.readInt();
        intervalValues = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            IntervalValue intervalValue = new IntervalValue(new BigDecimal(input.readLine()), input.readInt(), input.readInt());
            intervalValues.add(intervalValue);
        }
    }

    /**
     * Used by the Serialization interface
     *
     * @param output Object
     * @throws java.io.IOException thrown when something goes wrong
     */
    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeLong(endTime.getTime());
        output.writeInt(eiStatus);
        output.writeInt(protocolStatus);
        output.writeInt(tariffCode);

        output.writeInt(intervalValues.size());
        for (IntervalValue intervalValue : intervalValues) {
            output.writeBytes(intervalValue.getNumber().toString() + "\n");
            output.writeInt(intervalValue.getProtocolStatus());
            output.writeInt(intervalValue.getEiStatus());
        }
    }

    /**
     * Returns a String interpretation for this IntervalData object
     *
     * @return String
     */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.
                append(getEndTime()).
                append(" ").
                append(getProtocolStatus()).
                append(" ").
                append(getEiStatus()).
                append(" ")
                .append("Values: ");
        for (IntervalValue intervalValue : intervalValues) {
            stringBuilder.append(intervalValue.toString());
        }
        return stringBuilder.toString();
    }

}