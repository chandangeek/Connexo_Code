package com.energyict.protocolimplv2.dlms.acud.profiledata;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.ProtocolException;
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
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcudLoadProfileDataReader {

    protected final static ObisCode ELECTRICITY_MAXIMUM_DEMAND_LP = ObisCode.fromString("1.0.98.1.3.255");
    protected final static ObisCode ELECTRICITY_INSTANTANEOUS_LP = ObisCode.fromString("1.96.98.128.0.255");

    private final IssueFactory issueFactory;
    private final OfflineDevice offlineDevice;
    protected final AbstractDlmsProtocol protocol;
    private final CollectedDataFactory collectedDataFactory;
    private List<CollectedLoadProfileConfiguration> loadProfileConfigs;
    /**
     * Keep track of a list of statusMask per LoadProfileReader
     */
    private Map<LoadProfileReader, Integer> statusMasksMap = new HashMap<>();
    /**
     * Keeps track of the link between a LoadProfileReader and channel lists
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;
    private Map<ObisCode, Integer> intervalMap;

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
                List<ChannelInfo> channelInfos = getChannelInfo(captureObjects, loadProfileConfiguration.getMeterSerialNumber());

                if (loadProfileConfiguration.getObisCode().equals(ELECTRICITY_MAXIMUM_DEMAND_LP)) {
                    // remap duplicated timestamp channel to 1.5.x.x.x.255
                    channelInfos.stream().filter(
                            ci -> ci.getUnit().equals(Unit.get(BaseUnit.SECOND))
                    ).forEach(
                            ci -> ci.setName(ObisCode.setFieldAndGet(ObisCode.fromString(ci.getName()), 2, 5).toString())
                    );
                }
                // Remember these, they are re-used in method #getLoadProfileData();
                getStatusMasksMap().put(loadProfileReader, getStatusMask(captureObjects));
                getChannelInfosMap().put(loadProfileReader, channelInfos);

                loadProfileConfiguration.setProfileInterval(profileGeneric.getCapturePeriod());
                loadProfileConfiguration.setChannelInfos(channelInfos);
                loadProfileConfiguration.setSupportedByMeter(true);
                this.loadProfileConfigs.add(loadProfileConfiguration);
            } catch (IOException e) {   //Object not found in IOL, should never happen
                throw DLMSIOExceptionHandler.handle(e, this.protocol.getDlmsSessionProperties().getRetries() + 1);
            }
        }
        return this.loadProfileConfigs;
    }


    /**
     * @param correctedLoadProfileObisCode the load profile obiscode. If it is not null, this implementation will additionally read out
     *                                     its interval (attribute 4) and cache it in the intervalMap
     * @param channelObisCodes             the obiscodes of the channels that we should read out the units for
     */
    public Map<ObisCode, Unit> readUnits(ObisCode correctedLoadProfileObisCode, List<ObisCode> channelObisCodes) throws ProtocolException {
        Map<ObisCode, Unit> result = new HashMap<>();

        Map<ObisCode, DLMSAttribute> attributes = new HashMap<>();
        for (ObisCode channelObisCode : channelObisCodes) {
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), channelObisCode);
            if (uo != null) {
                DLMSAttribute unitAttribute;
                if (uo.getDLMSClassId() == DLMSClassId.REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.EXTENDED_REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, ExtendedRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.DEMAND_REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                } else {
                    throw new ProtocolException("Unexpected captured_object in load profile: " + uo.getDescription());
                }
                attributes.put(channelObisCode, unitAttribute);
            }
        }

        //Also read out the profile interval in this bulk request
        DLMSAttribute profileIntervalAttribute = null;

        profileIntervalAttribute = new DLMSAttribute(correctedLoadProfileObisCode, 4, DLMSClassId.PROFILE_GENERIC);
        attributes.put(correctedLoadProfileObisCode, profileIntervalAttribute);

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSessionProperties().isBulkRequest(), new ArrayList<>(attributes.values()));

        try {
            AbstractDataType attribute = composedCosemObject.getAttribute(profileIntervalAttribute);
            getIntervalMap().put(correctedLoadProfileObisCode, attribute.intValue());
        } catch (IOException e) {
               throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
        }

        for (ObisCode channelObisCode : channelObisCodes) {
            DLMSAttribute dlmsAttribute = attributes.get(channelObisCode);
            try {
                result.put(channelObisCode, new ScalerUnit(composedCosemObject.getAttribute(dlmsAttribute)).getEisUnit());
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                    throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
                } //Else: throw ConnectionCommunicationException
            } catch (IllegalArgumentException e) {
                throw new CommunicationException(ProtocolExceptionMessageSeeds.UNEXPECTED_RESPONSE);
            }
        }
        return result;
    }

    public Map<ObisCode, Integer> getIntervalMap() {
        if (intervalMap == null) {
            intervalMap = new HashMap<>();
        }
        return intervalMap;
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
                    AcudProfileIntervals intervals = new AcudProfileIntervals(profile.getBufferData(fromCalendar, toCalendar),
                            DLMSProfileIntervals.DefaultClockMask, getStatusMasksMap().get(loadProfileReader), -1, null,
                            loadProfileReader.getProfileObisCode());
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
        if (this.loadProfileConfigs != null) {
            for (CollectedLoadProfileConfiguration lpc : this.loadProfileConfigs) {
                if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getMeterSerialNumber().equalsIgnoreCase(lpc.getMeterSerialNumber())) {
                    return lpc;
                }
            }
        }
        return null;
    }

    private int getStatusMask(List<CapturedObject> capturedObjects) {
        int counter = 0;
        int statusMask = 0;
        for (CapturedObject capturedObject : capturedObjects) {
            if (isStatusChannelData(capturedObject)) {
                statusMask |= (int) Math.pow(2, counter);
                break;
            }
            counter++;
        }
        return statusMask;
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
                ChannelInfo channelInfo = new ChannelInfo(counter, obisCode.toString(), unit, serialNumber);
                if (isCumulative(obisCode)) {
                    channelInfo.setCumulative();
                }
                channelInfos.add(channelInfo);
                counter++;
            }
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

    protected Map<LoadProfileReader, Integer> getStatusMasksMap() {
        return statusMasksMap;
    }

    private Map<LoadProfileReader, List<ChannelInfo>> getChannelInfosMap() {
        if (channelInfosMap == null) {
            channelInfosMap = new HashMap<>();
        }
        return channelInfosMap;
    }

    /**
     * Check if the data is a interval status.
     *
     * @param capturedObject The captured object to validate
     * @return true if the channel is not a timestamp and a interval state bit
     */
    private boolean isStatusChannelData(CapturedObject capturedObject) {
        DLMSClassId classId = DLMSClassId.findById(capturedObject.getClassId());
        return classId != DLMSClassId.CLOCK && classId == DLMSClassId.DATA;
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