/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.energyict.mdc.sap.soap.webservices.impl.MeasurementTaskAssignmentChangeProcessor.DEFAULT_GROUP_NAME;
import static com.energyict.mdc.sap.soap.webservices.impl.MeasurementTaskAssignmentChangeProcessor.DEFAULT_TASK_NAME;

public class UpdateSapExportTaskHandler implements TaskExecutor {
    private final MeteringGroupsService meteringGroupsService;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final DataExportService dataExportService;

    public UpdateSapExportTaskHandler(MeteringGroupsService meteringGroupsService,
                                      SAPCustomPropertySets sapCustomPropertySets,
                                      DataExportService dataExportService) {
        this.meteringGroupsService = meteringGroupsService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.dataExportService = dataExportService;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        Optional<EnumeratedEndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(WebServiceActivator.getExportTaskDeviceGroupName().orElse(DEFAULT_GROUP_NAME))
                .filter(EnumeratedEndDeviceGroup.class::isInstance)
                .map(EnumeratedEndDeviceGroup.class::cast);
        if (endDeviceGroup.isPresent()) {
            Optional<? extends ExportTask> exportTask = dataExportService.getReadingTypeDataExportTaskByName(WebServiceActivator.getExportTaskName().orElse(DEFAULT_TASK_NAME));
            if (exportTask.isPresent()) {
                exportTask.get().getReadingDataSelectorConfig().ifPresent(config -> {
                    Map<IdentifiedObject, List<ReadingTypeDataExportItem>> exportItemsMap = config.getExportItems().stream()
                            .collect(Collectors.groupingBy(ReadingTypeDataExportItem::getDomainObject));
                    endDeviceGroup.get().getEntries().stream()
                            .filter(endDevice -> canDeviceBeRemoved(endDevice.getMember(), config, exportItemsMap.getOrDefault(endDevice.getMember(), Collections.emptyList())))
                            .forEach(device -> endDeviceGroup.get().remove(device));
                });
            } else {
                endDeviceGroup.get().delete();
            }
        }
    }

    private boolean canDeviceBeRemoved(EndDevice member, ReadingDataSelectorConfig config, List<ReadingTypeDataExportItem> exportItems) {
        if (exportItems.isEmpty()) {
            if (member instanceof Meter) {
                Set<ReadingType> readingTypeSet = config.getReadingTypes();
                return ((Meter) member).getChannelsContainers().stream()
                        .map(ChannelsContainer::getChannels)
                        .flatMap(List::stream)
                        .map(Channel::getReadingTypes)
                        .flatMap(List::stream)
                        .noneMatch(readingTypeSet::contains);
            }
            return true;
        } else {
            return exportItems.stream().allMatch(item -> isNothingToExportOnDataSource(item, member));
        }
    }

    private boolean isNothingToExportOnDataSource(ReadingTypeDataExportItem item, EndDevice member) {
        Optional<Range<Instant>> rangeForExport = sapCustomPropertySets.getLastProfileIdIntervalForChannelOnDevice(Long.parseLong(member.getAmrId()), item.getReadingType().getMRID())
                .map(Interval::toOpenClosedRange);
        return !rangeForExport.isPresent()
                || rangeForExport.get().hasUpperBound()
                && item.getSelector().isExportContinuousData() // data export time period is based on lastExportedPeriodEnd only in case of continuous data
                && item.getLastExportedNewData()
                .filter(rangeForExport.get().upperEndpoint()::isBefore)
                .isPresent();
    }
}
