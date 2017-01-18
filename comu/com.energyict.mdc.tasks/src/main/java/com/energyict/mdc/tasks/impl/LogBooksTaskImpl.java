package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.LOG_BOOKS_FLAG;
import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.SLAVE_DEVICES_FLAG;

/**
 * Implementation for a {@link com.energyict.mdc.tasks.LogBooksTask}
 *
 * @author gna
 * @since 2/05/12 - 11:11
 */
class LogBooksTaskImpl extends ProtocolTaskImpl implements LogBooksTask {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags(LOG_BOOKS_FLAG, SLAVE_DEVICES_FLAG);

    enum Fields {
        LOGBOOK_TYPE_USAGES("logBookTypeUsageInProtocolTasks");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private List<LogBookTypeUsageInProtocolTask> logBookTypeUsageInProtocolTasks = new ArrayList<>();

    @Inject
    LogBooksTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    void deleteDependents() {
        logBookTypeUsageInProtocolTasks.clear();
    }

    @Override
    public List<LogBookType> getLogBookTypes() {
        List<LogBookType> logBookTypes = new ArrayList<>(logBookTypeUsageInProtocolTasks.size());
        for (LogBookTypeUsageInProtocolTask logBookTypeUsageInProtocolTask : logBookTypeUsageInProtocolTasks) {
            logBookTypes.add(logBookTypeUsageInProtocolTask.getLogBookType());
        }
        return logBookTypes;
    }

    @Override
    public void setLogBookTypes(List<LogBookType> logBookTypes) {
        List<LogBookType> wantedLogBookTypes = new ArrayList<>(logBookTypes);
        Iterator<LogBookTypeUsageInProtocolTask> iterator = logBookTypeUsageInProtocolTasks.iterator();
        while(iterator.hasNext()){
            LogBookTypeUsageInProtocolTask logBookTypeUsageInProtocolTask = iterator.next();
            LogBookType stillWantedLogBookType = getById(wantedLogBookTypes, logBookTypeUsageInProtocolTask.getLogBookType().getId());
            if(stillWantedLogBookType == null){
                iterator.remove();
            } else {
                wantedLogBookTypes.remove(stillWantedLogBookType);
            }
        }

        for (LogBookType wantedLogBookType : wantedLogBookTypes) {
            LogBookTypeUsageInProtocolTaskImpl logBookTypeUsageInProtocolTask = new LogBookTypeUsageInProtocolTaskImpl();
            logBookTypeUsageInProtocolTask.setLogBooksTask(this);
            logBookTypeUsageInProtocolTask.setLogBookType(wantedLogBookType);
            this.logBookTypeUsageInProtocolTasks.add(logBookTypeUsageInProtocolTask);
        }
    }

}