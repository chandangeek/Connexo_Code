package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.dlms.cosem.SFSKPhyMacSetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ObiscodeMapper;

/**
 *
 * @author Koen
 */
public class As220ObisCodeMapper implements ObiscodeMapper {


	private static final int		VALUE_OFFSET				= 8;

	private static final ObisCode	NR_CONFIGCHANGES_OBISCODE	= ObisCode.fromString("0.0.96.2.0.255");
	private static final ObisCode	ALARM_REGISTER_OBISCODE		= ObisCode.fromString("0.0.97.98.0.255");
	private static final ObisCode	FILTER_REGISTER_OBISCODE	= ObisCode.fromString("0.0.97.98.10.255");
	private static final ObisCode	ERROR_REGISTER_OBISCODE		= ObisCode.fromString("0.0.97.97.0.255");
	private static final ObisCode	LOGICAL_DEVICENAME_OBISCODE	= ObisCode.fromString("0.0.42.0.0.255");

	private static final ObisCode[] simpleDataRegisters = new ObisCode[] {
		NR_CONFIGCHANGES_OBISCODE,
		ALARM_REGISTER_OBISCODE,
		FILTER_REGISTER_OBISCODE,
	    ERROR_REGISTER_OBISCODE,
	    LOGICAL_DEVICENAME_OBISCODE
	};

	private static final ObisCode	SFSK_PHY_MAC_SETUP			= ObisCode.fromString("0.0.26.0.0.255");
	private static final ObisCode	SFSK_ACTIVE_INITIATOR		= ObisCode.fromString("0.0.26.1.0.255");
	private static final ObisCode	SFSK_SYNC_TIMEOUTS			= ObisCode.fromString("0.0.26.2.0.255");

	private CosemObjectFactory cosemObjectFactory;
	private SFSKPhyMacSetup sFSKPhyMacSetup = null;

	/** Creates a new instance of ObisCodeMapper */
    public As220ObisCodeMapper(CosemObjectFactory cof) {
        this.cosemObjectFactory=cof;
    }

    private CosemObjectFactory getCosemObjectFactory() {
		return cosemObjectFactory;
	}

    public SFSKPhyMacSetup getsFSKPhyMacSetup() throws IOException {
    	if (sFSKPhyMacSetup == null) {
    		sFSKPhyMacSetup = getCosemObjectFactory().getSFSKPhyMacSetup();
    	}
    	return sFSKPhyMacSetup;
	}

    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        RegisterInfo regInfo = RegisterDescription.getRegisterInfo(obisCode);
    	return regInfo != null ? regInfo : new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

    	for (int i = 0; i < simpleDataRegisters.length; i++) {
    		if (obisCode.equals(simpleDataRegisters[i])) {
    			return readDataAsRegisterValue(obisCode);
    		}
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
		}

        // *********************************************************************************
        CosemObject cosemObject = getCosemObjectFactory().getCosemObject(obisCode);

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

	/**
	 * This method reads a data class from the device, and creates a
	 * {@link RegisterValue} with a {@link Quantity} of the dlms attribute 8.
	 * This attribute is expected to be a numerical value (Integer, unsigned, ...)
	 * or an {@link OctetString}
	 * @return The {@link RegisterValue}
	 * @throws IOException
	 */
	private RegisterValue readDataAsRegisterValue(ObisCode obisCode) throws IOException {
		RegisterValue register;
		GenericRead gr = getCosemObjectFactory().getGenericRead(obisCode, VALUE_OFFSET);
		AbstractDataType adt = AXDRDecoder.decode(gr.getResponseData());
		if (adt.isOctetString()) {
			String text = adt.getOctetString().stringValue();
			register = new RegisterValue(obisCode, null, new Date(), null, new Date(), new Date(), 0, text);
		} else {
			register = new RegisterValue(obisCode);
			Quantity quantity = new Quantity(adt.longValue(), Unit.getUndefined());
			register.setQuantity(quantity);
		}
		return register;
	}

} // public class ObisCodeMapper
