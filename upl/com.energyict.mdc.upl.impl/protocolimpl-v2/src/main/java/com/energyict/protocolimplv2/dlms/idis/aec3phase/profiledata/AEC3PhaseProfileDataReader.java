package com.energyict.protocolimplv2.dlms.idis.aec3phase.profiledata;

import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
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
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocolimplv2.dlms.idis.aec.profiledata.AECProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.aec3phase.AEC3Phase;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AEC3PhaseProfileDataReader <T extends AEC3Phase> extends AECProfileDataReader <AEC3Phase> {
    public AEC3PhaseProfileDataReader(AEC3Phase protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, long limitMaxNrOfDays) {
        super(protocol, collectedDataFactory, issueFactory, limitMaxNrOfDays);
    }

    protected boolean isProfileStatus(ObisCode obisCode) {
        return (obisCode.getA() == 0 && (obisCode.getB() >= 0 && obisCode.getB() <= 6) && obisCode.getC() == 96 && (obisCode.getD() == 5 || obisCode.getD() == 10 ) && (obisCode.getE() == 0 || obisCode.getE() == 1 || obisCode.getE() == 2 || obisCode.getE() == 3) && obisCode.getF() == 255);
    }

    @Override
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
        if (correctedLoadProfileObisCode != null) {
            profileIntervalAttribute = new DLMSAttribute(correctedLoadProfileObisCode, 4, DLMSClassId.PROFILE_GENERIC);
            attributes.put(correctedLoadProfileObisCode, profileIntervalAttribute);
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSessionProperties().isBulkRequest(), new ArrayList<>(attributes.values()));

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
                try {
                    result.put(channelObisCode, new ScalerUnit(composedCosemObject.getAttribute(dlmsAttribute)).getEisUnit());
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
                    } //Else: throw ConnectionCommunicationException
                } catch (IllegalArgumentException e) {
                    throw new CommunicationException(ProtocolExceptionMessageSeeds.UNEXPECTED_RESPONSE);
                }
            } else {
                String message = "The OBIS code " + channelObisCode + " found in the meter load profile capture objects list, is NOT supported by the meter itself." +
                        " If ReadCache property is not active, try again with this property enabled. Otherwise, please reprogram the meter with a valid set of capture objects.";

                if (protocol.getDlmsSessionProperties().validateLoadProfileChannels()) {
                    throw new ProtocolException(message);
                } else {
                    protocol.getLogger().warning(message);
                }
            }
        }
        return result;
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfile> result = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            CollectedLoadProfile collectedLoadProfile = this.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode(), protocol.getOfflineDevice().getDeviceIdentifier()));

            List<ChannelInfo> channelInfos = getChannelInfosMap().get(loadProfileReader);
            ObisCode correctedLoadProfileObisCode = getCorrectedLoadProfileObisCode(loadProfileReader);
            if (isSupported(loadProfileReader) && (channelInfos != null)) {

                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(correctedLoadProfileObisCode, protocol.useDsmr4SelectiveAccessFormat());
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
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(timeStamp);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            timeStamp = cal.getTime();
                        } else if (previousTimeStamp != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(previousTimeStamp);
                            cal.add(Calendar.SECOND, getIntervalMap().get(correctedLoadProfileObisCode));
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            timeStamp = cal.getTime();
                        } else {
                            Issue problem = getIssueFactory().createProblem(loadProfileReader, "loadProfileXBlockingIssue", correctedLoadProfileObisCode, "Invalid interval data, timestamp should be the first captured object of type OctetString or NullData");
                            collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                            break;  //Stop parsing, move on
                        }
                        previousTimeStamp = timeStamp;

                        if (hasStatusInformation(correctedLoadProfileObisCode)) {
                            status = structure.getInteger(1);
                            offset = 2;
                        } else {
                            // no status is expected in this load profile - i.e. billing profile
                        }

                        final List<IntervalValue> values = new ArrayList<>();

                        for (int channel = 0; channel < channelInfos.size(); channel++) {
                            if (structure.isBigDecimal(channel + offset)) {
                                value = new IntervalValue(structure.getBigDecimalValue(channel + offset), status, getEiServerStatus(status));
                            } else {
                                // unwanted channel (like a date), will be removed by com.energyict.comserver.commands.core.SimpleComCommand.removeUnwantedChannels()
                                value = new IntervalValue(BigDecimal.ZERO, 0, 0);
                            }

                            values.add(value);
                        }

                        intervalDatas.add(new IntervalData(timeStamp, 0, 0, 0, values));
                    }

                    collectedLoadProfile.setCollectedIntervalData(intervalDatas, channelInfos);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        Issue problem = getIssueFactory().createProblem(loadProfileReader, "loadProfileXBlockingIssue", correctedLoadProfileObisCode, e.getMessage());
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue problem = getIssueFactory().createWarning(loadProfileReader, "loadProfileXnotsupported", correctedLoadProfileObisCode);
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            //TODO: see how to handle this validation in Connexo
            //validateLoadProfileData(collectedLoadProfile, loadProfileReader);
            result.add(collectedLoadProfile);
        }

        return result;
    }
}
