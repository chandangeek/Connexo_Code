package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.protocol.api.calendars.ProtocolSupportedCalendarOptions;

import com.google.inject.Inject;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public class TimeOfUseOptionsImpl implements TimeOfUseOptions {

    enum Fields {
        DEVICETYPE("deviceType"),
        SEND_ACTIVITY_CALENDAR("send"),
        SEND_ACTIVITY_CALENDAR_WITH_DATE("sendWithDate"),
        SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_TYPE("sendWithDateAndType"),
        SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT("sendWithDateAndContract"),
        SEND_ACTIVITY_CALENDAR_WITH_DATETIME("sendWithDateTime"),
        SEND_SPECIAL_DAYS_CALENDAR("sendSpecialDays"),
        SEND_SPECIAL_DAYS_CALENDAR_WITH_TYPE("sendSpecialDaysWithType"),
        SEND_SPECIAL_DAYS_CALENDAR_WITH_CONTRACT_AND_DATE("sendSpecialDaysWithContractAndDate"),
        CLEAR_AND_DISABLE_PASSIVE_TARIFF("clearAndDisablePassiveTariff"),
        ACTIVATE_PASSIVE_CALENDAR("activatePassive");

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
    private boolean send;
    private boolean sendWithDate;
    private boolean sendWithDateAndType;
    private boolean sendWithDateAndContract;
    private boolean sendWithDateTime;
    private boolean sendSpecialDays;
    private boolean sendSpecialDaysWithType;
    private boolean sendSpecialDaysWithContractAndDate;
    private boolean clearAndDisablePassiveTariff;
    private boolean activatePassive;

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
    public TimeOfUseOptionsImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public TimeOfUseOptions init(DeviceType deviceType) {
        this.deviceType.set(deviceType);
        return this;
    }


    @Override
    public void setOptions(Set<ProtocolSupportedCalendarOptions> allowedOptions) {
        clearOptions();
        allowedOptions.stream().forEach(op -> {
            switch (op) {
                case SEND_ACTIVITY_CALENDAR:
                    this.send = true;
                    break;
                case SEND_ACTIVITY_CALENDAR_WITH_DATE:
                    this.sendWithDate = true;
                    break;
                case SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_TYPE:
                    this.sendWithDateAndType = true;
                    break;
                case SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT:
                    this.sendWithDateAndContract = true;
                    break;
                case SEND_ACTIVITY_CALENDAR_WITH_DATETIME:
                    this.sendWithDateTime = true;
                    break;
                case SEND_SPECIAL_DAYS_CALENDAR:
                    this.sendSpecialDays = true;
                    break;
                case SEND_SPECIAL_DAYS_CALENDAR_WITH_TYPE:
                    this.sendSpecialDaysWithType = true;
                    break;
                case SEND_SPECIAL_DAYS_CALENDAR_WITH_CONTRACT_AND_DATE:
                    this.sendSpecialDaysWithContractAndDate = true;
                    break;
                case CLEAR_AND_DISABLE_PASSIVE_TARIFF:
                    this.clearAndDisablePassiveTariff = true;
                    break;
                case ACTIVATE_PASSIVE_CALENDAR:
                    this.activatePassive = true;
                    break;

            }
        });
    }

    @Override
    public Set<ProtocolSupportedCalendarOptions> getOptions() {
        Set<ProtocolSupportedCalendarOptions> allowedOptions = new LinkedHashSet<>();
        if (send) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        }
        if (sendWithDate) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE);
        }
        if (sendWithDateAndType) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_TYPE);
        }
        if (sendWithDateAndContract) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT);
        }
        if (sendWithDateTime) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        }
        if (sendSpecialDays) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR);
        }
        if (sendSpecialDaysWithType) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_TYPE);
        }
        if (sendSpecialDaysWithContractAndDate) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_CONTRACT_AND_DATE);
        }
        if (clearAndDisablePassiveTariff) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.CLEAR_AND_DISABLE_PASSIVE_TARIFF);
        }
        if (activatePassive) {
            allowedOptions.add(ProtocolSupportedCalendarOptions.ACTIVATE_PASSIVE_CALENDAR);
        }

        return allowedOptions;
    }

    @Override
    public void save() {
        if (dataModel.mapper(TimeOfUseOptions.class).getUnique("deviceType", deviceType.get()).isPresent()) {
            doUpdate();
        } else {
            doPersist();
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

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
    }

    private void clearOptions() {
        send = false;
        sendWithDate = false;
        sendWithDateAndType = false;
        sendWithDateAndContract = false;
        sendWithDateTime = false;
        sendSpecialDays = false;
        sendSpecialDaysWithType = false;
        sendSpecialDaysWithContractAndDate = false;
        clearAndDisablePassiveTariff = false;
        activatePassive = false;
    }

    DeviceType getDeviceType(){
        return this.deviceType.get();
    }
}
