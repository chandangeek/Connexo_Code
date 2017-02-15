/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeMapper.java
 *
 * Created on 15 september 2006, 13:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.itron.datastar.basepages.BasePagesFactory;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    Datastar datastar;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(Datastar datastar) {
        this.datastar=datastar;
    }


    public String getRegisterInfo() throws IOException {
        StringBuffer strBuff = new StringBuffer();

        int nrOfChannels = ((BasePagesFactory)datastar.getBasePagesFactory()).getOperatingSetUpBasePage().getNrOfChannels();
        for (int i=0;i<nrOfChannels;i++) {
            strBuff.append("1."+(i+1)+".82.8.0.255 channel "+(i+1)+" running total\n");
        }
        for (int i=0;i<nrOfChannels;i++) {
            strBuff.append("1."+(i+1)+".128.8.0.255 channel "+(i+1)+" encoder register reading\n");
        }

        return strBuff.toString();
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obc) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));

        if ((obisCode.getA() == 1) && (obisCode.getD() == 8) && (obisCode.getE() == 0) && (obisCode.getF() == 255)) {
            if ((obisCode.getB() >= 1) && (obisCode.getB() <= ((BasePagesFactory)datastar.getBasePagesFactory()).getOperatingSetUpBasePage().getNrOfChannels())) {

                if (obisCode.getC() == 82) {
                    BigDecimal bd = ((BasePagesFactory)datastar.getBasePagesFactory()).getCurrentMassMemoryRecordBasePage().getTotals()[obisCode.getB()-1];
                    bd = bd.multiply(datastar.getAdjustRegisterMultiplier()); // KV 28062007
                    return new RegisterValue(obisCode,new Quantity(bd,Unit.get("")));
                }
                else if (obisCode.getC() == 128) {
                    BigDecimal bd = ((BasePagesFactory)datastar.getBasePagesFactory()).getCurrentMassMemoryRecordBasePage().getEncoders()[obisCode.getB()-1];
                    bd = bd.multiply(datastar.getAdjustRegisterMultiplier()); // KV 28062007
                    return new RegisterValue(obisCode,new Quantity(bd,Unit.get("")));
                }

            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }


}
