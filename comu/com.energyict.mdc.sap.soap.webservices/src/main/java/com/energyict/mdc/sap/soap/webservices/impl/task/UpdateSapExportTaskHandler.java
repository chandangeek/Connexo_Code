/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.task;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import java.util.Optional;

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
        if (endDeviceGroup.isPresent()) {
            ((EnumeratedEndDeviceGroup) endDeviceGroup.get()).getEntries().stream().filter(endDevice -> deviceCanBeRemoved(endDevice))
                    .forEach(device -> ((EnumeratedEndDeviceGroup) endDeviceGroup.get()).remove(device));
        }
    }

    private boolean deviceCanBeRemoved(EnumeratedGroup.Entry<EndDevice> endDeviceEntry) {
        Optional<? extends ExportTask> exportTask = dataExportService.getReadingTypeDataExportTaskByName(WebServiceActivator.getExportTaskName().orElse(DEFAULT_TASK_NAME));
        if (exportTask.isPresent()) {
            boolean isAlreadyExported = exportTask.get().getReadingDataSelectorConfig().isPresent()
                    && exportTask.get().getReadingDataSelectorConfig().get().getExportItems().stream()
                    .filter(item -> ((EndDevice) item.getDomainObject()).getId() == endDeviceEntry.getMember().getId())
                    .allMatch(item -> {
                        Optional<Interval> lastProfileIdInterval = sapCustomPropertySets.getLastProfileIdIntervalForChannelOnDevice(Long.parseLong(endDeviceEntry.getMember().getAmrId()),
                                item.getReadingType().getMRID());
                        if (lastProfileIdInterval.isPresent()) {
                            if (lastProfileIdInterval.get().getEnd() == null) {
                                return false;
                            }
                            return item.getLastExportedPeriodEnd().isPresent() && item.getLastExportedPeriodEnd().get().isAfter(lastProfileIdInterval.get().getEnd());
                        }
                        return true;
                    });

            return isAlreadyExported;
        }
        return true;
    }
}
