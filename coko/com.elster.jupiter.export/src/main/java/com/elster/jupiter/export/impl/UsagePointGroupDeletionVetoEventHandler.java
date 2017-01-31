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
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.GroupEventData;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Functions;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.jupiter.export.usagepointgroup.deletionEventHandler", service = TopicHandler.class, immediate = true)
public class UsagePointGroupDeletionVetoEventHandler implements TopicHandler {

    private volatile DataExportService dataExportService;
    private volatile Thesaurus thesaurus;

    @SuppressWarnings("unused") // for OSGI
    public UsagePointGroupDeletionVetoEventHandler() {
    }

    @Inject
    public UsagePointGroupDeletionVetoEventHandler(DataExportService dataExportService, Thesaurus thesaurus) {
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
        UsagePointGroup usagePointGroup = (UsagePointGroup) eventSource.getGroup();
        List<? extends ExportTask> tasks = dataExportService.findExportTasks().find();

        tasks.stream()
                .map(ExportTask::getStandardDataSelectorConfig)
                .flatMap(Functions.asStream())
                .map(UsagePointGroupGetter::getUsagePointGroup)
                .flatMap(Functions.asStream())
                .filter(usagePointGroup::equals)
                .findAny()
                .ifPresent(selector -> {
                    throw new VetoDeleteUsagePointGroupException(thesaurus, usagePointGroup);
                });
    }

    @Override
    public String getTopicMatcher() {
        return EventType.USAGEPOINTGROUP_VALIDATE_DELETED.topic();
    }

    private static class UsagePointGroupGetter implements DataSelectorConfig.DataSelectorConfigVisitor {

        private UsagePointGroup usagePointGroup;

        private static Optional<UsagePointGroup> getUsagePointGroup(DataSelectorConfig selectorConfig) {
            UsagePointGroupGetter visitor = new UsagePointGroupGetter();
            selectorConfig.apply(visitor);
            return Optional.ofNullable(visitor.usagePointGroup);
        }

        @Override
        public void visit(MeterReadingSelectorConfig config) {
            // no usage point groups used
        }

        @Override
        public void visit(UsagePointReadingSelectorConfig config) {
            usagePointGroup = config.getUsagePointGroup();
        }

        @Override
        public void visit(EventSelectorConfig config) {
            // no usage point group used
        }
    }
}
