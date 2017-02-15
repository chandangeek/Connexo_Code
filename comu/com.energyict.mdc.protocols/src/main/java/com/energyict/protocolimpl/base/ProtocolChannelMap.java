/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProtocolChannelMap.java
 *
 * Created on 12 september 2003, 9:46
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Koen
 *         changes:
 *         KV 04012005 Allow ',' as separator
 *         <p/>
 *         sequence==false
 *         Individual channels are separated by ':' or ','.
 *         e.g. 4.4:7.6
 *         e.g. 5,6,7
 *         e.g. 0,0,0,0
 *         sequence == true
 *         e.g. 0 3 4
 *         will be 1,0,0,1,1 means channel 0,3 and 4 enabled and active!
 *         mapping:
 *         e.g. 2.5 6.7  all 0 based!
 *         means keep rtu channel 2 and save it to the 5 in the database
 *         e.g. 2.5.6.7+7 6.7.1.8+5
 *         means 2.5 6.7 mapping with registers 6.7+7 and 1.8+5
 */
public class ProtocolChannelMap {

    List protocolChannels = null;
    boolean mappedChannels = false; // KV 06092005 K&P

    /**
     * Creates a new instance of ChannelMap
     *
     * @param channelConfig
     * @throws InvalidPropertyException
     */
    public ProtocolChannelMap(String channelConfig) throws InvalidPropertyException {
        this(channelConfig != null ? channelConfig : "", (channelConfig != null) && (channelConfig.contains(" "))); // KV 06092005 K&P
    }

    /**
     * Creates a new instance of ChannelMap
     *
     * @param channelConfig
     * @param sequence
     * @throws InvalidPropertyException
     */
    public ProtocolChannelMap(String channelConfig, boolean sequence) throws InvalidPropertyException {
        if (sequence && (channelConfig.contains("."))) { // KV 06092005 K&P
            mappedChannels = true;
        }
        protocolChannels = new ArrayList();
        parse(sequence ? convert2NonSequence(channelConfig) : channelConfig);
    }

    /**
     * Creates a new instance of ChannelMap
     *
     * @param protocolChannels
     */
    public ProtocolChannelMap(List protocolChannels) {
        this.protocolChannels = protocolChannels;
    }

    public String toString() {
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("ProtocolChannelMap:\n");
        strBuff.append("mappedChannels = ").append(isMappedChannels()).append("\n");
        for (int i = 0; i < protocolChannels.size(); i++) {
            ProtocolChannel pc = (ProtocolChannel) protocolChannels.get(i);
            strBuff.append(pc).append("\n");
        }
        return strBuff.toString();
    }

    private String convert2NonSequence(String channelConfig) throws InvalidPropertyException { // KV 06092005 K&P changes
        StringTokenizer strTok = new StringTokenizer(channelConfig, ",: ");
        int[] channelIds = new int[strTok.countTokens()];
        String[] eiServerChannelIds = new String[strTok.countTokens()];
        int highestChannel = 0;

        int channelIndex = 0;
        while (strTok.hasMoreTokens()) {
            String token = strTok.nextToken();
            int rtuChannel;
            try {
                if (mappedChannels) {
                    int dotIndex = token.indexOf(".");
                    rtuChannel = Integer.parseInt(token.substring(0, dotIndex));
                    eiServerChannelIds[channelIndex] = token.substring(dotIndex + 1);
                } else {
                    rtuChannel = Integer.parseInt(token);
                }
            } catch (NumberFormatException e) {
                throw new InvalidPropertyException("The given channelConfig could not be parsed. " + e.getMessage() + " [" + channelConfig + "]");
            }

            channelIds[channelIndex] = rtuChannel;
            highestChannel = channelIds[channelIndex] > highestChannel ? channelIds[channelIndex] : highestChannel;
            channelIndex++;
        }

        int[] channels = new int[highestChannel + 1];
        for (int i = 0; i < channels.length; i++) {
            channels[i] = mappedChannels ? -1 : 0;
        }

        int count = 0;
        for (int i = 0; i < channelIds.length; i++) {
            channels[channelIds[i]] = mappedChannels ? count++ : 1;
        }

        StringBuilder strBuff = new StringBuilder();
        for (int i = 0; i < channels.length; i++) {
            if (mappedChannels) {
                strBuff.append(channels[i] == -1 ? "-1" : eiServerChannelIds[channels[i]]);
                strBuff.append(i < (channels.length - 1) ? "," : "");
            } else {
                strBuff.append(channels[i]);
                strBuff.append(i < (channels.length - 1) ? "," : "");
            }
        }
        return strBuff.toString();
    } // private String convert2NonSequence(String channelConfig)

    public String getChannelRegisterMap() {
        boolean init = true;
        StringBuilder strBuff = new StringBuilder();
        Iterator it = protocolChannels.iterator();
        while (it.hasNext()) {
            if (!init) {
                strBuff.append(":");
            }
            init = false;
            ProtocolChannel protocolChannel = (ProtocolChannel) it.next();
            strBuff.append(protocolChannel.getRegister());
        }
        return strBuff.toString();
    }

    public boolean hasEqualRegisters(ProtocolChannelMap protocolChannelMap) {
        return (getChannelRegisterMap().compareTo(protocolChannelMap.getChannelRegisterMap()) == 0);
    }

    /**
     * Getter for the List of ProtocolChannel's
     * @return
     */
    public List getProtocolChannels() {
        return protocolChannels;
    }

    /**
     * Get the number of protocolchannels in the channelList
     * This method returns the same as getProtocolChannels().size()
     * When the protocolChannels are not initialized, it will return 0.
     *
     * @return
     */
    public int getNrOfProtocolChannels() {
        return protocolChannels != null ? protocolChannels.size() : 0;
    }

    /**
     *
     * @return
     */
    public int getNrOfUsedProtocolChannels() {
        int nrOfUsedProtocolChannels = 0;
        for (int channel = 0; channel < getProtocolChannels().size(); channel++) {
            if (!isProtocolChannelZero(channel))
                nrOfUsedProtocolChannels++;
        }
        return nrOfUsedProtocolChannels;
    }

    /**
     * Save getter for a protocolChannel from the ProtocolChannel list.
     * When the index is out of bounds, we will return a null to prevent a OutOfBoundsException
     * @param index
     * @return
     */
    public ProtocolChannel getProtocolChannel(int index) {
        if (index >= protocolChannels.size()) {
            return null;
        } else {
            return (ProtocolChannel) protocolChannels.get(index);
        }
    }

    /**
     * Check if a given register is captured as a channel
     *
     * @param register
     * @return
     */
    public boolean channelExists(String register) {
        Iterator it = protocolChannels.iterator();
        while (it.hasNext()) {
            ProtocolChannel protocolChannel = (ProtocolChannel) it.next();
            if (protocolChannel.getRegister().compareTo(register) == 0) return true;
        }
        return false;
    }


    /**
     * Internal parser for the channelConfig
     *
     * @param channelConfig
     * @throws InvalidPropertyException
     */
    private void parse(String channelConfig) throws InvalidPropertyException {

        StringTokenizer st1 = new StringTokenizer(channelConfig, ":");
        StringTokenizer st2 = new StringTokenizer(channelConfig, ",");

        if (st1.countTokens() >= st2.countTokens()) {
            while (st1.countTokens() > 0) {
                String strChannel = st1.nextToken();
                protocolChannels.add(new ProtocolChannel(strChannel));
            }
        } else {
            while (st2.countTokens() > 0) {
                String strChannel = st2.nextToken();
                protocolChannels.add(new ProtocolChannel(strChannel));
            }
        }
    }

    /**
     * Check if the given index is a valid channel index according the current channelMap
     * @param index
     * @return
     */
    public boolean isProtocolChannel(int index) {
        if (index >= protocolChannels.size()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isProtocolChannelEnabled(int index) {
        return !isProtocolChannelZero(index);
    }

    public boolean isProtocolChannelZero(int index) {
        if (index >= protocolChannels.size()) {
            return false;
        } else {
            if (getProtocolChannel(index).getRegister().compareTo("0") != 0) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Convert this ChannelMap to a List of ChannelInfo Objects.
     *
     * @return List of ChannelInfo's
     */
    public List<ChannelInfo> toChannelInfoList() {
        List<ChannelInfo> result = new ArrayList<>();
        int channelCount = getNrOfProtocolChannels();
        for (int i = 0; i < channelCount; i++) {
            ChannelInfo ci = getProtocolChannel(i).toChannelInfo(i);
            if (ci != null) {
                result.add(ci);
            }
        }
        return result;
    }

    public boolean isMappedChannels() {
        return mappedChannels;
    }
}