package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.profile;

import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.*;
import com.energyict.mdw.core.Channel;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.*;

/**
 * The ApolloProfileBuilder contains functionality to construct a proper {@link com.energyict.protocol.ProfileData} object for the ComServer
 * <br/>
 * Copyrights EnergyICT<br/>
 * Date: 26-nov-2010<br/>
 * Time: 14:48:11<br/>
 */
public class ApolloProfileBuilder {

    /**
     * The used {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloMeter}
     */
    private final ApolloMeter meterProtocol;
    /**
     * The used {@link com.energyict.dlms.cosem.ProfileGeneric} DLMS Object
     */
    private final ProfileGeneric profileGeneric;
    /**
     * A list of ScalerUnits from the channels from the {@link #profileGeneric}
     */
    private ScalerUnit[] scalerUnits;
    /**
     * The number of useful Energy Channels
     */
    private int numberOfChannels = -1;

    /**
     * Default constructor
     *
     * @param meterProtocol  the used ApolloMeter protocol
     * @param profileGeneric the {@link #profileGeneric} to use for this builder
     */
    public ApolloProfileBuilder(ApolloMeter meterProtocol, ProfileGeneric profileGeneric) {
        this.meterProtocol = meterProtocol;
        this.profileGeneric = profileGeneric;
    }

    /**
     * Construct the list of {@link com.energyict.protocol.ChannelInfo} objects
     *
     * @return the list of ChannelInfos
     * @throws IOException if either the number of channels OR the array of scalerUnits could not be correctly fetched/build
     */
    public List<ChannelInfo> getChannelInfos() throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        int channelIndex = -1;
        for (int i = 0; i < getNumberOfChannels(); i++) {
            channelIndex = getChannelNumber(i, this.profileGeneric.getCapturePeriod());
            if(channelIndex != -1){
                ChannelInfo channelInfo = new ChannelInfo(i, channelIndex, "DLMS Apollo_EnergyChannel_" + i, getChannelUnit(i));
                channelInfo.setCumulative();
                channelInfos.add(channelInfo);
            }
        }
        return channelInfos;
    }

    /**
     * Calculates the channelNumber for the given profileIndex
     *
     * @param index             the profileIndex
     * @param intervalInSeconds the interval of the channel
     * @return the calculated channelIndex
     */
    private int getChannelNumber(final int index, int intervalInSeconds) {
        int channelIndex = 0;

        if (intervalInSeconds == 0) {
            intervalInSeconds = 31 * 86400;
        }

        for (int i = 0; i < this.meterProtocol.getMeter().getChannels().size(); i++) {
            if (this.meterProtocol.getMeter().getChannel(i).getInterval().getSeconds() == intervalInSeconds) {
                if (channelIndex == index) {
                    return this.meterProtocol.getMeter().getChannel(i).getLoadProfileIndex() - 1;
                }
                channelIndex++;
            }
        }
        return -1;
    }

    /**
     * Get the Unit for the given channelIndex
     *
     * @param index the index of the Energy channels
     * @return the requested Unit
     * @throws IOException if the scalerUnit array could not be builder
     */
    protected Unit getChannelUnit(int index) throws IOException {
        return getScalerUnits()[index].getUnit();
    }

    /**
     * Get the ScalerUnits for each Energy channel of the {@link #profileGeneric}
     *
     * @return an array of ScalerUnits
     * @throws IOException if an error occurs during the read of one of the scalers or units
     */
    protected ScalerUnit[] getScalerUnits() throws IOException {
        if (this.scalerUnits == null) {
            this.scalerUnits = new ScalerUnit[getNumberOfChannels()];
            int counter = 0;
            for (CapturedObject co : profileGeneric.getCaptureObjects()) {
                if (ProfileUtils.isChannelData(co)) {
                    try {
                        this.scalerUnits[counter] = this.meterProtocol.getApolloObjectFactory().getCosemObject(co.getLogicalName().getObisCode(), co.getClassId()).getScalerUnit();
                    } catch (IOException e) {
                        this.meterProtocol.getLogger().info("Could not fetch the scalerUnit from channel [" + co + "]. Data can not be correctly constructed.");
                        throw e;
                    }
                    counter++;
                }
            }
        }
        return this.scalerUnits;
    }

    /**
     * Getter for the number of Energy channels in the {@link #profileGeneric}
     *
     * @return the number of Energy channels
     * @throws IOException if the CapturedObjects could not be fetched
     */
    public int getNumberOfChannels() throws IOException {
        if (this.numberOfChannels == -1) {
            this.numberOfChannels = 0;
            for (CapturedObject co : profileGeneric.getCaptureObjects()) {
                if (ProfileUtils.isChannelData(co)) {
                    numberOfChannels++;
                }
            }
        }
        return this.numberOfChannels;
    }

    /**
     * Fetch the intervalList from the device
     *
     * @param fromCalendar the time to start reading from
     * @param toCalendar   the last interval to read
     * @return a list of IntervalData objects to put in the ProfileData object
     * @throws IOException
     */
    public List<IntervalData> getIntervalList(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        DLMSProfileIntervals intervals = new DLMSProfileIntervals(profileGeneric.getBufferData(fromCalendar, toCalendar), new ApolloProfileIntervalStatusBits());
        return intervals.parseIntervals(profileGeneric.getCapturePeriod());
    }

    /**
     * Get the lastReading of the profile. Use the ProfileGeneric interval to search for the channels of this loadProfile
     *
     * @return the date of the last stored entry for the profile with the given interval
     */
    public Date getLastProfileDate() throws IOException {
        Date lastDate = new Date();
        List<Channel> meterChannels = this.meterProtocol.getMeter().getChannels();
        for (Channel channel : meterChannels) {
            if (channel.getIntervalInSeconds() == this.profileGeneric.getCapturePeriod()) {
                Date channelLastReading = channel.getLastReading();
                if (channelLastReading == null) {
                    channelLastReading = ParseUtils.getClearLastMonthDate(this.meterProtocol.getMeter());
                }
                if (channelLastReading.before(lastDate)) {
                    lastDate = channelLastReading;
                }
            }
        }
        return lastDate;
    }
}
