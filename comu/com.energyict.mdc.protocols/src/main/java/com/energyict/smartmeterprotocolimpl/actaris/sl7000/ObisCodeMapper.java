/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.actaris.sl7000.composedobjects.ComposedMeterInfo;

import java.io.IOException;
import java.util.Date;

/**
*
* @author  sva
*/
public class ObisCodeMapper {

    public static final ObisCode OBISCODE_SERIAL_NUMBER_OBJ1 = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode OBISCODE_SERIAL_NUMBER_OBJ2 = ObisCode.fromString("0.0.96.1.255.255");
    private static ObisCode OBIS_NUMBER_OF_AVAILABLE_HISTORICAL_SETS = ObisCode.fromString("0.0.0.1.1.255");

    ActarisSl7000 meterProtocol;
    RegisterProfileMapper registerProfileMapper;
    MaximumDemandRegisterProfileMapper maximumDemandRegisterProfileMapper;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(ActarisSl7000 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) {
			return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(Register register) throws IOException {
        return (RegisterValue) doGetRegister(register);
    }

    private Object doGetRegister(Register register) throws IOException {
        int billingPoint;
        ObisCode obisCode = register.getObisCode();

        // *********************************************************************************
        // Historical data
        if (obisCode.getF() != 255) {
            billingPoint = Math.abs(obisCode.getF());
            // Billing point timestamp
            if ((obisCode.toString().indexOf("1.1.0.1.2.") != -1) || (obisCode.toString().indexOf("1.0.0.1.2.") != -1)) {
                return new RegisterValue(register, meterProtocol.getStoredValues().getBillingPointTimeDate(billingPoint));
            } else { // billing register
                HistoricalValue historicalValue = meterProtocol.getStoredValues().getHistoricalValue(obisCode);
                return new RegisterValue(register, historicalValue.getQuantityValue(), historicalValue.getCaptureTime(), historicalValue.getBillingDate());
            }
        }

        // *********************************************************************************
        // Billing counter
        if ((obisCode.toString().indexOf("1.1.0.1.0.255") != -1) || (obisCode.toString().indexOf("1.0.0.1.0.255") != -1)) {
            com.energyict.dlms.cosem.Register cosmeRegister = meterProtocol.getDlmsSession().getCosemObjectFactory().getRegister(OBIS_NUMBER_OF_AVAILABLE_HISTORICAL_SETS);
            return new RegisterValue(register, cosmeRegister.getQuantityValue());
        }
        // Firmware version
        if (obisCode.equals(ComposedMeterInfo.FIRMWARE_VERSION.getObisCode())) {
            return new RegisterValue(register, meterProtocol.getFirmwareVersion());
        }
        // Serial number
        if(obisCode.equals(OBISCODE_SERIAL_NUMBER_OBJ1)) {
            return new RegisterValue(register,  meterProtocol.getMeterSerialNumber());
        }
        // Programming ID
        if (obisCode.equals(ObisCode.fromString("0.0.96.2.0.255"))) {
             final ExtendedRegister extendedRegister = meterProtocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obisCode);
            return new RegisterValue(register, null, extendedRegister.getCaptureTime(), null, null, new Date(), 0, extendedRegister.getStatusText().trim());
        }
        // Number of configurations
         if (obisCode.equals(ObisCode.fromString("0.0.96.1.4.255"))) {
             final ExtendedRegister extendedRegister = meterProtocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(ObisCode.fromString("0.0.96.2.0.255"));
            return new RegisterValue(register, extendedRegister.getQuantityValue());
        }
        // DST working mode
        if (obisCode.equals(ObisCode.fromString("0.0.131.0.4.255"))) {
            return getDSTWorkingMode(register);
        }
        // DST switching times
        if (obisCode.equals(ObisCode.fromString("0.0.131.0.6.255")) | obisCode.equals(ObisCode.fromString("0.0.131.0.7.255"))) {
            return getDSTSwitchingTime(register);
        }
        // Battery end of life date
        if (obisCode.equals(ObisCode.fromString("0.0.96.6.2.255"))) {
            return getBatteryExpiryDate(register);
        }
        // Operation status fatal alarms
        if (obisCode.equals(ObisCode.fromString("0.0.97.97.1.255"))) {
            return getOperationStatus(register, 1);
        }
        // Operation status non-fatal alarms
        if (obisCode.equals(ObisCode.fromString("0.0.97.97.2.255"))) {
            return getOperationStatus(register, 5);
        }

        /**
         * Maximum demand registers can not be read out direct, instead they are mapped in a registerProfile!
         * Instead of reading out each maximum demand register separately, they must always be read out from a profile object
         * E.g.: read a profile generic buffer containing info about all maximum demand registers
         */
        if (getMaximumDemandRegisterProfileMapper().getProfileGenericForRegister(register) != null) {
            return getMaximumDemandRegisterProfileMapper().getRegister(register);
        }

        /**
         * Optionally, all demand/electricity related registers are mapped in a registerProfile.
         * Instead of reading out each demand/electricity registers separately (which should be possible anyway), they can also be read out in group
         * E.g.: read a profile generic buffer containing info about all demand/energy registers
         *
         */
        if (meterProtocol.getProperties().useRegisterProfile() && getRegisterProfileMapper().getProfileGenericForRegister(register) != -1) {
            return getRegisterProfileMapper().getRegister(register);
        }

        // *********************************************************************************
        // All other registers
        final UniversalObject uo = meterProtocol.getDlmsSession().getMeterConfig().findObject(obisCode);
        if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
            final com.energyict.dlms.cosem.Register cosmeRegister = meterProtocol.getDlmsSession().getCosemObjectFactory().getRegister(obisCode);
            return new RegisterValue(register, cosmeRegister.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            final DemandRegister demandRegister = meterProtocol.getDlmsSession().getCosemObjectFactory().getDemandRegister(obisCode);
            return new RegisterValue(register, demandRegister.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            final ExtendedRegister extendedRegister = meterProtocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(obisCode);
            return new RegisterValue(register, extendedRegister.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
            final Data dataRegister = meterProtocol.getDlmsSession().getCosemObjectFactory().getData(obisCode);
            OctetString octetString = dataRegister.getValueAttr().getOctetString();
            if (octetString != null && octetString.stringValue() != null) {
                return new RegisterValue(register, octetString.stringValue());
            }
            throw new NoSuchRegisterException();
        } else {
            throw new NoSuchRegisterException();
        }
    }

    private RegisterValue getDSTWorkingMode(Register register) throws IOException {
        final Data data = meterProtocol.getDlmsSession().getCosemObjectFactory().getData(register.getObisCode());
            long mode = data.getValue();
            String text;
            if (mode == 0) {
                text = "DST switching disabled.";
            } else if (mode == 1) {
                text = "DST switching enabled - generic mode";
            } else if (mode == 2) {
                text = "DST switching enabled - programmed mode";
            } else if (mode == 3) {
                text = "DST switching enabled - generic mode with season";
            } else if (mode == 4) {
                text = "DST switching enabled - programmed mode with season";
            } else {
                text = "Invalid mode";
            }
            return new RegisterValue(register, new Quantity(mode,  Unit.getUndefined()), null, null, null, new Date(), 0, text);
    }

    private RegisterValue getDSTSwitchingTime(Register register) throws IOException {
        ObisCode baseObis = ObisCode.fromString("0.0.131.0.6.255");

        // 1: date and time summer becomes active
        // 0: date and time we go back to normal time
        int element = register.getObisCode().equals(baseObis) ? 0 : 1;

        final Data data = meterProtocol.getDlmsSession().getCosemObjectFactory().getData(baseObis);
            Structure dataSequence = (Structure) data.getValueAttr();
            Structure innerStruct = ((Array) dataSequence.getStructure().getDataType(1)).getDataType(element).getStructure();
            byte[] dateAndTime = innerStruct.getDataType(0).getOctetString().getOctetStr();

        String text = "";
        text += (dateAndTime[0] == 0x7F) ? "Year: *" : "Year: " + dateAndTime[0];
        text += " - ";
        text += (dateAndTime[1] == 0x7F) ? "month: *" : "month: " + dateAndTime[1];
        text += " - ";
        text += (dateAndTime[2] == 0x7F) ? "day of month: *" : "day of month: " + dateAndTime[2];
        text += " - ";
        text += (dateAndTime[3] == 0x7F) ? "day of week: *" : "day of week: " + (dateAndTime[3] == 7 ? 0 : dateAndTime[3]);
        text += " - ";
        text += "hour: " + dateAndTime[4];
        return new RegisterValue(register,  text);
    }

    private RegisterValue getBatteryExpiryDate(Register register) throws IOException {
        Data data = meterProtocol.getDlmsSession().getCosemObjectFactory().getData(register.getObisCode());
        OctetString octetString = data.getValueAttr().getOctetString();

        String info = octetString.getOctetStr()[3] + "/" + octetString.getOctetStr()[2] + "/" +
                (short) (((octetString.getOctetStr()[0] & 0xFF) << 8) | (octetString.getOctetStr()[1] & 0xFF));
        return new RegisterValue(register, info);
    }

    private RegisterValue getOperationStatus(Register register, int length) throws IOException {
        com.energyict.dlms.cosem.Register cosemRegister = meterProtocol.getDlmsSession().getCosemObjectFactory().getRegister(register.getObisCode());
        BitString alarmStatuses = (BitString) cosemRegister.getValueAttr();
        return new RegisterValue(register, "0x" + ProtocolTools.getHexStringFromBytes(ProtocolTools.getBytesFromLong(alarmStatuses.longValue(), length), " "));
    }

    public RegisterProfileMapper getRegisterProfileMapper() {
        if (registerProfileMapper == null) {
            registerProfileMapper = new RegisterProfileMapper(meterProtocol);
        }
        return registerProfileMapper;
    }

    public MaximumDemandRegisterProfileMapper getMaximumDemandRegisterProfileMapper() {
        if (maximumDemandRegisterProfileMapper == null) {
            maximumDemandRegisterProfileMapper = new MaximumDemandRegisterProfileMapper(meterProtocol);
        }
        return maximumDemandRegisterProfileMapper;
    }
}
