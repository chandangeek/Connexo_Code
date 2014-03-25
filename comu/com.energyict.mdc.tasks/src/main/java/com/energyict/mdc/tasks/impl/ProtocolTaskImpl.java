package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;

/**
 * Implementation for a {@link com.energyict.mdc.tasks.ProtocolTask}
 *
 * @author gna
 * @since 23/04/12 - 11:47
 */
abstract class ProtocolTaskImpl implements ProtocolTask, OfflineDeviceContext {
    protected static final String CLOCK_DISCRIMINATOR = "0";

    static final Map<String, Class<? extends ProtocolTask>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ProtocolTask>>of(
                    CLOCK_DISCRIMINATOR, ClockTaskImpl.class);

    private final DataModel dataModel;

    enum Fields {
        COM_TASK("comTask");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    private final Reference<ComTask> comTask= ValueReference.absent();
    private DeviceOfflineFlags flags;

    @Inject
    ProtocolTaskImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    protected void setFlags(DeviceOfflineFlags flags) {
        this.flags = flags;
    }

    public long getId() {
        return id;
    }

    @Override
    public void save() {
        Save.UPDATE.save(this.dataModel, this);
    }

    /**
     * Return the {@link ComTask} this ProtocolTask belongs to
     *
     * @return the ComTask of this ProtocolTask
     */
    @Override
    public ComTask getComTask() {
        return comTask.get();
    }

    public void ownedBy(final ComTask comTask) {
        this.comTask.set(comTask);
    }

    @Override
    public boolean needsSlaveDevices() {
        return flags.needsSlaveDevices();
    }

    @Override
    public boolean needsSentMessages() {
        return flags.needsSentMessages();
    }

    @Override
    public boolean needsRegisters() {
        return flags.needsRegisters();
    }

    @Override
    public boolean needsPendingMessages() {
        return flags.needsPendingMessages();
    }

    @Override
    public boolean needsMasterLoadProfiles() {
        return flags.needsMasterLoadProfiles();
    }

    @Override
    public boolean needsLogBooks() {
        return flags.needsLogBooks();
    }

    @Override
    public boolean needsAllLoadProfiles() {
        return flags.needsAllLoadProfiles();
    }

    /**
     * Inheritors should implement this method if they have internal elements that
     * require explicit removal before the element itself van be deleted,
     */
    void deleteDependents() {

    }

}