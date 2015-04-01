package com.elster.jupiter.time;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Created by igh on 1/04/2015.
 */
public class AllRelativePeriod implements RelativePeriod {

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
        return null;
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
