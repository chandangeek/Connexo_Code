/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for a {@link com.energyict.mdc.tasks.ProtocolTask}
 *
 * @author gna
 * @since 23/04/12 - 11:47
 */
abstract class ProtocolTaskImpl implements ServerProtocolTask, OfflineDeviceContext {
    protected static final String BASIC_CHECK_DISCRIMINATOR = "0";
    protected static final String CLOCK_DISCRIMINATOR = "1";
    protected static final String MESSAGES_DISCRIMINATOR = "2";
    protected static final String LOAD_PROFILES_DISCRIMINATOR = "3";
    protected static final String LOG_BOOKS_DISCRIMINATOR = "4";
    protected static final String REGISTER_TASK_DISCRIMINATOR = "5";
    protected static final String STATUS_INFORMATION_DISCRIMINATOR = "6";
    protected static final String TOPOLOGY_DISCRIMINATOR = "7";
    protected static final String FIRMWARE_DISCRIMINATOR = "8";

    static final Map<String, Class<? extends ProtocolTask>> IMPLEMENTERS = new HashMap<>();
    static {
        IMPLEMENTERS.put(BASIC_CHECK_DISCRIMINATOR, BasicCheckTaskImpl.class);
        IMPLEMENTERS.put(CLOCK_DISCRIMINATOR, ClockTaskImpl.class);
        IMPLEMENTERS.put(MESSAGES_DISCRIMINATOR, MessagesTaskImpl.class);
        IMPLEMENTERS.put(LOAD_PROFILES_DISCRIMINATOR, LoadProfilesTaskImpl.class);
        IMPLEMENTERS.put(LOG_BOOKS_DISCRIMINATOR, LogBooksTaskImpl.class);
        IMPLEMENTERS.put(REGISTER_TASK_DISCRIMINATOR, RegistersTaskImpl.class);
        IMPLEMENTERS.put(STATUS_INFORMATION_DISCRIMINATOR, StatusInformationTaskImpl.class);
        IMPLEMENTERS.put(TOPOLOGY_DISCRIMINATOR, TopologyTaskImpl.class);
        IMPLEMENTERS.put(FIRMWARE_DISCRIMINATOR, FirmwareManagementTaskImpl.class);
    }

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
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    ProtocolTaskImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    protected TimeDuration postLoad(TimeDuration timeDuration) {
        if (timeDuration != null && timeDuration.getTimeUnitCode() <= 0) {
            /* The TimeDuration was injected by ORM but then the latter
             * realized to late that the database values were actually
             * null so it has not injected a value nor a unit. */
            return null;
        }
        else {
            return timeDuration;
        }
    }

    protected DataModel getDataModel() {
        return dataModel;
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
        comTask.map(ComTaskImpl.class::cast).get().touch();
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

    @Override
    public boolean needsFirmwareVersions() {
        return flags.needsFirmwareVersions();
    }

    @Override
    public boolean needsTouCalendar() {
        return flags.needsTouCalendar();
    }

    protected <T extends HasId> T getById(List<T> list, long id) {
        for (T t : list) {
            if (t.getId()==id) {
                return t;
            }
        }
        return null;
    }

    /**
     * Inheritors should implement this method if they have internal elements that
     * require explicit removal before the element itself van be deleted,
     */
    abstract void deleteDependents();

}