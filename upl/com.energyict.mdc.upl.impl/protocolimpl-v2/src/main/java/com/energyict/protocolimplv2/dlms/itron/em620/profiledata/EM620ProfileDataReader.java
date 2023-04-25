package com.energyict.protocolimplv2.dlms.itron.em620.profiledata;

import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.energyict.protocolimplv2.dlms.itron.em620.registers.EM620RegisterFactory.DEMAND_REGISTER_CAPTURE_OBJECT_ATTRIBUTE_INDEX;
import static com.energyict.protocolimplv2.dlms.itron.em620.registers.EM620RegisterFactory.EXTENDED_REGISTER_CAPTURE_OBJECT_ATTRIBUTE_INDEX;

public class EM620ProfileDataReader {

    private static final ObisCode LOAD_PROFILE_DATA_1_OBIS_CODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode LOAD_PROFILE_DATA_2_OBIS_CODE = ObisCode.fromString("1.0.99.2.0.255");

    private static final ObisCode CLOCK_OBIS_CODE = ObisCode.fromString("0.0.1.0.0.255");
    private static final ObisCode STATUS_OBIS_CODE_1 = ObisCode.fromString("0.0.96.10.1.255");
    private static final ObisCode STATUS_OBIS_CODE_2 = ObisCode.fromString("0.0.96.10.2.255");

    private static final String PREFIX_CHANNEL_MASK_FOR_EM620_PROFILE_DATA_READER_VALUE_0 = "0";
    private static final String PREFIX_CHANNEL_MASK_FOR_EM620_PROFILE_DATA_READER_VALUE_1 = "1";

    protected final AbstractDlmsProtocol protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    protected final List<ObisCode> supportedLoadProfiles;

    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap = new HashMap<>();
    private Map<ObisCode, Integer> intervalMap = new HashMap<>();
    private final Map<ObisCode, Boolean> hasStatus = new HashMap<>();
    private final Map<LoadProfileReader, int[]> channelMaskMap = new HashMap<LoadProfileReader, int[]>();
    private final Map<LoadProfileReader, CollectedLoadProfileConfiguration> loadProfileConfigurationMap = new HashMap<>();

    public EM620ProfileDataReader(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;

        supportedLoadProfiles = new ArrayList<>();
        supportedLoadProfiles.add(LOAD_PROFILE_DATA_1_OBIS_CODE);
        supportedLoadProfiles.add(LOAD_PROFILE_DATA_2_OBIS_CODE);
    }

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> allLoadProfileReaders) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();

        for (LoadProfileReader lpr : allLoadProfileReaders) {
            CollectedLoadProfileConfiguration lpc = this.collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            if (isSupported(lpr)) {
                List<ChannelInfo> channelInfos;
                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpr.getProfileObisCode());
                    channelInfos = getChannelInfo(profileGeneric.getCaptureObjects(), lpc.getMeterSerialNumber(), lpr);
                    getChannelInfosMap().put(lpr, channelInfos);    //Remember these, they are re-used in method #getLoadProfileData();
                } catch (IOException e) {   //Object not found in IOL, should never happen
                    throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
                }
                lpc.setChannelInfos(channelInfos);
                lpc.setSupportedByMeter(true);
                lpc.setProfileInterval(getIntervalMap().get(lpr.getProfileObisCode()));
            } else {
                lpc.setSupportedByMeter(false);
            }
            result.add(lpc);
            this.loadProfileConfigurationMap.put(lpr, lpc);
        }
        return result;
    }


    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> result = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : loadProfiles) {
            CollectedLoadProfile collectedLoadProfile = this.collectedDataFactory.createCollectedLoadProfile(
                    new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode(), this.protocol.getOfflineDevice().getDeviceIdentifier()));
            List<ChannelInfo> channelInfos = getChannelInfosMap().get(loadProfileReader);

            if (isSupported(loadProfileReader) && (channelInfos != null)) {
                setCollectedLoadProfile(loadProfileReader, collectedLoadProfile);
            } else {
                Issue problem = this.issueFactory.createWarning(loadProfileReader,
                        "load profile not supported " + loadProfileReader.getProfileObisCode(), loadProfileReader.getProfileObisCode());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            result.add(collectedLoadProfile);
        }
        return result;
    }

    private void setCollectedLoadProfile(LoadProfileReader loadProfileReader, CollectedLoadProfile collectedLoadProfile) {
        try {
            ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(loadProfileReader.getProfileObisCode());

            Calendar fromCalendar = Calendar.getInstance(this.protocol.getTimeZone());
            fromCalendar.setTime(loadProfileReader.getStartReadingTime());
            Calendar toCalendar = Calendar.getInstance(this.protocol.getTimeZone());
            toCalendar.setTime(loadProfileReader.getEndReadingTime());

            byte[] encodedData = profileGeneric.getBufferData(fromCalendar, toCalendar);

            int[] channelMask = channelMaskMap.get(loadProfileReader);
            if (channelMask == null) {
                throw new IOException("Failed to build the load profile data: Invalid ChannelMask!");
            }

            DLMSProfileIntervals intervalParser = new DLMSProfileIntervals(encodedData, channelMask[0], channelMask[1], channelMask[2], new EM620ProfileIntervalStatusBits());
            List<IntervalData> intervalDatas = intervalParser.parseIntervals(this.loadProfileConfigurationMap.get(loadProfileReader).getProfileInterval(), protocol.getTimeZone());
            collectedLoadProfile.setCollectedIntervalData(intervalDatas, getChannelInfosMap().get(loadProfileReader));
        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                Issue problem = this.issueFactory.createProblem(loadProfileReader, e.getMessage(), loadProfileReader.getProfileObisCode(), e.getMessage());
                collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
            }
        }
    }

    protected List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects, String serialNumber, LoadProfileReader lpr) throws IOException {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        String channelMask = new String();
        int clockMask = -1;
        int statusMask = -1;

        int i = 1;
        for (CapturedObject capturedObject : capturedObjects) {
            if (isChannel(capturedObject, lpr.getProfileObisCode())) {
                channelObisCodes.add(capturedObject.getLogicalName().getObisCode());
            }
            if (capturedObject.getLogicalName().toString().equals(CLOCK_OBIS_CODE.toString())) {
                channelMask = PREFIX_CHANNEL_MASK_FOR_EM620_PROFILE_DATA_READER_VALUE_0 + channelMask;
                clockMask = i;
                // DO nothing
            } else if (capturedObject.getLogicalName().toString().equals(STATUS_OBIS_CODE_1.toString()) ||
                    capturedObject.getLogicalName().toString().equals(STATUS_OBIS_CODE_2.toString())) {
                channelMask = PREFIX_CHANNEL_MASK_FOR_EM620_PROFILE_DATA_READER_VALUE_0 + channelMask;
                statusMask = i;
                // DO nothing
            } else {
                channelMask = PREFIX_CHANNEL_MASK_FOR_EM620_PROFILE_DATA_READER_VALUE_1 + channelMask;
            }
            i++;
        }
        channelMaskMap.put(lpr, new int[]{clockMask, statusMask, channelMask.length() > 2 ? Integer.parseInt(channelMask, 2) : 1});

        Map<ObisCode, Unit> unitMap = readUnits(lpr.getProfileObisCode(), channelObisCodes);

        List<ChannelInfo> channelInfos = new ArrayList<>();
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

    private Map<LoadProfileReader, List<ChannelInfo>> getChannelInfosMap() {
        if (channelInfosMap == null) {
            channelInfosMap = new HashMap<>();
        }
        return channelInfosMap;
    }

    public Map<ObisCode, Integer> getIntervalMap() {
        if (intervalMap == null) {
            intervalMap = new HashMap<>();
        }
        return intervalMap;
    }

    /**
     * Check if the captured_object can be considered as an EIServer channel.
     * Registers, extended registers and demand registers are used as channels.
     * Captured_objects with the obiscode of the clock (0.0.1.0.0.255), or the status register (0.x.96.10.x.255) are not considered as channels.
     * <p/>
     * In case of an unknown dlms class, or an unknown obiscode, a proper exception is thrown.
     */
    private boolean isChannel(CapturedObject capturedObject, ObisCode correctedLoadProfileObisCode) {
        int classId = capturedObject.getClassId();
        ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
        if (!isCaptureTime(capturedObject) && isDataRegisterClassId(classId)) {
            return true;
        }
        if (isClock(obisCode) || isCaptureTime(capturedObject)) {
            return false;
        }
        if (isProfileStatus(obisCode)) {
            hasStatus.put(correctedLoadProfileObisCode, true);
            return false;
        }
        return false;
    }

    private boolean isDataRegisterClassId(int classId) {
        return classId == DLMSClassId.REGISTER.getClassId() ||
                classId == DLMSClassId.EXTENDED_REGISTER.getClassId() ||
                classId == DLMSClassId.DEMAND_REGISTER.getClassId();
    }

    private boolean isProfileStatus(ObisCode obisCode) {
        return (obisCode.getA() == 0 &&
                obisCode.getC() == 96 &&
                obisCode.getD() == 10 &&
                (obisCode.getE() == 1 || obisCode.getE() == 2 || obisCode.getE() == 3) &&
                obisCode.getF() == 255);
    }

    private boolean isClock(ObisCode obisCode) {
        return (Clock.getDefaultObisCode().equals(obisCode));
    }

    private boolean isCaptureTime(CapturedObject capturedObject) {
        return (capturedObject.getAttributeIndex() == EXTENDED_REGISTER_CAPTURE_OBJECT_ATTRIBUTE_INDEX &&
                capturedObject.getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId()) ||
                (capturedObject.getAttributeIndex() == DEMAND_REGISTER_CAPTURE_OBJECT_ATTRIBUTE_INDEX &&
                        capturedObject.getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId());
    }

    private boolean isSupported(LoadProfileReader lpr) {
        for (ObisCode supportedLoadProfile : supportedLoadProfiles) {
            if (lpr.getProfileObisCode().equalsIgnoreBChannel(supportedLoadProfile)) {
                return true;
            }
        }
        return false;
    }

    public Map<ObisCode, Unit> readUnits(ObisCode correctedLoadProfileObisCode, List<ObisCode> channelObisCodes) throws IOException {
        Map<ObisCode, Unit> result = new HashMap<>();

        Map<ObisCode, DLMSAttribute> attributes = new HashMap<>();
        for (ObisCode channelObisCode : channelObisCodes) {
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), channelObisCode);
            if (uo != null) {
                DLMSAttribute unitAttribute = null;
                if (uo.getDLMSClassId() == DLMSClassId.REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.EXTENDED_REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, ExtendedRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.DEMAND_REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                } else {
                    throw new IOException("Unexpected captured_object in load profile: " + uo.getDescription());
                }
                attributes.put(channelObisCode, unitAttribute);
            }
        }

        //Also read out the profile interval in this bulk request
        DLMSAttribute profileIntervalAttribute = null;
        if (correctedLoadProfileObisCode != null) {
            profileIntervalAttribute = new DLMSAttribute(correctedLoadProfileObisCode, 4, DLMSClassId.PROFILE_GENERIC);
            attributes.put(correctedLoadProfileObisCode, profileIntervalAttribute);
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(
                protocol.getDlmsSession(),
                protocol.getDlmsSessionProperties().isBulkRequest(),
                new ArrayList<>(attributes.values()));

        if (correctedLoadProfileObisCode != null) {
            try {
                AbstractDataType attribute = composedCosemObject.getAttribute(profileIntervalAttribute);
                getIntervalMap().put(correctedLoadProfileObisCode, attribute.intValue());
            } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
            }
        }

        for (ObisCode channelObisCode : channelObisCodes) {
            DLMSAttribute dlmsAttribute = attributes.get(channelObisCode);
            if (dlmsAttribute != null) {
                result.put(channelObisCode, new ScalerUnit(composedCosemObject.getAttribute(dlmsAttribute)).getEisUnit());
            } else {
                String message = "The OBIS code " + channelObisCode + " found in the meter load profile capture objects list, is NOT supported by the meter itself." +
                        " If ReadCache property is not active, try again with this property enabled. Otherwise, please reprogram the meter with a valid set of capture objects.";
                    protocol.getLogger().warning(message);
            }
        }
        return result;
    }

    private boolean isCumulative(ObisCode obisCode) {
        return ParseUtils.isObisCodeCumulative(obisCode);
    }

    public Date getTime() {
        return this.protocol.getTime();
    }
}
