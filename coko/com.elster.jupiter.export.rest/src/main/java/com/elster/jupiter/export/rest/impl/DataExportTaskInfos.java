package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.nls.Thesaurus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class DataExportTaskInfos {

	public int total;
	public List<DataExportTaskInfo> dataExportTasks = new ArrayList<>();
	
	public DataExportTaskInfos() {
    }

    public DataExportTaskInfos(Iterable<? extends ReadingTypeDataExportTask> sets, Thesaurus thesaurus) {
	    addAll(sets, thesaurus);
	}

    public DataExportTaskInfo add(ReadingTypeDataExportTask ruleSet, Thesaurus thesaurus) {
        DataExportTaskInfo result = new DataExportTaskInfo(ruleSet, thesaurus);
        dataExportTasks.add(result);
	    total++;
	    return result;
	}

    public void addAll(Iterable<? extends ReadingTypeDataExportTask> sets, Thesaurus thesaurus) {
	    for (ReadingTypeDataExportTask each : sets) {
	        add(each, thesaurus);
	    }
	}
}


