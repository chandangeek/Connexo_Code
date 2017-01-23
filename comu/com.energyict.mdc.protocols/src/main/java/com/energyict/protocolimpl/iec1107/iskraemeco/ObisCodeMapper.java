/*
 * ObisCodeMapper.java
 *
 * Created on 15 juli 2004, 8:49
 */

package com.energyict.protocolimpl.iec1107.iskraemeco;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.iec1107.vdew.DateValuePair;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class ObisCodeMapper {
    IskraEmecoRegistry iskraEmecoRegistry;
    RegisterConfig regs;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(IskraEmecoRegistry iskraEmecoRegistry, TimeZone timeZone, RegisterConfig regs) {
        this.iskraEmecoRegistry=iskraEmecoRegistry;
        this.regs=regs;
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null,null,null);
        return (RegisterInfo)ocm.doGetRegister(obisCode,false);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode,true);
    }

    private int getBillingResetCounter(RegisterConfig regs) throws IOException {
        String strReg = regs.getMeterRegisterCode(ObisCode.fromString("1.0.0.1.0.255"));
        return ((Integer)iskraEmecoRegistry.getRegister(strReg+" INTEGER")).intValue();
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        RegisterValue registerValue=null;
        String registerName=null;
        Unit unit = null;
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

        ObisCode oc = new ObisCode(obisCode.getA(),obisCode.getB(),obisCode.getC(),obisCode.getD(),obisCode.getE(),255);


        if (read) {
            String strReg=null;
            if (obisCode.getB() == 2) {
               strReg = (obisCode.getC()==255?"":obisCode.getC()+".")+(obisCode.getD()==255?"":obisCode.getD()+".")+(obisCode.getE()==255?"":obisCode.getE()+"");
            } else {
				strReg = regs.getMeterRegisterCode(oc);
			}

            if (strReg == null) {
				throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
			}
            try {
                Date billingDate=null;
                if (billingPoint != -1) {
                    int VZ = getBillingResetCounter(regs);
                    strReg = strReg+"*"+(VZ-billingPoint);
                    // get billingpoint timestamp
                    ObisCode ocBillingDate = ObisCode.fromString("1.1.0.1.2.255");
                    String strRegBillingDate = regs.getMeterRegisterCode(ocBillingDate);
                    strRegBillingDate = strRegBillingDate+"*"+(VZ-billingPoint);
                    billingDate = ((DateValuePair)iskraEmecoRegistry.getRegister(strRegBillingDate+" DATE_VALUE_PAIR")).getDate();
                }

                DateValuePair dvp = (DateValuePair)iskraEmecoRegistry.getRegister(strReg+" DATE_VALUE_PAIR");

                Unit obisCodeUnit=obisCode.getUnitElectricity(0);
                if (obisCodeUnit.getDlmsCode() != 255) {
					obisCodeUnit = obisCode.getUnitElectricity(regs.getScaler());
				}

                Quantity quantity = new Quantity(dvp.getValue(),obisCodeUnit);
                if (quantity.getAmount() != null) {
                   Date date=dvp.getDate();
                   registerValue = new RegisterValue(obisCode, quantity, date , billingDate);
                   return registerValue;
                }
                else {
                   String strValue = (String)iskraEmecoRegistry.getRegister(strReg+" STRING");
                   registerValue = new RegisterValue(obisCode,strValue);
                   return registerValue;
                }

            }
            catch(IOException e) {
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! "+e.toString());
            }
        }
        else {
            return new RegisterInfo(obisCode.getDescription());
        }

    } // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException

} // public class ObisCodeMapper
