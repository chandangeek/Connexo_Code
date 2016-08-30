package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
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

    public DataExportTaskInfos(Iterable<? extends ExportTask> sets, Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService, boolean withExtendedHistory) {
	    if(withExtendedHistory){
			addAllWithExtendedHistory(sets, thesaurus, timeService, propertyValueInfoService);
		} else {
			addAllWithoutExtendedHistory(sets, thesaurus, timeService, propertyValueInfoService);
		}
	}

    private DataExportTaskInfo addWithExtendedHistory(ExportTask ruleSet, Thesaurus thesaurus, TimeService timService, PropertyValueInfoService propertyValueInfoService) {
        DataExportTaskInfo result = new DataExportTaskInfo(ruleSet, thesaurus, timService, propertyValueInfoService);
        dataExportTasks.add(result);
	    total++;
	    return result;
	}

	private DataExportTaskInfo addWithoutExtendedHistory(ExportTask ruleSet, Thesaurus thesaurus, TimeService timService, PropertyValueInfoService propertyValueInfoService) {
		DataExportTaskInfoWithoutExtendedHistory result = new DataExportTaskInfoWithoutExtendedHistory(ruleSet, thesaurus, timService, propertyValueInfoService);
		dataExportTasks.add(result);
		total++;
		return result;
	}

    private void addAllWithExtendedHistory(Iterable<? extends ExportTask> sets, Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService) {
	    for (ExportTask each : sets) {
			addWithExtendedHistory(each, thesaurus, timeService, propertyValueInfoService);
	    }
	}

	private void addAllWithoutExtendedHistory(Iterable<? extends ExportTask> sets, Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService) {
		for (ExportTask each : sets) {
			addWithoutExtendedHistory(each, thesaurus, timeService, propertyValueInfoService);
		}
	}
}


