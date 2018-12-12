package com.energyict.protocolimpl.dlms.edp.registers;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Float64;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.edp.CX20009;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 16:06
 * Author: khe
 */
public class RegisterReader {

    private final CX20009 protocol;

    public RegisterReader(CX20009 protocol) {
        this.protocol = protocol;
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {

        if (obisCode.getF() != 255) {
            HistoricalValue historicalValue = protocol.getStoredValues().getHistoricalValue(obisCode);
            return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
        }

        ObisCode readObisCode = obisCode;
        if (isTimeSwitchingTable(obisCode)) {         //B-field indicates the month, reset it here
            readObisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) 1);
        }

        if (isGPSCoordinates(obisCode)) {    //Looks like this one is not in the object list of the Janz c280 meter?
            Structure structure = protocol.getCosemObjectFactory().getData(obisCode).getValueAttr().getStructure();
            Float64 latitude = structure.getDataType(0).getFloat64();
            Float64 longitude = structure.getDataType(1).getFloat64();
            String description = "Latitude: " + latitude.getValue() + "; Longitude: " + longitude.getValue();
            return new RegisterValue(obisCode, description);
        }

        int classId = protocol.getMeterConfig().getClassId(readObisCode);
        if (classId == DLMSClassId.REGISTER.getClassId()) {
            Quantity quantityValue = protocol.getCosemObjectFactory().getRegister(readObisCode).getQuantityValue();
            return new RegisterValue(obisCode, quantityValue, null, new Date());
        } else if (classId == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            ExtendedRegister extendedRegister = protocol.getCosemObjectFactory().getExtendedRegister(readObisCode);
            return new RegisterValue(obisCode, extendedRegister.getQuantityValue(), extendedRegister.getCaptureTime(), new Date());
        } else if (classId == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            DemandRegister demandRegister = protocol.getCosemObjectFactory().getDemandRegister(readObisCode);
            return new RegisterValue(obisCode, demandRegister.getQuantityValue(), demandRegister.getCaptureTime(), new Date());
        } else if (classId == DLMSClassId.PROFILE_GENERIC.getClassId()) {
            if (isRelayLastTransition(obisCode)) {
                byte[] buffer = protocol.getCosemObjectFactory().getProfileGeneric(readObisCode).getBufferData();
                String description = RelayLastTransitions.parse(new Array(buffer, 0, 0), protocol.getTimeZone());
                return new RegisterValue(obisCode, description);
            }
        } else if (classId == DLMSClassId.DATA.getClassId()) {
            Data data = protocol.getCosemObjectFactory().getData(readObisCode);
            AbstractDataType valueAttr = data.getValueAttr();
            if (isRelayOperatingMode(obisCode)) {
                return new RegisterValue(obisCode, RelayOperatingMode.fromValue(valueAttr.getTypeEnum().getValue()).getDescription());
            } else if (isTimeOffsetsTable(obisCode)) {
                return new RegisterValue(obisCode, TimeOffsetsTable.parse(valueAttr));
            } else if (isGeneralInfo(obisCode)) {
                return new RegisterValue(obisCode, RelayGeneralInfo.parse(valueAttr, protocol.getTimeZone()));
            } else if (isTimeSwitchingTable(obisCode)) {
                return new RegisterValue(obisCode, TimeSwitchingTable.parse(valueAttr, obisCode));
            } else if (isAstronomicalClockInfo(obisCode)) {
                return new RegisterValue(obisCode, AstronomicalClockInfo.parse(valueAttr));
            } else if (isCircuitFaultStatus(obisCode)) {
                long value = valueAttr.longValue();
                return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()), new Date(), null, new Date(), new Date(), 0, CircuitFaultStatus.fromValue(valueAttr.longValue()).getDescription());
            } else if (isBillingTimeStamp(obisCode)) {
                Date timeStamp = valueAttr.getOctetString().getDateTime(protocol.getTimeZone()).getValue().getTime();
                return new RegisterValue(obisCode, timeStamp.toString());
            } else if (isPassiveEOBTimeStamp(obisCode)) {
                String description = valueAttr.getOctetString().toString();
                return new RegisterValue(obisCode, description);
            } else if (valueAttr.isBooleanObject()) {
                return new RegisterValue(obisCode, valueAttr.getBooleanObject().getState() ? "True" : "False");
            } else if (valueAttr.isVisibleString()) {
                return new RegisterValue(obisCode, valueAttr.getVisibleString().getStr());
            } else if (valueAttr.isOctetString()) {
                return new RegisterValue(obisCode, valueAttr.getOctetString().stringValue());
            } else if (valueAttr.isArray() || valueAttr.isStructure()) {
                throw new NoSuchRegisterException();    //Currently not supported
            } else {
                return new RegisterValue(obisCode, new Quantity(valueAttr.longValue(), Unit.getUndefined()));
            }
        } else if (classId == DLMSClassId.SINGLE_ACTION_SCHEDULE.getClassId()) {
            String executionTimeDescription = protocol.getCosemObjectFactory().getSingleActionSchedule(readObisCode).getExecutionTime().toString();
            return new RegisterValue(obisCode, executionTimeDescription);
        } else if (classId == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
            int value = protocol.getCosemObjectFactory().getDisconnector(readObisCode).getControlState().getValue();
            return new RegisterValue(obisCode, DisconnectControlState.fromValue(value).getDescription());
        } else if (classId == DLMSClassId.ACTIVITY_CALENDAR.getClassId()) {
            String name = protocol.getCosemObjectFactory().getActivityCalendar(readObisCode).readCalendarNameActive().stringValue();
            return new RegisterValue(obisCode, name);
        }
        throw new NoSuchRegisterException();
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

    private boolean isGPSCoordinates(ObisCode obisCode) {
        return obisCode.equals(ObisCode.fromString("0.65.0.30.4.255"));
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