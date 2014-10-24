package com.elster.jupiter.export.rest;

import com.elster.jupiter.export.ReadingTypeDataExportTask;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class DataExportTaskInfos {

	public int total;
	public List<DataExportTaskInfo> dataExportTasks = new ArrayList<>();
	
	public DataExportTaskInfos() {
    }

    public DataExportTaskInfos(Iterable<? extends ReadingTypeDataExportTask> sets) {
	    addAll(sets);
	}

    public DataExportTaskInfo add(ReadingTypeDataExportTask ruleSet) {
        DataExportTaskInfo result = new DataExportTaskInfo(ruleSet);
        dataExportTasks.add(result);
	    total++;
	    return result;
	}

    public void addAll(Iterable<? extends ReadingTypeDataExportTask> sets) {
	    for (ReadingTypeDataExportTask each : sets) {
	        add(each);
	    }
	}
}


