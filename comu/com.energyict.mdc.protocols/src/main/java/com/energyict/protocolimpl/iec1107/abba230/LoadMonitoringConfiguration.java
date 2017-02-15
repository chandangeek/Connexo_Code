/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * @author sva
 * @since 14/03/2016 - 15:46
 */
public class LoadMonitoringConfiguration {

    // Enable or disable the load monitoring function (all load monitors)
    private byte active;

    // Lower load monitoring active power threshold x 10 (in order to allow 1 dp). Range 0 - 120 percent
    // of system rating (balanced load, Un, Imax, UPF). A value of 0 disables the lower load monitor.
    private int lowerLevelThreshold;

    // Lower level duration (in seconds) for which the average active power is allowed to exceed the lower
    // load limiting threshold before the contactor is opened.
    private int lowerLevelDuration;

    // Enable or disable the lower load monitoring contactor open request function
    private byte lowerContactorOpen;

    // The lower auto arm period (in minutes) will start following an open request (if enabled), when the
    // period has elapsed the contactor will be requested to auto arm for closure, set to 0 to disable.
    private int lowerAutoArmPeriod;

    // Higher load monitoring active power threshold x 10 (in order to allow 1 dp). Range 0 - 150 percent
    // of system rating (balanced load, Un, Imax, UPF). A value of 0 disables the higher load monitor
    private int higherLevelThreshold;

    // Higher level duration (in seconds) for which the average active power is allowed to exceed the
    // higher load limiting threshold level before the contactor is opened
    private int higherLevelDuration;

    // Enable or disable the higher load monitoring contactor open request function
    private byte higherContactorOpen;

    // The higher auto arm period (in minutes) will start following an open request (if enabled), when the
    // period has elapsed the contactor will be requested to auto arm for closure, set to 0 to disable
    private int higherAutoArmPeriod;

    /**
     * Creates a new instance of LoadMonitoringConfiguration
     */
    public LoadMonitoringConfiguration(byte[] data) throws IOException {
        int i = 0;
        active = data[i];
        i++;

        lowerLevelThreshold = ProtocolUtils.getInt(data, i, 2);
        i += 2;
        lowerLevelDuration = ProtocolUtils.getInt(data, i, 2);
        i += 2;
        lowerContactorOpen = data[i];
        i++;
        lowerAutoArmPeriod = ProtocolUtils.getInt(data, i, 1);
        i++;

        higherLevelThreshold = ProtocolUtils.getInt(data, i, 2);
        i += 2;
        higherLevelDuration = ProtocolUtils.getInt(data, i, 2);
        i += 2;
        higherContactorOpen = data[i];
        i++;
        higherAutoArmPeriod = ProtocolUtils.getInt(data, i, 1);
        i++;
    }

    public boolean isActive() {
        return active == (byte) 1;
    }

    public byte getActive() {
        return active;
    }

    public LoadMonitoringConfiguration enable() {
        this.active = 1;
        return this;
    }

    public LoadMonitoringConfiguration disable() {
        this.active = 0;
        return this;
    }

    public int getLowerLevelThreshold() {
        return lowerLevelThreshold;
    }

    public LoadMonitoringConfiguration setLowerLevelThreshold(int lowerLevelThreshold) {
        this.lowerLevelThreshold = lowerLevelThreshold;
        return this;
    }

    public int getLowerLevelDuration() {
        return lowerLevelDuration;
    }

    public LoadMonitoringConfiguration setLowerLevelDuration(int lowerLevelDuration) {
        this.lowerLevelDuration = lowerLevelDuration;
        return this;
    }

    public boolean isLowerContactorOpen() {
        return lowerContactorOpen == (byte) 1;
    }

    public byte getLowerContactorOpen() {
        return lowerContactorOpen;
    }

    public LoadMonitoringConfiguration enableLowerContactorOpen() {
        this.lowerContactorOpen = 1;
        return this;
    }

    public LoadMonitoringConfiguration disableLowerContactorOpen() {
        this.lowerContactorOpen = 0;
        return this;
    }

    public int getLowerAutoArmPeriod() {
        return lowerAutoArmPeriod;
    }

    public LoadMonitoringConfiguration setLowerAutoArmPeriod(int lowerAutoArmPeriod) {
        this.lowerAutoArmPeriod = lowerAutoArmPeriod;
        return this;
    }

    public int getHigherLevelThreshold() {
        return higherLevelThreshold;
    }

    public LoadMonitoringConfiguration setHigherLevelThreshold(int higherLevelThreshold) {
        this.higherLevelThreshold = higherLevelThreshold;
        return this;
    }

    public int getHigherLevelDuration() {
        return higherLevelDuration;
    }

    public LoadMonitoringConfiguration setHigherLevelDuration(int higherLevelDuration) {
        this.higherLevelDuration = higherLevelDuration;
        return this;
    }

    public boolean isHigherContactorOpen() {
        return higherContactorOpen == (byte) 1;
    }

    public byte getHigherContactorOpen() {
        return higherContactorOpen;
    }

    public LoadMonitoringConfiguration enableHigherContactorOpen() {
        this.higherContactorOpen = 1;
        return this;
    }

    public LoadMonitoringConfiguration disableHigherContactorOpen() {
        this.higherContactorOpen = 0;
        return this;
    }

    public int getHigherAutoArmPeriod() {
        return higherAutoArmPeriod;
    }

    public LoadMonitoringConfiguration setHigherAutoArmPeriod(int higherAutoArmPeriod) {
        this.higherAutoArmPeriod = higherAutoArmPeriod;
        return this;
    }

    public String buildData() {
        byte[] bytes = ProtocolTools.concatByteArrays(
                new byte[]{active},
                ProtocolTools.getBytesFromInt(lowerLevelThreshold, 2),
                ProtocolTools.getBytesFromInt(lowerLevelDuration, 2),
                new byte[]{lowerContactorOpen},
                ProtocolTools.getBytesFromInt(lowerAutoArmPeriod, 1),
                ProtocolTools.getBytesFromInt(higherLevelThreshold, 2),
                ProtocolTools.getBytesFromInt(higherLevelDuration, 2),
                new byte[]{higherContactorOpen},
                ProtocolTools.getBytesFromInt(higherAutoArmPeriod, 1)
        );
        return buildHex(bytes);
    }

    private String buildHex(byte[] val) {
        byte[] data = new byte[val.length * 2];
        for (int i = 0; i < val.length; i++) {
            ProtocolUtils.val2HEXascii((int) val[i] & 0xFF, data, i * 2);
        }
        return new String(data);
    }
}