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

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    private final Datastar datastar;

    public ObisCodeMapper(Datastar datastar) {
        this.datastar=datastar;
    }

    public String getRegisterInfo() throws IOException {
        StringBuilder builder = new StringBuilder();

        int nrOfChannels = datastar.getBasePagesFactory().getOperatingSetUpBasePage().getNrOfChannels();
        for (int i=0;i<nrOfChannels;i++) {
            builder.append("1.").append(i + 1).append(".82.8.0.255 channel ").append(i + 1).append(" running total\n");
        }
        for (int i=0;i<nrOfChannels;i++) {
            builder.append("1.").append(i + 1).append(".128.8.0.255 channel ").append(i + 1).append(" encoder register reading\n");
        }

        return builder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(ObisCode obc) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));

        if ((obisCode.getA() == 1) && (obisCode.getD() == 8) && (obisCode.getE() == 0) && (obisCode.getF() == 255)) {
            if ((obisCode.getB() >= 1) && (obisCode.getB() <= datastar.getBasePagesFactory().getOperatingSetUpBasePage().getNrOfChannels())) {

                if (obisCode.getC() == 82) {
                    BigDecimal bd = datastar.getBasePagesFactory().getCurrentMassMemoryRecordBasePage().getTotals()[obisCode.getB()-1];
                    bd = bd.multiply(datastar.getAdjustRegisterMultiplier()); // KV 28062007
                    return new RegisterValue(obisCode,new Quantity(bd,Unit.get("")));
                }
                else if (obisCode.getC() == 128) {
                    BigDecimal bd = datastar.getBasePagesFactory().getCurrentMassMemoryRecordBasePage().getEncoders()[obisCode.getB()-1];
                    bd = bd.multiply(datastar.getAdjustRegisterMultiplier()); // KV 28062007
                    return new RegisterValue(obisCode,new Quantity(bd,Unit.get("")));
                }

            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

}