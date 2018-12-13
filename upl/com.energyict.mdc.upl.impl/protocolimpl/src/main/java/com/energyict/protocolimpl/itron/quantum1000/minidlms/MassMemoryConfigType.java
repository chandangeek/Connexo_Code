/*
 * Result.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MassMemoryConfigType {
    
    private int intervalLength; // UNSIGNED16,
    private int numberOfChannels; // UNSIGNED8,
    private long numberOfIntervalsToStore; // UNSIGNED32,
    private int minimumOutageSecs; //  UNSIGNED16,
    private boolean useGlobalEOISource; // BOOLEAN,
    private boolean runInTestMode; // BOOLEAN,
    private boolean useTOURateStatuses; // BOOLEAN,
    private int TOURateSchedule; // unsigned8,
    private ChannelConfig[] channelConfigs = new ChannelConfig[24];
    private boolean tou;
    
    /** Creates a new instance of Result */
    public MassMemoryConfigType(byte[] data,int offset, boolean tou) throws IOException {
        
        this.setTou(tou);
        setIntervalLength(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setNumberOfChannels(ProtocolUtils.getInt(data,offset++,1));
        setNumberOfIntervalsToStore(ProtocolUtils.getLong(data,offset,4));
        offset+=4;
        setMinimumOutageSecs(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setUseGlobalEOISource(ProtocolUtils.getInt(data,offset++,1) == 1);
        setRunInTestMode(ProtocolUtils.getInt(data,offset++,1) == 1);
        if (tou) {
            setUseTOURateStatuses(ProtocolUtils.getInt(data,offset++,1) == 1);
            setTOURateSchedule(ProtocolUtils.getInt(data,offset++,1));
        }
        for (int i=0;i<getChannelConfigs().length;i++) {
           getChannelConfigs()[i] = new ChannelConfig(data,offset);
           offset+=ChannelConfig.size();
        }
        
    } //  
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryConfigType:\n");
        for (int i=0;i<getChannelConfigs().length;i++) {
            strBuff.append("       channelConfigs["+i+"]="+getChannelConfigs()[i]+"\n");
        }
        strBuff.append("   intervalLength="+getIntervalLength()+"\n");
        strBuff.append("   minimumOutageSecs="+getMinimumOutageSecs()+"\n");
        strBuff.append("   numberOfChannels="+getNumberOfChannels()+"\n");
        strBuff.append("   numberOfIntervalsToStore="+getNumberOfIntervalsToStore()+"\n");
        strBuff.append("   runInTestMode="+isRunInTestMode()+"\n");
        strBuff.append("   useGlobalEOISource="+isUseGlobalEOISource()+"\n");
        if (isTou()) {
            strBuff.append("   useTOURateStatuses="+isUseTOURateStatuses()+"\n");
            strBuff.append("   TOURateSchedule="+getTOURateSchedule()+"\n");
        }
        return strBuff.toString();
    } 
    
    static public int size() {
        return 11 + 24*ChannelConfig.size();
    }
    
    static public int sizeTOU() {
        return 13 + 24*ChannelConfig.size();
    }

    public int getIntervalLength() {
        return intervalLength;
    }

    public void setIntervalLength(int intervalLength) {
        this.intervalLength = intervalLength;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public void setNumberOfChannels(int numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public long getNumberOfIntervalsToStore() {
        return numberOfIntervalsToStore;
    }

    public void setNumberOfIntervalsToStore(long numberOfIntervalsToStore) {
        this.numberOfIntervalsToStore = numberOfIntervalsToStore;
    }

    public int getMinimumOutageSecs() {
        return minimumOutageSecs;
    }

    public void setMinimumOutageSecs(int minimumOutageSecs) {
        this.minimumOutageSecs = minimumOutageSecs;
    }

    public boolean isUseGlobalEOISource() {
        return useGlobalEOISource;
    }

    public void setUseGlobalEOISource(boolean useGlobalEOISource) {
        this.useGlobalEOISource = useGlobalEOISource;
    }

    public boolean isRunInTestMode() {
        return runInTestMode;
    }

    public void setRunInTestMode(boolean runInTestMode) {
        this.runInTestMode = runInTestMode;
    }

    public boolean isUseTOURateStatuses() {
        return useTOURateStatuses;
    }

    public void setUseTOURateStatuses(boolean useTOURateStatuses) {
        this.useTOURateStatuses = useTOURateStatuses;
    }

    public int getTOURateSchedule() {
        return TOURateSchedule;
    }

    public void setTOURateSchedule(int TOURateSchedule) {
        this.TOURateSchedule = TOURateSchedule;
    }

    public ChannelConfig[] getChannelConfigs() {
        return channelConfigs;
    }

    public void setChannelConfigs(ChannelConfig[] channelConfigs) {
        this.channelConfigs = channelConfigs;
    }

    public boolean isTou() {
        return tou;
    }

    public void setTou(boolean tou) {
        this.tou = tou;
    }
    

    
}
