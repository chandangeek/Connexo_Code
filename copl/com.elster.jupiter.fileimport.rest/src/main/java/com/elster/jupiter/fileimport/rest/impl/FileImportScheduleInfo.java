package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lucian on 5/14/2015.
 */
public class FileImportScheduleInfo {

    public int id;
    public String name;
    public Boolean active;
    public String destinationName;
    public String importDirectory;
    public String inProcessDirectory;
    public String successDirectory;
    public String failureDirectory;
    public String pathMatcher;
    public String importerName;
    public Integer scanFrequency;
    public String application;
    public FileImporterInfo importerInfo;
    public List<PropertyInfo> properties = new ArrayList<>();

    public FileImportScheduleInfo(){

    }
    public FileImportScheduleInfo(ImportSchedule importSchedule, Thesaurus thesaurus) {

        active = importSchedule.isActive();
        name = importSchedule.getName();
        destinationName = importSchedule.getDestination().getName();
        importDirectory = importSchedule.getImportDirectory().getAbsolutePath();
        inProcessDirectory = importSchedule.getInProcessDirectory().getAbsolutePath();
        successDirectory = importSchedule.getSuccessDirectory().getAbsolutePath();
        failureDirectory = importSchedule.getFailureDirectory().getAbsolutePath();
        pathMatcher = importSchedule.getPathMatcher();
        importerName = importSchedule.getImporterName();
        application = importSchedule.getApplicationName();

        importerInfo = new FileImporterInfo(importerName,
                thesaurus.getStringBeyondComponent(importerName, importerName), Collections.<PropertyInfo>emptyList() );
        scanFrequency = ScanFrequency.toScanFrequency(importSchedule.getCronExpression());
        properties = new PropertyUtils().convertPropertySpecsToPropertyInfos(importSchedule.getPropertySpecs(), importSchedule.getProperties());

    }
}
