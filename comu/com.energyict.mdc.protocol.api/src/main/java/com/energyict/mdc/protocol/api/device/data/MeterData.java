/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * MeterData contains Energy information readout from a device
 */
public class MeterData {

    /**
     * A <CODE>List</CODE> of <CODE>ProfileData</CODE> objects which have been read by the protocol
     */
    private List<ProfileData> profileDatas = new ArrayList<ProfileData>();

    /**
     * Contains a information about readout registers.
     */
    private MeterReadingData meterReadingData;

    /**
     * Contains all available <CODE>MeterEvents</CODE> from a device
     */
    private List<MeterProtocolEvent> meterEvents = new ArrayList<>();

    /**
     * Default constructor
     */
    public MeterData() {
    }

    /**
     * Add a <CODE>ProfileData</CODE> object to the existing <CODE>List</CODE>
     *
     * @param data the ProfileData to add
     */
    public void addProfileData(ProfileData data) {
        profileDatas.add(data);
    }

    /**
     * Add a <CODE>List</CODE> of ProfileData objects to the existing <CODE>List</CODE>
     *
     * @param profileDatas the <CODE>ProfileDatas</CODE> to add
     */
    public void addAllProfileData(List<ProfileData> profileDatas) {
        this.profileDatas.addAll(profileDatas);
    }

    /**
     * Setter for the {@link #profileDatas}. (this will replace the already existing list)
     *
     * @param profileDatas the new {@link #profileDatas} to set
     */
    public void setProfileDatas(List<ProfileData> profileDatas) {
        this.profileDatas = profileDatas;
    }

    /**
     * Getter for the {@link #profileDatas}
     *
     * @return the {@link #profileDatas}
     */
    public List<ProfileData> getProfileDatas() {
        return profileDatas;
    }

    /**
     * Getter for the {@link #meterReadingData}
     *
     * @return the {@link #meterReadingData}
     */
    public MeterReadingData getMeterReadingData() {
        return meterReadingData;
    }

    /**
     * Setter for the {@link #meterReadingData}
     *
     * @param meterReadingData the new {@link #meterReadingData} to set
     */
    public void setMeterReadingData(MeterReadingData meterReadingData) {
        this.meterReadingData = meterReadingData;
    }

    /**
     * Getter for the {@link #meterEvents}
     *
     * @return the {@link #meterEvents}
     */
    public List<MeterProtocolEvent> getMeterEvents() {
        return meterEvents;
    }

    /**
     * Setter for the {@link #meterEvents}
     *
     * @param meterEvents the new <CODE>List</CODE> of {@link #meterEvents} to set
     */
    public void setMeterEvents(List<MeterProtocolEvent> meterEvents) {
        this.meterEvents = meterEvents;
    }

    /**
     * Add a given <CODE>MeterEvent</CODE> to the list of MeterEvents
     *
     * @param meterEvent the MeterEvent to add
     */
    public void addMeterEvent(MeterProtocolEvent meterEvent) {
        this.meterEvents.add(meterEvent);
    }
}
