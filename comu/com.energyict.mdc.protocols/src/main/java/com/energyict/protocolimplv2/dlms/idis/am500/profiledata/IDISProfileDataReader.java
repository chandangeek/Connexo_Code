package com.energyict.protocolimplv2.dlms.idis.am500.profiledata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.MessageSeeds;

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
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 29.09.15
 * Time: 10:10
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
    private final CollectedDataFactory collectedDataFactory;
    private final IssueService issueService;
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;

    public IDISProfileDataReader(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueService issueService) {
        this(protocol, DO_NOT_LIMIT_MAX_NR_OF_DAYS, collectedDataFactory, issueService);
    }

    public IDISProfileDataReader(AbstractDlmsProtocol protocol, long limitMaxNrOfDays, CollectedDataFactory collectedDataFactory, IssueService issueService) {
        this.protocol = protocol;
        this.limitMaxNrOfDays = limitMaxNrOfDays;
        this.collectedDataFactory = collectedDataFactory;
        this.issueService = issueService;

        supportedLoadProfiles = new ArrayList<>();
        supportedLoadProfiles.add(QUARTER_HOURLY_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(DAILY_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(OBISCODE_MBUS_LOAD_PROFILE);
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfile> result = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            CollectedLoadProfile collectedLoadProfile = this.collectedDataFactory.createCollectedLoadProfile(loadProfileReader.getLoadProfileIdentifier());

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
                            Issue problem = this.issueService.newIssueCollector().addProblem(loadProfileReader, "loadProfileXBlockingIssue", getCorrectedLoadProfileObisCode(loadProfileReader), "Invalid interval data, timestamp should be of type OctetString or NullData");
                            collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                            break;  //Stop parsing, move on
                        }
                        previousTimeStamp = timeStamp;

                        final int status = structure.getInteger(1);
                        final List<IntervalValue> values = new ArrayList<>();

                        for (int channel = 0; channel < channelInfos.size(); channel++) {
                            value = new IntervalValue(structure.getBigDecimalValue(channel + 2), status, getEiServerStatus(status));
                            values.add(value);
                        }

                        intervalDatas.add(new IntervalData(timeStamp, 0, 0, 0, values));
                    }

                    collectedLoadProfile.setCollectedData(intervalDatas, channelInfos);
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                        Issue problem = this.issueService.newIssueCollector().addProblem(loadProfileReader, "loadProfileXBlockingIssue", getCorrectedLoadProfileObisCode(loadProfileReader), e.getMessage());
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue problem = this.issueService.newIssueCollector().addWarning(loadProfileReader, MessageSeeds.LOADPROFILE_NOT_SUPPORTED.getKey(), getCorrectedLoadProfileObisCode(loadProfileReader));
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }

            result.add(collectedLoadProfile);
        }

        return result;
    }

    private Calendar getFromCalendar(LoadProfileReader loadProfileReader) {
        ProfileLimiter profileLimiter = new ProfileLimiter(Date.from(loadProfileReader.getStartReadingTime()), Date.from(loadProfileReader.getEndReadingTime()), (int) getLimitMaxNrOfDays());
        Calendar fromCal = Calendar.getInstance(protocol.getTimeZone());
        fromCal.setTime(profileLimiter.getFromDate());
        fromCal.set(Calendar.SECOND, 0);
        return fromCal;
    }

    private Calendar getToCalendar(LoadProfileReader loadProfileReader) {
        ProfileLimiter profileLimiter = new ProfileLimiter(Date.from(loadProfileReader.getStartReadingTime()), Date.from(loadProfileReader.getEndReadingTime()), (int) getLimitMaxNrOfDays());
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
            CollectedLoadProfileConfiguration lpc = this.collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getDeviceIdentifier());
            if (isSupported(lpr)) {
                List<ChannelInfo> channelInfos;
                int interval;
                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(getCorrectedLoadProfileObisCode(lpr));
                    channelInfos = getChannelInfo(profileGeneric.getCaptureObjects(), lpr.getMeterSerialNumber(), lpr);
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

    protected List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects, String serialNumber, LoadProfileReader loadProfileReader) {
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
            Optional<ReadingType> readingTypeFromConfiguredChannels = getReadingTypeFromConfiguredChannels(obisCode, loadProfileReader.getChannelInfos());
            if (readingTypeFromConfiguredChannels.isPresent()) {
                final ChannelInfo channelInfo = new ChannelInfo(counter, obisCode.toString(), unit == null ? Unit.get(BaseUnit.UNITLESS) : unit, serialNumber, readingTypeFromConfiguredChannels.get());
                if (isCumulative(obisCode)) {
                    channelInfo.setCumulative();
                }
                channelInfos.add(channelInfo);
                counter++;
            }
        }
        return channelInfos;
    }

    protected Optional<ReadingType> getReadingTypeFromConfiguredChannels(ObisCode obisCode, List<ChannelInfo> configuredChannelInfos) {
        return configuredChannelInfos.stream()
                .filter(channelInfo -> channelInfo.getChannelObisCode().equals(obisCode))
                .findFirst().map(ChannelInfo::getReadingType);
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

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsProperties().isBulkRequest(), new ArrayList<>(channelAttributes.values()));

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
