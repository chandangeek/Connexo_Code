package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.dlms.cosem.SFSKPhyMacSetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.DLMSAttributeMapper;
import com.energyict.protocolimpl.base.ObiscodeMapper;
import com.energyict.protocolimpl.dlms.as220.emeter.AS220ContactorController;
import com.energyict.protocolimpl.dlms.as220.emeter.DisconnectControlMapper;
import com.energyict.protocolimpl.dlms.as220.plc.SFSKActiveInitiatorMapper;
import com.energyict.protocolimpl.dlms.as220.plc.SFSKIec61334LLCSetupMapper;
import com.energyict.protocolimpl.dlms.as220.plc.SFSKMacCountersMapper;
import com.energyict.protocolimpl.dlms.as220.plc.SFSKPhyMacSetupMapper;
import com.energyict.protocolimpl.dlms.as220.plc.SFSKSyncTimeoutsMapper;

/**
 *
 * @author Koen
 */
public class As220ObisCodeMapper implements ObiscodeMapper {

	private static final int			VALUE_OFFSET				= 8;

	private static final ObisCode		NR_CONFIGCHANGES_OBISCODE	= ObisCode.fromString("0.0.96.2.0.255");
	private static final ObisCode		ALARM_REGISTER_OBISCODE		= ObisCode.fromString("0.0.97.98.0.255");
	private static final ObisCode		FILTER_REGISTER_OBISCODE	= ObisCode.fromString("0.0.97.98.10.255");
	private static final ObisCode		ERROR_REGISTER_OBISCODE		= ObisCode.fromString("0.0.97.97.0.255");
	private static final ObisCode		LOGICAL_DEVICENAME_OBISCODE	= ObisCode.fromString("0.0.42.0.0.255");

	private static final ObisCode		SFSK_PHY_MAC_SETUP			= ObisCode.fromString("0.0.26.0.0.255");
	private static final ObisCode		SFSK_ACTIVE_INITIATOR		= ObisCode.fromString("0.0.26.1.0.255");
	private static final ObisCode		SFSK_SYNC_TIMEOUTS			= ObisCode.fromString("0.0.26.2.0.255");
	private static final ObisCode		SFSK_MAC_COUNTERS			= ObisCode.fromString("0.0.26.3.0.255");
	private static final ObisCode		SFSK_IEC_LLC_SETIP			= ObisCode.fromString("0.0.26.5.0.255");

	private static final ObisCode		FIRMWARE_VERSION			= ObisCode.fromString("1.0.0.2.0.255");
	private static final ObisCode		DISCONNECTOR_OBISCODE		= AS220ContactorController.DISCONNECTOR_OBISCODE;

	private static final ObisCode		TARIFF_OBISCODE				= ObisCode.fromString("0.0.96.14.0.255");
	private static final ObisCode		METER_ID_OBISCODE			= ObisCode.fromString("0.0.96.1.0.255");

	private static final ObisCode[] SIMPLE_DATA_REGISTERS = new ObisCode[] {
		NR_CONFIGCHANGES_OBISCODE,
		ALARM_REGISTER_OBISCODE,
		FILTER_REGISTER_OBISCODE,
	    ERROR_REGISTER_OBISCODE,
	    LOGICAL_DEVICENAME_OBISCODE
	};

	private final DLMSAttributeMapper[] attributeMappers;
	private SFSKPhyMacSetup sFSKPhyMacSetup = null;
	private AS220 as220;

    public As220ObisCodeMapper(AS220 as220) {
		this.as220 = as220;
		this.attributeMappers = new DLMSAttributeMapper[] {
				new SFSKPhyMacSetupMapper(SFSK_PHY_MAC_SETUP, as220),
				new SFSKActiveInitiatorMapper(SFSK_ACTIVE_INITIATOR, as220),
				new SFSKSyncTimeoutsMapper(SFSK_SYNC_TIMEOUTS, as220),
				new SFSKMacCountersMapper(SFSK_MAC_COUNTERS, as220),
				new SFSKIec61334LLCSetupMapper(SFSK_IEC_LLC_SETIP, as220),
				new DisconnectControlMapper(DISCONNECTOR_OBISCODE, as220)
		};
    }

    public AS220 getAs220() {
    	return as220;
    }

    public DLMSAttributeMapper[] getAttributeMappers() {
		return attributeMappers;
	}

    private CosemObjectFactory getCosemObjectFactory() {
		return getAs220().getCosemObjectFactory();
	}

    public SFSKPhyMacSetup getsFSKPhyMacSetup() throws IOException {
    	if (sFSKPhyMacSetup == null) {
    		sFSKPhyMacSetup = getCosemObjectFactory().getSFSKPhyMacSetup();
    	}
    	return sFSKPhyMacSetup;
	}

    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
    	for (int i = 0; i < attributeMappers.length; i++) {
    		if (attributeMappers[i].isObisCodeMapped(obisCode)) {
    			return attributeMappers[i].getRegisterInfo(obisCode);
    		}
		}

    	RegisterInfo regInfo = RegisterDescription.getRegisterInfo(obisCode);
    	return regInfo != null ? regInfo : new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

    	for (int i = 0; i < SIMPLE_DATA_REGISTERS.length; i++) {
    		if (obisCode.equals(SIMPLE_DATA_REGISTERS[i])) {
    			return readDataAsRegisterValue(obisCode);
    		}
		}

    	for (int i = 0; i < attributeMappers.length; i++) {
    		if (attributeMappers[i].isObisCodeMapped(obisCode)) {
    			return attributeMappers[i].getRegisterValue(obisCode);
    		}
		}

    	if (obisCode.equals(ObisCode.fromString("0.0.0.0.0.0"))) {
    		UniversalObject[] uo = getAs220().getMeterConfig().getInstantiatedObjectList();
    		StringBuilder sb = new StringBuilder();
    		for (UniversalObject universalObject : uo) {
    			sb.append(universalObject.getObisCode()).append(" = ");
    			sb.append(DLMSClassId.findById(universalObject.getClassID()));
    			sb.append(" [").append(universalObject.getBaseName()).append("] ");
    			sb.append(universalObject.getObisCode().getDescription());
    			sb.append("\r\n");
    		}
			return new RegisterValue(obisCode, sb.toString());
    	}

        RegisterValue registerValue=null;
        int billingPoint=-1;

        // obis F code
		if ((obisCode.getF() >= 0) && (obisCode.getF() <= 99)) {
			billingPoint = obisCode.getF();
		} else if ((obisCode.getF() <= 0) && (obisCode.getF() >= -99)) {
			billingPoint = obisCode.getF() * -1;
		} else if (obisCode.getF() == 255) {
			billingPoint = -1;
		} else {
			throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
		}

        // *********************************************************************************
        // General purpose ObisRegisters & abstract general service
		if ((obisCode.toString().indexOf("1.0.0.1.0.255") != -1) || (obisCode.toString().indexOf("1.1.0.1.0.255") != -1)) { // billing counter
			registerValue = new RegisterValue(obisCode, getCosemObjectFactory().getCosemObject(ObisCode.fromString("1.0.0.1.0.255")).getQuantityValue());
			return registerValue;
		} else if ((obisCode.toString().indexOf("1.0.0.1.2.") != -1) || (obisCode.toString().indexOf("1.1.0.1.2.") != -1)) { // billing point timestamp
			if ((billingPoint >= 0) && (billingPoint < 99)) {
				registerValue = new RegisterValue(obisCode, getCosemObjectFactory().getStoredValues().getBillingPointTimeDate(billingPoint));
				return registerValue;
			} else {
				throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
			}
		} else if (obisCode.equals(SFSK_PHY_MAC_SETUP)) {
			return getsFSKPhyMacSetup().asRegisterValue();
		} else if (obisCode.equals(SFSK_ACTIVE_INITIATOR)) {
			return getCosemObjectFactory().getSFSKActiveInitiator().asRegisterValue();
		} else if (obisCode.equals(SFSK_SYNC_TIMEOUTS)) {
			return getCosemObjectFactory().getSFSKSyncTimeouts().asRegisterValue();
		} else if (obisCode.equals(SFSK_MAC_COUNTERS)) {
			return getCosemObjectFactory().getSFSKMacCounters().asRegisterValue();
		} else if (obisCode.equals(SFSK_IEC_LLC_SETIP)) {
			return getCosemObjectFactory().getSFSKIec61334LLCSetup().asRegisterValue();
		} else if( obisCode.equals(FIRMWARE_VERSION)) {
		    return new RegisterValue(FIRMWARE_VERSION, getAs220().getFirmwareVersion());
		} else if ( obisCode.equals(TARIFF_OBISCODE)) {
			return readDataAsRegisterValue(TARIFF_OBISCODE, TARIFF_OBISCODE);
		} else if ( obisCode.equals(METER_ID_OBISCODE)) {
			return new RegisterValue(METER_ID_OBISCODE, getAs220().getSerialNumber());
		}

        // *********************************************************************************
        CosemObject cosemObject = getCosemObjectFactory().getCosemObject(obisCode);

        if ((cosemObject == null) && (obisCode.getF() != 255)){
        	cosemObject = getCosemObjectFactory().getStoredValues().getHistoricalValue(obisCode);
        }

        if (cosemObject==null) {
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
		}

        Date captureTime = null;
        Date billingDate = null;
        String text = null;
        Quantity quantityValue = null;

        try {captureTime = cosemObject.getCaptureTime();} catch (IOException e) {}
		try {billingDate = cosemObject.getBillingDate();} catch (IOException e) {}
		try {quantityValue = cosemObject.getQuantityValue();} catch (IOException e) {}
		try {text = cosemObject.getText();} catch (IOException e) {}

		registerValue =
			new RegisterValue(
				obisCode,
				quantityValue,
				(captureTime == null ? billingDate : captureTime),
				null,
				billingDate,
				new Date(),
				0,
				text
			);

		return registerValue;

    } // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException

	private RegisterValue readDataAsRegisterValue(ObisCode deviceObis, ObisCode registerObis) throws IOException {
		RegisterValue register;
		GenericRead gr = getCosemObjectFactory().getGenericRead(deviceObis, VALUE_OFFSET);
		AbstractDataType adt = AXDRDecoder.decode(gr.getResponseData());
		if (adt.isOctetString()) {
			String text = adt.getOctetString().stringValue();
			register = new RegisterValue(registerObis != null ? registerObis : deviceObis, null, new Date(), null, new Date(), new Date(), 0, text);
		} else {
			register = new RegisterValue(registerObis != null ? registerObis : deviceObis);
			Quantity quantity = new Quantity(adt.longValue(), Unit.getUndefined());
			register.setQuantity(quantity);
		}
		return register;
	}


	/**
	 * This method reads a data class from the device, and creates a
	 * {@link RegisterValue} with a {@link Quantity} of the dlms attribute 8.
	 * This attribute is expected to be a numerical value (Integer, unsigned, ...)
	 * or an {@link OctetString}
	 * @return The {@link RegisterValue}
	 * @throws IOException
	 */
	private RegisterValue readDataAsRegisterValue(ObisCode obisCode) throws IOException {
		return readDataAsRegisterValue(obisCode, obisCode);
	}

} // public class ObisCodeMapper
