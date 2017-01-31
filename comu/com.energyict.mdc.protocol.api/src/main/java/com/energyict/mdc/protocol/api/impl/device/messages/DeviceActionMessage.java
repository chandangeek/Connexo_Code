/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

enum DeviceActionMessage implements DeviceMessageSpecEnum {

    BILLING_RESET(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET, "Billing reset"),
    GLOBAL_METER_RESET(DeviceMessageId.DEVICE_ACTIONS_GLOBAL_METER_RESET, "Global meter reset"),
    DEMAND_RESET(DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET, "Demand reset"),
    POWER_OUTAGE_RESET(DeviceMessageId.DEVICE_ACTIONS_POWER_OUTAGE_RESET, "Power outage reset"),
    POWER_QUALITY_RESET(DeviceMessageId.DEVICE_ACTIONS_POWER_QUALITY_RESET, "Power quality reset"),
    ERROR_STATUS_RESET(DeviceMessageId.DEVICE_ACTIONS_ERROR_STATUS_RESET, "Error status reset"),
    REGISTERS_RESET(DeviceMessageId.DEVICE_ACTIONS_REGISTERS_RESET, "Registers reset"),
    LOAD_LOG_RESET(DeviceMessageId.DEVICE_ACTIONS_LOAD_LOG_RESET, "Load log reset"),
    EVENT_LOG_RESET(DeviceMessageId.DEVICE_ACTIONS_EVENT_LOG_RESET, "Event log reset"),
    ALARM_REGISTER_RESET(DeviceMessageId.DEVICE_ACTIONS_ALARM_REGISTER_RESET, "Alarm register reset"),
    ERROR_REGISTER_RESET(DeviceMessageId.DEVICE_ACTIONS_ERROR_REGISTER_RESET, "Error register reset"),
    REBOOT_DEVICE(DeviceMessageId.DEVICE_ACTIONS_REBOOT_DEVICE, "Reboot device"),
    DISABLE_WEBSERVER(DeviceMessageId.DEVICE_ACTIONS_DISABLE_WEBSERVER, "Disable webserver"),
    ENABLE_WEBSERVER(DeviceMessageId.DEVICE_ACTIONS_ENABLE_WEBSERVER, "Enable webserver"),
    RESTORE_FACTORY_SETTINGS(DeviceMessageId.DEVICE_ACTIONS_RESTORE_FACTORY_SETTINGS, "Restore factory settings"),
    SetFTIONReboot(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_REBOOT, "FTION reboot") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.FTIONReboot, propertySpecService, thesaurus));
        }
    },
    SetFTIONInitialize(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_INITIALIZE, "FTION initialize") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.FTIONInitialize, propertySpecService, thesaurus));
        }
    },
    SetFTIONMailLog(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_MAIL_LOG, "FTION mail log") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.FTIONMailLog, propertySpecService, thesaurus));
        }
    },
    SetFTIONSaveConfig(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_SAVE_CONFIG, "FTION save configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.FTIONSaveConfig, propertySpecService, thesaurus));
        }
    },
    SetFTIONUpgrade(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_UPGRADE, "FTION upgrade") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.FTIONUpgrade, propertySpecService, thesaurus));
        }
    },
    SetFTIONClearMem(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_CLEAR_MEM, "FTION clear memory") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.FTIONClearMem, propertySpecService, thesaurus));
        }
    },
    SetFTIONMailConfig(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_MAIL_CONFIG, "FTION mail configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.FTIONMailConfig, propertySpecService, thesaurus));
        }
    },
    SetFTIONModemReset(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_MODEM_RESET, "FTION modem reset") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.FTIONModemReset, propertySpecService, thesaurus));
        }
    },
    SetChangeAdminPassword(DeviceMessageId.DEVICE_ACTIONS_CHANGE_ADMIN_PASSWORD, "Change admin password") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.AdminOld, propertySpecService, thesaurus));
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.AdminNew, propertySpecService, thesaurus));
        }
    },
    SetOutputOn(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_ON, "Set output on") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.OutputOn, propertySpecService, thesaurus));
        }
    },
    SetOutputOff(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_OFF, "Set output off") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.OutputOff, propertySpecService, thesaurus));
        }
    },
    SetOutputToggle(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_TOGGLE, "Set output toggle") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.OutputToggle, propertySpecService, thesaurus));
        }
    },
    SetOutputPulse(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_PULSE, "Set output pulse") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.OutputPulse, propertySpecService, thesaurus));
        }
    },
    SetAnalogOut(DeviceMessageId.DEVICE_ACTIONS_SET_ANALOG_OUT, "Set Analog out") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageConstants.id, DeviceMessageAttributes.DeviceActionMessageId)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(analogOutPossibleValues())
                            .finish());
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.AnalogOutValue, propertySpecService, thesaurus));
        }
    },
    BILLING_RESET_CONTRACT_1(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET_CONTRACT_1, "Billing reset contract 1"),
    BILLING_RESET_CONTRACT_2(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET_CONTRACT_2, "Billing reset contract 2"),
    SET_PASSIVE_EOB_DATETIME(DeviceMessageId.DEVICE_ACTIONS_SET_PASSIVE_EOB_DATETIME, "Set passive EOB date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageConstants.contractAttributeName, DeviceMessageAttributes.DeviceActionMessageContract)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(BigDecimal.ONE, BigDecimals.TWO)
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.year, DeviceMessageAttributes.DeviceActionMessageYear)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.month, DeviceMessageAttributes.DeviceActionMessageMonth)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.day, DeviceMessageAttributes.DeviceActionMessageDay)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.dayOfWeek, DeviceMessageAttributes.DeviceActionMessageDayOfWeek)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.hour, DeviceMessageAttributes.DeviceActionMessageHour)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.minute, DeviceMessageAttributes.DeviceActionMessageMinute)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageConstants.second, DeviceMessageAttributes.DeviceActionMessageSecond)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    REBOOT_APPLICATION(DeviceMessageId.DEVICE_ACTIONS_REBOOT_APPLICATION, "Reboot the application"),
    DEMAND_RESET_WITH_FORCE_CLOCK(DeviceMessageId.DEVICE_ACTIONS_DEMAND_RESET_WITH_FORCE_CLOCK, "Demand reset with force clock"),
    HARD_RESET_DEVICE(DeviceMessageId.DEVICE_ACTIONS_HARD_RESET_DEVICE, "Hard reset the device"),
    FTIONUpgrade(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE, "FTION upgrade"),
    RtuPlusServerEnterMaintenanceMode(DeviceMessageId.DEVICE_ACTIONS_RTU_PLUS_SERVER_ENTER_MAINTENANCE_MODE, "RtuPlusServer enter maintenance mode"),
    RtuPlusServerExitMaintenanceMode(DeviceMessageId.DEVICE_ACTIONS_RTU_PLUS_SERVER_EXIT_MAINTENANCE_MODE, "RtuPlusServer exit maintencance mode"),
    ForceMessageToFailed(DeviceMessageId.DEVICE_ACTIONS_FORCE_MESSAGE_TO_FAILED, "Force message to failed"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.deviceId, propertySpecService, thesaurus));
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.trackingId, propertySpecService, thesaurus));
        }
    },
    FTIONUpgradeAndInit(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE_AND_INIT, "FTION upgrade and init"),
    FTIONUpgradeAndInitWithNewEIServerURL(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE_AND_INIT_WITH_NEW_EISERVER_URL, "FTION upgrade and init with new EIServer url"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.servletURL, propertySpecService, thesaurus));
        }
    },
    FTIONUpgradeWithNewEIServerURL(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE_WITH_NEW_EISERVER_URL, "FTION upgrade with new EIServer url"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringSpec(DeviceMessageAttributes.servletURL, propertySpecService, thesaurus));
        }
    },
    FTIONInitDatabaseKeepConfig(DeviceMessageId.DEVICE_ACTIONS_FTION_INIT_DATABASE_KEEP_CONFIG, "FTION init database and keep configuration"),
    FTIONReboot(DeviceMessageId.DEVICE_ACTIONS_FTION_REBOOT, "FTION reboot"),
    FTIONRestart(DeviceMessageId.DEVICE_ACTIONS_FTION_RESTART, "FTION restart"),
    FTIONScanBus(DeviceMessageId.DEVICE_ACTIONS_FTION_SCAN_BUS, "FTION scan bus"),
    SyncMasterdata(DeviceMessageId.DEVICE_ACTIONS_SYNC_MASTERDATA, "Synchronize masterdata"),
    ;

    private static BigDecimal[] analogOutPossibleValues() {
        BigDecimal[] result = new BigDecimal[16];
        for (int index = 17; index <= 32; index++) {
            result[index - 17] = BigDecimal.valueOf(index);
        }
        return result;
    }

    private DeviceMessageId id;
    private String defaultTranslation;

    DeviceActionMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return DeviceActionMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

    protected PropertySpec stringSpec(DeviceMessageAttributes name, PropertySpecService service, Thesaurus thesaurus) {
        return service.stringSpec().named(name).fromThesaurus(thesaurus).markRequired().finish();
    }

}