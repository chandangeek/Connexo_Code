/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;

public class FileImportScheduleInfoFactory {

    private final AppService appService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final FileImportService fileImportService;
    private final AppNamesProvider appNamesProvider;
    private final FileImporterInfoFactory fileImporterInfoFactory;

    @Inject
    public FileImportScheduleInfoFactory(AppService appService, PropertyValueInfoService propertyValueInfoService, FileImportService fileImportService, AppNamesProvider appNamesProvider, FileImporterInfoFactory fileImporterInfoFactory) {
        this.appService = appService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.fileImportService = fileImportService;
        this.appNamesProvider = appNamesProvider;
        this.fileImporterInfoFactory = fileImporterInfoFactory;
    }

    public FileImportScheduleInfo asInfo(ImportSchedule importSchedule) {
        FileImportScheduleInfo info = new FileImportScheduleInfo();
        info.id = importSchedule.getId();
        info.active = importSchedule.isActive();
        info.name = importSchedule.getName();
        info.importDirectory = importSchedule.getImportDirectory().toString();
        info.inProcessDirectory = importSchedule.getInProcessDirectory().toString();
        info.successDirectory = importSchedule.getSuccessDirectory().toString();
        info.failureDirectory = importSchedule.getFailureDirectory().toString();
        info.pathMatcher = importSchedule.getPathMatcher();
        info.importerName = importSchedule.getImporterName();
        info.application = appNamesProvider.findAppNameByKey(importSchedule.getApplicationName());
        info.deleted = importSchedule.isDeleted();
        info.importerAvailable = importSchedule.isImporterAvailable();
        info.isActiveOnUI = importSchedule.getActiveOnUI();

        info.importerInfo = fileImporterInfoFactory.asInfo(fileImportService.getImportFactory(importSchedule.getImporterName()).get());
        info.scanFrequency = ScanFrequency.toScanFrequency(importSchedule.getScheduleExpression());

        if (Never.NEVER.equals(importSchedule.getScheduleExpression())) {
            info.schedule = null;
        } else {
            ScheduleExpression scheduleExpression = importSchedule.getScheduleExpression();
            if (scheduleExpression instanceof TemporalExpression) {
                info.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
                info.scanFrequency = info.schedule.count;
            } else if (scheduleExpression instanceof PeriodicalScheduleExpression) {
                info.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
                info.scanFrequency = info.schedule.count;
            } else {
                info.scanFrequency = ScanFrequency.toScanFrequency(scheduleExpression);
            }
        }

        info.scheduled = !appService.getImportScheduleAppServers(importSchedule.getId()).isEmpty();
        info.properties = propertyValueInfoService.getPropertyInfos(importSchedule.getPropertySpecs(), importSchedule.getProperties());
        info.version = importSchedule.getVersion();
        return info;
    }
}
