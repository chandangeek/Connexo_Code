package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ComposedCosemObject;
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
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.*;

public class AcudLoadProfileDataReader {

    private final IssueFactory issueFactory;
    private final OfflineDevice offlineDevice;
    protected final AbstractDlmsProtocol protocol;
    private final CollectedDataFactory collectedDataFactory;
    private List<CollectedLoadProfileConfiguration> loadProfileConfigs;
    /**
     * Keeps track of the link between a LoadProfileReader and channel lists
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;

    public AcudLoadProfileDataReader(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, OfflineDevice offlineDevice) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.offlineDevice = offlineDevice;
    }

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        this.loadProfileConfigs = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            try {
                CollectedLoadProfileConfiguration loadProfileConfiguration = collectedDataFactory
                        .createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
                ProfileGeneric profileGeneric = this.protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(loadProfileReader.getProfileObisCode());
                List<CapturedObject> captureObjects = profileGeneric.getCaptureObjects();
                List<ChannelInfo>  channelInfos = getChannelInfo(captureObjects, loadProfileConfiguration.getMeterSerialNumber());
                getChannelInfosMap().put(loadProfileReader, channelInfos);    //Remember these, they are re-used in method #getLoadProfileData();
                loadProfileConfiguration.setProfileInterval(readIntervalFromDevice(loadProfileReader.getProfileObisCode()));
                loadProfileConfiguration.setChannelInfos(channelInfos);
                loadProfileConfiguration.setSupportedByMeter(true);
                this.loadProfileConfigs.add(loadProfileConfiguration);
            } catch (IOException e) {   //Object not found in IOL, should never happen
                throw DLMSIOExceptionHandler.handle(e, this.protocol.getDlmsSessionProperties().getRetries() + 1);
            }
        }
        return this.loadProfileConfigs;
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReders) {
        List<CollectedLoadProfile> collectedLoadProfiles = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfileReders) {
            CollectedLoadProfileConfiguration loadProfileConfig = getLoadProfileConfiguration(loadProfileReader);
            CollectedLoadProfile collectedLoadProfile = this.collectedDataFactory.createCollectedLoadProfile(
                    new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(),
                            loadProfileReader.getProfileObisCode(), this.offlineDevice.getDeviceIdentifier()));
            List<ChannelInfo> channelInfos = getChannelInfosMap().get(loadProfileReader);
            if (channelInfos != null) {
                this.protocol.journal("Getting LoadProfile data for " + loadProfileReader +
                        " from " + loadProfileReader.getStartReadingTime() + " to " + loadProfileReader.getEndReadingTime());
                try {
                    ProfileGeneric profile = this.protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(loadProfileReader.getProfileObisCode());
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
                Issue problem = this.issueFactory.createWarning(loadProfileReader, "loadProfileXnotsupported", loadProfileReader.getProfileObisCode());
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
    protected CollectedLoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        if( this.loadProfileConfigs != null ) {
            for (CollectedLoadProfileConfiguration lpc : this.loadProfileConfigs) {
                if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getMeterSerialNumber().equalsIgnoreCase(lpc.getMeterSerialNumber())) {
                    return lpc;
                }
            }
        }
        return null;
    }

    private List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects, String serialNumber) throws IOException {
        Map<ObisCode, Unit> unitMap = new HashMap<>();
        List<ChannelInfo> channelInfos = new ArrayList<>();
        List<ObisCode> channelObisCodes = new ArrayList<>();

        for (CapturedObject capturedObject : capturedObjects) {
            if(isRealChannelData(capturedObject)) {
                Unit u = readUnitFromDevice(capturedObject);
                if (u.isUndefined()) {
                    u = Unit.get(BaseUnit.NOTAVAILABLE, u.getScale());
                }
                u = Unit.get(u.getBaseUnit().getDlmsCode(), 0); // SET SCALE to ZERO since it's already applied and converted to Engineering Unit by the protocol
                unitMap.put(capturedObject.getLogicalName().getObisCode(), u);
                channelObisCodes.add(capturedObject.getLogicalName().getObisCode());
            }
        }

        int counter = 0;
        for (ObisCode obisCode : channelObisCodes) {
            Unit unit = unitMap.get(obisCode);
            ChannelInfo channelInfo = new ChannelInfo(counter, obisCode.toString(), unit == null ? Unit.get(BaseUnit.UNITLESS) : unit, serialNumber);
            if (isCumulative(obisCode)) {
                channelInfo.setCumulative();
            }
            channelInfos.add(channelInfo);
            counter++;
        }
        return channelInfos;
    }

    /**
     * Read the unit for a given CapturedObject directly from the device, without using the cache.
     *
     * @param capturedObject The captured object to get the unit from
     * @return The unit or {@link Unit#getUndefined()} if not available
     * @throws IOException If there occurred an error while reading the unit from the device
     */
    public Unit readUnitFromDevice(final CapturedObject capturedObject) throws IOException {
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

    public int readIntervalFromDevice(ObisCode obis) {
        DLMSAttribute intervalAttribute = new DLMSAttribute(obis, 4, DLMSClassId.PROFILE_GENERIC);
        ComposedCosemObject cosemObject = this.protocol.getDlmsSession().getCosemObjectFactory().getComposedCosemObject(intervalAttribute);
        try {
            AbstractDataType attributeData = cosemObject.getAttribute(intervalAttribute);
            return attributeData.intValue();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, this.protocol.getDlmsSessionProperties().getRetries() + 1);
        }
    }

    private Map<LoadProfileReader, List<ChannelInfo>> getChannelInfosMap() {
        if (channelInfosMap == null) {
            channelInfosMap = new HashMap<>();
        }
        return channelInfosMap;
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

    private boolean isCumulative(ObisCode obisCode) {
        return ParseUtils.isObisCodeCumulative(obisCode);
    }
}