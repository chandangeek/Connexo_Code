package com.energyict.protocolimplv2.dlms.acud;

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
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcudLoadProfileDataReader {

    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;
    protected final AbstractDlmsProtocol protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final OfflineDevice offlineDevice;

    private static final int INSTRUMENTATION_OBIS_D_VALUE = 7;

    public AcudLoadProfileDataReader(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, OfflineDevice offlineDevice) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.offlineDevice = offlineDevice;
    }

    @SuppressWarnings("unchecked")
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfile> result = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            CollectedLoadProfile collectedLoadProfile = collectedDataFactory
                    .createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader
                            .getProfileObisCode(), offlineDevice.getDeviceIdentifier()));

            List<ChannelInfo> channelInfos = getChannelInfosMap().get(loadProfileReader);
            if ((channelInfos != null)) {
                try {
                    AcudProfileDataHelper profileHelper = new AcudProfileDataHelper(protocol, loadProfileReader);
                    List<IntervalData> intervalDatas = profileHelper.getIntervalData();
                    collectedLoadProfile.setCollectedIntervalData(intervalDatas, channelInfos);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties()
                            .getRetries() + 1)) {
                        Issue problem = issueFactory
                                .createProblem(loadProfileReader, "loadProfileXBlockingIssue", loadProfileReader.getProfileObisCode(), e
                                        .getMessage());
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue problem = issueFactory
                        .createWarning(loadProfileReader, "loadProfileXnotsupported", loadProfileReader.getProfileObisCode());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }

            result.add(collectedLoadProfile);
        }
        return result;
    }

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();
        List<ChannelInfo> channelInfos;

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            ObisCode profileObisCode = loadProfileReader.getProfileObisCode();
            CollectedLoadProfileConfiguration loadProfileConfiguration = collectedDataFactory
                    .createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
            try {
                ProfileGeneric profileGeneric = protocol.getDlmsSession()
                        .getCosemObjectFactory()
                        .getProfileGeneric(profileObisCode);
                channelInfos = getChannelInfo(profileGeneric.getCaptureObjects(), loadProfileConfiguration.getMeterSerialNumber(), profileObisCode);
                getChannelInfosMap().put(loadProfileReader, channelInfos);    //Remember these, they are re-used in method #getLoadProfileData();
            } catch (IOException e) {   //Object not found in IOL, should never happen
                throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
            }
            loadProfileConfiguration.setChannelInfos(channelInfos);
            loadProfileConfiguration.setSupportedByMeter(true);
            result.add(loadProfileConfiguration);
        }
        return result;
    }

    private List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects, String serialNumber, ObisCode correctedLoadProfileObisCode) throws
            IOException {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        List<ChannelInfo> channelInfos = new ArrayList<>();
        Map<ObisCode, Unit> unitMap = new HashMap<>();

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
        obis = getInstrumentationReadableObisCode(obis, classId);

        final int attr = capturedObject.getAttributeIndex();
        switch (classId) {
            case REGISTER: {
                return protocol.getDlmsSession().getCosemObjectFactory().getRegister(obis).getScalerUnit().getEisUnit();
            }

            case EXTENDED_REGISTER: {
                if (attr == ExtendedRegisterAttributes.VALUE.getAttributeNumber()) {
                    return protocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obis).getScalerUnit().getEisUnit();
                } else if (attr == ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber()) {
                    return Unit.get("ms");
                }
                return Unit.getUndefined();
            }

            case DEMAND_REGISTER: {
                if (attr == DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber()) {
                    return protocol.getDlmsSession().getCosemObjectFactory().getDemandRegister(obis).getScalerUnit().getEisUnit();
                } else if (attr == DemandRegisterAttributes.LAST_AVG_VALUE.getAttributeNumber()) {
                    return protocol.getDlmsSession().getCosemObjectFactory().getDemandRegister(obis).getScalerUnit().getEisUnit();
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

    private ObisCode getInstrumentationReadableObisCode(ObisCode obis, DLMSClassId classId) {
        //because the only the instant instrumentation obis is available to be readout as a separate register we need to change field D of the instrumentation channel and always set it to 7
        if(obis.getD() < 7
                && (classId.equals(DLMSClassId.REGISTER)
                || classId.equals(DLMSClassId.DEMAND_REGISTER)
                || classId.equals(DLMSClassId.EXTENDED_REGISTER))) {
            obis = new ObisCode(obis.getA(), obis.getB(), obis.getC(), INSTRUMENTATION_OBIS_D_VALUE, obis.getE(), obis.getF());
        }
        return obis;
    }

    private boolean isCumulative(ObisCode obisCode) {
        return ParseUtils.isObisCodeCumulative(obisCode);
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
}
