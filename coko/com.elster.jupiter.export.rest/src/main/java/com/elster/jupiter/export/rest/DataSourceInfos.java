package com.elster.jupiter.export.rest;

import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ReadingTypeDataExportTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igh on 24/11/2014.
 */
public class DataSourceInfos {

    public List<DataSourceInfo> dataSources = new ArrayList<DataSourceInfo>();

    public DataSourceInfos(List<? extends ReadingTypeDataExportItem> exportItems) {
        for (ReadingTypeDataExportItem item : exportItems) {
            dataSources.add(new DataSourceInfo(item));
        }
    }
}
