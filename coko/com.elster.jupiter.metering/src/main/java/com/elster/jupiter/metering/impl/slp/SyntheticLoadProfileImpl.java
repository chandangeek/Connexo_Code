/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.slp;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.units.Unit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Unique(fields = "name", groups = {Save.Create.class}, message = "{" + PrivateMessageSeeds.Constants.DUPLICATE_SLP_NAME + "}")
public class SyntheticLoadProfileImpl implements SyntheticLoadProfile {
    private final int FIRST_VALUE_OFFSET = 0;

    private final DataModel dataModel;
    private final IdsService idsService;

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SPECIAL_CHARS)
    private String name;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String description;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private Reference<ReadingType> readingType = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private Instant startTime;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private String duration;

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
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
    public TemporalAmount getInterval() { return timeSeries.get().interval();
    }

    @Override
    public ReadingType getReadingType() {
        return readingType.get();
    }

    void setReadingType(ReadingType readingType) {
        this.readingType.set(readingType);
    }

    @Override
    public Unit getUnitOfMeasure() {
        return this.getReadingType().getUnit().getUnit();
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

    @SuppressWarnings("unchecked")
    @Override
    public SqlFragment getRawValuesSql(Range<Instant> interval, Pair<String, String>... fieldSpecAndAliasNames) {
        return this.timeSeries.get().getRawValuesSql(interval, fieldSpecAndAliasNames);
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