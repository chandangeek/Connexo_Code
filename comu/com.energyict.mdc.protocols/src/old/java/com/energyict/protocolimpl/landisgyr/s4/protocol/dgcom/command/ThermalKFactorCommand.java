/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.math.BigDecimal;


/**
 *
 * @author Koen
 */
public class ThermalKFactorCommand extends AbstractCommand {

    private int thermalKFactor;
    private BigDecimal bdThermalKFactor;

    /** Creates a new instance of TemplateCommand */
    public ThermalKFactorCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ThermalKFactorCommand:\n");
        strBuff.append("   KFactor="+getThermalKFactor()+"\n");
        strBuff.append("   bdThermalKFactor="+getBdThermalKFactor()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        return new byte[]{(byte)0x17,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setThermalKFactor((int)ParseUtils.getBCD2LongLE(data, offset, 3));
        setBdThermalKFactor(BigDecimal.valueOf(getThermalKFactor()));
        setBdThermalKFactor(getBdThermalKFactor().movePointLeft(3)); // divide by 1000
    }

    public int getThermalKFactor() {
        return thermalKFactor;
    }

    private void setThermalKFactor(int thermalKFactor) {
        this.thermalKFactor = thermalKFactor;
    }

    public BigDecimal getBdThermalKFactor() {
        return bdThermalKFactor;
    }

    private void setBdThermalKFactor(BigDecimal bdThermalKFactor) {
        this.bdThermalKFactor = bdThermalKFactor;
    }
}
