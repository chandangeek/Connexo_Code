/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.EventType;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(name="com.elster.jupiter.export.delete.relativeperiod.eventhandler", service = TopicHandler.class, immediate = true)
public class RelativePeriodDeletionEventHandler implements TopicHandler {

    private volatile TimeService timeService;
    private volatile IDataExportService dataExportService;

    @Override
    public void handle(LocalEvent localEvent) {
        RelativePeriod relativePeriod = (RelativePeriod) localEvent.getSource();
        List<ExportTask> using = dataExportService.findExportTaskUsing(relativePeriod);
        if (!using.isEmpty()) {
            throw new RelativePeriodInUseException(getThesaurus(), using);
        }
    }

    private Thesaurus getThesaurus() {
        return dataExportService.getThesaurus();
    }

    @Override
    public String getTopicMatcher() {
        return EventType.RELATIVE_PERIOD_DELETED.topic();
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        this.dataExportService = (IDataExportService) dataExportService;
    }
}