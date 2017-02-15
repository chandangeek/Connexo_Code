/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeMapper.java
 *
 * Created on 15 juli 2004, 8:49
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.customerconfig.RegisterConfig;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class ObisCodeMapper {
    //TimeZone timeZone;
    DataReadingCommandFactory drcf;
    RegisterConfig regs;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(DataReadingCommandFactory drcf, TimeZone timeZone, RegisterConfig regs) {
        //this.timeZone=timeZone;
        this.drcf=drcf;
        this.regs=regs;
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null,null,null);
        return (RegisterInfo)ocm.doGetRegister(obisCode,false);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode,true);
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

        ObisCode oc = new ObisCode(obisCode.getA(),obisCode.getB(),obisCode.getC(),obisCode.getD(),obisCode.getE(),255);

        // Overal transformer ratio = 1.1.0.4.4.255
        // Configuration program number = 1.1.0.2.0.255
        if (oc.equals(ObisCode.fromString("1.1.0.4.4.255"))) {
            if(read)
                return new RegisterValue(oc,drcf.getCTVTRatio());
            else
                return new RegisterInfo("CTVT ratio");

        }
        else if (oc.equals(ObisCode.fromString("1.1.0.2.0.255"))) {
            if(read)
                return new RegisterValue(oc,drcf.getConfigInfo());
            else
                return new RegisterInfo("Program ID & programming date");
        }
        else {
            if (read) {
                int regId = regs.getMeterRegisterId(oc);
                if (regId == -1)
                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                try {
                    Date billingDate=drcf.getRegisterSet(billingPoint+1).getRegister(regId).getBillingTimestamp();
                    Quantity quantity=drcf.getRegisterSet(billingPoint+1).getRegister(regId).getQuantity();
                    Date date=drcf.getRegisterSet(billingPoint+1).getRegister(regId).getMdTimestamp();
                    if (quantity != null) {
                       registerValue = new RegisterValue(obisCode, quantity, date , billingDate);
                       return registerValue;
                    }
                    else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                }
                catch(IOException e) {
                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! "+e.toString());
                }
            }
            else {
                return new RegisterInfo(obisCode.getDescription());
            }
        }

    } // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException

} // public class ObisCodeMapper
