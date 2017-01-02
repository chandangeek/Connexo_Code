package com.energyict.protocolimplv2.elster.ctr.MTU155.profile;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimplv2.elster.ctr.EK155.EK155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.CTRRegisterMapping;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.SmsObisCodeMapper;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.Trace_CQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.CTRObjectInfo;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 16:14:08
 */
public class ProfileChannelForSms {

    private String deviceSerialNumber;
    private MTU155Properties properties;
    private Trace_CQueryResponseStructure response;
    private StartOfGasDayParser startOfGasDayParser;
    private boolean fetchTotals;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public ProfileChannelForSms(String deviceSerialNumber, MTU155Properties properties, Trace_CQueryResponseStructure queryResponseStructure, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.deviceSerialNumber = deviceSerialNumber;
        this.properties = properties;
        this.response = queryResponseStructure;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        int startHourOfDay = response.getEndOfDayTime().getIntValue();

        if (properties instanceof EK155Properties) {
            boolean startHourWithoutDST = false; // ((EK155Properties) properties).isStartHourWithoutDst(); TODO
            this.startOfGasDayParser = new StartOfGasDayParser(true, startHourOfDay, getDeviceTimeZone().useDaylightTime(), startHourOfDay, startHourWithoutDST);
        } else {
            this.startOfGasDayParser = new StartOfGasDayParser(false, startHourOfDay, getDeviceTimeZone().useDaylightTime(), -1, false);
        }
    }

    /**
     * @return the profile data
     * @throws CTRException
     */
    public ProfileData getProfileData() throws CTRException {
        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfos());
        pd.setIntervalDatas(getIntervalData());
        return pd;
    }

    private List<IntervalData> getIntervalData() {
        return new TraceCProfileParser(getResponse(), getDeviceTimeZone(), getStartOfGasDayParser(), removeDayProfileOffset()).getIntervalData();
    }

    protected List<ChannelInfo> getChannelInfos() throws CTRException {
        // Get the channel ID from the data received in the SMS
        CTRObjectID objectId = new CTRObjectID(getResponse().getId().getX(), getResponse().getId().getY(), 0);  // Z-field of CTR object ID should be set to 0 (=instantaneous value)
        CTRRegisterMapping ctrRegisterMapping = getSmsObisCodeMapper().searchRegisterMapping(objectId);

        if (ctrRegisterMapping != null) {
            ObisCode obisCode = ctrRegisterMapping.getObisCode();
            Unit unit = CTRObjectInfo.getUnit(objectId.toString());
            return Collections.singletonList(new ChannelInfo(0, obisCode.toString(), unit, getDeviceSerialNumber()));
        }
        throw new CTRException("SMS contained profile data, but failed to map the data to a device load profile channel");
    }

    private MTU155Properties getProperties() {
        return properties;
    }

    private TimeZone getDeviceTimeZone() {
        return getProperties().getTimeZone();
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    private SmsObisCodeMapper getSmsObisCodeMapper() {
        return new SmsObisCodeMapper(null, this.collectedDataFactory, this.issueFactory);
    }

    public Trace_CQueryResponseStructure getResponse() {
        return response;
    }

    public StartOfGasDayParser getStartOfGasDayParser() {
        return startOfGasDayParser;
    }

    public boolean removeDayProfileOffset() {
        return getProperties().removeDayProfileOffset();
    }
}