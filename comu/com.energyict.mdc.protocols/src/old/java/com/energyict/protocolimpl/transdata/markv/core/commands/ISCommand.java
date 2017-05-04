/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ISCommand.java
 *
 * Created on 11 augustus 2005, 14:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 *
 * @author koen
 */
public class ISCommand extends AbstractCommand {

    private static final CommandIdentification commandIdentification = new CommandIdentification("IS");

    private static final int RECORDING_TYPE_1=0x01;
    private static final int RECORDING_TYPE_2=0x02;


    private int profileInterval; // in seconds
    private int channelBitmap; // bit7..0 channel 8..1 enable/disable
    private int maxNrOfChannels; // 4 or 8
    private int recordingType; // RECORDING_TYPE_1 or RECORDING_TYPE_2
    private boolean dstEnabled; // false or true


    /** Creates a new instance of ISCommand */
    public ISCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
       return "ISCommand: profileInterval="+profileInterval+", channelBitmap=0x"+Integer.toHexString(channelBitmap)+", maxNrOfChannels="+maxNrOfChannels+", recordingType="+recordingType+", dstEnabled="+dstEnabled;
    }

    public boolean isRecordingType1() {
        return (getRecordingType()&RECORDING_TYPE_1) == RECORDING_TYPE_1;
    }
    public boolean isRecordingType2() {
        return (getRecordingType()&RECORDING_TYPE_2) == RECORDING_TYPE_2;
    }

    protected void parse(String strData) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(strData));
        setProfileInterval(Integer.parseInt(br.readLine())*60);
        int recType = Integer.parseInt(br.readLine());
        int chBitmap = Integer.parseInt(br.readLine());


        // KV_TO_DO recordingtype and DST???
        setRecordingType(recType & 0x03);
        setMaxNrOfChannels(((recType&0x80)==0x80)?8:4);
        setDstEnabled((getMaxNrOfChannels() == 8) ? ((recType&0x40)==0x40) : ((chBitmap&0x80)==0x80));
        setChannelBitmap((getMaxNrOfChannels() == 8) ? chBitmap : chBitmap&0x0F);
    }

    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    public int getProfileInterval() {
        return profileInterval;
    }

    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }

    public int getChannelBitmap() {
        return channelBitmap;
    }

    public void setChannelBitmap(int channelBitmap) {
        this.channelBitmap = channelBitmap;
    }

    public int getMaxNrOfChannels() {
        return maxNrOfChannels;
    }

    public void setMaxNrOfChannels(int maxNrOfChannels) {
        this.maxNrOfChannels = maxNrOfChannels;
    }

    public int getRecordingType() {
        return recordingType;
    }

    private void setRecordingType(int recordingType) {
        this.recordingType = recordingType;
    }

    public boolean isDstEnabled() {
        return dstEnabled;
    }

    public void setDstEnabled(boolean dstEnabled) {
        this.dstEnabled = dstEnabled;
    }

}