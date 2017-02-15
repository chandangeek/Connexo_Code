/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeMapper.java
 *
 * Created on 15 juli 2004, 8:49
 */

package com.energyict.protocolimpl.sctm.enermete70x;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

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

    /** Creates a new instance of ObisCodeMapper */

    public ObisCodeMapper(SCTMDumpData dump,TimeZone timeZone, RegisterConfig regs) {
        this.dump=dump;
        this.timeZone=timeZone;
        this.regs=regs;
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null,null,null);
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
        RegisterValue registerValue=null;
        String registerName=null;
        Unit unit = null;
        int billingPoint=-1;

        // obis F code
        if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
            billingPoint = obisCode.getF();
        else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99))
            billingPoint = obisCode.getF()*-1;
        else if (obisCode.getF() == 255)
            billingPoint = -1;
        else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

        // *********************************************************************************
        // General purpose ObisRegisters & abstract general service
        if ((obisCode.toString().indexOf("1.0.0.1.0.255") != -1) || (obisCode.toString().indexOf("1.1.0.1.0.255") != -1)) { // billing counter
            if (read) {
                registerValue = new RegisterValue(obisCode,new Quantity(new BigDecimal(dump.getBillingCounter()),Unit.get("")));
                return registerValue;
            }
            else return new RegisterInfo("billing counter");
        } // billing counter
        else if ((obisCode.toString().indexOf("1.0.0.1.2.") != -1) || (obisCode.toString().indexOf("1.1.0.1.2.") != -1)) { // billing point timestamp
            if ((billingPoint >= 0) && (billingPoint < 99)) {
                if (read) {
                    registerValue = new RegisterValue(obisCode,getBillingPointTimestamp(billingPoint));
                    return registerValue;
                }
                else return new RegisterInfo("billing point "+billingPoint+" timestamp");
            }
            else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        } // // billing point timestamp
        else {
            if (read) {
                ObisCode oc = new ObisCode(obisCode.getA(),1,obisCode.getC(),obisCode.getD(),obisCode.getE(),255);
                String strReg = regs.getMeterRegisterCode(oc);
                if (strReg == null)
                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

                Date billingDate=null;
                if (billingPoint != -1) {
                    int VZ = dump.getBillingCounter();
                    strReg = strReg+"*"+(VZ-billingPoint);
                    billingDate = getBillingPointTimestamp(billingPoint);
                }

                Quantity quantity = dump.getRegister(strReg);
                Date eventDate = dump.getRegisterDateTime(strReg, timeZone);
                if (quantity != null) {
                    if (billingPoint != -1)
                        registerValue = new RegisterValue(obisCode,quantity, eventDate==null?billingDate:eventDate, billingDate); // eventtime = toTime
                    else
                        registerValue = new RegisterValue(obisCode,quantity, eventDate);
                    return registerValue;
                }
                else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            }
            else {
                return new RegisterInfo(obisCode.getDescription());
            }
        }

    } // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException

} // public class ObisCodeMapper
