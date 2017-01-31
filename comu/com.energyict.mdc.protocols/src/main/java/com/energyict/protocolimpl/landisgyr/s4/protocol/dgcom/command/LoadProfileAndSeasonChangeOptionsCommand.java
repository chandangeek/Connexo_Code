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

import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;


/**
 *
 * @author Koen
 */
public class LoadProfileAndSeasonChangeOptionsCommand extends AbstractCommand {

	final int DEBUG=0;

    private int settings; // bit0 demand rest control
                  // bit1 15 minutes profile interval
                  // bit2 30 minutes profile interval
                  // bit3..4 not used
                  // bit5 5 minutes profile interval
                  // bit6 1 minutes profile interval
                  // bit7 season change control

    private int nrOfActiveChannels=1; // nr of active channels DX register always 1 channel

    private int intervalLengthAlternative; // BCD in minues

    private int nrOfAvailableIntervals; // nr of available intervals

    private int voltageSagThreshold;

    private int voltageSwellThreshold;

     /*
      * @return memorySize to read in bytes
      */
    public int getLoadProfileMemorySize(Date from) throws IOException {
        Date to = new Date();
        long seconds2read = (to.getTime() - from.getTime())/1000;
        if (DEBUG>=1) System.out.println("seconds2read="+seconds2read);
        // round up to next day...
        seconds2read = seconds2read + (86400 -(seconds2read%86400));
        if (DEBUG>=1) System.out.println("seconds2read modulo day="+seconds2read);
        // add 1 interval extra
        long nrOfIntervals = seconds2read/getProfileInterval() + 1;
        return (int)((nrOfIntervals*getNrOfActiveChannels()*2));
    }

    public Unit getLoadProfileChannelUnit(int channelIndex) throws IOException {
        return getCommandFactory().getLoadProfileMetricSelectionRXCommand().getUnit(channelIndex);
    }

    public BigDecimal getLoadProfileChannelMultiplier(int channelIndex) throws IOException {
        return getCommandFactory().getKFactorCommand().getBdKFactor().movePointLeft(3); // divide by 1000
    }

    /** Creates a new instance of TemplateCommand */
    public LoadProfileAndSeasonChangeOptionsCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileAndSeasonChangeOptionsCommand:\n");
        strBuff.append("   intervalLengthAlternative="+getIntervalLengthAlternative()+"\n");
        strBuff.append("   nrOfActiveChannels="+getNrOfActiveChannels()+"\n");
        strBuff.append("   nrOfAvailableIntervals="+getNrOfAvailableIntervals()+"\n");
        strBuff.append("   settings="+getSettings()+"\n");
        strBuff.append("   voltageSagThreshold="+getVoltageSagThreshold()+"\n");
        strBuff.append("   voltageSwellThreshold="+getVoltageSwellThreshold()+"\n");
        try {
           strBuff.append("   getProfileInterval="+getProfileInterval()+"\n");
        }
        catch(IOException e) {
            strBuff.append(e.toString());
        }
        return strBuff.toString();
    }

    /*
     * @return profile interval in seconds
     */
    public int getProfileInterval() throws IOException {

        int temp=0;
        if ((getSettings() & 0x02) == 0x02) temp = 900;
        if ((getSettings() & 0x04) == 0x04) temp = 1800;
        if ((getSettings() & 0x20) == 0x20) temp = 300;
        if ((getSettings() & 0x40) == 0x40) temp = 60;

        if (getCommandFactory().getFirmwareVersionCommand().isRX()) {
            if (getIntervalLengthAlternative() != 0)
                temp = getIntervalLengthAlternative()*60;
        }

        if (temp==0)
            throw new IOException("LoadProfileAndSeasonChangeOptionsCommand, getProfileInterval, error getting profile interval length (=0)!");
        return temp;
    }

    protected byte[] prepareBuild() throws IOException {
        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00))
            return new byte[]{(byte)0xC3,0,0,0,0,0,0,0,0};
        else
            return new byte[]{(byte)0x04,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setSettings(ProtocolUtils.getIntLE(data,offset++,1));
        if (getCommandFactory().getFirmwareVersionCommand().isRX()) {

            setNrOfActiveChannels(ProtocolUtils.getIntLE(data,offset++,1));
            setIntervalLengthAlternative((int)ParseUtils.getBCD2LongLE(data, offset++, 1));
            setNrOfAvailableIntervals(ProtocolUtils.getIntLE(data,offset,2)); offset+=2;

            if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00) {
                setVoltageSagThreshold(ProtocolUtils.getIntLE(data,offset++,1));
                setVoltageSwellThreshold(ProtocolUtils.getIntLE(data,offset++,1));
            }
        }

    }

    public int getSettings() {
        return settings;
    }

    public void setSettings(int settings) {
        this.settings = settings;
    }

    public int getNrOfActiveChannels() {
        return nrOfActiveChannels;
    }

    public void setNrOfActiveChannels(int nrOfActiveChannels) {
        this.nrOfActiveChannels = nrOfActiveChannels;
    }

    public int getIntervalLengthAlternative() {
        return intervalLengthAlternative;
    }

    public void setIntervalLengthAlternative(int intervalLengthAlternative) {
        this.intervalLengthAlternative = intervalLengthAlternative;
    }

    public int getNrOfAvailableIntervals() {
        return nrOfAvailableIntervals;
    }

    public void setNrOfAvailableIntervals(int nrOfAvailableIntervals) {
        this.nrOfAvailableIntervals = nrOfAvailableIntervals;
    }

    public int getVoltageSagThreshold() {
        return voltageSagThreshold;
    }

    public void setVoltageSagThreshold(int voltageSagThreshold) {
        this.voltageSagThreshold = voltageSagThreshold;
    }

    public int getVoltageSwellThreshold() {
        return voltageSwellThreshold;
    }

    public void setVoltageSwellThreshold(int voltageSwellThreshold) {
        this.voltageSwellThreshold = voltageSwellThreshold;
    }
}
