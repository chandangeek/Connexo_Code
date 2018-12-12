/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Functions;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.jupiter.export.enddevicegroup.deletionEventHandler", service = TopicHandler.class, immediate = true)
public class EndDeviceGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile DataExportService dataExportService;
    private volatile Thesaurus thesaurus;

    public EndDeviceGroupDeletionVetoEventHandler() {
    }

    @Inject
    public EndDeviceGroupDeletionVetoEventHandler(DataExportService dataExportService, Thesaurus thesaurus) {
        setDataExportService(dataExportService);
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus("DES", Layer.SERVICE);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        GroupEventData eventSource = (GroupEventData) localEvent.getSource();
        EndDeviceGroup endDeviceGroup = (EndDeviceGroup) eventSource.getGroup();
        List<? extends ExportTask> tasks = dataExportService.findReadingTypeDataExportTasks();

        tasks.stream()
                .map(ExportTask::getStandardDataSelectorConfig)
                .flatMap(Functions.asStream())
                .map(EndDeviceGroupGetter::getEndDeviceGroup)
                .flatMap(Functions.asStream())
                .filter(endDeviceGroup::equals)
                .findAny().ifPresent(readingTypeDataSelector -> {
            throw new VetoDeleteDeviceGroupException(thesaurus, endDeviceGroup);
        });
    }

    @Override
    public String getTopicMatcher() {
        return EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic();
    }

    private static class EndDeviceGroupGetter implements DataSelectorConfig.DataSelectorConfigVisitor {

        private EndDeviceGroup endDeviceGroup;

        private static Optional<EndDeviceGroup> getEndDeviceGroup(DataSelectorConfig selectorConfig) {
            EndDeviceGroupGetter visitor = new EndDeviceGroupGetter();
            selectorConfig.apply(visitor);
            return Optional.ofNullable(visitor.endDeviceGroup);
        }

        @Override
        public void visit(MeterReadingSelectorConfig config) {
            endDeviceGroup = config.getEndDeviceGroup();
        }

        @Override
        public void visit(UsagePointReadingSelectorConfig config) {
            // no device groups used
        }

        @Override
        public void visit(EventSelectorConfig config) {
            endDeviceGroup = config.getEndDeviceGroup();
        }
    }
}
