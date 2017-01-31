/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.elster.jupiter.metering.ReadingQualityType;
import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class represents the profile data read from a device.
 * It supports both interval data and event data.
 *
 * @author Karel, Koenraad
 *         </p>
 *         Changes:
 *         KV 23072004 add method markIntervalsAsBadTime
 */
public class ProfileData implements java.io.Serializable {

    /**
     * The id of the LoadProfile.
     */
    private long loadProfileId;

    /**
     * List of events we received from the meter
     */
    private List<MeterEvent> meterEvents = new ArrayList<>();

    /**
     * List of IntervalDatas we received from the meter
     */
    private List<IntervalData> intervalDatas = new ArrayList<>();

    /**
     * List of ChannelInfos related to the intervals we received from the meter
     */
    private List<ChannelInfo> channelInfos = new ArrayList<>();

    /**
     * Indication whether all {@link #intervalDatas}, including those before the device's last reading, should be stored.
     * If set to false, all IntervalData before the device's last reading will not be stored.
     */
    private boolean storeOlderValues = false;

    /**
     * <p></p>
     */
    public ProfileData() {
    }

    /**
     * Constructor which takes the {@link #loadProfileId} into account
     *
     * @param loadProfileId the id of the LoadProfile of this ProfileData.
     */
    public ProfileData(long loadProfileId) {
        this.loadProfileId = loadProfileId;
    }

    public long getLoadProfileId() {
        return loadProfileId;
    }

    public void setLoadProfileId(long loadProfileId) {
        this.loadProfileId = loadProfileId;
    }


    /**
     * Mark all IntervalData objects in the IntervalData objects List with IntervalStateBits.BADTIME
     */
    public void markIntervalsAsBadTime() {
        for (IntervalData intervalData : intervalDatas) {
            intervalData.addEiStatus(IntervalStateBits.BADTIME);
        }
    }

    /**
     * Getter for the ChannelInfo objects List
     *
     * @return List
     */
    public List<ChannelInfo> getChannelInfos() {
        return channelInfos;
    }

    /**
     * Setter for the ChannelInfo objects List
     *
     * @param channelInfos List
     */
    public void setChannelInfos(List<ChannelInfo> channelInfos) {
        this.channelInfos = channelInfos;
    }

    /**
     * Getter for the MeterEvent objects List
     *
     * @return List
     */
    public List<MeterEvent> getMeterEvents() {
        return meterEvents;
    }

    /**
     * Setter for the MeterEvent objects List
     *
     * @param meterEvents List
     */
    public void setMeterEvents(List<MeterEvent> meterEvents) {
        this.meterEvents = meterEvents;
    }

    /**
     * Getter for the IntervalData objects List
     *
     * @return List
     */
    public List<IntervalData> getIntervalDatas() {
        return intervalDatas;
    }

    /**
     * Setter for the IntervalData objects List
     *
     * @param intervalDatas List
     */
    public void setIntervalDatas(List<IntervalData> intervalDatas) {
        this.intervalDatas = intervalDatas;
    }


    /**
     * Sort IntervalData and MeterEvents in acsending order using the Date.
     */
    public void sort() {
        Collections.sort(intervalDatas);
        Collections.sort(meterEvents);
    }

    /**
     * <p></p>
     *
     * @return number of logical channels
     */
    public int getNumberOfChannels() {
        return channelInfos.size();
    }

    /**
     * <p></p>
     *
     * @return the number of interval data records
     */
    public int getNumberOfIntervals() {
        return intervalDatas.size();
    }

    /**
     * <p></p>
     *
     * @return the number of events
     */
    public int getNumberOfEvents() {
        return meterEvents.size();
    }

    /**
     * adds a channelInfo to the receiver
     *
     * @param channelInfo <br>
     */
    public void addChannel(ChannelInfo channelInfo) {
        if (channelInfo.getId() != getNumberOfChannels()) {
            throw new IndexOutOfBoundsException();
        }
        channelInfos.add(channelInfo);
    }

    /**
     * adds an interval to the receiver
     *
     * @param interval <br>
     */
    public void addInterval(IntervalData interval) {
        if (interval.getValueCount() != getNumberOfChannels()) {
            throw new IndexOutOfBoundsException();
        }
        intervalDatas.add(interval);
    }

    /**
     * adds an event to the receiver
     *
     * @param event <br>
     */
    public void addEvent(MeterEvent event) {
        meterEvents.add(event);
    }

    /**
     * <p></p>
     *
     * @return an iterator over the receiver's ChannelInfo's
     */
    public ListIterator<ChannelInfo> getChannelIterator() {
        return channelInfos.listIterator();
    }

    /**
     * <p></p>
     *
     * @return an iterator over the receiver's intervalDatas
     */
    public ListIterator<IntervalData> getIntervalIterator() {
        return intervalDatas.listIterator();
    }

    /**
     * <p></p>
     *
     * @return an iterator over the receiver's events
     */
    public ListIterator<MeterEvent> getEventIterator() {
        return meterEvents.listIterator();
    }

    /**
     * <p></p>
     *
     * @param index channel index (zero based)
     * @return the channelinfo for the given index
     */
    public ChannelInfo getChannel(int index) {
        return channelInfos.get(index);
    }

    /**
     * <p></p>
     *
     * @param index intervalData index (zero based)
     * @return the intervalData at the given index
     */
    public IntervalData getIntervalData(int index) {
        return intervalDatas.get(index);
    }

    /**
     * <p></p>
     *
     * @param index the meterevent index (zero based)
     * @return the MeterEvent at the given index
     */
    public MeterEvent getEvent(int index) {
        return meterEvents.get(index);
    }

    /**
     * <p> Set the interval status based on the information contained in the meter
     * events.
     * </p><p>
     * It is the responsible of the MeterProtocol implementer to provide the correct
     * interval status. MeterProtocol implementers can use this utility method to update
     * the interval status based on the collected events.
     * </p>
     *
     * @param interval interval length in minutes
     */
    public void applyEvents(int interval) {
        Iterator<MeterEvent> eventIterator = getEventIterator();
        while (eventIterator.hasNext()) {
            applyEvent(eventIterator.next(), interval);
        }
    }

    /**
     * <p>Updates the interval status based on the information of a single event.
     * </p>
     *
     * @param event    <br>
     * @param interval interval length in minutes
     */
    protected void applyEvent(MeterEvent event, int interval) {
        Iterator<IntervalData> intervalIterator = getIntervalIterator();
        while (intervalIterator.hasNext()) {
            intervalIterator.next().apply(event, interval);
        }
    }

    /**
     * <p>Generate meter events based on the interval status.
     * </p><p>
     * This method is provided as a service for those protocols that
     * do not support events, but have interval statusses. It is the
     * responsibility of the MeterProtocol implementer to call this
     * method if needed
     */
    public void generateEvents() {
        Iterator<IntervalData> intervalIterator = getIntervalIterator();
        while (intervalIterator.hasNext()) {
            meterEvents.addAll(intervalIterator.next().generateEvents());
        }
    }

    /**
     * Returns a representation of this profiledata's values as a string
     *
     * @return String
     */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        int i, iNROfChannels = getNumberOfChannels();
        int t, iNROfIntervals = getNumberOfIntervals();
        int z, iNROfEvents = getNumberOfEvents();
        stringBuilder.append("Channels: ").append(iNROfChannels).append("\n");
        stringBuilder.append("Intervals par channel: ").append(iNROfIntervals).append("\n");
        for (t = 0; t < iNROfIntervals; t++) {
            IntervalData intervalData = getIntervalData(t);
            stringBuilder.append(" Interval ").append(t).append(" endtime = ").append(intervalData.getEndTime()).append("\n");
            for (i = 0; i < iNROfChannels; i++) {

                StringBuilder readingQualitiesDescription = new StringBuilder();
                for (ReadingQualityType readingQualityType : intervalData.getReadingQualityTypes()) {
                    if (readingQualitiesDescription.length() > 0) {
                        readingQualitiesDescription.append(", ");
                    }
                    readingQualitiesDescription.append(readingQualityType.getCode());
                }

                stringBuilder.append("Channel ")
                        .append(i)
                        .append(" Interval ")
                        .append(t)
                        .append(" = ")
                        .append(intervalData.get(i)
                                .doubleValue())
                        .append(" ")
                        .append(intervalData.getEiStatusTranslation(i))
                        .append(" ")
                        .append(readingQualitiesDescription)
                        .append(" ")
                        .append(getChannel(i).getUnit())
                        .append(" ")
                        .append(getChannel(i).getScaler())
                        .append(" ")
                        .append(getChannel(i).isCumulative())
                        .append("\n");
            }
        }
        stringBuilder.append("Events in profiledata: ").append(iNROfEvents).append("\n");
        for (z = 0; z < iNROfEvents; z++) {
            stringBuilder.append("Event ")
                    .append(z)
                    .append(" = ")
                    .append(getEvent(z).getEiCode())
                    .append(", ")
                    .append(getEvent(z).getProtocolCode())
                    .append(" at ")
                    .append(getEvent(z).getTime());
            if (getEvent(z).getMessage() != null) {
                stringBuilder.append(", ").append(getEvent(z).getMessage());
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();

    }

    /**
     * @return value of {@link #storeOlderValues}
     */
    public boolean shouldStoreOlderValues() {
        return storeOlderValues;
    }

    public void setStoreOlderValues(final boolean storeOlderValues) {
        this.storeOlderValues = storeOlderValues;
    }

}