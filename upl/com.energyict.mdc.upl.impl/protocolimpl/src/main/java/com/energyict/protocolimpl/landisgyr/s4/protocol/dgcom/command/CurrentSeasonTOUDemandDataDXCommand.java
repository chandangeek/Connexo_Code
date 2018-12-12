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

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;


/**
 *
 * @author Koen
 */
public class CurrentSeasonTOUDemandDataDXCommand extends AbstractCommand {
   
    public final int NR_OF_RATES=4;
    
    private Date[] maximumKWtimestampsRates = new Date[NR_OF_RATES];
    private int[] maximumKWs = new int[NR_OF_RATES];
    
    /** Creates a new instance of TemplateCommand */
    public CurrentSeasonTOUDemandDataDXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CurrentSeasonTOUDemandDataDXCommand:\n");
        for (int i=0;i<NR_OF_RATES;i++)
        strBuff.append("   maximumKWs["+i+"]="+getMaximumKWsRates()[i]+"\n");
        for (int i=0;i<NR_OF_RATES;i++)
        strBuff.append("   maximumKWtimestampsRates["+i+"]="+getMaximumKWtimestampsRates()[i]+"\n");
        return strBuff.toString();
    }
    
    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isDX())
            return new byte[]{(byte)0x61,0,0,0,0,0,0,0,0};
        else
            throw new IOException("CurrentSeasonTOUDemandDataDXCommand, only for DX meters!");
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
        for (int i=0;i<NR_OF_RATES;i++) {
            getMaximumKWtimestampsRates()[i] = Utils.getTimestampwwhhddYYDDMM(data,offset, getCommandFactory().getS4().getTimeZone());
            offset+=6;
        }
        for (int i=0;i<NR_OF_RATES;i++) {
            getMaximumKWsRates()[i] = ProtocolUtils.getIntLE(data,offset,2);
            offset+=2;
        }
    }

    public Date[] getMaximumKWtimestampsRates() {
        return maximumKWtimestampsRates;
    }

    public void setMaximumKWtimestampsRates(Date[] maximumKWtimestampsRates) {
        this.maximumKWtimestampsRates = maximumKWtimestampsRates;
    }

    public int[] getMaximumKWsRates() {
        return maximumKWs;
    }

    public void setMaximumKWsRates(int[] maximumKWs) {
        this.maximumKWs = maximumKWs;
    }
}
