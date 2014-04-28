package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.impl.offline.OfflineLogBookImpl;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;

import javax.inject.Inject;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 25/03/14
 * Time: 15:50
 */
public class LogBookImpl implements LogBook {

    private final DataModel dataModel;

    private long id;
    private Reference<Device> device = ValueReference.absent();
    private Reference<LogBookSpec> logBookSpec = ValueReference.absent();
    private UtcInstant lastReading;


    @Inject
    public LogBookImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    LogBookImpl initialize(LogBookSpec logBookSpec, Device device) {
        this.logBookSpec.set(logBookSpec);
        this.device.set(device);
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Device getDevice() {
        return this.device.get();
    }

    @Override
    public Date getLastLogBook() {
        if (lastReading != null) {
            return lastReading.toDate();
        } else {
            return null;
        }
    }

    private void update() {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public ObisCode getDeviceObisCode() {
        return getLogBookSpec().getDeviceObisCode();
    }

    @Override
    public LogBookSpec getLogBookSpec() {
        return logBookSpec.orNull();
    }

    @Override
    public LogBookType getLogBookType() {
        return getLogBookSpec().getLogBookType();
    }

    @Override
    public OfflineLogBook goOffline() {
        return new OfflineLogBookImpl(this);
    }

    abstract static class LogBookUpdater implements LogBook.LogBookUpdater {

        final LogBookImpl logBook;

        protected LogBookUpdater(LogBookImpl logBook) {
            this.logBook = logBook;
        }

        @Override
        public LogBook.LogBookUpdater setLastLogBookIfLater(Date lastReading) {
            UtcInstant logBookLastReading = this.logBook.lastReading;
            if (lastReading != null && (logBookLastReading == null || lastReading.after(logBookLastReading.toDate()))) {
                this.logBook.lastReading = new UtcInstant(lastReading);
            }
            return this;
        }

        @Override
        public void update() {
            this.logBook.update();
        }
    }
}
