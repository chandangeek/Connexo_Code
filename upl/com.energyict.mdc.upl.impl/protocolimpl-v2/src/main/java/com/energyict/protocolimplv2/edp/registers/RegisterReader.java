package com.energyict.protocolimplv2.edp.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Float64;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.tasks.support.DeviceRegisterSupport;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.edp.CX20009;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 16:06
 * Author: khe
 */
public class RegisterReader implements DeviceRegisterSupport {

    private final CX20009 protocol;

    public RegisterReader(CX20009 protocol) {
        this.protocol = protocol;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>();
        for (OfflineRegister register : registers) {
            ObisCode obisCode = register.getObisCode();
            ObisCode readObisCode = ObisCode.fromByteArray(obisCode.getLN());
            if (isTimeSwitchingTable(obisCode)) {         //B-field indicates the month, reset it here
                readObisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) 1);
            }
            try {

                if (isGPSCoordinates(obisCode)) {    //Looks like this one is not in the object list of the Janz c280 meter?
                    Structure structure = protocol.getDlmsSession().getCosemObjectFactory().getData(obisCode).getValueAttr().getStructure();
                    Float64 latitude = structure.getDataType(0).getFloat64();
                    Float64 longitude = structure.getDataType(1).getFloat64();
                    String description = "Latitude: " + latitude.getValue() + "; Longitude: " + longitude.getValue();
                    collectedRegisters.add(createCollectedRegister(register, null, description, null));
                    continue;
                }

                int classId = protocol.getDlmsSession().getMeterConfig().getClassId(readObisCode);
                if (classId == DLMSClassId.REGISTER.getClassId()) {
                    Quantity quantityValue = getCosemObjectFactory().getRegister(readObisCode).getQuantityValue();
                    collectedRegisters.add(createCollectedRegister(register, quantityValue, null, null));
                } else if (classId == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                    ExtendedRegister extendedRegister = getCosemObjectFactory().getExtendedRegister(readObisCode);
                    collectedRegisters.add(createCollectedRegister(register, extendedRegister.getQuantityValue(), null, extendedRegister.getCaptureTime()));
                } else if (classId == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                    DemandRegister demandRegister = getCosemObjectFactory().getDemandRegister(readObisCode);
                    collectedRegisters.add(createMaxDemandRegister(register, demandRegister.getQuantityValue(), null, demandRegister.getCaptureTime()));
                } else if (classId == DLMSClassId.PROFILE_GENERIC.getClassId()) {
                    if (isRelayLastTransition(obisCode)) {
                        byte[] buffer = getCosemObjectFactory().getProfileGeneric(readObisCode).getBufferData();
                        String description = RelayLastTransitions.parse(new Array(buffer, 0, 0), protocol.getTimeZone());
                        collectedRegisters.add(createCollectedRegister(register, null, description, null));
                    }
                } else if (classId == DLMSClassId.DATA.getClassId()) {
                    Data data = getCosemObjectFactory().getData(readObisCode);
                    AbstractDataType valueAttr = data.getValueAttr();
                    if (isRelayOperatingMode(obisCode)) {
                        collectedRegisters.add(createCollectedRegister(register, null, RelayOperatingMode.fromValue(valueAttr.getTypeEnum().getValue()).getDescription(), null));
                    } else if (isTimeOffsetsTable(obisCode)) {
                        collectedRegisters.add(createCollectedRegister(register, null, TimeOffsetsTable.parse(valueAttr), null));
                    } else if (isGeneralInfo(obisCode)) {
                        collectedRegisters.add(createCollectedRegister(register, null, RelayGeneralInfo.parse(valueAttr, protocol.getTimeZone()), null));
                    } else if (isTimeSwitchingTable(obisCode)) {
                        collectedRegisters.add(createCollectedRegister(register, null, TimeSwitchingTable.parse(valueAttr, obisCode), null));
                    } else if (isAstronomicalClockInfo(obisCode)) {
                        collectedRegisters.add(createCollectedRegister(register, null, AstronomicalClockInfo.parse(valueAttr), null));
                    } else if (isCircuitFaultStatus(obisCode)) {
                        long status = valueAttr.longValue();
                        collectedRegisters.add(createCollectedRegister(register, new Quantity(status, Unit.getUndefined()), CircuitFaultStatus.parse(status), null));
                    } else if (isBillingTimeStamp(obisCode)) {
                        Date timeStamp = valueAttr.getOctetString().getDateTime(protocol.getTimeZone()).getValue().getTime();
                        collectedRegisters.add(createCollectedRegister(register, null, timeStamp.toString(), null));
                    } else if (isPassiveEOBTimeStamp(obisCode)) {
                        String description = valueAttr.getOctetString().toString();
                        collectedRegisters.add(createCollectedRegister(register, null, description, null));
                    } else if (valueAttr.isBooleanObject()) {
                        collectedRegisters.add(createCollectedRegister(register, null, valueAttr.getBooleanObject().getState() ? "True" : "False", null));
                    } else if (valueAttr.isVisibleString()) {
                        collectedRegisters.add(createCollectedRegister(register, null, valueAttr.getVisibleString().getStr(), null));
                    } else if (valueAttr.isOctetString()) {
                        collectedRegisters.add(createCollectedRegister(register, null, valueAttr.getOctetString().stringValue(), null));
                    } else if (valueAttr.isArray() || valueAttr.isStructure()) {
                        createFailureCollectedRegister(register, ResultType.NotSupported);      //Not yet supported in the protocol
                    } else {
                        collectedRegisters.add(createCollectedRegister(register, new Quantity(valueAttr.longValue(), Unit.getUndefined()), null, null));
                    }
                } else if (classId == DLMSClassId.SINGLE_ACTION_SCHEDULE.getClassId()) {
                    String executionTimeDescription = getCosemObjectFactory().getSingleActionSchedule(readObisCode).getExecutionTime().toString();
                    collectedRegisters.add(createCollectedRegister(register, null, executionTimeDescription, null));
                } else if (classId == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
                    int value = getCosemObjectFactory().getDisconnector(readObisCode).getControlState().getValue();
                    collectedRegisters.add(createCollectedRegister(register, null, DisconnectControlState.fromValue(value).getDescription(), null));
                } else if (classId == DLMSClassId.ACTIVITY_CALENDAR.getClassId()) {
                    String name = getCosemObjectFactory().getActivityCalendar(readObisCode).readCalendarNameActive().stringValue();
                    collectedRegisters.add(createCollectedRegister(register, null, name, null));
                } else {
                    createFailureCollectedRegister(register, ResultType.NotSupported);         //Not yet supported in the protocol
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                    if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                        createFailureCollectedRegister(register, ResultType.NotSupported);    //Not in the device
                    } else {
                        createFailureCollectedRegister(register, ResultType.InCompatible, e.getMessage());     //Unexpected response
                    }
                }
            }
        }
        return collectedRegisters;
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... arguments) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createWarning(register.getObisCode(), "registerXissue", register.getObisCode(), arguments[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        }
        return collectedRegister;
    }

    private CollectedRegister createCollectedRegister(OfflineRegister offlineRegister, Quantity quantity, String text, Date eventTime) {
        CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(quantity, text);
        deviceRegister.setCollectedTimeStamps(new Date(), null, new Date(), eventTime);
        return deviceRegister;
    }

    private CollectedRegister createMaxDemandRegister(OfflineRegister offlineRegister, Quantity quantity, String text, Date eventTime) {
        CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(quantity, text);
        deviceRegister.setCollectedTimeStamps(new Date(), null, new Date(), eventTime);
        return deviceRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }

    private boolean isGPSCoordinates(ObisCode obisCode) {
        return obisCode.equals(ObisCode.fromString("0.65.0.30.4.255"));
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return protocol.getDlmsSession().getCosemObjectFactory();
    }

    private boolean isBillingTimeStamp(ObisCode obisCode) {
        return obisCode.equals(ObisCode.fromString("0.0.94.35.11.255"))             //Timestamp of last billing reset, period 1
                || obisCode.equals(ObisCode.fromString("0.0.94.35.12.255"));
    }

    private boolean isPassiveEOBTimeStamp(ObisCode obisCode) {
        return obisCode.equals(ObisCode.fromString("0.0.94.35.41.255"))         //Passive EOB period 1
                || obisCode.equals(ObisCode.fromString("0.0.94.35.42.255"));
    }

    private boolean isRelayLastTransition(ObisCode obisCode) {
        return obisCode.equals(ObisCode.fromString("0.1.94.35.47.255")) || obisCode.equals(ObisCode.fromString("0.1.94.35.147.255"));
    }

    private boolean isGeneralInfo(ObisCode obisCode) {
        return obisCode.equals(ObisCode.fromString("0.1.94.35.49.255")) || obisCode.equals(ObisCode.fromString("0.1.94.35.149.255"));
    }

    private boolean isCircuitFaultStatus(ObisCode obisCode) {
        return obisCode.equals(ObisCode.fromString("0.1.94.35.43.255"));
    }

    private boolean isAstronomicalClockInfo(ObisCode obisCode) {
        return obisCode.equals(ObisCode.fromString("0.1.94.35.42.255"));
    }

    private boolean isTimeSwitchingTable(ObisCode obisCode) {
        if (obisCode.getB() == 0) {
            return false;   //B-field indicates month (1-based)
        }
        obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) 1);
        return obisCode.equals(ObisCode.fromString("0.1.94.35.41.255")) || obisCode.equals(ObisCode.fromString("0.1.94.35.141.255"));
    }

    private boolean isTimeOffsetsTable(ObisCode obisCode) {
        return obisCode.equals(ObisCode.fromString("0.1.94.35.48.255")) || obisCode.equals(ObisCode.fromString("0.1.94.35.148.255"));
    }

    private boolean isRelayOperatingMode(ObisCode obisCode) {
        return obisCode.equals(ObisCode.fromString("0.1.94.35.40.255")) || obisCode.equals(ObisCode.fromString("0.1.94.35.140.255"));
    }
}