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
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.energyict.mdc.sap.soap.webservices.impl.MeasurementTaskAssignmentChangeProcessor.DEFAULT_GROUP_NAME;
import static com.energyict.mdc.sap.soap.webservices.impl.MeasurementTaskAssignmentChangeProcessor.DEFAULT_TASK_NAME;

public class UpdateSapExportTaskHandler implements TaskExecutor {
    private final MeteringGroupsService meteringGroupsService;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final Clock clock;
    private final DataExportService dataExportService;

    public UpdateSapExportTaskHandler(MeteringGroupsService meteringGroupsService,
                                      SAPCustomPropertySets sapCustomPropertySets,
                                      Clock clock,
                                      DataExportService dataExportService) {
        this.meteringGroupsService = meteringGroupsService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.clock = clock;
        this.dataExportService = dataExportService;
    }

    @Override
    public void execute(TaskOccurrence taskOccurrence) {
        Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(WebServiceActivator.getExportTaskDeviceGroupName().orElse(DEFAULT_GROUP_NAME));
        Instant now = clock.instant();
        if (endDeviceGroup.isPresent()) {
            List<EnumeratedGroup.Entry<EndDevice>> devicesToRemove = ((EnumeratedEndDeviceGroup) endDeviceGroup.get()).getEntries().stream().filter(endDevice -> deviceCanBeRemoved(endDevice, now)).collect(Collectors.toList());
            if (!devicesToRemove.isEmpty()) {
                devicesToRemove.forEach(device -> ((EnumeratedEndDeviceGroup) endDeviceGroup.get()).remove(device));
            }
        }
    }

    private boolean deviceCanBeRemoved(EnumeratedGroup.Entry<EndDevice> endDeviceEntry, Instant now) {
        List<ChannelSpec> channelSpecs = sapCustomPropertySets.getChannelsWithProfileIdForDevice(endDeviceEntry.getMember().getId(), now);
        Optional<? extends ExportTask> exportTask = dataExportService
                .getReadingTypeDataExportTaskByName(WebServiceActivator.getExportTaskName().orElse(DEFAULT_TASK_NAME));
        if (!channelSpecs.isEmpty() && exportTask.isPresent()) {
            boolean isAlreadyExported = exportTask.get().getReadingDataSelectorConfig().isPresent()
                    && exportTask.get().getReadingDataSelectorConfig().get().getExportItems().stream()
                    .filter(item -> ((EndDevice) item.getDomainObject()).getId() == endDeviceEntry.getMember().getId())
                    .filter(item -> channelSpecs.stream().anyMatch(channel -> item.getReadingType().getMRID().equals(channel.getReadingType().getMRID())))
                    .allMatch(item -> item.getLastExportedDate().isPresent() && item.getLastExportedDate().get().isBefore(now));
            if (isAlreadyExported) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
