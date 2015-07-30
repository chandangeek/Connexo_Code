/*
 * ObisCodeMapper.java
 *
 * Created on 17 augustus 2004, 9:21
 */

package com.energyict.protocolimpl.dlms.actarissl7000;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.DLMSLNSL7000;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
/**
 *
 * @author  Koen
 */
public class ObisCodeMapper {
    
    DLMSLNSL7000 meterProtocol;
    RegisterProfileMapper registerProfileMapper=null;
    
    
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(DLMSLNSL7000 meterProtocol) {
        this.meterProtocol = meterProtocol;
        registerProfileMapper = new RegisterProfileMapper(meterProtocol.getCosemObjectFactory(), meterProtocol);
        
    }
    
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) {
			return new RegisterInfo(obisCode.getDescription());
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
    	RegisterValue regValue;
    	try {
        	regValue = (RegisterValue) doGetRegister(obisCode);
		} catch (Exception e) {
			throw new NoSuchRegisterException("Problems while reading " + obisCode + ": " + e.getMessage());
		}

		if ((regValue.getEventTime() != null) && (regValue.getEventTime().getTime() <= 0) ||
                (regValue.getToTime() != null) && (regValue.getToTime().getTime() <= 0)) {
        	throw new NoSuchRegisterException("Value with obiscode: "+obisCode+" contains a uninitialized eventDate: " + regValue.getEventTime());
        }
		return regValue;
    }
    
    private Object doGetRegister(ObisCode obisCode) throws IOException {
        int billingPoint;
        
        // ********************************************************************************* 
        // Historical data
        if (obisCode.getF() != 255) {
            billingPoint = Math.abs(obisCode.getF());
            // Billing point timestamp
            if ((obisCode.toString().indexOf("1.1.0.1.2.") != -1) || (obisCode.toString().indexOf("1.0.0.1.2.") != -1)) {
                return new RegisterValue(obisCode, meterProtocol.getCosemObjectFactory().getStoredValues().getBillingPointTimeDate(billingPoint));
            } else { // billing register
                // Electricity related ObisRegisters mapped to a registerProfile
                if ((obisCode.getA() == 1) && (obisCode.getB() == 1) || (obisCode.getA() == 1) && (obisCode.getC() == 1)) {
                    ObisCode profileObisCode = registerProfileMapper.getMDProfileObisCode(obisCode);
                    if (profileObisCode == null) {
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!"); 
                    }
                    HistoricalValue historicalValue = meterProtocol.getCosemObjectFactory().getStoredValues().getHistoricalValue(profileObisCode);
                    return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
                } else {    // Other billing registers
                    ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 0xFF);
                    HistoricalValue historicalValue = meterProtocol.getCosemObjectFactory().getStoredValues().getHistoricalValue(baseObisCode);
                    return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
                }
            }
        }
        
        // *********************************************************************************
        // Billing counter
        if ((obisCode.toString().indexOf("1.1.0.1.0.255") != -1) || (obisCode.toString().indexOf("1.0.0.1.0.255") != -1)) {
            return new RegisterValue(obisCode, new Quantity(new Integer(meterProtocol.getCosemObjectFactory().getStoredValues().getBillingPointCounter()), Unit.getUndefined()));
        }
        // Firmware version
        if (obisCode.equals(meterProtocol.getMeterConfig().getVersionObject().getObisCode())) {
            return new RegisterValue(obisCode, meterProtocol.getFirmwareVersion());
        }
        // Serial number
        if(obisCode.equals(meterProtocol.getMeterConfig().getSerialNumberObject().getObisCode())) {
            return new RegisterValue(obisCode,  meterProtocol.getSerialNumber());
        }
        // Programming ID
        if (obisCode.equals(ObisCode.fromString("0.0.96.2.0.255"))) {
             final ExtendedRegister register = meterProtocol.getCosemObjectFactory().getExtendedRegister(obisCode);
            return new RegisterValue(obisCode, null, register.getCaptureTime(), null, null, new Date(), 0, register.getStatusText().trim());
        }
        // Number of configurations
         if (obisCode.equals(ObisCode.fromString("0.0.96.1.4.255"))) {
             final ExtendedRegister register = meterProtocol.getCosemObjectFactory().getExtendedRegister(ObisCode.fromString("0.0.96.2.0.255"));
            return new RegisterValue(obisCode, register.getQuantityValue());
        }
        // Number of channels
        if (obisCode.equals(ObisCode.fromString("0.0.96.3.0.255"))) {
            int numberOfChannels = meterProtocol.getNumberOfChannels();
            return new RegisterValue(obisCode,  new Quantity(numberOfChannels, Unit.getUndefined()));
        }
        // Profile interval
        if (obisCode.equals(ObisCode.fromString("0.0.136.0.1.255"))) {
            int profileInterval = meterProtocol.getProfileInterval();
            return new RegisterValue(obisCode,  new Quantity(profileInterval,  Unit.get(BaseUnit.SECOND)));
        }
        // DST working mode
        if (obisCode.equals(ObisCode.fromString("0.0.131.0.4.255"))) {
            return getDSTWorkingMode(obisCode);
        }
        // DST switching times
        if (obisCode.equals(ObisCode.fromString("0.0.131.0.6.255")) | obisCode.equals(ObisCode.fromString("0.0.131.0.7.255"))) {
            return getDSTSwitchingTime(obisCode);
        }
        // Battery end of life date
        if (obisCode.equals(ObisCode.fromString("0.0.96.6.2.255"))) {
            return getBatteryExpiryDate(obisCode);
        }
        // Operation status fatal alarms
        if (obisCode.equals(ObisCode.fromString("0.0.97.97.1.255"))) {
            return getOperationStatus(obisCode, 1);
        }
        // Operation status non-fatal alarms
        if (obisCode.equals(ObisCode.fromString("0.0.97.97.2.255"))) {
            return getOperationStatus(obisCode, 5);
        }
        // Electricity related ObisRegisters mapped to a registerProfile
        if ((obisCode.getA() == 1) && (obisCode.getB() == 1) || (obisCode.getA() == 1) && (obisCode.getC() == 1)) {
            CosemObject cosemObject = registerProfileMapper.getRegister(obisCode);
            if (cosemObject == null) {
                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            }
            Date captureTime = cosemObject.getCaptureTime();
            Date billingDate = cosemObject.getBillingDate();
            return new RegisterValue(obisCode, cosemObject.getQuantityValue(), captureTime == null ? billingDate : captureTime, null,
                    cosemObject.getBillingDate(), new Date(), 0, cosemObject.getText());
        }
        
        // *********************************************************************************
        // All other registers
        final UniversalObject uo = meterProtocol.getMeterConfig().findObject(obisCode);
        if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
            final com.energyict.dlms.cosem.Register register = meterProtocol.getCosemObjectFactory().getRegister(obisCode);
            return new RegisterValue(obisCode, register.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            final DemandRegister register = meterProtocol.getCosemObjectFactory().getDemandRegister(obisCode);
            return new RegisterValue(obisCode, register.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            final ExtendedRegister register = meterProtocol.getCosemObjectFactory().getExtendedRegister(obisCode);
            return new RegisterValue(obisCode, register.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
            final Data register = meterProtocol.getCosemObjectFactory().getData(obisCode);
            OctetString octetString = register.getValueAttr().getOctetString();
            if (octetString != null && octetString.stringValue() != null) {
                return new RegisterValue(obisCode, octetString.stringValue());
            }
            throw new NoSuchRegisterException();
        } else {
            throw new NoSuchRegisterException();
            }
            }

    private RegisterValue getDSTWorkingMode(ObisCode obisCode) throws IOException {
        final Data data = meterProtocol.getCosemObjectFactory().getData(obisCode);
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
            return new RegisterValue(obisCode, new Quantity(mode,  Unit.getUndefined()), null, null, null, new Date(), 0, text);
            }

    private RegisterValue getDSTSwitchingTime(ObisCode obisCode) throws IOException {
        ObisCode baseObis = ObisCode.fromString("0.0.131.0.6.255");

        // 1: date and time summer becomes active
        // 0: date and time we go back to normal time
        int element = obisCode.equals(baseObis) ? 0 : 1;

        final Data data = meterProtocol.getCosemObjectFactory().getData(baseObis);
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
        return new RegisterValue(obisCode,  text);
        }
        
    private RegisterValue getBatteryExpiryDate(ObisCode obisCode) throws IOException {
        Data data = meterProtocol.getCosemObjectFactory().getData(obisCode);
        OctetString octetString = data.getValueAttr().getOctetString();

        String info = octetString.getOctetStr()[3] + "/" + octetString.getOctetStr()[2] + "/" +
                (short) (((octetString.getOctetStr()[0] & 0xFF) << 8) | (octetString.getOctetStr()[1] & 0xFF));
        return new RegisterValue(obisCode, info);
    }

    private RegisterValue getOperationStatus(ObisCode obisCode, int length) throws IOException {
        com.energyict.dlms.cosem.Register register = meterProtocol.getCosemObjectFactory().getRegister(obisCode);
        BitString alarmStatuses = (BitString) register.getValueAttr();
        return new RegisterValue(obisCode, "0x" + ProtocolTools.getHexStringFromBytes(ProtocolTools.getBytesFromLong(alarmStatuses.longValue(), length), " "));
    }
}
