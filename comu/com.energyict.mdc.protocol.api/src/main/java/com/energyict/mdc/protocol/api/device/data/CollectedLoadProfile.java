package com.energyict.mdc.protocol.api.device.data;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;

import java.util.List;

/**
 * A CollectedLoadProfile identifies a load profile
 * (by {@link #getLoadProfileIdentifier()}), the respective collected {@link IntervalData}
 * starting from the load profile's last reading
 * and the corresponding {@link ChannelInfo}.
 */
public interface CollectedLoadProfile extends CollectedData {

    /**
     * @return the collected {@link IntervalData} since the LoadProfile's last reading.
     */
    public List<IntervalData> getCollectedIntervalData();

    /**
     * @return the channel configuration of the collected {@link IntervalData}
     */
    public List<ChannelInfo> getChannelInfo();

    /**
     * @return true if <b>all</b> {@link IntervalData}, <i>including those before the LoadProfile's last reading</i>,
     *         should be stored.
     *         If set to false, <b>only</b> {@link IntervalData} after the LoadProfile's last reading will be stored.
     */
    public boolean doStoreOlderValues();

    /**
     * Should provide an identifier to uniquely identify the requested LoadProfile.
     *
     * @return the {@link LoadProfileIdentifier loadProfileIdentifier}
     *         of the BusinessObject which is actionHolder of the request
     */
    public LoadProfileIdentifier getLoadProfileIdentifier();

    /**
     * Set all collected device information
     *
     * @param collectedIntervalData the collected list of <code>IntervalData</code>
     * @param deviceChannelInfo     the corresponding list of <code>ChannelInfo</code>
     */
    public void setCollectedData(List<IntervalData> collectedIntervalData, List<ChannelInfo> deviceChannelInfo);

    /**
     * Should provide the {@link Interval} for which {@link IntervalData} is collected.
     *
     * @return The Interval
     */
    public Interval getCollectedIntervalDataRange();

}