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

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class ThirdMetricValuesCommand extends AbstractCommand {

    private Date maxkM3Timestamp;
    private int maxkM3InPulses;
    private int powerFactorAtMaxkM3;
    private int coincidentkM3InPulses;
    private long totalkM3h;



    /** Creates a new instance of TemplateCommand */
    public ThirdMetricValuesCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ThirdMetricValues:\n");
        strBuff.append("   coincidentkM3InPulses="+getCoincidentkM3InPulses()+"\n");
        strBuff.append("   maxkM3InPulses="+getMaxkM3InPulses()+"\n");
        strBuff.append("   maxkM3Timestamp="+getMaxkM3Timestamp()+"\n");
        strBuff.append("   powerFactorAtMaxkM3="+getPowerFactorAtMaxkM3()+"\n");
        strBuff.append("   totalkM3h="+getTotalkM3h()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if ((getCommandFactory().getFirmwareVersionCommand().isRX()))
            return new byte[]{(byte)0x99,0,0,0,0,0,0,0,0};
        else throw new IOException("ThirdMetricValues, only for RX meters!");
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setMaxkM3Timestamp(Utils.getTimestampwwhhddYYDDMM(data, offset, getCommandFactory().getS4s().getTimeZone())); offset+=6;
        setMaxkM3InPulses(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setPowerFactorAtMaxkM3((int)ParseUtils.getBCD2LongLE(data, offset, 2)); offset+=2;
        setCoincidentkM3InPulses(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;
        setTotalkM3h(ParseUtils.getBCD2LongLE(data, offset, 6));

    }

    public Date getMaxkM3Timestamp() {
        return maxkM3Timestamp;
    }

    public void setMaxkM3Timestamp(Date maxkM3Timestamp) {
        this.maxkM3Timestamp = maxkM3Timestamp;
    }

    public int getMaxkM3InPulses() {
        return maxkM3InPulses;
    }

    public void setMaxkM3InPulses(int maxkM3InPulses) {
        this.maxkM3InPulses = maxkM3InPulses;
    }

    public int getPowerFactorAtMaxkM3() {
        return powerFactorAtMaxkM3;
    }

    public void setPowerFactorAtMaxkM3(int powerFactorAtMaxkM3) {
        this.powerFactorAtMaxkM3 = powerFactorAtMaxkM3;
    }

    public int getCoincidentkM3InPulses() {
        return coincidentkM3InPulses;
    }

    public void setCoincidentkM3InPulses(int coincidentkM3InPulses) {
        this.coincidentkM3InPulses = coincidentkM3InPulses;
    }

    public long getTotalkM3h() {
        return totalkM3h;
    }

    public void setTotalkM3h(long totalkM3h) {
        this.totalkM3h = totalkM3h;
    }
}
