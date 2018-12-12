package com.energyict.protocolimpl.iec1107.abba1700.counters;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Date;

/**
 * The PowerDownCounter keeps track of how many times the powerDown event has occurred plus the lates three eventTimes
 */
public class PowerDownCounter2 extends AbstractCounter {

    private Date eventStart0;
    private Date eventStart1;
    private Date eventStart2;
    private Date eventStart3;
    private Date eventStart4;

    /**
     * Create new instance
     *
     * @param protocolLink the used ProtocolLink
     */
    public PowerDownCounter2(final ProtocolLink protocolLink) {
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
}
