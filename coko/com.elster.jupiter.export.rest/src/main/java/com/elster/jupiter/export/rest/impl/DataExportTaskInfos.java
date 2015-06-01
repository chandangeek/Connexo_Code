package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class DataExportTaskInfos {

	public int total;
	public List<DataExportTaskInfo> dataExportTasks = new ArrayList<>();
	
	public DataExportTaskInfos() {
    }

    public DataExportTaskInfos(Iterable<? extends ReadingTypeDataExportTask> sets, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
	    addAll(sets, thesaurus, timeService, propertyUtils);
	}

    private DataExportTaskInfo add(ReadingTypeDataExportTask ruleSet, Thesaurus thesaurus, TimeService timService, PropertyUtils propertyUtils) {
        DataExportTaskInfo result = new DataExportTaskInfo(ruleSet, thesaurus, timService, propertyUtils);
        dataExportTasks.add(result);
	    total++;
	    return result;
	}

    private void addAll(Iterable<? extends ReadingTypeDataExportTask> sets, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
	    for (ReadingTypeDataExportTask each : sets) {
	        add(each, thesaurus, timeService, propertyUtils);
	    }
	}
}


