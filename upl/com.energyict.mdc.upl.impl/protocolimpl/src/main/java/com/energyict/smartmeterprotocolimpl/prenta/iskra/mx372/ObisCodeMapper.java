package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 20/01/12
 * Time: 10:35
 */
public class ObisCodeMapper {

    private final CosemObjectFactory cof;
    private final IskraMx372 meterProtocol;

    public ObisCodeMapper(CosemObjectFactory cof, IskraMx372 meterProtocol) {
        this.cof = cof;
        this.meterProtocol = meterProtocol;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(Register register) throws IOException {
        return (RegisterValue)doGetRegister(register);
    }

    private Object doGetRegister(Register register) throws IOException {

        RegisterValue registerValue;
        ObisCode obisCode = register.getObisCode();
	    String obisCodeString = obisCode.toString();
	    try {
        	if (obisCode.getF() != 255){
                throw new NoSuchRegisterException("ObisCode "+ obisCodeString +" is not supported! - The device doesn't support billing points.");
        	}

            // *********************************************************************************
            // General purpose ObisRegisters & abstract general service
            // Activity Calendar name
            if (obisCodeString.contains("0.0.13.0.0.255")){
            	registerValue = new RegisterValue(register,
            			null,
            			null, null, null, new Date(), 0,
            			new String(cof.getActivityCalendar(obisCode).readCalendarNameActive().toBigDecimal().toString()));
            	return registerValue;
            }

            // MBus register
            if (obisCode.getC() == 128) {
            	if ( (obisCode.getD() == 50) && (obisCode.getE() == 0) ){

                    // The MBus registers are defined with B field 0. This field should be changed, to reflect the actual slave. This can be done based on the serial number of the slave.
                    int i = meterProtocol.getPhysicalAddressFromSerialNumber(register.getSerialNumber()) +1;
                    ObisCode obisCodeToRead = ProtocolTools.setObisCodeField(obisCode, 1, (byte) i);

            		ExtendedRegister cofRegister = cof.getExtendedRegister(obisCodeToRead);
            		BigDecimal am = BigDecimal.valueOf(cofRegister.getValue());
            		Unit u;
            		if (cofRegister.getScalerUnit().getUnitCode() != 0){
            			u = cofRegister.getScalerUnit().getEisUnit();
            		} else {
            			u = Unit.get(BaseUnit.UNITLESS, 0);
            		}
            		Date captime = cofRegister.getCaptureTime();
            		return new RegisterValue(register, new Quantity(am, u), null, captime);
            	}
            }

            // Abstract ObisRegisters
            if ((obisCode.getA() == 0) && (obisCode.getB() == 0)) {

            	if("0.0.128.20.3.255".equalsIgnoreCase(obisCodeString)){
	            	registerValue = new RegisterValue(register, null,
	            			null, null, null, new Date(), 0, new VisibleString(cof.getData(obisCode).getAttrbAbstractDataType(2).getBEREncodedByteArray(), 0).getStr());
	            	return registerValue;
	            } else if("0.0.128.20.20.255".equalsIgnoreCase(obisCodeString)){
	            	registerValue = new RegisterValue(register, null,
	            			null, null, null, new Date(), 0, ParseUtils.decimalByteToString(OctetString.fromByteArray(cof.getData(obisCode).getAttrbAbstractDataType(2).getBEREncodedByteArray()).getContentBytes()));
	            	return registerValue;
	            } else if(obisCodeString.equalsIgnoreCase(cof.getAutoConnect().getObisCode().toString())){
	            	Array phoneList = cof.getAutoConnect().readDestinationList();
	            	StringBuilder numbers = new StringBuilder();
	            	for(int i = 0; i < phoneList.nrOfDataTypes(); i++){
	            		numbers.append(new String(OctetString.fromByteArray(phoneList.getDataType(i).getBEREncodedByteArray()).getContentBytes()));
	            		if(i < phoneList.nrOfDataTypes() - 1){
	            			numbers.append(";");
	            		}
	            	}
	            	registerValue = new RegisterValue(register, null, null, null, null, new Date(), 0, numbers.toString());
	            	return registerValue;
	            }

	            CosemObject cosemObject = cof.getCosemObject(obisCode);

	            if (cosemObject==null){
	                throw new NoSuchRegisterException("ObisCode "+ obisCodeString +" is not supported!");
	            }

	            if ( (obisCodeString.contains("0.0.128.30.21.255")) ) { // Disconnector
                    registerValue = new RegisterValue(register,
                            cosemObject.getQuantityValue(),
                            null, null, null,
                            new Date(),0);
                    return registerValue;
	            }else if((obisCodeString.contains("0.0.128.30.22.255")) ) {	//ConnectorMode
	            	registerValue = new RegisterValue(register,
                            cosemObject.getQuantityValue(),
                            null, null, null,
                            new Date(),0);
                    return registerValue;
	            }

	            else if ((obisCodeString.contains("0.0.128.7.")) || (obisCodeString.contains("0.0.128.8."))){
	            	registerValue = new RegisterValue(register,cosemObject.getQuantityValue());
	            	return registerValue;
	            }

	            else if(obisCode.getD() == 101){
	            	if(obisCode.getE() == 18 || obisCode.getE() == 28){	// firware versions(Core/Module)
	            		registerValue = new RegisterValue(register, null,
	            				null, null, null, new Date(), 0, ParseUtils.decimalByteToString(cof.getData(obisCode).getDataContainer().getRoot().getOctetString(0).getArray()));
	            		return registerValue;
	            	}
	            }
                Date captureTime = null;
				try {
                   captureTime = cosemObject.getCaptureTime();
                }
                catch(ClassCastException e) {
                    // absorb
                    String s = "On Hold!";
                }
                try {
                    registerValue = new RegisterValue(register,
                                                      cosemObject.getQuantityValue(),
                                                      captureTime,
                                                      null,
                                                      null,
                                                      new Date(),
                                                      0,
                                                      cosemObject.getText());
                    return registerValue;
                }
                catch(ClassCastException e) {
                    throw new NoSuchRegisterException("ObisCode "+ obisCodeString +" is not supported!");
                }
            }


            // *********************************************************************************
            // Electricity related ObisRegisters
            if ((obisCode.getA() == 1) && ((obisCode.getB() == 0) || (obisCode.getB() <= 2))) {
                if (obisCode.getD() == 8 || obisCode.getD() == 4 || obisCode.getD() == 5) { // cumulative values, indexes / current average /  last average
                    com.energyict.dlms.cosem.Register cofRegister = cof.getRegister(obisCode);
                    return new RegisterValue(register, new Quantity(BigDecimal.valueOf(cofRegister.getValue()), cofRegister.getScalerUnit().getEisUnit()));
                } else if (obisCode.getD() == 6) { // maximum demand values
                    ExtendedRegister cofRegister = cof.getExtendedRegister(obisCode);
                    return new RegisterValue(register, new Quantity(BigDecimal.valueOf(cofRegister.getValue()), cofRegister.getScalerUnit().getEisUnit()), cofRegister.getCaptureTime());
                } // maximum demand values
            } // if ((obisCode.getA() == 1) && (obisCode.getB() == 0)) {

        } catch (IOException e) {
        }
        throw new NoSuchRegisterException("ObisCode " + obisCodeString + " is not supported!");
    }

}