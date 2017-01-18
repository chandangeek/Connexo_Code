package com.energyict.protocolimplv2.elster.ctr.MTU155.profile;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.elster.ctr.EK155.EK155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.CTRRegisterMapping;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.SmsObisCodeMapper;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.Trace_CQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.CTRObjectInfo;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 16:14:08
 */
public class ProfileChannelForSms {

    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private String deviceSerialNumber;
    private MTU155Properties properties;
    private Trace_CQueryResponseStructure response;
    private StartOfGasDayParser startOfGasDayParser;
    private boolean fetchTotals;

    public ProfileChannelForSms(MdcReadingTypeUtilService readingTypeUtilService, IssueService issueService, CollectedDataFactory collectedDataFactory, String deviceSerialNumber, MTU155Properties properties, Trace_CQueryResponseStructure queryResponseStructure) {
        this.readingTypeUtilService = readingTypeUtilService;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.deviceSerialNumber = deviceSerialNumber;
        this.properties = properties;
        this.response = queryResponseStructure;
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
            return Arrays.asList(new ChannelInfo(0, obisCode.toString(), unit, getDeviceSerialNumber(), this.readingTypeUtilService.getReadingTypeFrom(obisCode, unit)));
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
        return new SmsObisCodeMapper(null, this.readingTypeUtilService, this.issueService, collectedDataFactory);
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