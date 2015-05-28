package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ReadingTypeDataSelector;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.inject.Inject;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadingTypeDataSelectorImpl implements IReadingTypeDataSelector {

    private final TransactionService transactionService;
    private final Logger logger;
    private final MeteringService meteringService;
    private final DataModel dataModel;

    private long id;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<RelativePeriod> exportPeriod = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();
    private Reference<RelativePeriod> updatePeriod = ValueReference.absent();
    private Reference<IExportTask> exportTask = ValueReference.absent();

    private boolean exportUpdate;
    private boolean exportContinuousData;
    private ValidatedDataOption validatedDataOption;
    @Valid
    @Size(min=1, message = "{" + MessageSeeds.Keys.MUST_SELECT_AT_LEAST_ONE_READING_TYPE + "}")
    private List<ReadingTypeInDataSelector> readingTypes = new ArrayList<>();
    private List<ReadingTypeDataExportItemImpl> exportItems = new ArrayList<>();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    ReadingTypeDataSelectorImpl(DataModel dataModel, TransactionService transactionService, MeteringService meteringService, Logger logger) {
        this.dataModel = dataModel;
        this.transactionService = transactionService;
        this.meteringService = meteringService;
        this.logger = logger;
    }

    public static ReadingTypeDataSelectorImpl from(DataModel dataModel, IExportTask exportTask, RelativePeriod exportPeriod, EndDeviceGroup endDeviceGroup) {
        return dataModel.getInstance(ReadingTypeDataSelectorImpl.class).init(exportTask, exportPeriod, endDeviceGroup);
    }

    private ReadingTypeDataSelectorImpl init(IExportTask exportTask, RelativePeriod exportPeriod, EndDeviceGroup endDeviceGroup) {
        this.exportTask.set(exportTask);
        this.exportPeriod.set(exportPeriod);
        this.endDeviceGroup.set(endDeviceGroup);
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Stream<ExportData> selectData(DataExportOccurrence occurrence) {
        IExportTask task = (IExportTask) occurrence.getTask();
        Set<IReadingTypeDataExportItem> activeItems;
        try (TransactionContext context = transactionService.getContext()) {
            activeItems = getActiveItems(task, occurrence);

            getExportItems().stream()
                    .filter(item -> !activeItems.contains(item))
                    .peek(IReadingTypeDataExportItem::deactivate)
                    .forEach(IReadingTypeDataExportItem::update);
            activeItems.stream()
                    .peek(IReadingTypeDataExportItem::activate)
                    .forEach(IReadingTypeDataExportItem::update);
            context.commit();
        }

        return activeItems.stream()
                .map(item -> DefaultItemDataSelector.INSTANCE.selectData(occurrence, item))
                .flatMap(Functions.asStream());
    }

    private Set<IReadingTypeDataExportItem> getActiveItems(IExportTask task, DataExportOccurrence occurrence) {
        return getEndDeviceGroup().getMembers(occurrence.getExportedDataInterval()).stream()
                .map(EndDeviceMembership::getEndDevice)
                .filter(device -> device instanceof Meter)
                .map(Meter.class::cast)
                .flatMap(meter -> readingTypeDataExportItems(task, meter))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Stream<IReadingTypeDataExportItem> readingTypeDataExportItems(IExportTask task, Meter meter) {
        return getReadingTypes().stream()
                .map(r -> getExportItems().stream()
                                .map(IReadingTypeDataExportItem.class::cast)
                                .filter(item -> r.equals(item.getReadingType()))
                                .filter(i -> i.getReadingContainer().is(meter))
                                .findAny()
                                .orElseGet(() -> addExportItem(meter, r))
                );
    }

    @Override
    public List<IReadingTypeDataExportItem> getExportItems() {
        return Collections.unmodifiableList(exportItems);
    }

    @Override
    public IReadingTypeDataExportItem addExportItem(Meter meter, ReadingType readingType) {
        ReadingTypeDataExportItemImpl item = ReadingTypeDataExportItemImpl.from(dataModel, this, meter, readingType);
        exportItems.add(item);
        return item;
    }

    @Override
    public History<ReadingTypeDataSelector> getHistory() {
        List<JournalEntry<ReadingTypeDataSelector>> journal = dataModel.mapper(ReadingTypeDataSelector.class).getJournal(getId());
        return new History<>(journal, this);
    }

    @Override
    public DataExportStrategy getStrategy() {
        return new DataExportStrategyImpl(exportUpdate, exportContinuousData, validatedDataOption);
    }

    @Override
    public void setExportPeriod(RelativePeriod relativePeriod) {
        this.exportPeriod.set(relativePeriod);
    }

    @Override
    public void removeReadingType(ReadingType readingType) {
        this.readingTypes.removeIf(r -> r.getReadingType().equals(readingType));
    }

    @Override
    public Set<ReadingType> getReadingTypes() {
        return readingTypes.stream()
                .map(ReadingTypeInDataSelector::getReadingType)
                .collect(Collectors.toSet());
    }

    @Override // TODO consider removal
    public void addReadingType(String mRID) {
        readingTypes.add(toReadingTypeInExportTask(mRID));
    }

    @Override
    public EndDeviceGroup getEndDeviceGroup() {
        return endDeviceGroup.get();
    }

    @Override
    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup.set(endDeviceGroup);
    }

    @Override
    public void addReadingType(ReadingType readingType) {
        if (getReadingTypes().contains(readingType)) {
            return;
        }
        readingTypes.add(ReadingTypeInDataSelector.from(dataModel, this, readingType));

    }

    @Override
    public void setUpdatePeriod(RelativePeriod updatePeriod) {
        this.updatePeriod.set(updatePeriod);
    }

    @Override
    public Set<ReadingType> getReadingTypes(Instant at) {
        List<JournalEntry<ReadingTypeInDataSelector>> readingTypes = dataModel.mapper(ReadingTypeInDataSelector.class).at(at).find(ImmutableMap.of("readingTypeDataExportTask", this));
        return readingTypes.stream()
                .map(JournalEntry::get)
                .map(ReadingTypeInDataSelector::getReadingType)
                .collect(Collectors.toSet());
    }

    @Override
    public RelativePeriod getExportPeriod() {
        return exportPeriod.get();
    }

    @Override
    public Optional<RelativePeriod> getUpdatePeriod() {
        return updatePeriod.getOptional();
    }

    @Override
    public IExportTask getExportTask() {
        return exportTask.get();
    }

    private ReadingTypeInDataSelector toReadingTypeInExportTask(String mRID) {
        return meteringService.getReadingType(mRID)
                .map(r -> ReadingTypeInDataSelector.from(dataModel, this, r))
                .orElseGet(() -> readingTypeInDataSelectorFor(mRID));
    }

    private ReadingTypeInDataSelector readingTypeInDataSelectorFor(String mRID) {
        ReadingTypeInDataSelector empty = ReadingTypeInDataSelector.from(dataModel, this, mRID);
        if (getId() != 0) {
            Save.UPDATE.validate(dataModel, empty);
        }
        return empty;
    }

    @Override
    public void setValidatedDataOption(ValidatedDataOption validatedDataOption) {
        this.validatedDataOption = validatedDataOption;
    }

    @Override
    public void setExportUpdate(boolean exportUpdate) {
        this.exportUpdate = exportUpdate;
    }

    @Override
    public void setExportContinuousData(boolean exportContinuousData) {
        this.exportContinuousData = exportContinuousData;
    }

    @Override
    public Range<Instant> adjustedExportPeriod(DataExportOccurrence occurrence, ReadingTypeDataExportItem item) {
        return getStrategy().adjustedExportPeriod(occurrence, item);
    }

    @Override
    public void save() {
        if (id == 0) {
            dataModel.mapper(ReadingTypeDataSelector.class).persist(this);
        } else {
            dataModel.mapper(ReadingTypeDataSelector.class).update(this);
        }

    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }
}
