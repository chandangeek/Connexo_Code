/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.slp;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.units.Unit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Unique(fields = "name", groups = {Save.Create.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_SLP_NAME + "}")
public class SyntheticLoadProfileImpl implements SyntheticLoadProfile {
    private final int FIRST_VALUE_OFFSET = 0;

    private final DataModel dataModel;
    private final IdsService idsService;

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String name;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_TOO_LONG + "}")
    private String description;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private String interval;
    private Unit unitOfMeasure;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Instant startTime;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private String duration;

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    private Reference<TimeSeries> timeSeries = ValueReference.absent();

    // Audit fields
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Inject
    SyntheticLoadProfileImpl(DataModel dataModel, IdsService idsService) {
        this.dataModel = dataModel;
        this.idsService = idsService;
    }

    SyntheticLoadProfileImpl initialize(String name) {
        this.name = name;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Duration getInterval() { return Duration.parse(interval);
    }

    void setInterval(Duration interval) {
        this.interval = interval.toString();
    }

    @Override
    public Unit getUnitOfMeasure() {
        return unitOfMeasure;
    }

    void setUnitOfMeasure(Unit unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    @Override
    public Instant getStartTime() {
        return startTime;
    }

    void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    @Override
    public Period getDuration() {
        return Period.parse(duration);
    }

    void setDuration(Period duration) {
        this.duration = duration.toString();
    }

    TimeSeries getTimeSeries() {
        return timeSeries.get();
    }

    void setTimeSeries(TimeSeries timeSeries) {
        this.timeSeries.set(timeSeries);
    }

    @Override
    public void addValues(Map<Instant, BigDecimal> values) {
        TimeSeriesDataStorer storer = idsService.createOverrulingStorer();
        addValues(storer, values);
        storer.execute();
    }

    @Override
    public Optional<BigDecimal> getValue(Instant date) {
        Optional<TimeSeriesEntry> entry = getTimeSeries().getEntry(date);
        return entry.map(e -> e.getBigDecimal(FIRST_VALUE_OFFSET));
    }

    @Override
    public Map<Instant, BigDecimal> getValues(Range<Instant> range) {
        ImmutableMap.Builder<Instant, BigDecimal> result = ImmutableMap.builder();
        for (TimeSeriesEntry entry : getTimeSeries().getEntries(range)) {
            result.put(entry.getTimeStamp(), entry.getBigDecimal(FIRST_VALUE_OFFSET));
        }
        return result.build();
    }

    @Override
    public void delete() {
        dataModel.mapper(SyntheticLoadProfileImpl.class).remove(this);
    }

    private void addValues(TimeSeriesDataStorer storer, Map<Instant, BigDecimal> values) {
        TimeSeries timeSeries = getTimeSeries();
        values.entrySet().forEach(entry -> storer.add(timeSeries, entry.getKey(), entry.getValue()));
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SyntheticLoadProfileImpl)) {
            return false;
        }
        SyntheticLoadProfileImpl cf = (SyntheticLoadProfileImpl) o;
        return Objects.equals(id, cf.id);
    }

    public void save() {
        if (this.getId() == 0) {
            Save.CREATE.save(this.dataModel, this);
        }
        Save.UPDATE.save(this.dataModel, this);
    }
}