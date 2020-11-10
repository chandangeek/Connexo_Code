package com.energyict.protocolimplv2.dlms.idis.hs3300.profiles;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HS3300LoadProfileDataReader {

    private static final ObisCode LP_PERIOD_1_15_MIN_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode LP_PERIOD_2_24_HOU_OBISCODE = ObisCode.fromString("1.0.99.2.0.255");

    private final AbstractDlmsProtocol protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final List<ObisCode> supportedLoadProfiles;

    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;
    private List<CollectedLoadProfileConfiguration> loadProfileConfigurationList;

    public HS3300LoadProfileDataReader(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;

        supportedLoadProfiles = new ArrayList<>();
        supportedLoadProfiles.add(LP_PERIOD_1_15_MIN_OBISCODE);
        supportedLoadProfiles.add(LP_PERIOD_2_24_HOU_OBISCODE);
    }

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        this.loadProfileConfigurationList = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            CollectedLoadProfileConfiguration loadProfileConfiguration = this.collectedDataFactory
                    .createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
            if (isSupported(loadProfileReader)) {
                List<ChannelInfo> channelInfos;
                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(loadProfileReader.getProfileObisCode());
                    channelInfos = getChannelInfo(profileGeneric.getCaptureObjects(), loadProfileConfiguration.getMeterSerialNumber());
                    getChannelInfosMap().put(loadProfileReader, channelInfos); // Remember these, they are re-used in method #getLoadProfileData();
                    loadProfileConfiguration.setProfileInterval( profileGeneric.getCapturePeriod() );
                } catch (IOException e) { // Object not found in IOL, should never happen
                    throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
                }
                loadProfileConfiguration.setChannelInfos(channelInfos);
                loadProfileConfiguration.setSupportedByMeter(true);
            } else {
                loadProfileConfiguration.setSupportedByMeter(false);
            }
            this.loadProfileConfigurationList.add(loadProfileConfiguration);
        }

        return loadProfileConfigurationList;
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfile> collectedLoadProfiles = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            CollectedLoadProfileConfiguration loadProfileConfig = getLoadProfileConfiguration(loadProfileReader);
            CollectedLoadProfile collectedLoadProfile = this.collectedDataFactory.createCollectedLoadProfile(
                    new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(),
                            loadProfileReader.getProfileObisCode(), this.protocol.getOfflineDevice().getDeviceIdentifier()));
            List<ChannelInfo> channelInfos = getChannelInfosMap().get(loadProfileReader);
            if (channelInfos != null) {
                this.protocol.journal("Getting LoadProfile data for " + loadProfileReader +
                        " from " + loadProfileReader.getStartReadingTime() + " to " + loadProfileReader.getEndReadingTime());
                try {
                    ProfileGeneric profile = this.protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(loadProfileReader.getProfileObisCode());
                    profile.setUseWildcardClockStatus(true);

                    Calendar fromCalendar = Calendar.getInstance(this.protocol.getTimeZone());
                    fromCalendar.setTime(loadProfileReader.getStartReadingTime());
                    Calendar toCalendar = Calendar.getInstance(this.protocol.getTimeZone());
                    toCalendar.setTime(loadProfileReader.getEndReadingTime());

                    DLMSProfileIntervals intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), DLMSProfileIntervals.DefaultClockMask, 0, -1, null);
                    List<IntervalData> collectedIntervalData = intervals.parseIntervals(loadProfileConfig.getProfileInterval(), protocol.getTimeZone());

                    this.protocol.journal(" > load profile intervals parsed: " + collectedIntervalData.size());
                    collectedLoadProfile.setCollectedIntervalData(collectedIntervalData, channelInfos);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, this.protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        Issue problem = this.issueFactory.createProblem(loadProfileReader, "loadProfileXIssue", loadProfileReader.getProfileObisCode(), e);
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue problem = this.issueFactory.createWarning(loadProfileReader,
                        loadProfileReader.getProfileObisCode().toString() + " load profile is not supported.",
                        loadProfileReader.getProfileObisCode());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            collectedLoadProfiles.add(collectedLoadProfile);
        }
        return collectedLoadProfiles;
    }

    /**
     * Look for the <CODE>DeviceLoadProfileConfiguration</CODE> in the previously build up list
     *
     * @param loadProfileReader the reader linking to the <CODE>DeviceLoadProfileConfiguration</CODE>
     * @return requested configuration
     */
    private CollectedLoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        if (this.loadProfileConfigurationList != null) {
            for (CollectedLoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
                if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getMeterSerialNumber().equalsIgnoreCase(lpc.getMeterSerialNumber())) {
                    return lpc;
                }
            }
        }
        return null;
    }

    private boolean isSupported(LoadProfileReader loadProfileReader) {
        for (ObisCode supportedLoadProfile : supportedLoadProfiles) {
            if (loadProfileReader.getProfileObisCode().equalsIgnoreBChannel(supportedLoadProfile)) {
                return true;
            }
        }
        return false;
    }

    private Map<LoadProfileReader, List<ChannelInfo>> getChannelInfosMap() {
        if (channelInfosMap == null) {
            channelInfosMap = new HashMap<>();
        }
        return channelInfosMap;
    }

    private List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects, String serialNumber) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<>();

        int counter = 0;
        for (CapturedObject capturedObject : capturedObjects) {
            if (isRealChannelData(capturedObject)) {
                final ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
                Unit unit = readUnitFromDevice(capturedObject);
                if (unit.isUndefined()) {
                    unit = Unit.get(BaseUnit.NOTAVAILABLE, unit.getScale());
                }
                unit = Unit.get(unit.getBaseUnit().getDlmsCode(), 0); // SET SCALE to ZERO since it's already applied and converted to Engineering Unit by the protocol

                ChannelInfo channelInfo = new ChannelInfo(counter, obisCode.toString(), unit, serialNumber);
                if (ParseUtils.isObisCodeCumulative(obisCode)) {
                    channelInfo.setCumulative();
                }
                channelInfos.add(channelInfo);

                counter++;
            }
        }

        return channelInfos;
    }

    /**
     * Check if the data is a real channel instead of meta data about the actual value like
     * the timestamp or interval status.
     *
     * @param capturedObject The captured object to validate
     * @return true if the channel is not a timestamp of interval state bit
     */
    private boolean isRealChannelData(CapturedObject capturedObject) {
        DLMSClassId classId = DLMSClassId.findById(capturedObject.getClassId());
        return classId != DLMSClassId.CLOCK && classId != DLMSClassId.DATA;
    }

    /**
     * Read the unit for a given CapturedObject directly from the device, without using the cache.
     *
     * @param capturedObject The captured object to get the unit from
     * @return The unit or {@link Unit#getUndefined()} if not available
     * @throws IOException If there occurred an error while reading the unit from the device
     */
    private Unit readUnitFromDevice(final CapturedObject capturedObject) throws IOException {
        ObisCode obis = capturedObject.getObisCode();
        final DLMSClassId classId = DLMSClassId.findById(capturedObject.getClassId());
        final int attr = capturedObject.getAttributeIndex();
        switch (classId) {
            case REGISTER: {
                return this.protocol.getDlmsSession().getCosemObjectFactory().getRegister(obis).getScalerUnit().getEisUnit();
            }
            case EXTENDED_REGISTER: {
                if (attr == ExtendedRegisterAttributes.VALUE.getAttributeNumber()) {
                    return this.protocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obis).getScalerUnit().getEisUnit();
                } else if (attr == ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber()) {
                    return Unit.get("ms");
                }
                return Unit.getUndefined();
            }
            case DEMAND_REGISTER: {
                if (attr == DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber()) {
                    return this.protocol.getDlmsSession().getCosemObjectFactory().getDemandRegister(obis).getScalerUnit().getEisUnit();
                } else if (attr == DemandRegisterAttributes.LAST_AVG_VALUE.getAttributeNumber()) {
                    return this.protocol.getDlmsSession().getCosemObjectFactory().getDemandRegister(obis).getScalerUnit().getEisUnit();
                } else if (attr == DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber()) {
                    return Unit.get("ms");
                }
                return Unit.getUndefined();
            }
            default: {
                return Unit.getUndefined();
            }
        }
    }

}
