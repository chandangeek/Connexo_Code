package com.energyict.protocolimpl.iec1107.abba1700.counters;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Date;

/**
 * The ReverseRun counter keeps track of how many times a reverseRun event has occurred plus the latest three eventTimes
 */
public class ReverseRunCounter2 extends AbstractCounter {

    private int cumulativeEventTime;

    private Date eventStart0;
    private Date eventStart1;
    private Date eventStart2;
    private Date eventStart3;
    private Date eventStart4;
    private Date eventStop0;
    private Date eventStop1;
    private Date eventStop2;
    private Date eventStop3;
    private Date eventStop4;

    /**
     * Create new instance
     *
     * @param protocolLink the used ProtocolLink
     */
    public ReverseRunCounter2(final ProtocolLink protocolLink) {
        super(protocolLink);
    }

    /**
     * Parse the date according to the counter specification
     *
     * @param data the data to parse
     * @throws java.io.IOException when the conversion of certain types did not succeed
     */
    @Override
    public void parse(final byte[] data) throws ProtocolException {

        int offset = 0;
        setCounter(ProtocolUtils.getIntLE(data, offset, CounterLength));
        offset += CounterLength;

        setCumulativeEventTime((int) (ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask));
        offset += DateTimeLength;

        long shift = (long) ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask;
        setEventStart0(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        offset += DateTimeLength;
        shift = (long) ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask;
        setEventStart1(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        offset += DateTimeLength;
        shift = (long) ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask;
        setEventStart2(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        offset += DateTimeLength;
        shift = (long) ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask;
        setEventStart3(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        offset += DateTimeLength;
        shift = (long) ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask;
        setEventStart4(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        offset += DateTimeLength;

        shift = (long) ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask;
        setEventStop0(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        offset += DateTimeLength;
        shift = (long) ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask;
        setEventStop1(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        offset += DateTimeLength;
        shift = (long) ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask;
        setEventStop2(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        offset += DateTimeLength;
        shift = (long) ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask;
        setEventStop3(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        offset += DateTimeLength;
        shift = (long) ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask;
        setEventStop4(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        offset += DateTimeLength;

    }

    public int getCumulativeEventTime() {
        return cumulativeEventTime;
    }

    public void setCumulativeEventTime(final int cumulativeEventTime) {
        this.cumulativeEventTime = cumulativeEventTime;
    }

    public Date getEventStart0() {
        return eventStart0;
    }

    public void setEventStart0(final Date eventStart0) {
        this.eventStart0 = eventStart0;
    }

    public Date getEventStart1() {
        return eventStart1;
    }

    public void setEventStart1(final Date eventStart1) {
        this.eventStart1 = eventStart1;
    }

    public Date getEventStart2() {
        return eventStart2;
    }

    public void setEventStart2(final Date eventStart2) {
        this.eventStart2 = eventStart2;
    }

    public Date getEventStart3() {
        return eventStart3;
    }

    public void setEventStart3(final Date eventStart3) {
        this.eventStart3 = eventStart3;
    }

    public Date getEventStart4() {
        return eventStart4;
    }

    public void setEventStart4(final Date eventStart4) {
        this.eventStart4 = eventStart4;
    }

    public Date getEventStop0() {
        return eventStop0;
    }

    public void setEventStop0(final Date eventStop0) {
        this.eventStop0 = eventStop0;
    }

    public Date getEventStop1() {
        return eventStop1;
    }

    public void setEventStop1(final Date eventStop1) {
        this.eventStop1 = eventStop1;
    }

    public Date getEventStop2() {
        return eventStop2;
    }

    public void setEventStop2(final Date eventStop2) {
        this.eventStop2 = eventStop2;
    }

    public Date getEventStop3() {
        return eventStop3;
    }

    public void setEventStop3(final Date eventStop3) {
        this.eventStop3 = eventStop3;
    }

    public Date getEventStop4() {
        return eventStop4;
    }

    public void setEventStop4(final Date eventStop4) {
        this.eventStop4 = eventStop4;
    }
}
