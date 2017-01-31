/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.idis;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimpl.dlms.idis.events.DisconnectorControlLog;
import com.energyict.protocolimpl.dlms.idis.events.FraudDetectionLog;
import com.energyict.protocolimpl.dlms.idis.events.PowerFailureEventLog;
import com.energyict.protocolimpl.dlms.idis.events.PowerQualityEventLog;
import com.energyict.protocolimpl.dlms.idis.events.StandardEventLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class ProfileDataReader {

    protected IDIS idis;

    private static ObisCode DISCONNECTOR_CONTROL_LOG = ObisCode.fromString("0.0.99.98.2.255");
    private static ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    private static ObisCode FRAUD_DETECTION_LOG = ObisCode.fromString("0.0.99.98.1.255");
    private static ObisCode POWER_FAILURE_EVENT_LOG = ObisCode.fromString("1.0.99.97.0.255");
    private static ObisCode POWER_QUALITY_LOG = ObisCode.fromString("0.0.99.98.4.255");
    private List<ChannelInfo> channelInfo = null;
    private static final ObisCode OBISCODE_NR_OF_POWER_FAILURES = ObisCode.fromString("0.0.96.7.9.255");

    public ProfileDataReader(IDIS idis) {
        this.idis = idis;
    }

    public ProfileData getProfileData(ProfileLimiter limiter, boolean includeEvents) throws IOException {
        Date from = limiter.getFromDate();
        Date to = limiter.getToDate();
        return getProfileData(from, to, includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if (to == null) {
            to = new Date();
        }
        ProfileData profileData = new ProfileData();
        Calendar fromCal = Calendar.getInstance(idis.getTimeZone());
        fromCal.setTime(from);
        fromCal.set(Calendar.SECOND, 0);
        Calendar toCal = Calendar.getInstance(idis.getTimeZone());
        toCal.setTime(to);
        toCal.set(Calendar.SECOND, 0);

        ProfileGeneric profileGeneric = idis.getCosemObjectFactory().getProfileGeneric(idis.getLoadProfileObisCode());
        profileData.setChannelInfos(getChannelInfo(profileGeneric.getCaptureObjects()));
        DataContainer buffer = profileGeneric.getBuffer(fromCal, toCal);
        Object[] loadProfileEntries = buffer.getRoot().getElements();
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        IntervalValue value;

        for (int index = 0; index < loadProfileEntries.length; index++) {
            DataStructure structure = buffer.getRoot().getStructure(index);
            Date timeStamp = structure.getOctetString(0).toDate(idis.getTimeZone());
            int status = structure.getInteger(1);
            List<IntervalValue> values = new ArrayList<IntervalValue>();
            for (int channel = 0; channel < profileData.getChannelInfos().size(); channel++) {
                value = new IntervalValue(structure.getInteger(channel + 2), status, getEiServerStatus(status));
                values.add(value);
            }
            intervalDatas.add(new IntervalData(timeStamp, 0, 0, 0, values));
        }
        profileData.setIntervalDatas(intervalDatas);

        if (includeEvents) {
            List<MeterEvent> meterEvents = getMeterEvents(fromCal, toCal);
            profileData.setMeterEvents(meterEvents);
        }
        return profileData;
    }

    protected List<MeterEvent> getMeterEvents(Calendar fromCal, Calendar toCal) throws IOException {
        List<MeterEvent> events = new ArrayList<MeterEvent>();
        events.addAll(getStandardEventLog(fromCal, toCal));
        events.addAll(getFraudDetectionLog(fromCal, toCal));
        events.addAll(getDisconnectorControlLog(fromCal, toCal));
        events.addAll(getPowerQualityEventLog(fromCal, toCal));
        events.addAll(getPowerFailureEventLog(fromCal, toCal));
        return events;
    }

    protected List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects) throws IOException {
        if (channelInfo == null) {
        List<ChannelInfo> infos = new ArrayList<ChannelInfo>();
        int counter = 0;

        for (CapturedObject capturedObject : capturedObjects) {
                if (isChannel(capturedObject)) {
                ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
                    Unit unit;
                    try {
                        unit = idis.readRegister(obisCode).getQuantity().getUnit();
                    } catch (NoSuchRegisterException e) {
                        unit = Unit.get("");
                    }
                    ChannelInfo channelInfo = new ChannelInfo(counter, obisCode.toString(), unit);
                    if (isCumulative(capturedObject)) {
                        channelInfo.setCumulative();
                    }
                    infos.add(channelInfo);

                    idis.getLogger().info("Channel " + counter + ": " + obisCode.toString() + ", unit: " + unit.toString() + ", cumulative: " + (channelInfo.isCumulative() ? "yes" : "no"));
                counter++;
            }
        }
            channelInfo = infos;
        }
        return channelInfo;
    }

    protected boolean isCumulative(CapturedObject capturedObject) {
        return ParseUtils.isObisCodeCumulative(capturedObject.getLogicalName().getObisCode()) || isNrOfPowerFailures(capturedObject.getLogicalName().getObisCode());
    }

    private boolean isNrOfPowerFailures(ObisCode obisCode) {
        return OBISCODE_NR_OF_POWER_FAILURES.equals(obisCode);
    }

    protected boolean isChannel(CapturedObject capturedObject) {
        int classId = capturedObject.getClassId();
        ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
        if (classId == DLMSClassId.REGISTER.getClassId() || classId == DLMSClassId.EXTENDED_REGISTER.getClassId() || classId == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            return true;
        } else if (!isClock(obisCode) && !isProfileStatus(obisCode)) {
            return true;
        }
        return false;
    }

    private boolean isProfileStatus(ObisCode obisCode) {
        return (obisCode.getA() == 0 && (obisCode.getB() >= 0 && obisCode.getB() <= 4) && obisCode.getC() == 96 && obisCode.getD() == 10 && (obisCode.getE() == 1 || obisCode.getE() == 2 || obisCode.getE() == 3) && obisCode.getF() == 255);
    }

    private boolean isClock(ObisCode obisCode) {
        return (Clock.getDefaultObisCode().equals(obisCode));
    }

    private int getEiServerStatus(int protocolStatus) {
        int status = IntervalStateBits.OK;
        if ((protocolStatus & 0x80) == 0x80) {
            status = status | IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatus & 0x20) == 0x20) {
            status = status | IntervalStateBits.BADTIME;
        }
        if ((protocolStatus & 0x04) == 0x04) {
            status = status | IntervalStateBits.CORRUPTED;
        }
        if ((protocolStatus & 0x02) == 0x02) {
            status = status | IntervalStateBits.BADTIME;
        }
        if ((protocolStatus & 0x01) == 0x01) {
            status = status | IntervalStateBits.CORRUPTED;
        }
        return status;
    }

    private List<MeterEvent> getPowerQualityEventLog(Calendar fromCal, Calendar toCal) throws IOException {
        try {
            DataContainer powerQualityLogDC = idis.getCosemObjectFactory().getProfileGeneric(POWER_QUALITY_LOG).getBuffer(fromCal, toCal);
            PowerQualityEventLog powerQualityLog = new PowerQualityEventLog(idis.getTimeZone(), powerQualityLogDC);
            return powerQualityLog.getMeterEvents();
        } catch (DataAccessResultException e) {
            idis.getLogger().log(Level.WARNING, "Power quality event log is not supported by the device:" + e.getMessage());
            return new ArrayList<MeterEvent>();
        }
    }

    private List<MeterEvent> getPowerFailureEventLog(Calendar fromCal, Calendar toCal) throws IOException {
        try {
            DataContainer powerFailureEventLogDC = idis.getCosemObjectFactory().getProfileGeneric(POWER_FAILURE_EVENT_LOG).getBuffer(fromCal, toCal);
            PowerFailureEventLog powerFailureEventLog = new PowerFailureEventLog(idis.getTimeZone(), powerFailureEventLogDC);
            return powerFailureEventLog.getMeterEvents();
        } catch (DataAccessResultException e) {
            idis.getLogger().log(Level.WARNING, "Power failure event log is not supported by the device:" + e.getMessage());
            return new ArrayList<MeterEvent>();
        }
    }

    private List<MeterEvent> getDisconnectorControlLog(Calendar fromCal, Calendar toCal) throws IOException {
        try {
            DataContainer disconnectorControlLogDC = idis.getCosemObjectFactory().getProfileGeneric(DISCONNECTOR_CONTROL_LOG).getBuffer(fromCal, toCal);
            DisconnectorControlLog disconnectorControlLog = new DisconnectorControlLog(idis.getTimeZone(), disconnectorControlLogDC);
            return disconnectorControlLog.getMeterEvents();
        } catch (DataAccessResultException e) {
            idis.getLogger().log(Level.WARNING, "Disconnector control log is not supported by the device:" + e.getMessage());
            return new ArrayList<MeterEvent>();
        }
    }

    private List<MeterEvent> getFraudDetectionLog(Calendar fromCal, Calendar toCal) throws IOException {
        try {
            DataContainer fraudDetectionLogDC = idis.getCosemObjectFactory().getProfileGeneric(FRAUD_DETECTION_LOG).getBuffer(fromCal, toCal);
            FraudDetectionLog fraudDetectionLog = new FraudDetectionLog(idis.getTimeZone(), fraudDetectionLogDC);
            return fraudDetectionLog.getMeterEvents();
        } catch (DataAccessResultException e) {
            idis.getLogger().log(Level.WARNING, "Fraud detection log is not supported by the device:" + e.getMessage());
            return new ArrayList<MeterEvent>();
        }
    }

    private List<MeterEvent> getStandardEventLog(Calendar fromCal, Calendar toCal) throws IOException {
        try {
            DataContainer standardEventLogDC = idis.getCosemObjectFactory().getProfileGeneric(STANDARD_EVENT_LOG).getBuffer(fromCal, toCal);
            StandardEventLog standardEventLog = new StandardEventLog(idis.getTimeZone(), standardEventLogDC);
            return standardEventLog.getMeterEvents();
        } catch (DataAccessResultException e) {
            idis.getLogger().log(Level.WARNING, "Standard event log is not supported by the device:" + e.getMessage());
            return new ArrayList<MeterEvent>();
        }
    }
}