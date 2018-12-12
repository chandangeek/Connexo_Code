/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.google.common.collect.Range;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MockDeviceLoadProfile implements CollectedLoadProfile {

    /**
     * The unique identifier of the LoadProfile for this collected data
     */
    private final LoadProfileIdentifier loadProfileIdentifier;

    private List<Issue> issues;

    /**
     * The collected intervals for the LoadProfile
     */
    private List<IntervalData> collectedIntervalData;
    private Range<Instant> collectedIntervalDataRange = Range.all();

    /**
     * The <code>ChannelInfo</code> corresponding with the {@link #collectedIntervalData}
     */
    private List<ChannelInfo> deviceChannelInfo;

    private boolean doStoreOlderValues = false;

    /**
     * Indication whether the collected intervalData may be incomplete or not. <br/>
     * IntervalData is considered incomplete in case the intervalData contains only data for part of the channels
     * of the corresponding LoadProfile
     * <ul>
     * <li>false: the collected intervalData should be complete, thus there should be data present for <b>all channels</b> of the corresponding loadProfile</li>
     * <li>true: the collected intervalData may be incomplete; interval data can be missing for some of the channels of the corresponding loadProfile<br/>
     * E.g.: the loadProfile is defined in EIMaster with 32 channels, but the collected intervalData only contains data for 4 channels</li>
     * </ul>
     * <b>Remark:</b> By default this is set to <i>false</i>, <i>true</i> should only be used in specific cases (such as EIWeb)!
     */
    private boolean allowIncompleteLoadProfileData = false;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public MockDeviceLoadProfile() {
        this.loadProfileIdentifier = null;
    }

    /**
     * Default constructor
     *
     * @param loadProfileIdentifier the identifier for this collected data
     */
    public MockDeviceLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        super();
        this.loadProfileIdentifier = loadProfileIdentifier;
    }

    @Override
    public ResultType getResultType() {
        return ResultType.Supported;
    }

    @Override
    public List<Issue> getIssues() {
        if (issues == null) {
            issues = new ArrayList<>();
        }
        return issues;
    }

    @Override
    public void setFailureInformation(ResultType resultType, Issue issue) {
        getIssues().add(issue);
    }

    @Override
    public void setFailureInformation(ResultType resultType, List<Issue> issues) {
        getIssues().addAll(issues);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
        return comTask.isConfiguredToCollectLoadProfileData();
    }

    @Override
    @XmlAttribute
    public List<IntervalData> getCollectedIntervalData() {
        if (this.collectedIntervalData == null) {
            return Collections.emptyList();
        }
        return collectedIntervalData;
    }

    @XmlAttribute
    public Range<Instant> getCollectedIntervalDataRange() {
        return collectedIntervalDataRange;
    }

    /**
     * @return the channel configuration of the collected {@link com.energyict.protocol.IntervalData}
     */
    @Override
    @XmlAttribute
    public List<ChannelInfo> getChannelInfo() {
        if (this.deviceChannelInfo == null) {
            return Collections.emptyList();
        }
        return deviceChannelInfo;
    }

    /**
     * Setter for xml unmarshalling purposes only
     */
    public void setChannelInfo(List<ChannelInfo> deviceChannelInfo) {
        this.deviceChannelInfo = deviceChannelInfo;
    }

    @Override
    @XmlAttribute
    public boolean isDoStoreOlderValues() {
        return doStoreOlderValues;
    }

    public void setDoStoreOlderValues(final boolean doStoreOlderValues) {
        this.doStoreOlderValues = doStoreOlderValues;
    }

    @Override
    @XmlAttribute
    public boolean isAllowIncompleteLoadProfileData() {
        return allowIncompleteLoadProfileData;
    }

    public void setAllowIncompleteLoadProfileData(boolean allowIncompleteLoadProfileData) {
        this.allowIncompleteLoadProfileData = allowIncompleteLoadProfileData;
    }

    @Override
    @XmlAttribute
    public LoadProfileIdentifier getLoadProfileIdentifier() {
        return this.loadProfileIdentifier;
    }

    /**
     * Set all collected device information
     *
     * @param collectedIntervalData the collected list of <code>IntervalData</code>
     * @param deviceChannelInfo     the corresponding list of <code>ChannelInfo</code>
     */
    @Override
    public void setCollectedIntervalData(final List<IntervalData> collectedIntervalData, final List<ChannelInfo> deviceChannelInfo) {
        this.collectedIntervalData = collectedIntervalData;
        this.collectedIntervalDataRange = this.calculateCollectedIntervalDataRange(collectedIntervalData);
        this.deviceChannelInfo = deviceChannelInfo;
    }

    private Range<Instant> calculateCollectedIntervalDataRange(List<IntervalData> collectedIntervalData) {
        Date fromDate = null;
        Date toDate = null;
        for (IntervalData intervalData : collectedIntervalData) {
            fromDate = this.min(fromDate, intervalData.getEndTime());
            toDate = this.max(toDate, intervalData.getEndTime());
        }
        if (fromDate == null) {
            if (toDate == null) {
                return Range.all();
            } else {
                return Range.atMost(toDate.toInstant());
            }
        } else {
            if (toDate == null) {
                return Range.atLeast(fromDate.toInstant());
            } else {
                return Range.closed(fromDate.toInstant(), toDate.toInstant());
            }
        }
    }

    private Date min(Date currentMinimum, Date candidate) {
        if (currentMinimum == null || candidate.before(currentMinimum)) {
            return candidate;
        } else {
            return currentMinimum;
        }
    }

    private Date max(Date currentMaximum, Date candidate) {
        if (currentMaximum == null || candidate.after(currentMaximum)) {
            return candidate;
        } else {
            return currentMaximum;
        }
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}