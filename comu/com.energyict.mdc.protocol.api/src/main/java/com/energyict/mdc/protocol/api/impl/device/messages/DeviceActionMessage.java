package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a summary of all messages related to general Device Actions.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum DeviceActionMessage implements DeviceMessageSpecEnum {

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
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.FTIONReboot, true, new StringFactory()));
        }
    },
    SetFTIONInitialize(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_INITIALIZE, "FTION initialize") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.FTIONInitialize, true, new StringFactory()));
        }
    },
    SetFTIONMailLog(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_MAIL_LOG, "FTION mail log") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.FTIONMailLog, true, new StringFactory()));
        }
    },
    SetFTIONSaveConfig(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_SAVE_CONFIG, "FTION save configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.FTIONSaveConfig, true, new StringFactory()));
        }
    },
    SetFTIONUpgrade(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_UPGRADE, "FTION upgrade") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.FTIONUpgrade, true, new StringFactory()));
        }
    },
    SetFTIONClearMem(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_CLEAR_MEM, "FTION clear memory") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.FTIONClearMem, true, new StringFactory()));
        }
    },
    SetFTIONMailConfig(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_MAIL_CONFIG, "FTION mail configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.FTIONMailConfig, true, new StringFactory()));
        }
    },
    SetFTIONModemReset(DeviceMessageId.DEVICE_ACTIONS_SET_FTION_MODEM_RESET, "FTION modem reset") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.FTIONModemReset, true, new StringFactory()));
        }
    },
    SetChangeAdminPassword(DeviceMessageId.DEVICE_ACTIONS_CHANGE_ADMIN_PASSWORD, "Change admin password") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.AdminOld, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.AdminNew, true, new StringFactory()));
        }
    },
    SetOutputOn(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_ON, "Set output on") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.OutputOn, true, new StringFactory()));
        }
    },
    SetOutputOff(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_OFF, "Set output off") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.OutputOff, true, new StringFactory()));
        }
    },
    SetOutputToggle(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_TOGGLE, "Set output toggle") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.OutputToggle, true, new StringFactory()));
        }
    },
    SetOutputPulse(DeviceMessageId.DEVICE_ACTIONS_SET_OUTPUT_PULSE, "Set output pulse") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.OutputPulse, true, new StringFactory()));
        }
    },
    SetAnalogOut(DeviceMessageId.DEVICE_ACTIONS_SET_ANALOG_OUT, "Set Analog out") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, true, analogOutPossibleValues()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.AnalogOutValue, true, new StringFactory()));
        }
    },
    BILLING_RESET_CONTRACT_1(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET_CONTRACT_1, "Billing reset contract 1"),
    BILLING_RESET_CONTRACT_2(DeviceMessageId.DEVICE_ACTIONS_BILLING_RESET_CONTRACT_2, "Billing reset contract 2"),
    SET_PASSIVE_EOB_DATETIME(DeviceMessageId.DEVICE_ACTIONS_SET_PASSIVE_EOB_DATETIME, "Set passive EOB date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(DeviceMessageConstants.contractAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.year, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.month, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.day, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.dayOfWeek, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.hour, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.minute, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.second, true, ""));
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
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.deviceId, true, ""));
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.trackingId, true, ""));
        }
    },
    FTIONUpgradeAndInit(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE_AND_INIT, "FTION upgrade and init"),
    FTIONUpgradeAndInitWithNewEIServerURL(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE_AND_INIT_WITH_NEW_EISERVER_URL, "FTION upgrade and init with new EIServer url"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.servletURL, true, ""));
        }
    },
    FTIONUpgradeWithNewEIServerURL(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE_WITH_NEW_EISERVER_URL, "FTION upgrade with new EIServer url"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.servletURL, true, ""));
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}