/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.events;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocolimpl.dlms.DLMSMeterEventMapper;
import com.energyict.protocolimpl.dlms.DefaultDLMSMeterEventMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

public class PrimeBasicEventLog {

    private final ObisCode obisCode;
    private final DLMSMeterEventMapper eventMapper;
    private final DlmsSession session;
    private final String name;

    /** The event group. */
    private final int eventGroup;

    public PrimeBasicEventLog(DlmsSession session, ObisCode obisCode, int eventGroup, String name) {
        this.session = session;
        this.obisCode = obisCode;
        this.eventMapper = new AS300EventLogMapper(eventGroup);
        this.name = name == null ? "" : name;
        this.eventGroup = eventGroup;
    }

    public List<MeterEvent> getEvents(Calendar from, Calendar to) throws IOException {
        session.getLogger().log(Level.INFO, "Fetching EVENTS from " + toString() + " from [" + from.getTime() + "] to [" + to.getTime() + "]");
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        Array eventArray = getEventArray(from, to);
        for (AbstractDataType abstractEventData : eventArray.getAllDataTypes()) {
            BasicEvent basicEvent = getBasicEvent(abstractEventData);
            if (basicEvent != null) {
                meterEvents.add(basicEvent.getMeterEvent(eventMapper));
            }
        }
        return meterEvents;
    }

    private BasicEvent getBasicEvent(AbstractDataType abstractEventData) throws IOException {
        if (abstractEventData.isStructure()) {
            Structure structure = abstractEventData.getStructure();
            return structure != null ? new BasicEvent(structure, session.getTimeZone()) : null;
        } else {
            session.getLogger().severe("Expected Array of Structures but one entry was a [" + abstractEventData.getClass().getName() + "]");
        }
        return null;
    }

    private Array getEventArray(Calendar from, Calendar to) throws IOException {
        byte[] rawData = session.getCosemObjectFactory().getProfileGeneric(obisCode).getBufferData(from, to);
        AbstractDataType abstractData = AXDRDecoder.decode(rawData);
        if (!abstractData.isArray()) {
            throw new IOException("Expected Array of events, but received [" + abstractData.getClass().getName() + "]");
        }
        return abstractData.getArray();
    }

    private final class BasicEvent extends Structure {

        private final TimeZone timeZone;

        private final List<AbstractDataType> capturedObjects = new ArrayList<AbstractDataType>();

        private final int eventCode;

        private final Date eventTime;

        public BasicEvent(Structure eventStructure, TimeZone timeZone) throws IOException {
            super(eventStructure.getBEREncodedByteArray(), 0, 0);

            OctetString eventDateString = getDataType(0).getOctetString();
            AXDRDateTime timeStamp = new AXDRDateTime(eventDateString.getBEREncodedByteArray(), 0, timeZone);

            this.eventTime = timeStamp.getValue().getTime();
            this.eventCode = getDataType(1).intValue();

            this.timeZone = timeZone;

        	int index = 2;

        	while (index < this.nrOfDataTypes()) {
        		this.capturedObjects.add(this.getDataType(index));
        		index++;
        	}
        }

        public MeterEvent getMeterEvent(DLMSMeterEventMapper mapper) throws IOException {
            return mapper.getMeterEvent(getEventTime(), getEventCode(), eventGroup, this.capturedObjects);
        }

        private Date getEventTime() throws IOException {
        	return this.eventTime;
        }

        private int getEventCode() {
            return this.eventCode;
        }
    }

    private static class AS300EventLogMapper extends DefaultDLMSMeterEventMapper {

    	/** The group for the power contracts. */
    	private static final int POWER_CONTRACT_GROUP = 1;

    	/** The switch control group. */
    	private static final int SWITCH_CONTROL_GROUP = 2;

    	/** The code for the contract power changed. */
    	private static final int CONTRACT_POWER_CHANGED = 96;

        private final int eventGroup;

        public AS300EventLogMapper(int eventGroup) {
            this.eventGroup = eventGroup;
        }

        protected int getEisEventCode(int meterEventCode) {
            return PrimeEvents.find(meterEventCode, eventGroup).getEIServerCode();
        }

        protected String getEventMessage(int meterEventCode) {
        	final StringBuilder builder = new StringBuilder(PrimeEvents.find(meterEventCode, eventGroup).getDescription());

        	if (this.eventGroup == POWER_CONTRACT_GROUP && meterEventCode == CONTRACT_POWER_CHANGED) {

        	}

            return builder.toString();
        }

    }

    @Override
    public String toString() {
        return "[" + obisCode + "], [" + name + "]";
    }

}
