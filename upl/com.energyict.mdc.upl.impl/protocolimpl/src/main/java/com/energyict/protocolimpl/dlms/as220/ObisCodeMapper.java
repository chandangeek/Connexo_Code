package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

	private CosemObjectFactory cof;

	/** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(CosemObjectFactory cof) {
        this.cof=cof;
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode);
    }

    private Object doGetRegister(ObisCode obisCode) throws IOException {

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
			registerValue = new RegisterValue(obisCode, cof.getCosemObject(ObisCode.fromString("1.0.0.1.0.255")).getQuantityValue());
			return registerValue;
		} else if ((obisCode.toString().indexOf("1.0.0.1.2.") != -1) || (obisCode.toString().indexOf("1.1.0.1.2.") != -1)) { // billing point timestamp
			if ((billingPoint >= 0) && (billingPoint < 99)) {
				registerValue = new RegisterValue(obisCode, cof.getStoredValues().getBillingPointTimeDate(billingPoint));
				return registerValue;
			} else {
				throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
			}
		} // // billing point timestamp

        // *********************************************************************************
        CosemObject cosemObject = cof.getCosemObject(obisCode);

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

} // public class ObisCodeMapper
