package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;

import java.util.ArrayList;
import java.util.List;

public class FileImportScheduleInfo {

    public long id;
    public String name;
    public Boolean active;
    public Boolean deleted;
    public Boolean scheduled;
    public Boolean importerAvailable;
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

}
