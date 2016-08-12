package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class ValidationContextImpl implements ValidationContext {
    private ChannelsContainer channelsContainer;
    private Set<QualityCodeSystem> qualityCodeSystems;
    private ReadingType readingType;
    private MetrologyContract metrologyContract;

    public ValidationContextImpl(ChannelsContainer channelsContainer) {
        this(Collections.emptySet(), channelsContainer);
    }

    public ValidationContextImpl(Set<QualityCodeSystem> qualityCodeSystems, ChannelsContainer channelsContainer) {
        setChannelsContainer(channelsContainer);
        setQualityCodeSystems(qualityCodeSystems);
    }

    public final ValidationContextImpl setChannelsContainer(ChannelsContainer channelsContainer) {
        this.channelsContainer = channelsContainer;
        return this;
    }

    public final ValidationContextImpl setQualityCodeSystems(Set<QualityCodeSystem> qualityCodeSystems) {
        if (qualityCodeSystems == null || qualityCodeSystems.isEmpty()) {
            this.qualityCodeSystems = Collections.emptySet();
        } else {
            this.qualityCodeSystems = EnumSet.copyOf(qualityCodeSystems);
        }
        return this;
    }

    public ValidationContextImpl setReadingType(ReadingType readingType) {
        this.readingType = readingType;
        return this;
    }

    public ValidationContextImpl setMetrologyContract(MetrologyContract metrologyContract) {
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
}
