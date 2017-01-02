package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

class TimeOfUseOptionsImpl implements TimeOfUseOptions {

    enum Fields {
        DEVICETYPE("deviceType"),
        OPTION_BITS("options");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private long options;

    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;

    private final DataModel dataModel;

    @Inject
    TimeOfUseOptionsImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public TimeOfUseOptions init(DeviceType deviceType) {
        this.deviceType.set(deviceType);
        return this;
    }

    @Override
    public void setOptions(Set<ProtocolSupportedCalendarOptions> allowedOptions) {
        clearOptions();
        allowedOptions
                .stream()
                .mapToLong(this::toBitMask)
                .forEach(bitMask -> this.options = this.options | bitMask);
    }

    private void clearOptions() {
        this.options = 0;
    }

    private long toBitMask(ProtocolSupportedCalendarOptions option) {
        return 1 << option.ordinal();
    }

    @Override
    public Set<ProtocolSupportedCalendarOptions> getOptions() {
        EnumSet<ProtocolSupportedCalendarOptions> allowedOptions = EnumSet.noneOf(ProtocolSupportedCalendarOptions.class);
        Stream
                .of(ProtocolSupportedCalendarOptions.values())
                .filter(each -> (this.toBitMask(each) & this.options) != 0)
                .forEach(allowedOptions::add);
        return allowedOptions;
    }

    @Override
    public void save() {
        if (dataModel.mapper(TimeOfUseOptions.class).getUnique("deviceType", deviceType.get()).isPresent()) {
            Save.UPDATE.save(dataModel, this);
        } else {
            Save.CREATE.save(dataModel, this);
        }
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    DeviceType getDeviceType(){
        return this.deviceType.get();
    }

}