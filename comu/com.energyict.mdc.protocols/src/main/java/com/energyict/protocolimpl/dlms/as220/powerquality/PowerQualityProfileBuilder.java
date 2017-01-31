/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.powerquality;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.generic.StatusCodeProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * TODO allot has to be done to parse this content, waiting for the new firmware so the ObisCode is correct
 * <p/>
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 11-okt-2010
 * Time: 13:05:21
 * To change this template use File | Settings | File Templates.
 */
public class PowerQualityProfileBuilder {

    private static final int STATUS_MISSING_VALUE = 0x10000000;
    private final PowerQuality powerQuality;


    /**
     * Static ObisCode for the Status
     */
    private final static ObisCode OBISCODE_STATUS = ObisCode.fromString("0.0.96.10.1.255");

    /**
     * Represents the number of channels of the profile
     */
    private int nbOfChannels = -1;

    /**
     * Represents the interval of the PowerQualityLoadProfile
     */
    private int profileInterval = -1;

    /**
     * The used list of {@link com.energyict.dlms.cosem.CapturedObject}s
     */
    private List<CapturedObject> capturedObjects;

    public PowerQualityProfileBuilder(PowerQuality powerQuality) {
        this.powerQuality = powerQuality;
    }

    public ScalerUnit[] buildScalerUnits() throws IOException {
        ScalerUnit[] scalerUnits = new ScalerUnit[getNrOfChannels()];

        int index = 0;
        for (CapturedObject capturedObject : getCapturedObjects()) {
            ObisCode obis = capturedObject.getLogicalName().getObisCode();
            if (obis.getA() != 0) {
                if (index <= getNrOfChannels()) {
                    scalerUnits[index] = this.powerQuality.getAs220().getCosemObjectFactory().getCosemObject(obis).getScalerUnit();
                    index++;
                } else {
                    throw new IOException("There are more channels in the captured objects [ PowerQualityLoadProfile ] than needed [" + getNrOfChannels() + "].");
                }
            }
        }

        return scalerUnits;
    }

    public List<ChannelInfo> buildChannelInfos(ScalerUnit[] scalerunit) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (int i = 0; i < scalerunit.length; i++) {
            ChannelInfo channelInfo = new ChannelInfo(i, "dlms" + this.powerQuality.getAs220().getDeviceID() + "_PQ-channel_" + i, scalerunit[i].getEisUnit());
            channelInfos.add(channelInfo);
        }
        return channelInfos;
    }

    public List<IntervalData> buildIntervalData(DataContainer dc, AS220 as220) throws IOException {
        List<IntervalData> intervalList = new ArrayList<IntervalData>();
        Calendar cal = null;
        IntervalData currentInterval = null;
        int profileStatus = 0;
        if (dc.getRoot().getElements().length != 0) {

            for (int i = 0; i < dc.getRoot().getElements().length; i++) {
                if (dc.getRoot().getStructure(i).isOctetString(0)) {
                    OctetString octetString = OctetString.fromByteArray(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex()).getArray());
                    cal = new AXDRDateTime(octetString.getBEREncodedByteArray(), 0, as220.getTimeZone()).getValue();
                } else {
                    if (cal != null) {
                        cal.add(Calendar.SECOND, getProfileInterval());
                    }
                }
                if (cal != null) {

                    if (getProfileStatusChannelIndex() != -1) {
                        profileStatus = profileStatus | dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex());
                    }

                    currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus);
                    if (currentInterval != null) {
                        IntervalValue intervalValue = (IntervalValue) currentInterval.getIntervalValues().get(0);
                        if (intervalValue.getNumber().intValue() != STATUS_MISSING_VALUE) {             //Don't add this value
                            if (ParseUtils.isOnIntervalBoundary(cal, as220.getProfileInterval())) {     //Don't add values with invalid timestamps
                        intervalList.add(currentInterval);
                    }
                            profileStatus = 0; //Reset, else keep the status for the next interval
                }
            }
                }
            }
        } else {
            this.powerQuality.getAs220().getLogger().info("No entries in PowerQualityLoadProfile");
        }
        return intervalList;
    }

    protected IntervalData getIntervalData(DataStructure ds, Calendar cal, int status) throws IOException {

        IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
        try {
            for (int i = 0; i < getCapturedObjects().size(); i++) {
                if (!isProfileStatusObisCode(getCapturedObjects().get(i).getLogicalName().getObisCode()) &&
                        !isClockObisCode(getCapturedObjects().get(i).getLogicalName().getObisCode())) {
                    id.addValue(new Integer(ds.getInteger(i)));
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to parse the intervalData objects form the datacontainer.");
        }

        return id;
    }

    protected boolean isProfileStatusObisCode(final ObisCode oc) throws IOException {
        return oc.equals(OBISCODE_STATUS);
    }

    protected boolean isClockObisCode(final ObisCode oc) throws IOException {
        return oc.equals(this.powerQuality.getAs220().getMeterConfig().getClockObject().getObisCode());
    }

    protected int getProfileClockChannelIndex() throws IOException {
        try {
            for (int i = 0; i < getCapturedObjects().size(); i++) {
                if (isClockObisCode(getCapturedObjects().get(i).getLogicalName().getObisCode())) {
                    return i;
                }
            }
        } catch (IOException e) {
            throw new IOException("Could not retrieve the index of the profileData's clock attribute.");
        }
        return -1;
    }

    protected int getProfileStatusChannelIndex() throws IOException {
        try {
            for (int i = 0; i < getCapturedObjects().size(); i++) {
                if (isProfileStatusObisCode(((CapturedObject) (getCapturedObjects().get(i))).getLogicalName().getObisCode())) {
                    return i;
                }
            }
        } catch (IOException e) {
            throw new IOException("Could not retrieve the index of the profileData's status attribute.");
        }
        return -1;
    }

    /**
     * Get the number of channels
     *
     * @return the number of channels
     * @throws IOException when an error occurred during the reading of the captured objects, which are necessary for calculating the number
     */
    public int getNrOfChannels() throws IOException {
        if (this.nbOfChannels == -1) {

            nbOfChannels = 0;
            for (CapturedObject capturedObject : getCapturedObjects()) {
                if (capturedObject.getLogicalName().getObisCode().getD() == 7) {
                    nbOfChannels++;
                }
            }

        }
        return nbOfChannels;
    }

    public int getProfileInterval() throws IOException {
        if (this.profileInterval == -1) {
            this.profileInterval = powerQuality.getGenericPowerQualityProfile().getCapturePeriod();
        }
        return this.profileInterval;

    }

    /**
     * Laizy load the Captured Objects
     *
     * @return the list of captured Objects
     * @throws IOException if the list could not be fetched from the device
     */
    private List<CapturedObject> getCapturedObjects() throws IOException {
        if (this.capturedObjects == null) {
            this.capturedObjects = powerQuality.getGenericPowerQualityProfile().getCaptureObjects();
        }
        return capturedObjects;
    }

}
