/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LoadSurveyInterpreter.java
 *
 * Created on 11 maart 2004, 11:07
 */

package com.energyict.protocolimpl.pact.core.survey;

import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.protocolimpl.pact.core.common.ChannelMap;
import com.energyict.protocolimpl.pact.core.meterreading.MeterReadingsInterpreter;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public abstract class LoadSurveyInterpreterImpl implements LoadSurveyInterpreter {

    protected static final int DEBUG=0;

    protected abstract void parseData(byte[] data) throws IOException;
    protected abstract int[] doGetEnergyTypeCodes();
    protected abstract int doGetNrOfBlocks(Date from, Date to) throws IOException;
    protected abstract int doGetNrOfDays(Date from, Date to) throws IOException;

    private int[] energyTypeCodes=null;
    private byte[] loadSurveyData;
    private MeterReadingsInterpreter mri;
    private ProfileData profileData=null;
    private ChannelMap channelMap=null;
    private TimeZone timeZone=null;
    private boolean statusFlagChannel=true;
    /** Creates a new instance of MeterReadingBlocks */
    public LoadSurveyInterpreterImpl(MeterReadingsInterpreter mri,TimeZone timeZone) {
        this.timeZone=timeZone;
        this.mri=mri;
        profileData = new ProfileData();
    }

    public void parse(byte[] loadSurveyData, ChannelMap channelMap, boolean statusFlagChannel) throws IOException {
    	if(loadSurveyData != null){
    		this.loadSurveyData=loadSurveyData.clone();
    	}
        this.channelMap=channelMap;
        this.statusFlagChannel=statusFlagChannel;
        if ((loadSurveyData==null) || (loadSurveyData.length == 0)) {
			return;
		}
        parseData(loadSurveyData);
    }

    public byte[] getLoadSurveyData() {
        return loadSurveyData;
    }

    public String toString() {
        return profileData.toString();
    }

    /** Getter for property mri.
     * @return Value of property mri.
     *
     */
    public MeterReadingsInterpreter getMri() {
        return mri;
    }

    /** Setter for property mri.
     * @param mri New value of property mri.
     *
     */
    public void setMri(MeterReadingsInterpreter mri) {
        this.mri = mri;
    }

    /** Getter for property profileData.
     * @return Value of property profileData.
     *
     */
    public com.energyict.mdc.protocol.api.device.data.ProfileData getProfileData() {
        return profileData;
    }

    /** Setter for property profileData.
     * @param profileData New value of property profileData.
     *
     */
    public void setProfileData(com.energyict.mdc.protocol.api.device.data.ProfileData profileData) {
        this.profileData = profileData;
    }

    /** Getter for property channelMap.
     * @return Value of property channelMap.
     *
     */
    public ChannelMap getChannelMap() {
        return channelMap;
    }

    /** Setter for property channelMap.
     * @param channelMap New value of property channelMap.
     *
     */
    public void setChannelMap(ChannelMap channelMap) {
        this.channelMap = channelMap;
    }

    /** Getter for property timeZone.
     * @return Value of property timeZone.
     *
     */
    public java.util.TimeZone getTimeZone() {
        return timeZone;
    }

    /** Setter for property timeZone.
     * @param timeZone New value of property timeZone.
     *
     */
    public void setTimeZone(java.util.TimeZone timeZone) {
        this.timeZone = timeZone;
    }



    public int[] getEnergyTypeCodes() {
        if (energyTypeCodes == null) {
			energyTypeCodes = doGetEnergyTypeCodes();
		}
        return energyTypeCodes;
    }

    public int getEnergyTypeCode(int channel) {
        return getEnergyTypeCodes()[channel];
    }

    public int getNrOfSurveyChannels() {
        if (getEnergyTypeCodes() == null) {
			return 0;
		} else {
			return getEnergyTypeCodes().length;
		}
    }

    public int getNrOfBlocks(Date from, Date to) throws IOException {
        return doGetNrOfBlocks(from,to);
    }
    public int getNrOfDays(Date from, Date to) throws IOException {
        return doGetNrOfDays(from,to);
    }

    /**
     * Getter for property statusFlagChannel.
     * @return Value of property statusFlagChannel.
     */
    public boolean isStatusFlagChannel() {
        return statusFlagChannel;
    }
}
