package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.ExportTask;
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

    public DataExportTaskInfos(Iterable<? extends ExportTask> sets, Thesaurus thesaurus, TimeService timeService) {
	    addAll(sets, thesaurus, timeService);
	}

    public DataExportTaskInfo add(ExportTask ruleSet, Thesaurus thesaurus, TimeService timService) {
        DataExportTaskInfo result = new DataExportTaskInfo(ruleSet, thesaurus, timService);
        dataExportTasks.add(result);
	    total++;
	    return result;
	}

    public void addAll(Iterable<? extends ExportTask> sets, Thesaurus thesaurus, TimeService timeService) {
	    for (ExportTask each : sets) {
	        add(each, thesaurus, timeService);
	    }
	}
}


