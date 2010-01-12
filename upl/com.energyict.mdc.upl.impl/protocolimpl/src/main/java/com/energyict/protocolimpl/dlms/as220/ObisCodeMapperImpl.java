package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ObiscodeMapper;

/**
 *
 * @author Koen
 */
public class ObisCodeMapperImpl implements ObiscodeMapper {

	private static final int	VALUE_OFFSET	= 8;
	private static final ObisCode	ERROR_REG_OBISCODE	= ObisCode.fromString("0.0.97.97.0.255");
	private static final ObisCode	ALARM_REG_OBISCODE	= ObisCode.fromString("0.0.97.98.0.255");

	private CosemObjectFactory cosemObjectFactory;

	/** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapperImpl(CosemObjectFactory cof) {
        this.cosemObjectFactory=cof;
    }

    private CosemObjectFactory getCosemObjectFactory() {
		return cosemObjectFactory;
	}

    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

    	if (obisCode.equals(ERROR_REG_OBISCODE)) {
    		return getErrorRegister();
    	} else if (obisCode.equals(ALARM_REG_OBISCODE)) {
    		return getAlarmRegister();
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
		} // // billing point timestamp

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
	 * @return
	 * @throws IOException
	 */
	private RegisterValue getAlarmRegister() throws IOException {
        RegisterValue alarmRegister = new RegisterValue(ALARM_REG_OBISCODE);
		long alarmValue = getCosemObjectFactory().getGenericRead(ALARM_REG_OBISCODE, VALUE_OFFSET).getValue();
        alarmRegister.setQuantity(new Quantity(alarmValue, Unit.getUndefined()));
		return alarmRegister;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private RegisterValue getErrorRegister()  throws IOException {
        RegisterValue errorRegister = new RegisterValue(ERROR_REG_OBISCODE);
		long errorValue = getCosemObjectFactory().getGenericRead(ERROR_REG_OBISCODE, VALUE_OFFSET).getValue();
		errorRegister.setQuantity(new Quantity(errorValue, Unit.getUndefined()));
		return errorRegister;
	}

} // public class ObisCodeMapper
