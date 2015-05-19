package com.elster.jupiter.time.impl;

import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Created by igh on 1/04/2015.
 */
public enum AllRelativePeriod implements RelativePeriod {

    INSTANCE;

    @Override
    public String getName() {
        return "All";
    }

    @Override
    public RelativeDate getRelativeDateFrom() {
        return null;
    }

    @Override
    public RelativeDate getRelativeDateTo() {
        return null;
    }

    @Override
    public Range<ZonedDateTime> getInterval(ZonedDateTime referenceDate) {
        return Range.all();
    }

    @Override
    public List<RelativePeriodCategory> getRelativePeriodCategories() {
        return null;
    }

    @Override
    public void addRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) {

    }

    @Override
    public void removeRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) {

    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public long getVersion() {
        return 0;
    }

    @Override
    public Instant getCreateTime() {
        return null;
    }

    @Override
    public Instant getModTime() {
        return null;
    }

    @Override
    public String getUserName() {
        return null;
    }

    @Override
    public void save() {

    }

    @Override
    public void delete() {

    }
}
