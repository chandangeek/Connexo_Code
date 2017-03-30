/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1700.counters;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.Date;

/**
 * The phaseFailureCounter keeps track of how many times a phaseFailure has occurred plus the latest three eventTimes and phase indication
 */
public class PhaseFailureCounter2 extends AbstractCounter {

    protected static final int PhaseIndicationLength = 1;

    private int failedPhase0;
    private int failedPhase1;
    private int failedPhase2;
    private int failedPhase3;
    private int failedPhase4;

    private int eventCountPhaseA;
    private int eventCountPhaseB;
    private int eventCountPhaseC;

    private int outTimePhaseA;
    private int outTimePhaseB;
    private int outTimePhaseC;

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
    public PhaseFailureCounter2(final ProtocolLink protocolLink) {
        super(protocolLink);
    }

    /**
     * Parse the date according to the counter specification
     *
     * @param data the data to parse
     * @throws java.io.IOException when the conversion of certain types did not succeed
     */
    @Override
    public void parse(final byte[] data) throws IOException {

        int offset = 0;

        setEventCountPhaseA(ProtocolUtils.getIntLE(data, offset, CounterLength));
        offset += CounterLength;
        setEventCountPhaseB(ProtocolUtils.getIntLE(data, offset, CounterLength));
        offset += CounterLength;
        setEventCountPhaseC(ProtocolUtils.getIntLE(data, offset, CounterLength));
        offset += CounterLength;

        setOutTimePhaseA((int) (ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask));
        offset += DateTimeLength;
        setOutTimePhaseB((int) (ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask));
        offset += DateTimeLength;
        setOutTimePhaseC((int) (ProtocolUtils.getIntLE(data, offset, DateTimeLength) & DateTimeMask));
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

        setFailedPhase0(ProtocolUtils.getIntLE(data, offset, PhaseIndicationLength));
        offset += PhaseIndicationLength;
        setFailedPhase1(ProtocolUtils.getIntLE(data, offset, PhaseIndicationLength));
        offset += PhaseIndicationLength;
        setFailedPhase2(ProtocolUtils.getIntLE(data, offset, PhaseIndicationLength));
        offset += PhaseIndicationLength;
        setFailedPhase3(ProtocolUtils.getIntLE(data, offset, PhaseIndicationLength));
        offset += PhaseIndicationLength;
        setFailedPhase4(ProtocolUtils.getIntLE(data, offset, PhaseIndicationLength));
        offset += PhaseIndicationLength;
    }

    public int getFailedPhase0() {
        return failedPhase0;
    }

    public void setFailedPhase0(final int failedPhase0) {
        this.failedPhase0 = failedPhase0;
    }

    public int getFailedPhase1() {
        return failedPhase1;
    }

    public void setFailedPhase1(final int failedPhase1) {
        this.failedPhase1 = failedPhase1;
    }

    public int getFailedPhase2() {
        return failedPhase2;
    }

    public void setFailedPhase2(final int failedPhase2) {
        this.failedPhase2 = failedPhase2;
    }

    public int getFailedPhase3() {
        return failedPhase3;
    }

    public void setFailedPhase3(final int failedPhase3) {
        this.failedPhase3 = failedPhase3;
    }

    public int getFailedPhase4() {
        return failedPhase4;
    }

    public void setFailedPhase4(final int failedPhase4) {
        this.failedPhase4 = failedPhase4;
    }

    public int getEventCountPhaseA() {
        return eventCountPhaseA;
    }

    public void setEventCountPhaseA(final int eventCountPhaseA) {
        this.eventCountPhaseA = eventCountPhaseA;
    }

    public int getEventCountPhaseB() {
        return eventCountPhaseB;
    }

    public void setEventCountPhaseB(final int eventCountPhaseB) {
        this.eventCountPhaseB = eventCountPhaseB;
    }

    public int getEventCountPhaseC() {
        return eventCountPhaseC;
    }

    public void setEventCountPhaseC(final int eventCountPhaseC) {
        this.eventCountPhaseC = eventCountPhaseC;
    }

    public int getOutTimePhaseA() {
        return outTimePhaseA;
    }

    public void setOutTimePhaseA(final int outTimePhaseA) {
        this.outTimePhaseA = outTimePhaseA;
    }

    public int getOutTimePhaseB() {
        return outTimePhaseB;
    }

    public void setOutTimePhaseB(final int outTimePhaseB) {
        this.outTimePhaseB = outTimePhaseB;
    }

    public int getOutTimePhaseC() {
        return outTimePhaseC;
    }

    public void setOutTimePhaseC(final int outTimePhaseC) {
        this.outTimePhaseC = outTimePhaseC;
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
