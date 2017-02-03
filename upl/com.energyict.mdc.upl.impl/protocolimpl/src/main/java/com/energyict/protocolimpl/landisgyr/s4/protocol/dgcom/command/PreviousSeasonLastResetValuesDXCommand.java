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
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;


/**
 *
 * @author Koen
 */
public class PreviousSeasonLastResetValuesDXCommand extends AbstractCommand {
    
    private long totalKWHAtLastDemandReset;
    private Date timestampMaxDemandAtLastDemandReset;
    private int maxDemandAtLastDemandReset;
    
    /** Creates a new instance of TemplateCommand */
    public PreviousSeasonLastResetValuesDXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PreviousSeasonLastResetValuesDXCommand:\n");
        strBuff.append("   maxDemandAtLastDemandReset="+getMaxDemandAtLastDemandReset()+"\n");
        strBuff.append("   timestampMaxDemandAtLastDemandReset="+getTimestampMaxDemandAtLastDemandReset()+"\n");
        strBuff.append("   totalKWHAtLastDemandReset="+getTotalKWHAtLastDemandReset()+"\n");
        return strBuff.toString();
    }
    
    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isDX())
            return new byte[]{(byte)0x86,0,0,0,0,0,0,0,0};
        else
            throw new IOException("PreviousSeasonLastResetValuesDXCommand, only for DX meters!");
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setTotalKWHAtLastDemandReset(ParseUtils.getBCD2LongLE(data, offset, 6)); offset+=6;
        setTimestampMaxDemandAtLastDemandReset(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4().getTimeZone()));
        setMaxDemandAtLastDemandReset(ProtocolUtils.getIntLE(data, offset, 2)); offset+=2;
    }

    public long getTotalKWHAtLastDemandReset() {
        return totalKWHAtLastDemandReset;
    }

    public void setTotalKWHAtLastDemandReset(long totalKWHAtLastDemandReset) {
        this.totalKWHAtLastDemandReset = totalKWHAtLastDemandReset;
    }

    public Date getTimestampMaxDemandAtLastDemandReset() {
        return timestampMaxDemandAtLastDemandReset;
    }

    public void setTimestampMaxDemandAtLastDemandReset(Date timestampMaxDemandAtLastDemandReset) {
        this.timestampMaxDemandAtLastDemandReset = timestampMaxDemandAtLastDemandReset;
    }

    public int getMaxDemandAtLastDemandReset() {
        return maxDemandAtLastDemandReset;
    }

    public void setMaxDemandAtLastDemandReset(int maxDemandAtLastDemandReset) {
        this.maxDemandAtLastDemandReset = maxDemandAtLastDemandReset;
    }
}
