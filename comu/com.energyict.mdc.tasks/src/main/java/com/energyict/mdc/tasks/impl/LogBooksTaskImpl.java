package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.tasks.LogBooksTask;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.LOG_BOOKS_FLAG;
import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.SLAVE_DEVICES_FLAG;

/**
 * Implementation for a {@link com.energyict.mdc.tasks.LogBooksTask}
 *
 * @author gna
 * @since 2/05/12 - 11:11
 */
class LogBooksTaskImpl extends ProtocolTaskImpl implements LogBooksTask {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags(LOG_BOOKS_FLAG, SLAVE_DEVICES_FLAG);

    @Inject
    public LogBooksTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    public List<LogBookType> getLogBookTypes() {
        return Collections.emptyList(); // TODO Implement once JP-343 is done
    }

    @Override
    public void setLogBookTypes(List<LogBookType> logBookTypes) {
        // TODO Implement once JP-343 is done
    }

}