package com.elster.jupiter.time.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.time.*;
import com.google.common.collect.Range;
import com.google.inject.Inject;

import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Unique(fields="name", groups = Save.Create.class)
public class RelativePeriodImpl extends EntityImpl implements RelativePeriod {
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    @NotNull
    private RelativeDate from;
    @NotNull
    private RelativeDate to;

    private final EventService eventService;

    private List<RelativePeriodCategoryUsage> relativePeriodCategoryUsages = new ArrayList<>();
    //private final Provider<RelativePeriodCategoryUsageImpl> relativePeriodCategoryUsageFactory;

    @Inject
    public RelativePeriodImpl(DataModel dataModel, /*Provider<RelativePeriodCategoryUsageImpl> relativePeriodCategoryUsageFactory,*/ EventService eventService) {
        super(dataModel);
       // this.relativePeriodCategoryUsageFactory = relativePeriodCategoryUsageFactory;
        this.eventService = eventService;
    }

    @Override
    public Range<ZonedDateTime> getInterval(ZonedDateTime referenceDate) {
        return Range.closed(this.from.getRelativeDate(referenceDate), this.to.getRelativeDate(referenceDate));
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
        this.name = name;
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

    /*@Override
    public List<RelativePeriodCategory> getRelativePeriodCategories() {
        List<RelativePeriodCategoryUsage> usages = getUsages();
        return usages.stream().map(usage -> usage.getRelativePeriodCategory()).collect(Collectors.toList());
    }

    @Override
    public RelativePeriodCategoryUsage addRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) {
        RelativePeriodCategoryUsage usage =
                relativePeriodCategoryUsageFactory.get().init(this, relativePeriodCategory);
        usage.save();
        return usage;
    }

    @Override
    public void removeRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) throws Exception {
        List<RelativePeriodCategoryUsage> usages = getUsageQuery().select(where("relativePeriodId").isEqualTo(this.getId())
                        .and(where("relativePeriodCategoryId").isEqualTo(relativePeriodCategory.getId())));
        if(!usages.isEmpty()) {
            RelativePeriodCategoryUsage usage = usages.get(0);
            eventService.postEvent(EventType.CATEGORY_USAGE_DELETED.topic(), usage);
            usage.delete();
        }
    }

    private List<RelativePeriodCategoryUsage> getUsages() {
        return getUsageQuery().select(where("relativePeriodId").isEqualTo(this.getId()));
    }

    private QueryExecutor<RelativePeriodCategoryUsage> getUsageQuery() {
        QueryExecutor<RelativePeriodCategoryUsage> usageQuery = dataModel.query(RelativePeriodCategoryUsage.class);
        return usageQuery;
    }
    */
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
    public void removeRelativePeriodCategory(RelativePeriodCategory relativePeriodCategory) throws Exception  {
        Iterator<RelativePeriodCategoryUsage> relativePeriodCategoryUsageIterator = this.relativePeriodCategoryUsages.iterator();
        while (relativePeriodCategoryUsageIterator.hasNext()) {
            RelativePeriodCategoryUsage relativePeriodCategoryUsage = relativePeriodCategoryUsageIterator.next();
            if (relativePeriodCategoryUsage.sameRelativePeriodCategory(relativePeriodCategory)) {
                eventService.postEvent(EventType.CATEGORY_USAGE_DELETED.topic(), relativePeriodCategoryUsage);
                relativePeriodCategoryUsageIterator.remove();
            }
        }
    }

}
