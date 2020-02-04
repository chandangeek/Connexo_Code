/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

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
        Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(WebServiceActivator.getExportTaskDeviceGroupName().orElse(DEFAULT_GROUP_NAME));
        Optional<? extends ExportTask> exportTask = dataExportService.getReadingTypeDataExportTaskByName(WebServiceActivator.getExportTaskName().orElse(DEFAULT_TASK_NAME));
        if (endDeviceGroup.isPresent()) {
            if (exportTask.isPresent()) {
                Map<IdentifiedObject, List<ReadingTypeDataExportItem>> exportItemsMap = exportTask.get().getReadingDataSelectorConfig().get().getExportItems().stream()
                        .collect(Collectors.groupingBy(item -> item.getDomainObject()));
                ((EnumeratedEndDeviceGroup) endDeviceGroup.get()).getEntries().stream()
                        .filter(endDevice -> deviceCanBeRemoved(exportTask.get(), exportItemsMap, endDevice.getMember()))
                        .forEach(device -> ((EnumeratedEndDeviceGroup) endDeviceGroup.get()).remove(device));
            } else {
                endDeviceGroup.get().delete();
            }
        }
    }

    private boolean deviceCanBeRemoved(ExportTask exportTask, Map<IdentifiedObject, List<ReadingTypeDataExportItem>> exportItemsMap, EndDevice member) {
        boolean isSelectorConfigPresent = exportTask.getReadingDataSelectorConfig().isPresent();
        if (isSelectorConfigPresent) {
            List<? extends ReadingTypeDataExportItem> exportItems = exportItemsMap.getOrDefault(member, Collections.emptyList());
            if (exportItems.isEmpty()) {
                if (member instanceof Meter) {
                    Set<ReadingType> readingTypeSet = exportTask.getReadingDataSelectorConfig().get().getReadingTypes();
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
        } else {
            return false;
        }
    }

    private boolean isNothingToExportOnDataSource(ReadingTypeDataExportItem item, EndDevice member) {
        Optional<Interval> lastProfileIdInterval = sapCustomPropertySets.getLastProfileIdIntervalForChannelOnDevice(Long.parseLong(member.getAmrId()),
                item.getReadingType().getMRID());
        if (lastProfileIdInterval.isPresent()) {
            if (lastProfileIdInterval.get().getEnd() == null) {
                return false;
            }
            return item.getLastExportedPeriodEnd().isPresent() && item.getLastExportedPeriodEnd().get().isAfter(lastProfileIdInterval.get().getEnd());
        }
        return true;
    }
}
