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

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class NegativeEnergyCommand extends AbstractCommand {

    private long negativeEnergyInPulses;
    private long leadingkvarhInPulses; // for RX 3.00 and higher

    /** Creates a new instance of TemplateCommand */
    public NegativeEnergyCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("NegativeEnergyCommand:\n");
        strBuff.append("   leadingkvarhInPulses="+getLeadingkvarhInPulses()+"\n");
        strBuff.append("   negativeEnergyInPulses="+getNegativeEnergyInPulses()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if ((getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00))
            return new byte[]{(byte)0xC9,0,0,0,0,0,0,0,0};
        else
            return new byte[]{(byte)0x53,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {

        setNegativeEnergyInPulses(ParseUtils.getBCD2LongLE(data,0, 6));

        if ((getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00))
            setLeadingkvarhInPulses(ParseUtils.getBCD2LongLE(data,6, 6));
    }

    public long getNegativeEnergyInPulses() {
        return negativeEnergyInPulses;
    }

    private void setNegativeEnergyInPulses(long negativeEnergyInPulses) {
        this.negativeEnergyInPulses = negativeEnergyInPulses;
    }

    public long getLeadingkvarhInPulses() {
        return leadingkvarhInPulses;
    }

    private void setLeadingkvarhInPulses(long leadingkvarhInPulses) {
        this.leadingkvarhInPulses = leadingkvarhInPulses;
    }
}
