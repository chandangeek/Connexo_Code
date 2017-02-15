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

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class LoadProfileMetricSelectionRXCommand extends AbstractCommand {

    Unit[] units = new Unit[]{Unit.get("kWh"), // import 0
                              Unit.get("kWh"), // export 1
                              Unit.get("kvarh"), // rms not valid in FW V2.12 or greater 2
                              Unit.get("kVAh"), // rms 3
                              Unit.get("kvarh"), // kQh 4
                              null, // index 5 does not exist! 5
                              Unit.get("kvarh"), // lagging 6
                              Unit.get("kvarh"), // leading 7
                              Unit.get(BaseUnit.VOLTSQUAREHOUR), // A 8
                              Unit.get(BaseUnit.VOLTSQUAREHOUR), // B 9
                              Unit.get(BaseUnit.VOLTSQUAREHOUR), // C 10
                              Unit.get(BaseUnit.AMPERESQUAREHOUR), // Neutral 11
                              Unit.get(BaseUnit.AMPERESQUAREHOUR), // A 12
                              Unit.get(BaseUnit.AMPERESQUAREHOUR), // B 13
                              Unit.get(BaseUnit.AMPERESQUAREHOUR), // C 14
                              Unit.get(""), // External input 1 15
                              Unit.get(""), // External input 1 FW V3.0 16
                              Unit.get("kVAh"), // td lagging FW V2.12 17
                              Unit.get("kVAh"), // td leading FW V2.12 18
                              Unit.get("V"), // FW V3.00 Sag A 19
                              Unit.get("V"), // FW V3.00 Sag B 20
                              Unit.get("V"), // FW V3.00 Sag C 21
                              Unit.get("V"), // FW V3.00 Swell A 22
                              Unit.get("V"), // FW V3.00 Swell B 23
                              Unit.get("V"), // FW V3.00 Swell C 24
                              Unit.get("V"), // FW V3.00 Sag V any phase 25
                              Unit.get("V")}; // FW V3.00 Swell V any phase 26

    private int[] channelMetrics;

    /** Creates a new instance of TemplateCommand */
    public LoadProfileMetricSelectionRXCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileMetricSelectionRXCommand:\n");
        for (int i=0;i<getChannelMetrics().length;i++) {
            strBuff.append("       channelMetrics["+i+"]="+getChannelMetrics()[i]+"\n");
        }
        return strBuff.toString();
    }

    public boolean isEnergy(int channelIndex) {
        return (((getChannelMetrics()[channelIndex]>=0) && (getChannelMetrics()[channelIndex]<=4)) ||
                ((getChannelMetrics()[channelIndex]>=6) && (getChannelMetrics()[channelIndex]<=7)) ||
                ((getChannelMetrics()[channelIndex]>=17) && (getChannelMetrics()[channelIndex]<=18)));
    }

    public Unit getUnit(int channelIndex) {
        return units[getChannelMetrics()[channelIndex]];
    }

    protected byte[] prepareBuild() throws IOException {
        if (getCommandFactory().getFirmwareVersionCommand().isRX())
            return new byte[]{(byte)0xA6,0,0,0,0,0,0,0,0};
        else
            throw new IOException("LoadProfileMetricSelectionRXCommand, only for RX meters!");
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setChannelMetrics(new int[getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getNrOfActiveChannels()]);
        for (int i=0;i<getChannelMetrics().length;i++) {
            getChannelMetrics()[i] = (int)data[i]&0xFF;
        }
    }

    public int[] getChannelMetrics() {
        return channelMetrics;
    }

    public void setChannelMetrics(int[] channelMetrics) {
        this.channelMetrics = channelMetrics;
    }
}
