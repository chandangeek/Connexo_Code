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
public class RateBinsAndTotalEnergyDXCommand extends AbstractCommand {

    public final int NR_OF_RATES=4;
    private long[] ratekWHInPulses = new long[NR_OF_RATES];
    private long totalKWHInPulses;

    /** Creates a new instance of TemplateCommand */
    public RateBinsAndTotalEnergyDXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

     public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RateBinsAndTotalEnergyDXCommand:\n");
        for (int i=0;i<getRatekWHInPulses().length;i++) {
            strBuff.append("       ratekWHInPulses["+i+"]="+getRatekWHInPulses()[i]+"\n");
        }
        strBuff.append("   size="+getSize()+"\n");
        strBuff.append("   totalKWHInPulses="+getTotalKWHInPulses()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isDX())
            return new byte[]{(byte)0x05,0,0,0,0,0,0,0,0};
        else
            throw new IOException("RateBinsAndTotalEnergyDXCommand, only for DX meters!");
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        for (int i=0;i<NR_OF_RATES;i++) {
            getRatekWHInPulses()[i] = ParseUtils.getBCD2LongLE(data, offset, 6);
            offset+=6;
        }
        setTotalKWHInPulses(ParseUtils.getBCD2LongLE(data, offset, 6));
        offset+=6;
    }

    public long[] getRatekWHInPulses() {
        return ratekWHInPulses;
    }

    public void setRatekWHInPulses(long[] ratekWHInPulses) {
        this.ratekWHInPulses = ratekWHInPulses;
    }

    public long getTotalKWHInPulses() {
        return totalKWHInPulses;
    }

    public void setTotalKWHInPulses(long totalKWHInPulses) {
        this.totalKWHInPulses = totalKWHInPulses;
    }
}
