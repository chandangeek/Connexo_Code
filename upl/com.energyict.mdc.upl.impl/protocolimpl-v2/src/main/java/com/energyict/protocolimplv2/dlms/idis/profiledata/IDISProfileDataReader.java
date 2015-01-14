package com.energyict.protocolimplv2.dlms.idis.profiledata;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.DeviceLoadProfileConfiguration;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.AM500;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 10:35
 */
public class IDISProfileDataReader {

    private static final ObisCode QUARTER_HOURLY_LOAD_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode DAILY_LOAD_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.2.0.255");

    private static final ObisCode OBISCODE_NR_OF_POWER_FAILURES = ObisCode.fromString("0.0.96.7.9.255");

    private final AM500 AM500;
    private final List<ObisCode> supportedLoadProfiles;
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;

    public IDISProfileDataReader(AM500 AM500) {
        this.AM500 = AM500;
        supportedLoadProfiles = new ArrayList<>(2);
        supportedLoadProfiles.add(QUARTER_HOURLY_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(DAILY_LOAD_PROFILE_OBISCODE);
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        long limitMaxNrOfDays = AM500.getDlmsSessionProperties().getLimitMaxNrOfDays();
        List<CollectedLoadProfile> result = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId()));

            List<ChannelInfo> channelInfos = getChannelInfosMap().get(loadProfileReader);
            if (supportedLoadProfiles.contains(loadProfileReader.getProfileObisCode()) && (channelInfos != null)) {

                ProfileLimiter profileLimiter = new ProfileLimiter(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime(), (int) limitMaxNrOfDays);
                Calendar fromCal = Calendar.getInstance(AM500.getTimeZone());
                fromCal.setTime(profileLimiter.getFromDate());
                fromCal.set(Calendar.SECOND, 0);
                Calendar toCal = Calendar.getInstance(AM500.getTimeZone());
                toCal.setTime(profileLimiter.getToDate());
                toCal.set(Calendar.SECOND, 0);

                try {
                    ProfileGeneric profileGeneric = AM500.getDlmsSession().getCosemObjectFactory().getProfileGeneric(loadProfileReader.getProfileObisCode());
                    DataContainer buffer = profileGeneric.getBuffer(fromCal, toCal);
                    Object[] loadProfileEntries = buffer.getRoot().getElements();
                    List<IntervalData> intervalDatas = new ArrayList<>();
                    IntervalValue value;

                    for (int index = 0; index < loadProfileEntries.length; index++) {
                        DataStructure structure = buffer.getRoot().getStructure(index);
                        Date timeStamp = structure.getOctetString(0).toDate(AM500.getTimeZone());
                        int status = structure.getInteger(1);
                        List<IntervalValue> values = new ArrayList<>();
                        for (int channel = 0; channel < channelInfos.size(); channel++) {
                            value = new IntervalValue(structure.getInteger(channel + 2), status, getEiServerStatus(status));
                            values.add(value);
                        }
                        intervalDatas.add(new IntervalData(timeStamp, 0, 0, 0, values));
                    }

                    collectedLoadProfile.setCollectedIntervalData(intervalDatas, channelInfos);
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, AM500.getDlmsSession())) {
                        Issue<LoadProfileReader> problem = MdcManager.getIssueCollector().addProblem(loadProfileReader, "loadProfileXBlockingIssue", loadProfileReader.getProfileObisCode(), e.getMessage());
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue<LoadProfileReader> problem = MdcManager.getIssueCollector().addWarning(loadProfileReader, "loadProfileXnotsupported", loadProfileReader.getProfileObisCode());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }

            result.add(collectedLoadProfile);
        }

        return result;
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

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();

        for (LoadProfileReader lpr : loadProfileReaders) {
            DeviceLoadProfileConfiguration lpc = new DeviceLoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            if (isSupported(lpr)) {
                List<ChannelInfo> channelInfos;
                int interval;
                try {
                    ProfileGeneric profileGeneric = AM500.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpr.getProfileObisCode());
                    channelInfos = getChannelInfo(profileGeneric.getCaptureObjects());
                    getChannelInfosMap().put(lpr, channelInfos);    //Remember these, they are re-used in method #getLoadProfileData();
                    interval = profileGeneric.getCapturePeriod();
                } catch (IOException e) {   //Object not found in IOL, should never happen
                    throw IOExceptionHandler.handle(e, AM500.getDlmsSession());
                }
                lpc.setChannelInfos(channelInfos);
                lpc.setSupportedByMeter(true);
                lpc.setProfileInterval(interval);
            } else {
                lpc.setSupportedByMeter(false);
            }
            result.add(lpc);
        }

        return result;
    }

    private Map<LoadProfileReader, List<ChannelInfo>> getChannelInfosMap() {
        if (channelInfosMap == null) {
            channelInfosMap = new HashMap<>();
        }
        return channelInfosMap;
    }

    protected List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects) throws IOException {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        for (CapturedObject capturedObject : capturedObjects) {
            if (isChannel(capturedObject)) {
                channelObisCodes.add(capturedObject.getLogicalName().getObisCode());
            }
        }

        Map<ObisCode, Unit> unitMap = readUnits(channelObisCodes);

        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        int counter = 0;
        for (ObisCode obisCode : channelObisCodes) {
            Unit unit = unitMap.get(obisCode);
            ChannelInfo channelInfo = new ChannelInfo(counter, obisCode.toString(), unit == null ? Unit.get(BaseUnit.UNITLESS) : unit);
            if (isCumulative(obisCode)) {
                channelInfo.setCumulative();
            }
            channelInfos.add(channelInfo);
            counter++;
        }
        return channelInfos;
    }

    public Map<ObisCode, Unit> readUnits(List<ObisCode> channelObisCodes) {
        Map<ObisCode, Unit> result = new HashMap<>();

        Map<ObisCode, DLMSAttribute> channelAttributes = new HashMap<>();
        for (ObisCode channelObisCode : channelObisCodes) {
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.AM500.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), channelObisCode);
            if (uo != null) {
                DLMSAttribute unitAttribute = null;
                if (uo.getDLMSClassId() == DLMSClassId.REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.EXTENDED_REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, ExtendedRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.DEMAND_REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                }
                channelAttributes.put(channelObisCode, unitAttribute);
            }
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(AM500.getDlmsSession(), AM500.getDlmsSessionProperties().isBulkRequest(), new ArrayList<>(channelAttributes.values()));

        for (ObisCode channelObisCode : channelObisCodes) {
            DLMSAttribute dlmsAttribute = channelAttributes.get(channelObisCode);
            if (dlmsAttribute != null) {
                try {
                    result.put(channelObisCode, new ScalerUnit(composedCosemObject.getAttribute(dlmsAttribute)).getEisUnit());
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, AM500.getDlmsSession())) {
                        result.put(channelObisCode, Unit.get(BaseUnit.UNITLESS));
                    }
                }
            } else {
                result.put(channelObisCode, Unit.get(BaseUnit.UNITLESS));
            }

        }
        return result;
    }

    private boolean isCumulative(ObisCode obisCode) {
        return ParseUtils.isObisCodeCumulative(obisCode) || isNrOfPowerFailures(obisCode);
    }

    private boolean isNrOfPowerFailures(ObisCode obisCode) {
        return OBISCODE_NR_OF_POWER_FAILURES.equals(obisCode);
    }

    private boolean isChannel(CapturedObject capturedObject) {
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

    private boolean isSupported(LoadProfileReader lpr) {
        return supportedLoadProfiles.contains(lpr.getProfileObisCode());
    }
}