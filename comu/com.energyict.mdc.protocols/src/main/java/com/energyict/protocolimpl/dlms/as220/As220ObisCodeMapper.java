/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.dlms.cosem.SFSKPhyMacSetup;
import com.energyict.protocolimpl.base.DLMSAttributeMapper;
import com.energyict.protocolimpl.base.ObiscodeMapper;
import com.energyict.protocolimpl.dlms.as220.emeter.AS220ContactorController;
import com.energyict.protocolimpl.dlms.as220.emeter.DisconnectControlMapper;
import com.energyict.protocolimpl.dlms.as220.emeter.LimiterControlMapper;
import com.energyict.protocolimpl.dlms.as220.objects.CalendarStatus;
import com.energyict.protocolimpl.dlms.as220.plc.SFSKActiveInitiatorMapper;
import com.energyict.protocolimpl.dlms.as220.plc.SFSKIec61334LLCSetupMapper;
import com.energyict.protocolimpl.dlms.as220.plc.SFSKMacCountersMapper;
import com.energyict.protocolimpl.dlms.as220.plc.SFSKPhyMacSetupMapper;
import com.energyict.protocolimpl.dlms.as220.plc.SFSKSyncTimeoutsMapper;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class As220ObisCodeMapper implements ObiscodeMapper {

	private static final int			VALUE_OFFSET				= 8;

	public static final ObisCode		NR_CONFIGCHANGES_OBISCODE	= ObisCode.fromString("0.0.96.2.0.255");
	public static final ObisCode		ALARM_REGISTER_OBISCODE		= ObisCode.fromString("0.0.97.98.0.255");
	public static final ObisCode		ERROR_REGISTER_OBISCODE		= ObisCode.fromString("0.0.97.97.0.255");

	public static final ObisCode		SFSK_PHY_MAC_SETUP			= ObisCode.fromString("0.0.26.0.0.255");
	public static final ObisCode		SFSK_ACTIVE_INITIATOR		= ObisCode.fromString("0.0.26.1.0.255");
	public static final ObisCode		SFSK_SYNC_TIMEOUTS			= ObisCode.fromString("0.0.26.2.0.255");
	public static final ObisCode		SFSK_MAC_COUNTERS			= ObisCode.fromString("0.0.26.3.0.255");
	public static final ObisCode		SFSK_IEC_LLC_SETIP			= ObisCode.fromString("0.0.26.5.0.255");

	public static final ObisCode		FIRMWARE_VERSION			= ObisCode.fromString("1.0.0.2.0.255");
	public static final ObisCode		DISCONNECTOR_OBISCODE		= AS220ContactorController.DISCONNECTOR_OBISCODE;
    public static final ObisCode        LIMITER_OBISCODE            = ObisCode.fromString("0.0.17.0.0.255");

	public static final ObisCode		TARIFF_OBISCODE				= ObisCode.fromString("0.0.96.14.0.255");
	public static final ObisCode	    METER_ID_OBISCODE			= ObisCode.fromString("0.0.96.1.0.255");

    public static final ObisCode        V1_UNDER_LIMIT_CTR          = ObisCode.fromString("1.0.32.32.0.255");
    public static final ObisCode        V2_UNDER_LIMIT_CTR          = ObisCode.fromString("1.0.52.32.0.255");
    public static final ObisCode        V3_UNDER_LIMIT_CTR          = ObisCode.fromString("1.0.72.32.0.255");
    public static final ObisCode        V1_OVER_LIMIT_CTR           = ObisCode.fromString("1.0.32.36.0.255");
    public static final ObisCode        V2_OVER_LIMIT_CTR           = ObisCode.fromString("1.0.52.36.0.255");
    public static final ObisCode        V3_OVER_LIMIT_CTR           = ObisCode.fromString("1.0.72.36.0.255");

    public static final ObisCode        DEVICE_ID1_OBISCODE         = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode        DEVICE_ID2_OBISCODE         = ObisCode.fromString("0.1.96.1.0.255");
    public static final ObisCode        DEVICE_ID3_OBISCODE         = ObisCode.fromString("0.2.96.1.0.255");
    public static final ObisCode        DEVICE_ID4_OBISCODE         = ObisCode.fromString("0.3.96.1.0.255");
    public static final ObisCode        DEVICE_ID5_OBISCODE         = ObisCode.fromString("0.4.96.1.0.255");

    public static final ObisCode        ACTIVITY_CALENDAR_NAME      = ObisCode.fromString("0.0.13.0.0.255");
    public static final ObisCode        CALENDAR_STATUS_OBIS        = ObisCode.fromString("0.130.26.32.0.255");

    public static final ObisCode        VITELEC_VERSION_OBIS        = ObisCode.fromString("0.0.96.50.0.255");

	private static final ObisCode[] SIMPLE_DATA_REGISTERS = new ObisCode[] {
		NR_CONFIGCHANGES_OBISCODE,
		ALARM_REGISTER_OBISCODE,
	    ERROR_REGISTER_OBISCODE,
        V1_UNDER_LIMIT_CTR,
        V2_UNDER_LIMIT_CTR,
        V3_UNDER_LIMIT_CTR,
        V1_OVER_LIMIT_CTR,
        V2_OVER_LIMIT_CTR,
        V3_OVER_LIMIT_CTR,
        DEVICE_ID1_OBISCODE,
        DEVICE_ID2_OBISCODE,
        DEVICE_ID3_OBISCODE,
        DEVICE_ID4_OBISCODE,
        DEVICE_ID5_OBISCODE
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
				new DisconnectControlMapper(DISCONNECTOR_OBISCODE, as220),
                new LimiterControlMapper(LIMITER_OBISCODE, as220)
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
            Quantity billingQuantity = new Quantity(getCosemObjectFactory().getStoredValues().getBillingPointCounter(), Unit.get(""));
            registerValue = new RegisterValue(obisCode, billingQuantity);
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
		} else if ( obisCode.equals(ACTIVITY_CALENDAR_NAME)){
            return new RegisterValue(ACTIVITY_CALENDAR_NAME, getAs220().geteMeter().getActivityCalendarController().getCalendarName());
		} else if ( obisCode.equals(CALENDAR_STATUS_OBIS)) {
            CalendarStatus status = new CalendarStatus(getCosemObjectFactory(), CALENDAR_STATUS_OBIS);
            return new RegisterValue(CALENDAR_STATUS_OBIS, status.getCalendarStatus());
        } else if (obisCode.equals(VITELEC_VERSION_OBIS)) {
            final Data register = getCosemObjectFactory().getData(obisCode);
            Unsigned8 major = (Unsigned8) register.getValueAttr().getStructure().getDataType(0);
            Unsigned8 minor = (Unsigned8) register.getValueAttr().getStructure().getDataType(1);
            String version = "Vitilec Version (major:minor) " + major.getValue() + ":" + minor.getValue();
            return new RegisterValue(obisCode, version);
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
	 * or an {@link com.energyict.dlms.axrdencoding.OctetString}
	 * @return The {@link RegisterValue}
	 * @throws IOException
	 */
	private RegisterValue readDataAsRegisterValue(ObisCode obisCode) throws IOException {
		return readDataAsRegisterValue(obisCode, obisCode);
	}

} // public class ObisCodeMapper
