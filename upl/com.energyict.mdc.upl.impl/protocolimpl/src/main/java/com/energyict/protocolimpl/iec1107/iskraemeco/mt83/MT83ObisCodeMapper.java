/*
 * ObisCodeMapper.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import java.util.*;
import java.io.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.cbo.*;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig.*;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew.*;

/**
 *
 * @author  jme
 */
public class MT83ObisCodeMapper {
	private static final int DEBUG = 0;

	//private static final Date JAN2008 = new Date(946681200000L);

	MT83Registry mt83Registry;
	RegisterConfig regs;

	/** Creates a new instance of ObisCodeMapper */
	public MT83ObisCodeMapper(MT83Registry mt83Registry, TimeZone timeZone, RegisterConfig regs) {
		this.mt83Registry=mt83Registry;
		this.regs=regs;
	}

	static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
		MT83ObisCodeMapper ocm = new MT83ObisCodeMapper(null,null,null);
		return (RegisterInfo)ocm.doGetRegister(obisCode,false);
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		return (RegisterValue)doGetRegister(obisCode,true);
	}

	private int getBillingResetCounter(RegisterConfig regs) throws IOException {
		return ((Integer)mt83Registry.getRegister(MT83Registry.BILLING_RESET_COUNTER)).intValue();
	}

	private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
		RegisterValue registerValue=null;
		int billingPoint=-1;

		// obis F code
		if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
			billingPoint = obisCode.getF();
		else if ((obisCode.getF() <= 0) && (obisCode.getF() >= -99))
			billingPoint = obisCode.getF()*-1;
		else if (obisCode.getF() == 255)
			billingPoint = -1;
		else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! (1) ");

		if (!read) return new RegisterInfo(obisCode.getDescription());

		String billingDateRegister;
		Date eventDate = null;
		Date fromDate = null;
		Date toDate = null;

		String strReg=null;
		strReg = regs.getMeterRegisterCode(obisCode);
		
		if (strReg == null) 
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! (2) ");
		try {

			if (billingPoint != -1) {

				int VZ = getBillingResetCounter(regs);
				int obisF = (VZ - Math.abs(obisCode.getF()));
				if ((obisF < 1) || (obisF > VZ)) throw new IOException("Invalid historical register! VZ = " + VZ + " Requested = " + obisCode.getF());
				
				
				MT83.sendDebug("Changing obiscode from " + obisCode.toString(), DEBUG);
				
				obisCode = ObisCode.fromString(strReg);
				strReg = (new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), obisF)).toString();
 
				MT83.sendDebug("Changing obiscode " + obisCode.toString() + " to [" + strReg + "]", DEBUG);

				if (DEBUG == 1) {
					MT83.sendDebug("doGetregister(): Billingpoint = " + billingPoint + " - getBillingResetCounter = " + VZ + " - strReg = " + strReg, DEBUG);
				}

				ObisCode billingStartObis = ObisCode.fromString(MT83Registry.BILLING_DATE_START);
				billingDateRegister = new ObisCode(billingStartObis.getA(), billingStartObis.getB(), billingStartObis.getC(), billingStartObis.getD(), billingStartObis.getE(), billingStartObis.getF() + ObisCode.fromString(strReg).getF()).toString();
				
				MT83.sendDebug("Reading toDate from obis: " + billingDateRegister.toString(), DEBUG);
				
				toDate = ((Date)mt83Registry.getRegister(billingDateRegister.toString()));

				billingDateRegister = new ObisCode(billingStartObis.getA(), billingStartObis.getB(), billingStartObis.getC(), billingStartObis.getD(), billingStartObis.getE(), billingStartObis.getF() + (ObisCode.fromString(strReg).getF() + 1)).toString();

				MT83.sendDebug("Reading fromDate from obis: " + billingDateRegister.toString() + "\n", DEBUG);
				
				try {
					fromDate = ((Date)mt83Registry.getRegister(billingDateRegister.toString()));
				} catch (Exception e) {
					MT83.sendDebug("fromDate does not exist. [" + billingDateRegister + "]", DEBUG);
					fromDate = null;
				}
			}

			MT83.sendDebug("Reading register: Obis = " + obisCode.toString() + " Edis: " + strReg, 0);
			DateValuePair dvp = (DateValuePair)mt83Registry.getRegister(strReg+" DATE_VALUE_PAIR");
			Unit obisCodeUnit = dvp.getUnit(); 
			if (!obisCode.getUnitElectricity(0).getBaseUnit().toString().equalsIgnoreCase(obisCodeUnit.getBaseUnit().toString())) {
				if (!obisCodeUnit.isUndefined()) throw new NoSuchRegisterException("Unit of the obiscode (" + obisCode.getUnitElectricity(0).getBaseUnit() +") doesn't match the unit of the register received from the meter (" + obisCodeUnit.getBaseUnit() + ")");
				obisCodeUnit = obisCode.getUnitElectricity(0);
				if (obisCodeUnit.getDlmsCode() != 255)
					obisCodeUnit = obisCode.getUnitElectricity(regs.getScaler());
			}

			Quantity quantity = new Quantity(dvp.getValue(),obisCodeUnit);
			if (quantity.getAmount() != null) {
				eventDate = dvp.getDate();
				if (eventDate != null) {
					fromDate = null;
					toDate = null;
				}
				registerValue = new RegisterValue(obisCode, quantity, eventDate, fromDate, toDate);
			} else {
				String strValue = (String)mt83Registry.getRegister(strReg+" STRING");
				registerValue = new RegisterValue(obisCode,strValue);
			}
			return registerValue;
		}
		catch(IOException e) {
			MT83.sendDebug(e.getMessage(), DEBUG);
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! (3) " + e.toString());
		}

	} // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException      

} // public class ObisCodeMapper
