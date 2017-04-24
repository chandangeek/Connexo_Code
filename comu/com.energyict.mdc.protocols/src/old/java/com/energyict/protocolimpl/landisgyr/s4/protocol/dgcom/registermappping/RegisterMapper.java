/*
 * RegisterMapper.java
 *
 * Created on 12 juni 2006, 15:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.registermappping;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
abstract public class RegisterMapper {

    abstract protected void buildRegisterValues(int billingPoint) throws IOException;
    abstract protected String getBillingExtensionDescription() throws IOException;

    S4 s4;
    List registers;

    boolean current;
    boolean[] selfread;
    private int nrOfBillingPeriods;

    public RegisterMapper(S4 s4) throws IOException {
        this.s4=s4;
        setNrOfBillingPeriods(s4.getCommandFactory().getSelfReadConfigurationCommand().getNrOfSelfReadsToStore());
        registers = new ArrayList();
        current=false;
        selfread = new boolean[getNrOfBillingPeriods()];
        for (int i=0;i<getNrOfBillingPeriods();i++)
            selfread[i] = false;
    }

    public RegisterValue getRegisterValue(ObisCode obc) throws IOException {

        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        buildRegisterValues(obisCode.getF());

        for (int i=0;i<getRegisters().size();i++) {
            Register reg = (Register)getRegisters().get(i);
            if (reg.getRegisterValue().getObisCode().equals(obisCode)) {
                return reg.getRegisterValue();
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }


    public String getRegisterInfo() throws IOException {
        StringBuffer strBuff = new StringBuffer();
        buildRegisterValues(255);
        for (int i=0;i<getRegisters().size();i++) {
            strBuff.append(getRegisters().get(i)+"\n");
        }
        strBuff.append(getBillingExtensionDescription());
        return strBuff.toString();
    }

    public List getRegisters() {
        return registers;
    }

    public int getNrOfBillingPeriods() {
        return nrOfBillingPeriods;
    }

    public void setNrOfBillingPeriods(int nrOfBillingPeriods) {
        this.nrOfBillingPeriods = nrOfBillingPeriods;
    }
}
