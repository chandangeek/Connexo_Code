/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivationChannelsContainer;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public class ValidationContextImpl implements ValidationContext {
    private ChannelsContainer channelsContainer;
    private Set<QualityCodeSystem> qualityCodeSystems;
    private ReadingType readingType;
    private MetrologyContract metrologyContract;
    private Logger logger;

    public ValidationContextImpl(ChannelsContainer channelsContainer) {
        this(channelsContainer instanceof MetrologyContractChannelsContainer ? EnumSet.of(QualityCodeSystem.MDM) :
                        channelsContainer instanceof MeterActivationChannelsContainer ? EnumSet.of(QualityCodeSystem.MDC) :
                                Collections.emptySet()
                , channelsContainer);
    }

    public ValidationContextImpl(Set<QualityCodeSystem> qualityCodeSystems, ChannelsContainer channelsContainer) {
        setChannelsContainer(channelsContainer);
        setQualityCodeSystems(qualityCodeSystems);
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            setMetrologyContract(((MetrologyContractChannelsContainer) channelsContainer).getMetrologyContract());
        }
    }

    public ValidationContextImpl(Set<QualityCodeSystem> qualityCodeSystems, ChannelsContainer channelsContainer, Logger logger) {
        this(qualityCodeSystems, channelsContainer);
        this.setLogger(logger);
    }

    public ValidationContextImpl(Set<QualityCodeSystem> qualityCodeSystems, ChannelsContainer channelsContainer, ReadingType readingType) {
        this(qualityCodeSystems, channelsContainer);
        this.setReadingType(readingType);
    }

    public ValidationContextImpl(Set<QualityCodeSystem> qualityCodeSystems, ChannelsContainer channelsContainer, ReadingType readingType, Logger logger) {
        this(qualityCodeSystems, channelsContainer, readingType);
        this.setLogger(logger);
    }

    public ValidationContextImpl(Set<QualityCodeSystem> qualityCodeSystems, ChannelsContainer channelsContainer, MetrologyContract metrologyContract) {
        this(qualityCodeSystems, channelsContainer);
        setMetrologyContract(metrologyContract);
    }

    private ValidationContext setChannelsContainer(ChannelsContainer channelsContainer) {
        this.channelsContainer = channelsContainer;
        return this;
    }

    private ValidationContext setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public final ValidationContext setQualityCodeSystems(Set<QualityCodeSystem> qualityCodeSystems) {
        if (qualityCodeSystems == null || qualityCodeSystems.isEmpty()) {
            this.qualityCodeSystems = Collections.emptySet();
        } else {
            this.qualityCodeSystems = EnumSet.copyOf(qualityCodeSystems);
        }
        return this;
    }

    private ValidationContext setReadingType(ReadingType readingType) {
        this.readingType = readingType;
        return this;
    }

    private ValidationContext setMetrologyContract(MetrologyContract metrologyContract) {
        this.metrologyContract = metrologyContract;
        return this;
    }

    @Override
    public Set<QualityCodeSystem> getQualityCodeSystems() {
        return Collections.unmodifiableSet(this.qualityCodeSystems);
    }

    @Override
    public ChannelsContainer getChannelsContainer() {
        return this.channelsContainer;
    }

    @Override
    public Optional<Meter> getMeter() {
        return this.channelsContainer.getMeter();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return this.channelsContainer.getUsagePoint();
    }

    @Override
    public Optional<ReadingType> getReadingType() {
        return Optional.ofNullable(this.readingType);
    }

    @Override
    public Optional<MetrologyContract> getMetrologyContract() {
        return Optional.ofNullable(this.metrologyContract);
    }

    @Override
    public Optional<Logger> getLogger() {
        return Optional.ofNullable(this.logger);
    }
}