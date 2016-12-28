/*
 * ObisCodeMapper.java
 *
 * Created on 15 juli 2004, 8:49
 */

package com.energyict.protocolimpl.sctm.mtt3a;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.siemens7ED62.SCTMDumpData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class ObisCodeMapper {
    SCTMDumpData dump;
    TimeZone timeZone;
    RegisterConfig regs;
    int autoBillingPointNrOfDigits;

    public ObisCodeMapper(SCTMDumpData dump,TimeZone timeZone, RegisterConfig regs,int autoBillingPointNrOfDigits) {
        this.dump=dump;
        this.timeZone=timeZone;
        this.regs=regs;
        this.autoBillingPointNrOfDigits=autoBillingPointNrOfDigits;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null,null,null,-1);
        return (RegisterInfo)ocm.doGetRegister(obisCode,false);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode,true);
    }

    public Date getBillingPointTimestamp(int billingPoint) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.add(Calendar.MONTH,(-1)*billingPoint);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        RegisterValue registerValue;
        int billingPoint;

        // obis F code
        String obisCodeString = obisCode.toString();
        if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99)) {
            billingPoint = obisCode.getF();
        } else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99)) {
            billingPoint = obisCode.getF() * -1;
        } else if (obisCode.getF() == 255) {
            billingPoint = -1;
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCodeString + " is not supported!");
        }

        // *********************************************************************************
        // General purpose ObisRegisters & abstract general service
        if ((obisCodeString.contains("1.0.0.1.0.255")) || (obisCodeString.contains("1.1.0.1.0.255"))) { // billing counter
            if (read) {
                registerValue = new RegisterValue(obisCode,new Quantity(new BigDecimal(dump.getBillingCounter()),Unit.get("")));
                return registerValue;
            }
            else {
                return new RegisterInfo("billing counter");
            }
        } // billing counter
        else if ((obisCodeString.contains("1.0.0.1.2.")) || (obisCodeString.contains("1.1.0.1.2."))) { // billing point timestamp
            if ((billingPoint >= 0) && (billingPoint < 99)) {
                if (read) {
                    registerValue = new RegisterValue(obisCode,getBillingPointTimestamp(billingPoint));
                    return registerValue;
                }
                else {
                    return new RegisterInfo("billing point " + billingPoint + " timestamp");
                }
            }
            else {
                throw new NoSuchRegisterException("ObisCode " + obisCodeString + " is not supported!");
            }
        } // // billing point timestamp
        else {
            if (read) {
                ObisCode oc = new ObisCode(obisCode.getA(),1,obisCode.getC(),obisCode.getD(),obisCode.getE(),255);
                String strReg = regs.getMeterRegisterCode(oc);
                if (strReg == null) {
                    throw new NoSuchRegisterException("ObisCode " + obisCodeString + " is not supported!");
                }

                Date billingDate=null;
                if (billingPoint != -1) {
                    int VZ = dump.getBillingCounter();
                    if (autoBillingPointNrOfDigits == 1) {
                        int length = dump.getBillingCounterLength();
                        strReg = strReg+"*"+ProtocolUtils.buildStringDecimal((VZ-billingPoint), length);
                    }
                    else if (autoBillingPointNrOfDigits == 0) {
                        strReg = strReg+"*"+(VZ-billingPoint);
                    }
                    else {
                        strReg = strReg+"*"+ProtocolUtils.buildStringDecimal((VZ-billingPoint), autoBillingPointNrOfDigits);
                    }
                    billingDate = getBillingPointTimestamp(billingPoint);
                }

                Quantity quantity = dump.getRegister(strReg);
                Date eventDate = dump.getRegisterDateTime(strReg, timeZone);
                if (quantity != null) {
                    if (billingPoint != -1) {
                        registerValue = new RegisterValue(obisCode, quantity, eventDate == null ? billingDate : eventDate, billingDate); // eventtime = toTime
                    } else {
                        registerValue = new RegisterValue(obisCode, quantity, eventDate);
                    }
                    return registerValue;
                }
                else {
                    throw new NoSuchRegisterException("ObisCode " + obisCodeString + " is not supported!");
                }
            }
            else {
                return new RegisterInfo(obisCodeString);
            }
        }

    }

}