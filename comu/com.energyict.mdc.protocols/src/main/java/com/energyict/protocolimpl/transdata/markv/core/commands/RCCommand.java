/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RCCommand.java
 *
 * Created on 10 augustus 2005, 16:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ProtocolChannelMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author koen
 */
public class RCCommand extends AbstractCommand {

    private List intervals;
    private static final int DEBUG = 0;
    private static final CommandIdentification commandIdentification = new CommandIdentification("RC",true,false);

    /** Creates a new instance of RCCommand */
    public RCCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
         StringBuilder strBuff = new StringBuilder();
         strBuff.append("RCCommand:\n");
         for(int interval=0;interval<intervals.size();interval++) {
             int[] channelValues = (int[])intervals.get(interval);
             strBuff.append("interval "+interval+": ");
             for(int channel=0;channel<channelValues.length;channel++) {
                 strBuff.append(channelValues[channel]+" ");
             }
             strBuff.append("\n");
         }
         return strBuff.toString();
    }

    protected void parse(String strData) throws IOException {
        int nrOfChannels = getCommandFactory().getDCCommand().getProtocolChannelMap().getNrOfProtocolChannels();
        int recordingType = getCommandFactory().getISCommand().getRecordingType();
        ProtocolChannelMap pcm = getCommandFactory().getDCCommand().getProtocolChannelMap();
        BufferedReader br = new BufferedReader(new StringReader(strData));
        int nrOfRecords = Integer.parseInt(br.readLine());
        int offset=1024;

        byte[] data = strData.getBytes();

        if (DEBUG>=1) {
            System.out.println("\nKV_DEBUG> nrOfChannels="+nrOfChannels);
            System.out.println("KV_DEBUG> recordingType="+recordingType);
            System.out.println("KV_DEBUG> protocolChannelMap="+pcm);
            System.out.println("KV_DEBUG> nrOfRecords="+nrOfRecords);
        }
        int nrOfIntervals = nrOfRecords/pcm.getNrOfUsedProtocolChannels();

        intervals = new ArrayList();
        for (int interval=0;interval<nrOfIntervals;interval++) {
            int[] channelValues = new int[pcm.getNrOfUsedProtocolChannels()];
            //for(int channel=0;channel<pcm.getNrOfProtocolChannels();channel++) {
            for(int channel=(channelValues.length-1);channel>=0;channel--) {
                if (!pcm.isProtocolChannelZero(channel)) {
                   channelValues[channel] = ProtocolUtils.getInt(data,offset, 2);
//System.out.println("interval= "+interval+", channel="+channel+", value="+channelValues[channel]);
                   offset+=2;
                }
            }
            intervals.add(channelValues);
        }
    }

    public void setNrOfRecords(int nrOfRecords) {
        commandIdentification.setArguments(new String[]{Integer.toString(nrOfRecords)});
    }

    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    public List getIntervals() {
        return intervals;
    }

} // public class RCCommand extends AbstractCommand
