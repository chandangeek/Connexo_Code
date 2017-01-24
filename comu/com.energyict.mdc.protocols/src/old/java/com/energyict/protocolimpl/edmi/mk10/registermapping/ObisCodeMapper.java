/*
 * ObisCodeMapper.java
 *
 * Created on 23 maart 2006, 15:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.registermapping;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.edmi.mk10.MK10;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author koen
 */
public class ObisCodeMapper {

	private static final int DEBUG = 0;
	MK10 mk10;

	/** Creates a new instance of ObisCodeMapper */
	public ObisCodeMapper(MK10 mk10) {
		this.mk10=mk10;
	}

	public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
		return new RegisterInfo(obisCode.getDescription());
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

		if (DEBUG == 1) {
			this.mk10.sendDebug(" MK10 OBISCODE REQUEST:  " + obisCode.toString() + " - " + obisCode.getDescription());
		}
		RegisterValue registerValue=null;
		int billingPoint=-1;

		// obis F code
		if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99)) {
			billingPoint = obisCode.getF();
		} else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99)) {
			billingPoint = obisCode.getF()*-1;
		} else if (obisCode.getF() == 255) {
			billingPoint = -1;
		} else {
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
		}

		// *********************************************************************************
		// General purpose ObisRegisters & abstract general service
		if ((obisCode.toString().indexOf("1.0.0.1.0.255") != -1) ||(obisCode.toString().indexOf("1.1.0.1.0.255") != -1)) { // billing counter
			return new RegisterValue(obisCode,new Quantity(new BigDecimal(""+mk10.getObicCodeFactory().getBillingInfo().getNrOfBillingResets()),Unit.get("")));
		} // billing counter
		else if ((obisCode.toString().indexOf("1.0.0.1.2.") != -1) || (obisCode.toString().indexOf("1.1.0.1.2.") != -1)) { // billing point timestamp
			if (billingPoint == 0) {
				return new RegisterValue(obisCode,mk10.getObicCodeFactory().getBillingInfo().getToDate());
			} else {
				throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
			}
		} // billing point timestamp
		else if ((obisCode.toString().indexOf("1.0.0.4.2.255") != -1) ||(obisCode.toString().indexOf("1.1.0.4.2.255") != -1)) { // CT numerator
			return new RegisterValue(obisCode,new Quantity(mk10.getCommandFactory().getReadCommand(0xF700).getRegister().getBigDecimal(),Unit.get("")));
		} // CT numerator
		else if ((obisCode.toString().indexOf("1.0.0.4.3.255") != -1) ||(obisCode.toString().indexOf("1.1.0.4.3.255") != -1)) { // VT numerator
			return new RegisterValue(obisCode,new Quantity(mk10.getCommandFactory().getReadCommand(0xF701).getRegister().getBigDecimal(),Unit.get("")));
		} // VT numerator
		else if ((obisCode.toString().indexOf("1.0.0.4.5.255") != -1) ||(obisCode.toString().indexOf("1.1.0.4.5.255") != -1)) { // CT denominator
			return new RegisterValue(obisCode,new Quantity(mk10.getCommandFactory().getReadCommand(0xF702).getRegister().getBigDecimal(),Unit.get("")));
		} // CT denominator
		else if ((obisCode.toString().indexOf("1.0.0.4.6.255") != -1) ||(obisCode.toString().indexOf("1.1.0.4.6.255") != -1)) { // VT denominator
			return new RegisterValue(obisCode,new Quantity(mk10.getCommandFactory().getReadCommand(0xF703).getRegister().getBigDecimal(),Unit.get("")));
		} // VT denominator
		else {
			// electricity related registers
			return mk10.getObicCodeFactory().getRegisterValue(obisCode);
		}

	} // public RegisterValue getRegisterValue(ObisCode obisCode)

} // public class ObisCodeMapper
