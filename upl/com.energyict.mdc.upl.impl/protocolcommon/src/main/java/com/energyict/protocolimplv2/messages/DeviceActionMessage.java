package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceGroup;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.protocolimplv2.messages.enums.DaysOfMonth;
import com.energyict.protocolimplv2.messages.enums.DaysOfWeek;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Provides a summary of all messages related to general Device Actions.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum DeviceActionMessage implements DeviceMessageSpecSupplier {

    BILLING_RESET(8001, "Billing reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    BILLING_RESET_CONTRACT_1(8030, "Billing reset contract 1") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    BILLING_RESET_CONTRACT_2(8031, "Billing reset contract 2") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SET_PASSIVE_EOB_DATETIME(8032, "Write the passive end of billing date and time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.contractAttributeName, DeviceMessageConstants.contractAttributeDefaultTranslation, BigDecimal.ONE, BigDecimal.valueOf(2)),
                    this.stringSpec(service, DeviceMessageConstants.year, DeviceMessageConstants.yearDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.month, DeviceMessageConstants.monthDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.day, DeviceMessageConstants.dayDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.dayOfWeek, DeviceMessageConstants.dayOfWeekDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.hour, DeviceMessageConstants.hourDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.minute, DeviceMessageConstants.minuteDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.second, DeviceMessageConstants.secondDefaultTranslation)
            );
        }
    },
    GLOBAL_METER_RESET(8002, "Global meter reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    DEMAND_RESET(8003, "Demand reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    POWER_OUTAGE_RESET(8004, "Power outage reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    POWER_QUALITY_RESET(8005, "Power quality reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ERROR_STATUS_RESET(8006, "Error status reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    REGISTERS_RESET(8007, "Registers reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    LOAD_LOG_RESET(8008, "Load log reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    EVENT_LOG_RESET(8009, "Event log reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ALARM_REGISTER_RESET(8010, "Alarm register reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ERROR_REGISTER_RESET(8011, "Error register reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    REBOOT_DEVICE(8012, "Reboot the device") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    DISABLE_WEBSERVER(8013, "Disable webserver") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ENABLE_WEBSERVER(8014, "Enable webserver") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    RESTORE_FACTORY_SETTINGS(8015, "Restore factory settings") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SetFTIONReboot(8016, "FTION reboot") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.FTIONReboot, DeviceMessageConstants.FTIONRebootDefaultTranslation));
        }
    },
    SetFTIONInitialize(8017, "FTION initialize") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.FTIONInitialize, DeviceMessageConstants.FTIONInitializeDefaultTranslation));
        }
    },
    SetFTIONMailLog(8018, "FTION mail log") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.FTIONMailLog, DeviceMessageConstants.FTIONMailLogDefaultTranslation));
        }
    },
    SetFTIONSaveConfig(8019, "FTION save configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.FTIONSaveConfig, DeviceMessageConstants.FTIONSaveConfigDefaultTranslation));
        }
    },
    SetFTIONUpgrade(8020, "FTION upgrade") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.FTIONUpgrade, DeviceMessageConstants.FTIONUpgradeDefaultTranslation));
        }
    },
    SetFTIONClearMem(8021, "FTION clear memory") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.FTIONClearMem, DeviceMessageConstants.FTIONClearMemDefaultTranslation));
        }
    },
    SetFTIONMailConfig(8022, "FTION mail configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.FTIONMailConfig, DeviceMessageConstants.FTIONMailConfigDefaultTranslation));
        }
    },
    SetFTIONModemReset(8023, "FTION modem reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.FTIONModemReset, DeviceMessageConstants.FTIONModemResetDefaultTranslation));
        }
    },
    SetChangeAdminPassword(8024, "Change admin password") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.keyAccessorTypeReferenceSpec(service, DeviceMessageConstants.AdminPassword, DeviceMessageConstants.AdminPasswordDefaultTranslation)
            );
        }
    },
    SetAnalogOut(8029, "Set analog out") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.id, DeviceMessageConstants.idDefaultTranslation, getPossibleValues()),
                    this.stringSpec(service, DeviceMessageConstants.AnalogOutValue, DeviceMessageConstants.AnalogOutValueDefaultTranslation));
        }
    },

    FTIONUpgrade(8036, "FTION upgrade") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    RtuPlusServerEnterMaintenanceMode(8037, "Enter maintenance mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    RtuPlusServerExitMaintenanceMode(8038, "Exit maintenance mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ForceMessageToFailed(8039, "Force message to failed state") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.deviceId, DeviceMessageConstants.deviceIdDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.trackingId, DeviceMessageConstants.trackingIdDefaultTranslation)
            );
        }
    },
    FTIONUpgradeAndInit(8040, "FTION upgrade and initialize") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    FTIONUpgradeAndInitWithNewEIServerURL(8041, "Upgrade and initialize with new EIServer URL") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.servletURL, DeviceMessageConstants.servletURLDefaultTranslation));
        }
    },
    FTIONUpgradeWithNewEIServerURL(8042, "Upgrade with new EIServer URL") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.servletURL, DeviceMessageConstants.servletURLDefaultTranslation));
        }
    },
    FTIONInitDatabaseKeepConfig(8043, "Initialize the database and keep the configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    FTIONReboot(8044, "Reboot") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    FTIONRestart(8045, "Restart") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    FTIONScanBus(8046, "Scan the bus") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SyncMasterdata(8047, "Synchronize master data") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    RebootApplication(8033, "Reboot application") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    DemandResetWithForceClock(8034, "Demand reset with force clock") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    HardResetDevice(8035, "Hard reset the device") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SyncMasterdataForDC(8048, "Sync master data for DC") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    PauseDCScheduler(8049, "Pause DC scheduler") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    ResumeDCScheduler(8050, "Resume DC scheduler") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SyncDeviceDataForDC(8051, "Sync device data for DC") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.cleanUpUnusedDeviceTypesAttributeName, DeviceMessageConstants.cleanUpUnusedDeviceTypesAttributeDefaultTranslation, false));
        }
    },
    SyncOneConfigurationForDC(8052, "Sync one configuration for DC") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.deviceConfigurationIDAttributeName, DeviceMessageConstants.deviceConfigurationIDAttributeDefaultTranslation));
        }
    },

    /**
     * Trigger the preliminary protocol (read out serial) for a group of meters.
     */
    TRIGGER_PRELIMINARY_PROTOCOL(8053, "Trigger the preliminary protocol for a group of meters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceGroupSpec(service, DeviceMessageConstants.deviceGroupAttributeName, DeviceMessageConstants.deviceGroupAttributeDefaultTranslation));
        }
    },
    SyncAllDevicesWithDC(8054, "Sync all devices with DC") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    SyncOneDeviceWithDC(8055, "Sync one device with DC") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.deviceId, DeviceMessageConstants.deviceIdDefaultTranslation));
        }
    },
    SyncOneDeviceWithDCAdvanced(8056, "Sync one device with DC (advanced)") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.deviceId, DeviceMessageConstants.deviceIdDefaultTranslation),
                    this.dateSpecBuilder(service, DeviceMessageConstants.previousStartDate, DeviceMessageConstants.previousStartDateDefaultTranslation).finish(),
                    this.booleanSpec(service, DeviceMessageConstants.ignorePreviousStartDate, DeviceMessageConstants.ignorePreviousStartDateDefaultTranslation, false),
                    this.dateSpecBuilder(service, DeviceMessageConstants.previousEndDate, DeviceMessageConstants.previousEndDateDefaultTranslation).finish(),
                    this.booleanSpec(service, DeviceMessageConstants.ignorePreviousEndDate, DeviceMessageConstants.ignorePreviousEndDateDefaultTranslation, false),
                    this.stringSpec(service, DeviceMessageConstants.previousConfigurationId, DeviceMessageConstants.previousConfigurationIdDefaultTranslation),
                    this.dateSpecBuilder(service, DeviceMessageConstants.currentStartDate, DeviceMessageConstants.currentStartDateDefaultTranslation).finish(),
                    this.booleanSpec(service, DeviceMessageConstants.ignoreStartDate, DeviceMessageConstants.ignoreStartDateDefaultTranslation, false),
                    this.dateSpecBuilder(service, DeviceMessageConstants.currentEndDate, DeviceMessageConstants.currentEndDateDefaultTranslation).finish(),
                    this.booleanSpec(service, DeviceMessageConstants.ignoreEndDate, DeviceMessageConstants.ignoreEndDateDefaultTranslation, false),
                    this.stringSpec(service, DeviceMessageConstants.currentConfigurationId, DeviceMessageConstants.currentConfigurationIdDefaultTranslation)
            );
        }
    },
    SetBufferForAllLoadProfiles(8057, "Set buffer size for all LP") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.bufferSize, DeviceMessageConstants.bufferSizeDefaultTranslation));
        }
    },
    SetBufferForSpecificLoadProfile(8058, "Set buffer size for a specific LP") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.obisCode, DeviceMessageConstants.obisCodeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.bufferSize, DeviceMessageConstants.bufferSizeDefaultTranslation)
            );
        }
    },
    SetBufferForAllEventLogs(8059, "Set buffer size for all event logs") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.bufferSize, DeviceMessageConstants.bufferSizeDefaultTranslation));
        }
    },
    SetBufferForSpecificEventLog(8060, "Set buffer size for specific event log") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.obisCode, DeviceMessageConstants.obisCodeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.bufferSize, DeviceMessageConstants.bufferSizeDefaultTranslation)
            );
        }
    },
    SetBufferForAllRegisters(8061, "Set buffer size for all registers") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.bufferSize, DeviceMessageConstants.bufferSizeDefaultTranslation));
        }
    },
    SetBufferForSpecificRegister(8062, "Set buffer size for specific register") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.obisCode, DeviceMessageConstants.obisCodeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.bufferSize, DeviceMessageConstants.bufferSizeDefaultTranslation)
            );
        }
    },
    BillingResetWithActivationDate(8063, "Billing reset with activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpecBuilder(service, DeviceMessageConstants.adHocEndOfBillingActivationDatedAttributeName, DeviceMessageConstants.adHocEndOfBillingActivationDatedAttributeDefaultTranslation).finish());
        }
    },
    RemoveLogicalDevice(8064, "Remove logical device") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.clientMacAddress, DeviceMessageConstants.clientMacAddressDefaultTranslation));
        }
    },

    ReadDLMSAttribute(8065, "Read DLMS Attribute") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.obisCode, DeviceMessageConstants.obisCodeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.attributeId, DeviceMessageConstants.attributeIdDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.classId, DeviceMessageConstants.classIdDefaultTranslation));
        }
    },

    SetCTVTRatios(8066, "Set CT VT ratios") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.CTRatioMultiplier, DeviceMessageConstants.CTRatioMultiplierDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.VTRatioMultiplier, DeviceMessageConstants.VTRatioMultiplierDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CTRatioDivisor, DeviceMessageConstants.CTRatioDivisorDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.VTRatioDivisor, DeviceMessageConstants.VTRatioDivisorDefaultTranslation));
        }
    },

    ProgramPulseInputParametersAndConstants(8067, "Program pulse input parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.PulseMode, DeviceMessageConstants.PulseModeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.PulseSubMode, DeviceMessageConstants.PulseSubModeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.PulseMultiplier, DeviceMessageConstants.PulseMultiplierDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.InputChannel, DeviceMessageConstants.InputChannelDefaultTranslation));
        }
    },

    ResetLogicalDevice(8068, "Reset logical device") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpec(service, DeviceMessageConstants.clientMacAddress, DeviceMessageConstants.clientMacAddressDefaultTranslation));
        }
    },

    FETCH_LOGGING(8069, "Fetch logging") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },

    SET_REMOTE_SYSLOG_CONFIG(8070, "Configure an external logging server") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.remoteSyslogTransportServiceType, DeviceMessageConstants.remoteSyslogTransportServiceTypeDefaultTranslation)
                            .addValues(TransportServiceType.getDescriptionValues())
                            .finish(),
                    this.stringSpec(service, DeviceMessageConstants.remoteSyslogDestination, DeviceMessageConstants.remoteSyslogDestinationDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.remoteSyslogPort, DeviceMessageConstants.remoteSyslogPortDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.remoteSyslogIpVersion, DeviceMessageConstants.remoteSyslogIpVersionDefaultTranslation)
                            .addValues(IPVersion.getDescriptionValues())
                            .finish());
        }
    },
    BillingDateConfiguration(8071, "Billing date configuration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpecBuilder(service, DeviceMessageConstants.adHocEndOfBillingActivationDatedAttributeName, DeviceMessageConstants.billingDateConfigurationDefaultTranslation).finish());
        }
    },

    SetModemResetSchedule(8072, "Set modem reset schedule") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.enableModemResetSchedule, DeviceMessageConstants.enableModemResetScheduleDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.daysOfMonthSchedule, DeviceMessageConstants.daysOfMonthScheduleDefaultTranslation)
                            .addValues(DaysOfMonth.getDaysOfMonthValues())
                            .setDefaultValue(DaysOfMonth.DLMSEncodings.ALL_DAYS.getDescription())
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.daysOfWeekSchedule, DeviceMessageConstants.daysOfWeekScheduleDefaultTranslation)
                            .addValues(DaysOfWeek.getDaysOfWeek())
                            .setDefaultValue(DaysOfWeek.ALL_DAYS)
                            .finish(),
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.hour, DeviceMessageConstants.hourDefaultTranslation)
                            .setDefaultValue(BigDecimal.valueOf(22))
                            .finish()
                    );

        }
    },
    LimitationActionDelay(8073, "Limitation action delay") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    PropertySpecFactory.boundedBigDecimalSpec(service, DeviceMessageConstants.limitationActionDelay, DeviceMessageConstants.limitationActionDelayDefaultTranslate, new BigDecimal(0), new BigDecimal(1275)));
        }
    },
    LimitationQuantityMeasure(8074, "Limitation quantity measure") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    PropertySpecFactory.hexStringSpecOfExactLength(service, DeviceMessageConstants.limitationMeasurementQuantity, DeviceMessageConstants.limitationMeasurementQuantityDefaultTranslate, 2));
        }
    };





    public enum IPVersion {
        IPv4(0, "IPv4"),
        IPv6(1, "IPv6")
        ;

        private final int id;
        private final String description;

        IPVersion(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static IPVersion valueForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String[] getDescriptionValues() {
            IPVersion[] allObjects = values();
            String[] result = new String[allObjects.length];
            for (int index = 0; index < allObjects.length; index++) {
                result[index] = allObjects[index].getDescription();
            }
            return result;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TransportServiceType {
        TCP(0, "TCP"),
        UDP(1, "UDP")
        ;

        private final int id;
        private final String description;

        TransportServiceType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static TransportServiceType valueForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String[] getDescriptionValues() {
            TransportServiceType[] allObjects = values();
            String[] result = new String[allObjects.length];
            for (int index = 0; index < allObjects.length; index++) {
                result[index] = allObjects[index].getDescription();
            }
            return result;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    private final long id;
    private final String defaultNameTranslation;

    DeviceActionMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }


    @Override
    public long id() {
        return this.id;
    }

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, boolean defaultValue) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .setDefaultValue(defaultValue)
                .markRequired()
                .finish();
    }

    protected PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpecBuilder<Date> dateSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpecBuilder<Date> dateTimeSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).addValues(possibleValues).finish();
    }

    protected PropertySpec stringSpecWithDefaultValue(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String defaultValue) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).setDefaultValue(defaultValue).finish();
    }

    protected PropertySpec deviceGroupSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(DeviceGroup.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec keyAccessorTypeReferenceSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(KeyAccessorType.class.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private static BigDecimal[] getPossibleValues() {
        return IntStream.range(17, 33).mapToObj(BigDecimal::valueOf).toArray(BigDecimal[]::new);
    }

    private String getNameResourceKey() {
        return DeviceActionMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.DEVICE_ACTIONS,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}