/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.ValidObisCode;
import com.energyict.mdc.common.device.config.DeleteEventType;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.LogBookSpec;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.device.config.exceptions.CannotChangeLogbookTypeOfLogbookSpecException;
import com.energyict.mdc.device.config.exceptions.LogbookTypeIsNotConfiguredOnDeviceTypeException;

import com.energyict.obis.ObisCode;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;

class LogBookSpecImpl extends PersistentIdObject<LogBookSpec> implements ServerLogBookSpec {

    private final Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.LOGBOOK_SPEC_LOGBOOK_TYPE_IS_REQUIRED + "}")
    private final Reference<LogBookType> logBookType = ValueReference.absent();
    private String overruledObisCodeString;
    @ValidObisCode(groups = { Save.Create.class, Save.Update.class })
    private ObisCode overruledObisCode;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    LogBookSpecImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(LogBookSpec.class, dataModel, eventService, thesaurus);
    }

    private LogBookSpecImpl initialize(DeviceConfiguration deviceConfiguration, LogBookType logBookType) {
        this.deviceConfiguration.set(deviceConfiguration);
        setLogBookType(logBookType);
        return this;
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return this.deviceConfiguration.get();
    }

    @Override
    public LogBookType getLogBookType() {
        return this.logBookType.get();
    }

    @Override
    public ObisCode getDeviceObisCode() {
        if (!Checks.is(this.overruledObisCodeString).empty()) {
            if (overruledObisCode!=null && overruledObisCode.toString().equals(this.overruledObisCodeString)) {
                return overruledObisCode; // to avoid making an invalid obis code valid
            }
            this.overruledObisCode = ObisCode.fromString(this.overruledObisCodeString);
            return overruledObisCode;
        }
        return getObisCode();
    }

    @Override
    public ObisCode getObisCode() {
        return getLogBookType().getObisCode();
    }

    @Override
    public void save() {
        validateDeviceTypeContainsLogbookType();
        super.save();
        getDataModel().touch(deviceConfiguration.get());
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    private void validateBeforeAddToConfiguration() {
        Save.CREATE.validate(this.getDataModel(), this);
        this.validateDeviceTypeContainsLogbookType();
    }

    private void validateDeviceTypeContainsLogbookType() {
        /* Here deviceType will contain different Java instance of logBookType, so we can't
           use the List.contains() without overriding the equals method */
        DeviceType deviceType = getDeviceConfiguration().getDeviceType();
        long expectedLogBookTypeId = getLogBookType().getId();
        for (LogBookType lbType : deviceType.getLogBookTypes()) {
            if (lbType.getId() == expectedLogBookTypeId) {
                return;
            }
        }
        throw new LogbookTypeIsNotConfiguredOnDeviceTypeException(getLogBookType(), this.getThesaurus(), MessageSeeds.LOGBOOK_SPEC_LOGBOOK_TYPE_IS_NOT_ON_DEVICE_TYPE);
    }

    private void validateUpdate() {
        Save.UPDATE.validate(this.getDataModel(), this);
    }

    @Override
    protected void doDelete() {
        this.getDeviceConfiguration().removeLogBookSpec(this);
    }

    @Override
    public void validateDelete() {
        // the configuration will validate the 'active' part
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.LOGBOOKSPEC;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.LOGBOOKSPEC;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.LOGBOOKSPEC;
    }

    @Override
    public String toString() {
        return getLogBookType().getName() + "/" + getObisCode().toString();
    }

    private void setLogBookType(LogBookType logBookType) {
        validateLogbookTypeForUpdate(logBookType);
        this.logBookType.set(logBookType);
    }

    private void validateLogbookTypeForUpdate(LogBookType loadProfileType) {
        if (this.logBookType.isPresent() && this.getLogBookType().getId() != loadProfileType.getId()) {
            throw new CannotChangeLogbookTypeOfLogbookSpecException(this.getThesaurus(), MessageSeeds.LOGBOOK_SPEC_CANNOT_CHANGE_LOGBOOK_TYPE);
        }
    }

    @Override
    public void setOverruledObisCode(ObisCode overruledObisCode) {
        if (overruledObisCode != null) {
            this.overruledObisCodeString = overruledObisCode.toString();
        } else {
            this.overruledObisCodeString = "";
        }
        this.overruledObisCode = overruledObisCode;
    }

    @Override
    public LogBookSpec cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        LogBookSpec.LogBookSpecBuilder builder = deviceConfiguration.createLogBookSpec(getLogBookType());
        builder.setOverruledObisCode(getObisCode().equals(getDeviceObisCode()) ? null : getDeviceObisCode());
        return builder.add();
    }

    abstract static class LogBookSpecBuilder implements LogBookSpec.LogBookSpecBuilder {

        final LogBookSpecImpl logBookSpec;

        LogBookSpecBuilder(Provider<LogBookSpecImpl> logBookSpecProvider, DeviceConfiguration deviceConfiguration, LogBookType logBookType) {
            this.logBookSpec = logBookSpecProvider.get().initialize(deviceConfiguration, logBookType);
        }

        @Override
        public LogBookSpec.LogBookSpecBuilder setOverruledObisCode(ObisCode overruledObisCode) {
            this.logBookSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public LogBookSpecImpl add() {
            this.logBookSpec.validateBeforeAddToConfiguration();
            return this.logBookSpec;
        }
    }

    abstract static class LogBookSpecUpdater implements LogBookSpec.LogBookSpecUpdater {

        final LogBookSpecImpl logBookSpec;

        LogBookSpecUpdater(LogBookSpecImpl logBookSpec) {
            this.logBookSpec = logBookSpec;
        }

        @Override
        public LogBookSpec.LogBookSpecUpdater setOverruledObisCode(ObisCode overruledObisCode) {
            this.logBookSpec.setOverruledObisCode(overruledObisCode);
            return this;
        }

        @Override
        public void update() {
            this.logBookSpec.validateUpdate();
            this.logBookSpec.save();
        }
    }
}
