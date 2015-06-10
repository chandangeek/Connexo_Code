package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lucian on 5/14/2015.
 */
public class FileImportScheduleInfo {

    public long id;
    public String name;
    public Boolean active;
    public Boolean deleted;
    public Boolean importerAvailable;
    //public String destinationName;
    public String importDirectory;
    public String inProcessDirectory;
    public String successDirectory;
    public String failureDirectory;
    public String pathMatcher;
    public String importerName;
    public Integer scanFrequency;
    public String application;
    public FileImporterInfo importerInfo;
    public PeriodicalExpressionInfo schedule;
    public List<PropertyInfo> properties = new ArrayList<>();

    public FileImportScheduleInfo(){

    }
    public FileImportScheduleInfo(ImportSchedule importSchedule, Thesaurus thesaurus, PropertyUtils propertyUtils) {

        id = importSchedule.getId();
        active = importSchedule.isActive();
        name = importSchedule.getName();
        //destinationName = importSchedule.getDestination().getName();
        importDirectory = importSchedule.getImportDirectory().toString();
        inProcessDirectory = importSchedule.getInProcessDirectory().toString();
        successDirectory = importSchedule.getSuccessDirectory().toString();
        failureDirectory = importSchedule.getFailureDirectory().toString();
        pathMatcher = importSchedule.getPathMatcher();
        importerName = importSchedule.getImporterName();
        application = importSchedule.getApplicationName();
        deleted = importSchedule.isDeleted();
        importerAvailable = importSchedule.isImporterAvailable();

        importerInfo = new FileImporterInfo(importerName,
                thesaurus.getStringBeyondComponent(importerName, importerName), Collections.<PropertyInfo>emptyList() );
        scanFrequency = ScanFrequency.toScanFrequency(importSchedule.getScheduleExpression());

        if (Never.NEVER.equals(importSchedule.getScheduleExpression())) {
            schedule = null;
        } else {
            ScheduleExpression scheduleExpression = importSchedule.getScheduleExpression();
            if (scheduleExpression instanceof TemporalExpression) {
                schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
                scanFrequency = schedule.count;
            } else if (scheduleExpression instanceof PeriodicalScheduleExpression) {
                schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
                scanFrequency = schedule.count;
            }
            else{
                scanFrequency = ScanFrequency.toScanFrequency(scheduleExpression);
            }
        }

        properties = propertyUtils.convertPropertySpecsToPropertyInfos(importSchedule.getPropertySpecs(), importSchedule.getProperties());

    }
}
