package com.elster.jupiter.time.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.EventType;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.RelativePeriodCategoryUsage;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Unique(fields = "name", groups = Save.Create.class, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
public class RelativePeriodImpl extends EntityImpl implements RelativePeriod {
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    @NotNull
    private RelativeDate from;
    @NotNull
    private RelativeDate to;

    private List<RelativePeriodCategoryUsage> relativePeriodCategoryUsages = new ArrayList<>();

    @Inject
    public RelativePeriodImpl(DataModel dataModel, EventService eventService) {
        super(dataModel, eventService);
    }

    @Override
    public Range<ZonedDateTime> getClosedZonedInterval(ZonedDateTime referenceDate) {
        return Range.closed(this.from.getRelativeDate(referenceDate), this.to.getRelativeDate(referenceDate));
    }

    @Override
    public Range<Instant> getClosedInterval(ZonedDateTime referenceDate) {
        return Range.closed(this.from.getRelativeDate(referenceDate).toInstant(), this.to.getRelativeDate(referenceDate).toInstant());
    }

    @Override
    public Range<ZonedDateTime> getOpenClosedZonedInterval(ZonedDateTime referenceDate) {
        return Range.openClosed(this.from.getRelativeDate(referenceDate), this.to.getRelativeDate(referenceDate));
    }

    @Override
    public Range<Instant> getOpenClosedInterval(ZonedDateTime referenceDate) {
        return Range.openClosed(this.from.getRelativeDate(referenceDate).toInstant(), this.to.getRelativeDate(referenceDate).toInstant());
    }

    @Override
    public Range<ZonedDateTime> getClosedOpenZonedInterval(ZonedDateTime referenceDate) {
        return Range.closedOpen(this.from.getRelativeDate(referenceDate), this.to.getRelativeDate(referenceDate));
    }

    @Override
    public Range<Instant> getClosedOpenInterval(ZonedDateTime referenceDate) {
        return Range.closedOpen(this.from.getRelativeDate(referenceDate).toInstant(), this.to.getRelativeDate(referenceDate).toInstant());
    }

    @Override
    public Range<ZonedDateTime> getOpenZonedInterval(ZonedDateTime referenceDate) {
        return Range.open(this.from.getRelativeDate(referenceDate), this.to.getRelativeDate(referenceDate));
    }

    @Override
    public Range<Instant> getOpenInterval(ZonedDateTime referenceDate) {
        return Range.open(this.from.getRelativeDate(referenceDate).toInstant(), this.to.getRelativeDate(referenceDate).toInstant());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public RelativeDate getRelativeDateFrom() {
        return this.from;
    }

    public String getFrom() {
        return this.from != null ? this.from.getRelativeDate() : "";
    }

    @Override
    public RelativeDate getRelativeDateTo() {
        return this.to;
    }

    public String getTo() {
        return this.to != null ? this.to.getRelativeDate() : "";
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public void setRelativeDateFrom(RelativeDate from) {
        this.from = from;
    }

    public void setFrom(String from) {
        this.from = new RelativeDate(from);
    }

    public void setRelativeDateTo(RelativeDate to) {
        this.to = to;
    }

    public void setTo(String to) {
        this.to = new RelativeDate(to);
    }

    public void setRelativePeriodCategoryUsages(List<RelativePeriodCategory> categories) {
        DiffList<RelativePeriodCategoryUsage> entryDiff = ArrayDiffList.fromOriginal(relativePeriodCategoryUsages);
        entryDiff.clear();
        List<RelativePeriodCategoryUsage> newCategories = new ArrayList<>();
        categories.stream().forEach(category -> newCategories.add(new RelativePeriodCategoryUsage(this, category)));
        entryDiff.addAll(newCategories);
        for (RelativePeriodCategoryUsage usage : entryDiff.getRemovals()) {
            removeRelativePeriodCategory(usage.getRelativePeriodCategory());
        }
        for (RelativePeriodCategoryUsage usage : entryDiff.getAdditions()) {
            addRelativePeriodCategory(usage.getRelativePeriodCategory());
        }
    }

    @Override
    public List<RelativePeriodCategory> getRelativePeriodCategories() {
        List<RelativePeriodCategory> usages = new ArrayList<>(this.relativePeriodCategoryUsages.size());
        this.relativePeriodCategoryUsages.stream().forEach(usage -> usages.add(usage.getRelativePeriodCategory()));

        return usages;
    }

    @Override
    public void addRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) {
        this.relativePeriodCategoryUsages.add(new RelativePeriodCategoryUsage(this, relativePeriodCategory));
    }

    @Override
    public void removeRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) {
        Iterator<RelativePeriodCategoryUsage> relativePeriodCategoryUsageIterator = this.relativePeriodCategoryUsages.iterator();
        while (relativePeriodCategoryUsageIterator.hasNext()) {
            RelativePeriodCategoryUsage relativePeriodCategoryUsage = relativePeriodCategoryUsageIterator.next();
            if (relativePeriodCategoryUsage.sameRelativePeriodCategory(relativePeriodCategory)) {
                getEventService().postEvent(EventType.CATEGORY_USAGE_DELETED.topic(), new RelativePeriodCategoryUsageEvent(relativePeriodCategoryUsage));
                relativePeriodCategoryUsageIterator.remove();
                return;
            }
        }
    }

    @Override
    EventType created() {
        return EventType.RELATIVE_PERIOD_CREATED;
    }

    @Override
    EventType updated() {
        return EventType.RELATIVE_PERIOD_UPDATED;
    }

    @Override
    EventType deleted() {
        return EventType.RELATIVE_PERIOD_DELETED;
    }
}
