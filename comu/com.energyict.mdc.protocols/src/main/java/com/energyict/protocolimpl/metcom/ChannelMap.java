/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ChannelMap.java
 *
 * Created on 18 maart 2004, 10:07
 */

package com.energyict.protocolimpl.metcom;

import java.util.StringTokenizer;

/**
 *
 * @author  Koen
 *
 * E.g. 4+,5,7 --> 3 buffers, 4 channels cumulative, 5 channels and 7 channels
 */
public class ChannelMap {
    int nrOfChannels;
    int nrOfBuffers;
    int nrOfUsedBuffers;
    int[] buffers;
    boolean[] cumulative;
    /** Creates a new instance of ChannelMap */
    public ChannelMap(String channelMapStr) {
        nrOfUsedBuffers=0;
        StringTokenizer strTok = new StringTokenizer(channelMapStr,",");
        setNrOfChannels(strTok.countTokens());
        setNrOfBuffers(strTok.countTokens());
        buffers = new int[getNrOfBuffers()];
        cumulative = new boolean[getNrOfBuffers()];
        for(int i=0;i<getNrOfBuffers();i++) {
            String tok = strTok.nextToken();
            if (tok.indexOf('+') >=0) {
                cumulative[i] = true;
                tok = tok.substring(0,tok.indexOf('+'));
            }
            else
                cumulative[i] = false;
            buffers[i] = Integer.parseInt(tok);
            if (buffers[i]==0) buffers[i]=1;
            if (buffers[i]>0) nrOfUsedBuffers++;
        }
    }

    public boolean isBufferCumulative(int i) {
       return cumulative[i];
    }

    public boolean useBuffer(int i) {
        return (getBuffers()[i] > 0);
    }

    public int getTotalNrOfChannels() {
        int total=0;
        for(int i=0;i<getNrOfBuffers();i++)
            if (getBuffers()[i] > 0)
               total+=getBuffers()[i];
        return total;
    }

    /** Getter for property nrOfChannels.
     * @return Value of property nrOfChannels.
     *
     */
    public int getNrOfChannels() {
        return nrOfChannels;
    }

    /** Setter for property nrOfChannels.
     * @param nrOfChannels New value of property nrOfChannels.
     *
     */
    public void setNrOfChannels(int nrOfChannels) {
        this.nrOfChannels = nrOfChannels;
    }

    /**
     * Getter for property nrOfBuffers.
     * @return Value of property nrOfBuffers.
     */
    public int getNrOfBuffers() {
        return nrOfBuffers;
    }



    /**
     * Setter for property nrOfBuffers.
     * @param nrOfBuffers New value of property nrOfBuffers.
     */
    public void setNrOfBuffers(int nrOfBuffers) {
        this.nrOfBuffers = nrOfBuffers;
    }

    /**
     * Getter for property buffers.
     * @return Value of property buffers.
     */
    public int[] getBuffers() {
        return this.buffers;
    }

    /**
     * Setter for property buffers.
     * @param buffers New value of property buffers.
     */
    public void setBuffers(int[] buffers) {
        this.buffers = buffers;
    }

    /**
     * Getter for property nrOfUsedBuffers.
     * @return Value of property nrOfUsedBuffers.
     */
    public int getNrOfUsedBuffers() {
        return nrOfUsedBuffers;
    }

    /**
     * Setter for property nrOfUsedBuffers.
     * @param nrOfUsedBuffers New value of property nrOfUsedBuffers.
     */
    public void setNrOfUsedBuffers(int nrOfUsedBuffers) {
        this.nrOfUsedBuffers = nrOfUsedBuffers;
    }

}
