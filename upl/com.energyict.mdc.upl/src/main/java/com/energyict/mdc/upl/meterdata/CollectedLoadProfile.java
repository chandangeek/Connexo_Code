package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.google.common.collect.Range;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.util.List;

/**
 * A CollectedLoadProfile identifies a {@link LoadProfile}
 * (by {@link #getLoadProfileIdentifier()}), the respective collected {@link IntervalData}
 * starting from the {@link LoadProfile#getLastReading() lastReading}
 * and the corresponding {@link ChannelInfo}.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface CollectedLoadProfile extends CollectedData {

    /**
     * @return the collected {@link IntervalData} since {@link LoadProfile#getLastReading()}
     */
    @XmlAttribute
    List<IntervalData> getCollectedIntervalData();

    /**
     * @return the channel configuration of the collected {@link IntervalData}
     */
    @XmlAttribute
    List<ChannelInfo> getChannelInfo();

    /**
     * @return true if <b>all</b> {@link IntervalData}, <i>including those before {@link LoadProfile#getLastReading()}</i>, should be stored.<br/>
     * If set to false, <b>only</b> {@link IntervalData} after {@link LoadProfile#getLastReading()} will be stored.
     */
    @XmlAttribute
    boolean isDoStoreOlderValues();

    /**
     * Only set the value to <b>true</b> if the collectedIntervalData contains data before the
     * lastReading of the profile which you want to store.<br>
     * Default value is set to <b>FALSE</b>, so no data is overwritten.
     *
     * @param doStoreOlderValues the indication whether to store older values
     */
    void setDoStoreOlderValues(final boolean doStoreOlderValues);

    /**
     * Indication whether the collected intervalData may be incomplete or not. <br/>
     * IntervalData is considered incomplete in case the intervalData contains only data for part of the channels
     * of the corresponding LoadProfile<br/>
     *
     * @return <li>false: the collected intervalData should be complete, thus there should be data present for <b>all channels</b> of the corresponding loadProfile. This is the default option</li>
     * <li>true: the collected intervalData may be incomplete; interval data can be missing for some of the channels of the corresponding loadProfile<br/>
     * E.g.: the loadProfile is defined in EIMaster with 32 channels, but the collected intervalData only contains data for 4 channels<br/>
     * <b>Remark:</b> By default this is set to <i>false</i>, <i>true</i> should only be used in specific cases (such as inbound EIWeb)!</li>
     * </ul>
     */
    @XmlAttribute
    boolean isAllowIncompleteLoadProfileData();

    /**
     * Set the value to <b>true</b> to allow incomplete intervalData <br/>
     * IntervalData is considered incomplete in case the intervalData contains only data for part of the channels
     * of the corresponding LoadProfile
     */
    void setAllowIncompleteLoadProfileData(boolean allowIncompleteLoadProfileData);

    /**
     * Should provide an identifier to uniquely identify the requested LoadProfile.
     *
     * @return the {@link LoadProfileIdentifier loadProfileIdentifier}
     * of the BusinessObject which is actionHolder of the request
     */
    @XmlAttribute
    LoadProfileIdentifier getLoadProfileIdentifier();

    /**
     * Set all collected device information
     *
     * @param collectedIntervalData the collected list of <code>IntervalData</code>
     * @param deviceChannelInfo     the corresponding list of <code>ChannelInfo</code>
     */
    void setCollectedIntervalData(List<IntervalData> collectedIntervalData, List<ChannelInfo> deviceChannelInfo);

    /**
     * Gets the time period for which {@link IntervalData} is collected.
     *
     * @return the time period
     */
    @XmlAttribute
    Range<Instant> getCollectedIntervalDataRange();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}