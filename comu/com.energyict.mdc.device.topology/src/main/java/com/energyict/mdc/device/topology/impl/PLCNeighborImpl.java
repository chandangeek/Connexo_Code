/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ImplField;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PLCNeighbor;

import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

/**
 * Provides an implementation for the PLCNeighbor interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (14:07)
 */
public abstract class PLCNeighborImpl implements PLCNeighbor {

    protected static final String G3_DISCRIMINATOR = "0";

    public enum Field implements ImplField {
        DEVICE("device"),
        NEIGHBOR("neighbor"),
        CREATION_TIME("interval.start"),
        MODULATION_SCHEME("modulationScheme"),
        MODULATION("modulation"),
        TX_GAIN("txGain"),
        TX_RESOLUTION("txResolution"),
        TX_COEFFICIENT("txCoefficient"),
        LINK_QUALITY_INDICATOR("linkQualityIndicator"),
        TIME_TO_LIVE("timeToLive"),
        TONE_MAP("toneMap"),
        TONE_MAP_TIME_TO_LIVE("toneMapTimeToLive"),
        PHASE_INFO("phaseInfo");

        private final String javaFieldName;

        private Field(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        @Override
        public String fieldName() {
            return javaFieldName;
        }
    }

    public static final Map<String, Class<? extends PLCNeighbor>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends PLCNeighbor>>of(
                    G3_DISCRIMINATOR, G3NeighborImpl.class);

    private final Clock clock;
    private final DataModel dataModel;

    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private ModulationScheme modulationScheme;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Modulation modulation;
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<Device> device = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<Device> neighbor = ValueReference.absent();
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Interval interval;

    protected PLCNeighborImpl(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    void init(Device device, Device neighbor, ModulationScheme modulationScheme, Modulation modulation) {
        this.interval = Interval.startAt(this.clock.instant());
        this.modulationScheme = modulationScheme;
        this.modulation = modulation;
        this.device.set(device);
        this.neighbor.set(neighbor);
    }

    @Override
    public ModulationScheme getModulationScheme() {
        return modulationScheme;
    }

    protected void setModulationScheme(ModulationScheme modulationScheme) {
        this.modulationScheme = modulationScheme;
    }

    @Override
    public Modulation getModulation() {
        return modulation;
    }

    protected void setModulation(Modulation modulation) {
        this.modulation = modulation;
    }

    @Override
    public Device getDevice() {
        return device.get();
    }

    @Override
    public Device getNeighbor() {
        return neighbor.get();
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    Instant getEffectiveStart() {
        return this.interval.getStart();
    }

    void save () {
        Save.action(this.version).save(this.dataModel, this);
    }

    void terminate() {
        this.terminate(this.clock.instant());
    }

    void terminate(Instant when) {
        if (!isEffectiveAt(when)) {
            throw new IllegalArgumentException();
        }
        this.interval = this.interval.withEnd(when);
    }

}