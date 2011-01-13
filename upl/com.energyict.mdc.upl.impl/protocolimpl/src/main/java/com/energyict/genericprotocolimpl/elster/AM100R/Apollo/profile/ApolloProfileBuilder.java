package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.profile;

import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.*;
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
        for (int i = 0; i < getNumberOfChannels(); i++) {
            ChannelInfo channelInfo = new ChannelInfo(i, "DLMS Apollo_EnergyChannel_" + i, getChannelUnit(i));
            channelInfos.add(channelInfo);
        }
        return channelInfos;
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
}
