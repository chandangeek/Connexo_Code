/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.events;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.legacy.MeterProtocolEventSupport;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD.ZMD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 15/09/2014 - 16:18
 */
public class LogBookReader implements MeterProtocolEventSupport {

    private static final ObisCode EVENT_OBIS = ObisCode.fromString("0.0.96.240.12.255");
    private static final int EVENT_NUMBER_ATTRIBUTE_INDEX = 2;
    private static final int EDIS_STATUS_ATTRIBUTE_INDEX = 19;
    private final ZMD meterProtocol;

    public LogBookReader(ZMD meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        Calendar fromCal = Calendar.getInstance(getMeterProtocol().getTimeZone());
        fromCal.setTime(lastLogbookDate);
        Calendar toCal = ProtocolUtils.getCalendar(getMeterProtocol().getTimeZone()); // Must be in the device time zone
        return getEventLog(fromCal, toCal);
    }

    private List<MeterEvent> getEventLog(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        ProfileGeneric profileGeneric = getMeterProtocol().getCosemObjectFactory().getProfileGeneric(getMeterProtocol().getMeterConfig().getEventLogObject().getObisCode());
        List<CapturedObject> captureObjects = profileGeneric.getCaptureObjects();

        if (getEventNumberCaptureObjectsIndex(captureObjects) != -1) {
            return readEventsBasedOnEventNumber(profileGeneric, fromCalendar, toCalendar, getEventNumberCaptureObjectsIndex(captureObjects));
        } else if (getEDISStatusCaptureObjectsIndex(captureObjects) != -1) {
            getMeterProtocol().getLogger().warning("Standard Event Log does not contain Event number - events will be created for EDIS status.");
            return readEventsBasedOnEDISStatus(profileGeneric, fromCalendar, toCalendar, getEDISStatusCaptureObjectsIndex(captureObjects));
        } else {
            throw new IOException("Failed to read out the events: the event profile does not contain Event number or EDIS status");
        }
    }

    private List<MeterEvent> readEventsBasedOnEventNumber(ProfileGeneric profileGeneric, Calendar fromCalendar, Calendar toCalendar, int eventNumberCaptureObjectsIndex) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList();
        DataContainer dc = profileGeneric.getBuffer(fromCalendar, toCalendar);

        for (int i = 0; i < dc.getRoot().getNrOfElements(); i++) {
            Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getMeterProtocol().getTimeZone());
            int eventNumber = dc.getRoot().getStructure(i).getInteger(eventNumberCaptureObjectsIndex);
            MeterEvent meterEvent = EventNumber.toMeterEvent(eventNumber, dateTime);
            if (meterEvent != null) {
                meterEvents.add(meterEvent);
            }
        }
        return meterEvents;
    }

    private List<MeterEvent> readEventsBasedOnEDISStatus(ProfileGeneric profileGeneric, Calendar fromCalendar, Calendar toCalendar, int edisStatusCaptureObjectsIndex) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList();
        DataContainer dc = profileGeneric.getBuffer(fromCalendar, toCalendar);

        for (int i = 0; i < dc.getRoot().getNrOfElements(); i++) {
            Date dateTime = dc.getRoot().getStructure(i).getOctetString(0).toDate(getMeterProtocol().getTimeZone());
            int edisStatus = dc.getRoot().getStructure(i).getInteger(edisStatusCaptureObjectsIndex);
            meterEvents.addAll(EDISStatus.getAllMeterEventsCorrespondingToEDISStatus(edisStatus, dateTime));
        }
        return meterEvents;
    }

    private int getEventNumberCaptureObjectsIndex(List<CapturedObject> captureObjects) throws IOException {
        int i = 0;
        for (CapturedObject captureObject : captureObjects) {
            if (captureObject.getObisCode().equals(EVENT_OBIS)) {
                if (captureObject.getAttributeIndex() == EVENT_NUMBER_ATTRIBUTE_INDEX) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    private int getEDISStatusCaptureObjectsIndex(List<CapturedObject> captureObjects) throws IOException {
        int i = 0;
        for (CapturedObject captureObject : captureObjects) {
            if (captureObject.getObisCode().equals(EVENT_OBIS)) {
                if (captureObject.getAttributeIndex() == EDIS_STATUS_ATTRIBUTE_INDEX) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    public ZMD getMeterProtocol() {
        return meterProtocol;
    }
}