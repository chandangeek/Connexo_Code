/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class ConcentratorConfiguration extends AbstractField<ConcentratorConfiguration> {

    public static final int LENGTH = 1;

    private int configurationCode;
    private BitSet configurationMask;

    public ConcentratorConfiguration() {
        this.configurationCode = 0;
        this.configurationMask = new BitSet();
    }

    public ConcentratorConfiguration(BitSet configurationMask) {
        this.configurationMask = configurationMask;
        this.configurationCode = ProtocolTools.getIntFromBytes(configurationMask.toByteArray());
    }

    @Override
    public ConcentratorConfiguration parse(byte[] rawData, int offset) throws ParsingException {
        this.configurationCode = getIntFromBytes(rawData, offset, LENGTH);
        this.configurationMask = BitSet.valueOf(ProtocolTools.getSubArray(rawData, offset, offset + LENGTH));
        return this;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(configurationCode, LENGTH);
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getConfigurationCode() {
        return configurationCode;
    }

    public BitSet getBitMask() {
        return configurationMask;
    }

    public String getMeterConfigurationInfo() {
        String configInfo = "";
        configInfo += configurationMask.get(0) ? "Alarm mode" : "Maintenance mode";
        configInfo += " - ";
        configInfo += configurationMask.get(0) ? "Automatic shutdown active" : "Automatic shutdown inactive";
        configInfo += " - ";
        configInfo += configurationMask.get(0) ? "Collector enabled" : "Collector blocked";
        configInfo += " - ";
        configInfo += configurationMask.get(0) ? "Internal sensor closed" : "Internal sensor open";
        configInfo += " - ";
        configInfo += configurationMask.get(0) ? "External sensor closed" : "External sensor open";
        configInfo += " - ";
        configInfo += configurationMask.get(0) ? "Sensor 2 in alarm mode" : "Sensor 2 in maintenance mode";
        configInfo += " - ";
        configInfo += configurationMask.get(0) ? "Automatic shutdown sensor 2 active" : "Automatic shutdown sensor 2 inactive";
        return configInfo;
    }
}