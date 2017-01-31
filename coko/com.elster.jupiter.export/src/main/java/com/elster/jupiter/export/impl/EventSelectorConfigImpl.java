/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.EndDeviceEventTypeFilter;
import com.elster.jupiter.export.EventDataExportStrategy;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.RelativePeriod;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class EventSelectorConfigImpl extends StandardDataSelectorConfigImpl implements EventSelectorConfig {

    static final String IMPLEMENTOR_NAME = "EventSelectorConfig";

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();

    @Valid
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MUST_SELECT_AT_LEAST_ONE_EVENT_TYPE + "}")
    private List<EndDeviceEventTypeFilter> eventTypeFilters = new ArrayList<>();

    @Inject
    public EventSelectorConfigImpl(DataModel dataModel) {
        super(dataModel);
    }

    static EventSelectorConfigImpl from(DataModel dataModel, IExportTask exportTask, RelativePeriod exportPeriod) {
        EventSelectorConfigImpl config = dataModel.getInstance(EventSelectorConfigImpl.class);
        config.init(exportTask, exportPeriod);
        return config;
    }

    @Override
    public EventDataExportStrategy getStrategy() {
        return new DataExportStrategyImpl(false, isExportContinuousData(), false, null, null, null);
    }

    @Override
    public EndDeviceGroup getEndDeviceGroup() {
        return endDeviceGroup.get();
    }

    @Override
    public List<EndDeviceEventTypeFilter> getEventTypeFilters() {
        return Collections.unmodifiableList(eventTypeFilters);
    }

    @Override
    public Predicate<? super EndDeviceEventRecord> getFilterPredicate() {
        return eventTypeFilters.stream()
                .map(EndDeviceEventTypeFilter::asEndDeviceEventPredicate)
                .reduce(t -> false, Predicate::or);
    }

    @Override
    public EventSelector createDataSelector(Logger logger) {
        return EventSelector.from(getDataModel(), this, logger);
    }

    @Override
    public void apply(DataSelectorConfigVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public History<DataSelectorConfig> getHistory() {
        List<JournalEntry<EventSelectorConfigImpl>> journal = getDataModel().mapper(EventSelectorConfigImpl.class).getJournal(getId());
        return new History<>(journal, this);
    }

    @Override
    public void delete() {
        this.eventTypeFilters.clear();
        super.delete();
    }

    @Override
    public EventSelectorConfig.Updater startUpdate() {
        return new UpdaterImpl();
    }

    class UpdaterImpl extends StandardDataSelectorConfigImpl.UpdaterImpl implements EventSelectorConfig.Updater {

        @Override
        public EventSelectorConfig.Updater setExportPeriod(RelativePeriod period) {
            super.setExportPeriod(period);
            return this;
        }

        @Override
        public EventSelectorConfig.Updater setExportContinuousData(boolean exportContinuousData) {
            super.setExportContinuousData(exportContinuousData);
            return this;
        }

        @Override
        public EventSelectorConfig.Updater setEndDeviceGroup(EndDeviceGroup group) {
            endDeviceGroup.set(group);
            return this;
        }

        @Override
        public EventSelectorConfig.Updater addEventTypeFilter(String code) {
            FieldBasedEndDeviceEventTypeFilter filter = FieldBasedEndDeviceEventTypeFilter.from(getDataModel(), EventSelectorConfigImpl.this, code);
            eventTypeFilters.add(filter);
            return this;
        }

        @Override
        public EventSelectorConfig.Updater removeEventTypeFilter(String code) {
            eventTypeFilters.stream()
                    .filter(endDeviceEventTypeFilter -> endDeviceEventTypeFilter.getCode().equals(code))
                    .findFirst()
                    .ifPresent(eventTypeFilters::remove);
            return this;
        }

        @Override
        public EventSelectorConfig complete() {
            return EventSelectorConfigImpl.this;
        }
    }
}
