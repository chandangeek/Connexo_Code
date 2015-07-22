package com.energyict.protocolimplv2.dlms.idis.am500.profiledata;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * <p/>
 * Supports both the e-meter and MBus meter load profiles
 * Note that in EIServer, they should be configured on the proper master and slave devices.
 *
 * @author khe
 * @since 6/01/2015 - 10:35
 */
public class IDISProfileDataReader {

    private static final ObisCode QUARTER_HOURLY_LOAD_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode DAILY_LOAD_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.2.0.255");
    private static final ObisCode OBISCODE_MBUS_LOAD_PROFILE = ObisCode.fromString("0.x.24.3.0.255");

    private static final ObisCode OBISCODE_NR_OF_POWER_FAILURES = ObisCode.fromString("0.0.96.7.9.255");
    private static final int DO_NOT_LIMIT_MAX_NR_OF_DAYS = 0;
    protected final AbstractDlmsProtocol protocol;
    protected final List<ObisCode> supportedLoadProfiles;
    private final long limitMaxNrOfDays;
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;

    public IDISProfileDataReader(AbstractDlmsProtocol protocol) {
        this(protocol, DO_NOT_LIMIT_MAX_NR_OF_DAYS);
    }

    public IDISProfileDataReader(AbstractDlmsProtocol protocol, long limitMaxNrOfDays) {
        this.protocol = protocol;
        this.limitMaxNrOfDays = limitMaxNrOfDays;

        supportedLoadProfiles = new ArrayList<>();
        supportedLoadProfiles.add(QUARTER_HOURLY_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(DAILY_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(OBISCODE_MBUS_LOAD_PROFILE);
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfile> result = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode()));

            List<ChannelInfo> channelInfos = getChannelInfosMap().get(loadProfileReader);
            if (isSupported(loadProfileReader) && (channelInfos != null)) {

                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(getCorrectedLoadProfileObisCode(loadProfileReader));
                    profileGeneric.setDsmr4SelectiveAccessFormat(true);
                    DataContainer buffer = profileGeneric.getBuffer(getFromCalendar(loadProfileReader), getToCalendar(loadProfileReader));
                    Object[] loadProfileEntries = buffer.getRoot().getElements();
                    List<IntervalData> intervalDatas = new ArrayList<>();
                    IntervalValue value;

                    Date previousTimeStamp = null;
                    for (int index = 0; index < loadProfileEntries.length; index++) {
                        DataStructure structure = buffer.getRoot().getStructure(index);
                        Date timeStamp;
                        if (structure.isOctetString(0)) {
                            OctetString octetString = structure.getOctetString(0);
                            timeStamp = octetString.toDate(AXDRDateTimeDeviationType.Negative, protocol.getTimeZone());
                        } else if (previousTimeStamp != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(previousTimeStamp);
                            cal.add(Calendar.SECOND, profileGeneric.getCapturePeriod());
                            timeStamp = cal.getTime();
                        } else {
                            Issue<LoadProfileReader> problem = MdcManager.getIssueCollector().addProblem(loadProfileReader, "loadProfileXBlockingIssue", getCorrectedLoadProfileObisCode(loadProfileReader), "Invalid interval data, timestamp should be of type OctetString or NullData");
                            collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                            break;  //Stop parsing, move on
                        }
                        previousTimeStamp = timeStamp;
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
                    if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                        Issue<LoadProfileReader> problem = MdcManager.getIssueCollector().addProblem(loadProfileReader, "loadProfileXBlockingIssue", getCorrectedLoadProfileObisCode(loadProfileReader), e.getMessage());
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue<LoadProfileReader> problem = MdcManager.getIssueCollector().addWarning(loadProfileReader, "loadProfileXnotsupported", getCorrectedLoadProfileObisCode(loadProfileReader));
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }

            result.add(collectedLoadProfile);
        }

        return result;
    }

    private Calendar getFromCalendar(LoadProfileReader loadProfileReader) {
        ProfileLimiter profileLimiter = new ProfileLimiter(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime(), (int) getLimitMaxNrOfDays());
        Calendar fromCal = Calendar.getInstance(protocol.getTimeZone());
        fromCal.setTime(profileLimiter.getFromDate());
        fromCal.set(Calendar.SECOND, 0);
        return fromCal;
    }

    private Calendar getToCalendar(LoadProfileReader loadProfileReader) {
        ProfileLimiter profileLimiter = new ProfileLimiter(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime(), (int) getLimitMaxNrOfDays());
        Calendar toCal = Calendar.getInstance(protocol.getTimeZone());
        toCal.setTime(profileLimiter.getToDate());
        toCal.set(Calendar.SECOND, 0);
        return toCal;
    }

    protected ObisCode getCorrectedLoadProfileObisCode(LoadProfileReader loadProfileReader) {
        return protocol.getPhysicalAddressCorrectedObisCode(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
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
            CollectedLoadProfileConfiguration lpc = MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            if (isSupported(lpr)) {
                List<ChannelInfo> channelInfos;
                int interval;
                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(getCorrectedLoadProfileObisCode(lpr));
                    channelInfos = getChannelInfo(profileGeneric.getCaptureObjects(), lpc.getMeterSerialNumber());
                    getChannelInfosMap().put(lpr, channelInfos);    //Remember these, they are re-used in method #getLoadProfileData();
                    interval = profileGeneric.getCapturePeriod();
                } catch (IOException e) {   //Object not found in IOL, should never happen
                    throw IOExceptionHandler.handle(e, protocol.getDlmsSession());
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

    protected List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects, String serialNumber) {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        for (CapturedObject capturedObject : capturedObjects) {
            if (isChannel(capturedObject)) {
                channelObisCodes.add(capturedObject.getLogicalName().getObisCode());
            }
        }

        Map<ObisCode, Unit> unitMap = readUnits(channelObisCodes);

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

    public Map<ObisCode, Unit> readUnits(List<ObisCode> channelObisCodes) {
        Map<ObisCode, Unit> result = new HashMap<>();

        Map<ObisCode, DLMSAttribute> channelAttributes = new HashMap<>();
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
                    //TODO: avoid nullpointer?
                }
                channelAttributes.put(channelObisCode, unitAttribute);
            }
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSessionProperties().isBulkRequest(), new ArrayList<>(channelAttributes.values()));

        for (ObisCode channelObisCode : channelObisCodes) {
            DLMSAttribute dlmsAttribute = channelAttributes.get(channelObisCode);
            if (dlmsAttribute != null) {
                try {
                    result.put(channelObisCode, new ScalerUnit(composedCosemObject.getAttribute(dlmsAttribute)).getEisUnit());
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                        result.put(channelObisCode, Unit.get(BaseUnit.UNITLESS));
                    }
                } catch (ApplicationException e) {
                    result.put(channelObisCode, Unit.get(BaseUnit.UNITLESS));
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
        return (obisCode.getA() == 0 && (obisCode.getB() >= 0 && obisCode.getB() <= 6) && obisCode.getC() == 96 && obisCode.getD() == 10 && (obisCode.getE() == 1 || obisCode.getE() == 2 || obisCode.getE() == 3) && obisCode.getF() == 255);
    }

    private boolean isClock(ObisCode obisCode) {
        return (Clock.getDefaultObisCode().equals(obisCode));
    }

    private boolean isSupported(LoadProfileReader lpr) {
        for (ObisCode supportedLoadProfile : supportedLoadProfiles) {
            if (lpr.getProfileObisCode().equalsIgnoreBChannel(supportedLoadProfile)) {
                return true;
            }
        }
        return false;
    }

    public long getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }
}