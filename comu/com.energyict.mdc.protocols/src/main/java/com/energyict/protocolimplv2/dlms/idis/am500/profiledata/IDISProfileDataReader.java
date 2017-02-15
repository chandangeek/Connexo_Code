/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am500.profiledata;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private Map<ObisCode, Integer> intervalMap;

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
            ObisCode correctedLoadProfileObisCode = getCorrectedLoadProfileObisCode(loadProfileReader);
            if (isSupported(loadProfileReader) && (channelInfos != null)) {

                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(correctedLoadProfileObisCode);
                    profileGeneric.setDsmr4SelectiveAccessFormat(true);
                    DataContainer buffer = profileGeneric.getBuffer(getFromCalendar(loadProfileReader), getToCalendar(loadProfileReader));
                    Object[] loadProfileEntries = buffer.getRoot().getElements();
                    List<IntervalData> intervalDatas = new ArrayList<>();
                    IntervalValue value;

                    Date previousTimeStamp = null;
                    for (int index = 0; index < loadProfileEntries.length; index++) {
                        int status = 0;
                        int offset = 1;
                        DataStructure structure = buffer.getRoot().getStructure(index);
                        Date timeStamp;
                        if (structure.isOctetString(0)) {
                            OctetString octetString = structure.getOctetString(0);
                            timeStamp = octetString.toDate(AXDRDateTimeDeviationType.Negative, protocol.getTimeZone());
                        } else if (previousTimeStamp != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(previousTimeStamp);
                            cal.add(Calendar.SECOND, getIntervalMap().get(correctedLoadProfileObisCode));
                            timeStamp = cal.getTime();
                        } else {
                            Problem problem = issueService.newProblem(loadProfileReader, MessageSeeds.LOADPROFILE_NOT_SUPPORTED, getCorrectedLoadProfileObisCode(loadProfileReader));
                            collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                            break;  //Stop parsing, move on
                        }
                        previousTimeStamp = timeStamp;

                        if (hasStatusInformation()) {
                            status = structure.getInteger(1);
                            offset = 2;
                        }

                        final List<IntervalValue> values = new ArrayList<>();

                        for (int channel = 0; channel < channelInfos.size(); channel++) {
                            value = new IntervalValue(structure.getBigDecimalValue(channel + offset), status, getEiServerStatus(status));
                            values.add(value);
                        }

                        intervalDatas.add(new IntervalData(timeStamp, 0, 0, 0, values));
                    }

                    collectedLoadProfile.setCollectedData(intervalDatas, channelInfos);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsProperties().getRetries() + 1)) {
                        Problem problem = issueService.newProblem(loadProfileReader, MessageSeeds.LOADPROFILE_NOT_SUPPORTED, getCorrectedLoadProfileObisCode(loadProfileReader));
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Problem problem = issueService.newProblem(loadProfileReader, MessageSeeds.LOADPROFILE_NOT_SUPPORTED, getCorrectedLoadProfileObisCode(loadProfileReader));
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

    protected int getEiServerStatus(int protocolStatus) {
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
                ObisCode correctedLoadProfileObisCode = getCorrectedLoadProfileObisCode(lpr);
                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(correctedLoadProfileObisCode);
                    channelInfos = getChannelInfo(profileGeneric.getCaptureObjects(), lpr, correctedLoadProfileObisCode);
                    getChannelInfosMap().put(lpr, channelInfos);    //Remember these, they are re-used in method #getLoadProfileData();
                } catch (IOException e) {   //Object not found in IOL, should never happen
                    throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsProperties().getRetries() + 1);
                }
                lpc.setChannelInfos(channelInfos);
                lpc.setSupportedByMeter(true);
                lpc.setProfileInterval(getIntervalMap().get(correctedLoadProfileObisCode));
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

    public Map<ObisCode, Integer> getIntervalMap() {
        if (intervalMap == null) {
            intervalMap = new HashMap<>();
        }
        return intervalMap;
    }

    protected List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects, LoadProfileReader loadProfileReader, ObisCode correctedLoadProfileObisCode) throws ProtocolException {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        for (CapturedObject capturedObject : capturedObjects) {
            if (isChannel(capturedObject, correctedLoadProfileObisCode)) {
                channelObisCodes.add(capturedObject.getLogicalName().getObisCode());
            }
        }

        Map<ObisCode, Unit> unitMap = readUnits(correctedLoadProfileObisCode, channelObisCodes);

        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (ObisCode obisCode : channelObisCodes) {
            getConfiguredChannelInfo(loadProfileReader, obisCode).ifPresent(configuredChannelInfo -> {
                Unit unit = unitMap.get(obisCode);
                ChannelInfo channelInfo = ChannelInfo.ChannelInfoBuilder.fromObisCode(obisCode)
                        .unit(unit == null ? Unit.get(BaseUnit.UNITLESS) : unit)
                        .readingType(configuredChannelInfo.getReadingType())
                        .meterIdentifier(configuredChannelInfo.getMeterIdentifier())
                        .build();
                if (isCumulative(obisCode)) {
                    channelInfo.setCumulative();
                }
                channelInfos.add(channelInfo);
            });
        }
        return channelInfos;
    }

    private Optional<ChannelInfo> getConfiguredChannelInfo(LoadProfileReader loadProfileReader, ObisCode obisCode) {
        return loadProfileReader.getChannelInfos().stream().filter(channelInfo -> channelInfo.getChannelObisCode().equals(obisCode)).findAny();
    }


    /**
     * @param correctedLoadProfileObisCode the load profile obiscode. If it is not null, this implementation will additionally read out
     * its interval (attribute 4) and cache it in the intervalMap
     * @param channelObisCodes the obiscodes of the channels that we should read out the units for
     */
    public Map<ObisCode, Unit> readUnits(ObisCode correctedLoadProfileObisCode, List<ObisCode> channelObisCodes) throws ProtocolException {
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
                    throw new ProtocolException("Unexpected captured_object in load profile: " + uo.getDescription());
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

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), true, new ArrayList<>(attributes.values()));

        if (correctedLoadProfileObisCode != null) {
            try {
                AbstractDataType attribute = composedCosemObject.getAttribute(profileIntervalAttribute);
                getIntervalMap().put(correctedLoadProfileObisCode, attribute.intValue());
            } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsProperties().getRetries() + 1);
            }
        }

        for (ObisCode channelObisCode : channelObisCodes) {
            DLMSAttribute dlmsAttribute = attributes.get(channelObisCode);
            if (dlmsAttribute != null) {
                try {
                    result.put(channelObisCode, new ScalerUnit(composedCosemObject.getAttribute(dlmsAttribute)).getEisUnit());
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsProperties().getRetries() + 1)) {
                        throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsProperties().getRetries() + 1);
                    } //Else: throw ConnectionCommunicationException
                }
            } else {
                //TODO: see https://jira.eict.vpdc/browse/COMMUNICATION-1672
                // this will throw up an exception if in the LP capture objects (from the meter) is found an obis code
                // which is not supported by the (same) meter - that's illogical!
                // we might want in the future to add a new parameter to skip this check (the storage works fine)
                throw new ProtocolException("The OBIS code " + channelObisCode + " found in the meter load profile capture objects list, is NOT supported by the meter itself. Please reprogram the meter with a valid set of capture objects.");
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

    /**
     * Check if the captured_object can be considered as an EIServer channel.
     * Registers, extended registers and demand registers are used as channels.
     * Captured_objects with the obiscode of the clock (0.0.1.0.0.255), or the status register (0.x.96.10.x.255) are not considered as channels.
     * <p>
     * In case of an unknown dlms class, or an unknown obiscode, a proper exception is thrown.
     */
    private boolean isChannel(CapturedObject capturedObject, ObisCode correctedLoadProfileObisCode) throws ProtocolException {
        int classId = capturedObject.getClassId();
        ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
        if (classId == DLMSClassId.REGISTER.getClassId() || classId == DLMSClassId.EXTENDED_REGISTER.getClassId() || classId == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            return true;
        }
        if (isClock(obisCode) || isProfileStatus(obisCode)) {
            return false;
        }
        throw new ProtocolException("Unexpected captured_object in load profile '" + correctedLoadProfileObisCode + "': " + capturedObject.toString());
    }

    private boolean isProfileStatus(ObisCode obisCode) {
        return (obisCode.getA() == 0 && (obisCode.getB() >= 0 && obisCode.getB() <= 6) && obisCode.getC() == 96 && obisCode.getD() == 10 && (obisCode.getE() == 1 || obisCode.getE() == 2 || obisCode.getE() == 3) && obisCode
                .getF() == 255);
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

    protected boolean hasStatusInformation() {
        return true;
    }
}