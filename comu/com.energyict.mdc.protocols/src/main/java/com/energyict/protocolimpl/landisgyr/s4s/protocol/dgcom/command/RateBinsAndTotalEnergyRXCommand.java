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
public class RateBinsAndTotalEnergyRXCommand extends AbstractCommand {

    public final int NR_OF_RATES=5;
    private long[] ratekWHInPulses = new long[NR_OF_RATES]; // TOU Data --> kWh
    private long totalKWHInPulses; // Energy & Demand --> total energy active kWh
    private long[] ratekMHInPulses = new long[NR_OF_RATES];
    private long totalKMHInPulses;

    /** Creates a new instance of TemplateCommand */
    public RateBinsAndTotalEnergyRXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RateBinsAndTotalEnergyRXCommand:\n");
        for (int i=0;i<getRatekMHInPulses().length;i++) {
            strBuff.append("       ratekMHInPulses["+i+"]="+getRatekMHInPulses()[i]+"\n");
        }
        for (int i=0;i<getRatekWHInPulses().length;i++) {
            strBuff.append("       ratekWHInPulses["+i+"]="+getRatekWHInPulses()[i]+"\n");
        }
        strBuff.append("   totalKMHInPulses="+getTotalKMHInPulses()+"\n");
        strBuff.append("   totalKWHInPulses="+getTotalKWHInPulses()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isRX())
            return new byte[]{(byte)0xA0,0,0,0,0,0,0,0,0};
        else
            throw new IOException("RateBinsAndTotalEnergyRXCommand, only for DX meters!");
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        for (int i=0;i<NR_OF_RATES;i++) {
            getRatekWHInPulses()[i] = ParseUtils.getBCD2LongLE(data, offset, 6);
            offset+=6;
        }
        setTotalKWHInPulses(ParseUtils.getBCD2LongLE(data, offset, 6));
        offset+=6;
        for (int i=0;i<NR_OF_RATES;i++) {
            getRatekMHInPulses()[i] = ParseUtils.getBCD2LongLE(data, offset, 6);
            offset+=6;
        }
        setTotalKMHInPulses(ParseUtils.getBCD2LongLE(data, offset, 6));
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

    public long[] getRatekMHInPulses() {
        return ratekMHInPulses;
    }

    public void setRatekMHInPulses(long[] ratekMHInPulses) {
        this.ratekMHInPulses = ratekMHInPulses;
    }

    public long getTotalKMHInPulses() {
        return totalKMHInPulses;
    }

    public void setTotalKMHInPulses(long totalKMHInPulses) {
        this.totalKMHInPulses = totalKMHInPulses;
    }
}
