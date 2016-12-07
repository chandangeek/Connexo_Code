package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.enums.AuthenticationMechanism;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 9:45
 * Author: khe
 */
public enum ConfigurationChangeDeviceMessage implements DeviceMessageSpecSupplier {

    WriteExchangeStatus(0, "Write exchange status") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.WriteExchangeStatus, DeviceMessageConstants.WriteExchangeStatusDefaultTranslation));
        }
    },
    WriteRadioAcknowledge(1, "Write radio acknowledge") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.WriteRadioAcknowledge, DeviceMessageConstants.WriteRadioAcknowledgeDefaultTranslation));
        }
    },
    WriteRadioUserTimeout(2, "Write radio user timeout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.temporalAmountSpec(service, DeviceMessageConstants.WriteRadioUserTimeout, DeviceMessageConstants.WriteRadioUserTimeoutDefaultTranslation));
        }
    },
    WriteNewPDRNumber(3, "Write new PDR number") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecOfExactLength(service, DeviceMessageConstants.newPDRAttributeName, DeviceMessageConstants.newPDRAttributeNameDefaultTranslation, 14));
        }
    },
    ConfigureConverterMasterData(4, "Configure converter master data") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.converterTypeAttributeName, DeviceMessageConstants.converterTypeAttributeDefaultTranslation)
                            .addValues("VOL1", "VOL2", "VEN1", "VEN2")
                            .markExhaustive()
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.converterSerialNumberAttributeName, DeviceMessageConstants.converterSerialNumberAttributeDefaultTranslation).finish()
            );
        }
    },
    ConfigureGasMeterMasterData(5, "Configure gas meter master data") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.meterTypeAttributeName, DeviceMessageConstants.meterTypeAttributeDefaultTranslation)
                            .addValues("MASS", "USON", "CORI", "VENT", "MEMB", "TURB", "ROTO", "Axxx")
                            .markExhaustive()
                            .finish(),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.meterCaliberAttributeName, DeviceMessageConstants.meterCaliberAttributeDefaultTranslation,
                            BigDecimal.ZERO, new BigDecimal(999999)
                    ),
                    this.stringSpecBuilder(service, DeviceMessageConstants.meterSerialNumberAttributeName, DeviceMessageConstants.meterSerialNumberAttributeDefaultTranslation).finish()
            );
        }
    },
    ConfigureGasParameters(6, "Configure gas parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.gasDensityAttributeName, DeviceMessageConstants.gasDensityAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.airDensityAttributeName, DeviceMessageConstants.airDensityAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.relativeDensityAttributeName, DeviceMessageConstants.relativeDensityAttributeDefaultTranslation),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.molecularNitrogenPercentageAttributeName, DeviceMessageConstants.molecularNitrogenPercentageAttributeDefaultTranslation,
                            BigDecimal.ZERO, new BigDecimal(100)),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.carbonDioxidePercentageAttributeName, DeviceMessageConstants.carbonDioxidePercentageAttributeDefaultTranslation,
                            BigDecimal.ZERO, new BigDecimal(100)),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.molecularHydrogenPercentageAttributeName, DeviceMessageConstants.molecularHydrogenPercentageAttributeDefaultTranslation,
                            BigDecimal.ZERO, new BigDecimal(100)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.higherCalorificValueAttributeName, DeviceMessageConstants.higherCalorificValueAttributeDefaultTranslation)
            );
        }
    },
    //EIWeb general messages
    SetDescription(7, "Set description") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetDescriptionAttributeName, DeviceMessageConstants.SetDescriptionAttributeDefaultTranslation).finish());
        }
    },
    SetIntervalInSeconds(8, "Set interval in seconds") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetIntervalInSecondsAttributeName, DeviceMessageConstants.SetIntervalInSecondsAttributeDefaultTranslation).finish());
        }
    },
    SetUpgradeUrl(9, "Set upgrade URL") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetUpgradeUrlAttributeName, DeviceMessageConstants.SetUpgradeUrlAttributeDefaultTranslation).finish());
        }
    },
    SetUpgradeOptions(10, "Set upgrade options") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetUpgradeOptionsAttributeName, DeviceMessageConstants.SetUpgradeOptionsAttributeDefaultTranslation).finish());
        }
    },
    SetDebounceTreshold(11, "Set debounce threshold") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetDebounceTresholdAttributeName, DeviceMessageConstants.SetDebounceTresholdAttributeDefaultTranslation).finish());
        }
    },
    SetTariffMoment(12, "Set tariff moment") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetTariffMomentAttributeName, DeviceMessageConstants.SetTariffMomentAttributeDefaultTranslation).finish());
        }
    },
    SetCommOffset(13, "Set comm offset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetCommOffsetAttributeName, DeviceMessageConstants.SetCommOffsetAttributeDefaultTranslation).finish());
        }
    },
    SetAggIntv(14, "Set aggregation interval") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetAggIntvAttributeName, DeviceMessageConstants.SetAggIntvAttributeDefaultTranslation).finish());
        }
    },
    SetPulseTimeTrue(15, "Set pulse time true") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetPulseTimeTrueAttributeName, DeviceMessageConstants.SetPulseTimeTrueAttributeDefaultTranslation).finish());
        }
    },
    SetDukePowerID(16, "Set DukePower ID") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetDukePowerIDAttributeName, DeviceMessageConstants.SetDukePowerIDAttributeDefaultTranslation).finish());
        }
    },
    SetDukePowerPassword(17, "Set DukePower password") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetDukePowerPasswordAttributeName, DeviceMessageConstants.SetDukePowerPasswordAttributeDefaultTranslation).finish());
        }
    },
    SetDukePowerIdleTime(18, "Set DukePower idle time") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetDukePowerIdleTimeAttributeName, DeviceMessageConstants.SetDukePowerIdleTimeAttributeDefaultTranslation).finish());
        }
    },
    UploadMeterScheme(19, "Upload meter scheme") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.MeterScheme, DeviceMessageConstants.MeterSchemeDefaultTranslation));
        }
    },
    UploadSwitchPointClockSettings(20, "Upload switch point clock settings") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.SwitchPointClockSettings, DeviceMessageConstants.SwitchPointClockSettingsDefaultTranslation));
        }
    },
    UploadSwitchPointClockUpdateSettings(21, "Upload switch point clock update settings") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.SwitchPointClockUpdateSettings, DeviceMessageConstants.SwitchPointClockUpdateSettingsDefaultTranslation));
        }
    },
    ProgramBatteryExpiryDate(22, "Program battery expiry date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateSpec(service, DeviceMessageConstants.ConfigurationChangeDate, DeviceMessageConstants.ConfigurationChangeDateDefaultTranslation));
        }
    },
    ChangeOfSupplier(23, "Change of supplier") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.ChangeOfSupplierName, DeviceMessageConstants.ChangeOfSupplierDefaultTranslation).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChangeOfSupplierID, DeviceMessageConstants.ChangeOfSupplierIDDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    ChangeOfTenancy(24, "Change of Tenancy") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation));
        }
    },
    SetCalorificValueAndActivationDate(25, "Set conversion factor and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.CalorificValue, DeviceMessageConstants.CalorificValueDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    SetConversionFactorAndActivationDate(26, "Set conversion factor and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.ConversionFactor, DeviceMessageConstants.ConversionFactorDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    SetAlarmFilter(27, "Set alarm filter") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.hexStringSpecOfExactLength(
                            service, DeviceMessageConstants.AlarmFilterAttributeName, DeviceMessageConstants.AlarmFilterAttributeDefaultTranslation,
                            4));
        }
    },
    ChangeDefaultResetWindow(28, "Change default reset window") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.DefaultResetWindowAttributeName, DeviceMessageConstants.DefaultResetWindowAttributeDefaultTranslation));
        }
    },
    ChangeAdministrativeStatus(29, "Change administrative status") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.bigDecimalSpec(
                            service, DeviceMessageConstants.AdministrativeStatusAttributeName, DeviceMessageConstants.AdministrativeStatusAttributeDefaultTranslation,
                            BigDecimal.ZERO,
                            BigDecimal.ONE,
                            new BigDecimal(2),
                            new BigDecimal(3)));
        }
    },
    BootSyncEnable(30, "Enable sync at boot") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.enableBootSync, DeviceMessageConstants.enableBootSyncDefaultTranslation).finish());
        }
    },
    WhitelistedPhoneNumbers(31, "Write white listed phone numbers") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.whiteListPhoneNumbersAttributeName, DeviceMessageConstants.whiteListPhoneNumbersAttributeDefaultTranslation).finish());
        }
    },
    EnableFW(32, "Enable firewall") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    DisableFW(33, "Disable firewall") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    EnableSSL(35, "Enable SSL for the web interface") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.enableSSL, DeviceMessageConstants.enableSSLDefaultTranslation));
        }
    },
    SetDeviceName(36, "Set device name") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.deviceName, DeviceMessageConstants.deviceNameDefaultTranslation).finish());
        }
    },
    SetNTPAddress(37, "Set NTP address") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.ntpAddress, DeviceMessageConstants.ntpAddressDefaultTranslation).finish());
        }
    },
    Clear_Faults_Flags(38, "Clear faults flags") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    Clear_Statistical_Values(39, "Clear statistical values") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    SyncNTPServer(40, "Synchronize NTP server") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    ConfigureAutomaticDemandReset(41, "Configure automatic demand reset") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.booleanSpec(service, DeviceMessageConstants.enableAutomaticDemandResetAttributeName, DeviceMessageConstants.enableAutomaticDemandResetAttributeDefaultTranslation),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.day, DeviceMessageConstants.dayDefaultTranslation,
                            BigDecimal.ZERO, BigDecimal.valueOf(31)),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.hour, DeviceMessageConstants.hourDefaultTranslation,
                            BigDecimal.ZERO, BigDecimal.valueOf(23))
            );
        }
    },
    ENABLE_DISCOVERY_ON_POWER_UP(43, "Enable discovery on power up") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    DISABLE_DISCOVERY_ON_POWER_UP(44, "Disable discovery on power up") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    ConfigureMasterBoardParameters(45, "Configure masterboard parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.localMacAddress, DeviceMessageConstants.localMacAddressDefaultTranslation)
                            .setDefaultValue(BigDecimal.valueOf(-1))
                            .finish(),
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.maxCredit, DeviceMessageConstants.maxCreditDefaultTranslation)
                            .setDefaultValue(BigDecimal.valueOf(-1))
                            .finish(),
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.zeroCrossDelay, DeviceMessageConstants.zeroCrossDelayDefaultTranslation)
                            .setDefaultValue(BigDecimal.valueOf(-1))
                            .finish(),
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.synchronisationBit, DeviceMessageConstants.synchronisationBitDefaultTranslation)
                            .setDefaultValue(BigDecimal.valueOf(-1))
                            .finish()
            );
        }
    },
    UpgradeSetOption(46, "Upgrade - Set an option") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation).finish());
        }
    },
    UpgradeClrOption(47, "Upgrade - Clear an option") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation).finish());
        }
    },
    ConfigureBillingPeriodStartDate(48, "Configure billing period start date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.year, DeviceMessageConstants.monthDefaultTranslation)
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish(),
                    this.bigDecimalSpecBuilder(service, DeviceMessageConstants.month, DeviceMessageConstants.monthDefaultTranslation)
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.day, DeviceMessageConstants.dayDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.dayOfWeek, DeviceMessageConstants.dayOfWeekDefaultTranslation)
                            .setDefaultValue("--")
                            .addValues("MO", "TU", "WE", "TH", "FR", "SA", "SU")
                            .markExhaustive()
                            .finish()
            );
        }
    },
    ConfigureBillingPeriodLength(49, "Configure billing period length") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.billingPeriodLengthAttributeName, DeviceMessageConstants.billingPeriodLengthAttributeDefaultTranslation));
        }
    },
    WriteNewOnDemandBillingDate(50, "Write new on demand billing date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.dateTimeSpec(service, DeviceMessageConstants.setOnDemandBillingDateAttributeName, DeviceMessageConstants.setOnDemandBillingDateAttributeDefaultTranslation),
                    this.bigDecimalSpec(
                            service, DeviceMessageConstants.OnDemandBillingReasonAttributeName, DeviceMessageConstants.OnDemandBillingReasonAttributeDefaultTranslation,
                            BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3),
                            BigDecimal.valueOf(4), BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(7))
            );
        }
    },
    ChangeUnitStatus(51, "Change unit status") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpecBuilder(
                            service, DeviceMessageConstants.UnitStatusAttributeName, DeviceMessageConstants.UnitStatusAttributeDefaultTranslation)
                            .addValues("Normal", "Maintenance")
                            .markExhaustive()
                            .finish());
        }
    },
    ConfigureStartOfGasDaySettings(52, "Configure start of gas day settings") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.booleanSpec(
                            service, DeviceMessageConstants.IgnoreDSTAttributeName, DeviceMessageConstants.IgnoreDSTAttributeDefaultTranslation,
                            Boolean.FALSE));
        }
    },
    ConfigureStartOfGasDay(53, "Configure start of gas day") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.timeSpec(service, DeviceMessageConstants.StartOfGasDayAttributeName, DeviceMessageConstants.StartOfGasDayAttributeDefaultTranslation));
        }
    },
    ConfigureRSSIMultipleSampling(54, "Configure RSSI multiple sampling") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.booleanSpec(
                            service, DeviceMessageConstants.enableRSSIMultipleSampling, DeviceMessageConstants.enableRSSIMultipleSamplingDefaultTranslation,
                            Boolean.TRUE));
        }
    },
    CHANGE_OF_TENANT(55, "Set tenant") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.tenantReference, DeviceMessageConstants.tenantReferenceDefaultTranslation).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.supplierReference, DeviceMessageConstants.supplierReferenceDefaultTranslation).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChangeOfSupplierID, DeviceMessageConstants.ChangeOfSupplierIDDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.scriptExecuted, DeviceMessageConstants.scriptExecutedDefaultTranslation).finish()
            );
        }
    },
    CHANGE_OF_TENANT_AND_ACTIVATION_DATE(56, "Set tenant and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.tenantReference, DeviceMessageConstants.tenantReferenceDefaultTranslation).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.supplierReference, DeviceMessageConstants.supplierReferenceDefaultTranslation).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChangeOfSupplierID, DeviceMessageConstants.ChangeOfSupplierIDDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.scriptExecuted, DeviceMessageConstants.scriptExecutedDefaultTranslation).finish(),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    CHANGE_OF_SUPPLIER(57, "Set Supplier") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.tenantReference, DeviceMessageConstants.tenantReferenceDefaultTranslation).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.supplierReference, DeviceMessageConstants.supplierReferenceDefaultTranslation).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChangeOfSupplierID, DeviceMessageConstants.ChangeOfSupplierIDDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.scriptExecuted, DeviceMessageConstants.scriptExecutedDefaultTranslation).finish()
            );
        }
    },
    CHANGE_OF_SUPPLIER_AND_ACTIVATION_DATE(58, "Set Supplier and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.tenantReference, DeviceMessageConstants.tenantReferenceDefaultTranslation).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.supplierReference, DeviceMessageConstants.supplierReferenceDefaultTranslation).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChangeOfSupplierID, DeviceMessageConstants.ChangeOfSupplierIDDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.scriptExecuted, DeviceMessageConstants.scriptExecutedDefaultTranslation).finish(),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    CHANGE_OF_SUPPLIER_IMPORT_ENERGY(59, "Set Supplier A+") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.tenantReference, DeviceMessageConstants.tenantReferenceDefaultTranslation).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.supplierReference, DeviceMessageConstants.supplierReferenceDefaultTranslation).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChangeOfSupplierID, DeviceMessageConstants.ChangeOfSupplierIDDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.scriptExecuted, DeviceMessageConstants.scriptExecutedDefaultTranslation).finish()
            );
        }
    },
    CHANGE_OF_SUPPLIER_IMPORT_ENERGY_AND_ACTIVATION_DATE(60, "Set Supplier A+ and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.tenantReference, DeviceMessageConstants.tenantReferenceDefaultTranslation).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.supplierReference, DeviceMessageConstants.supplierReferenceDefaultTranslation).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChangeOfSupplierID, DeviceMessageConstants.ChangeOfSupplierIDDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.scriptExecuted, DeviceMessageConstants.scriptExecutedDefaultTranslation).finish(),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    CHANGE_OF_SUPPLIER_EXPORT_ENERGY(61, "Set Supplier A-") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.tenantReference, DeviceMessageConstants.tenantReferenceDefaultTranslation).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.supplierReference, DeviceMessageConstants.supplierReferenceDefaultTranslation).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChangeOfSupplierID, DeviceMessageConstants.ChangeOfSupplierIDDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.scriptExecuted, DeviceMessageConstants.scriptExecutedDefaultTranslation).finish()
            );
        }
    },
    CHANGE_OF_SUPPLIER_EXPORT_ENERGY_AND_ACTIVATION_DATE(62, "Set Supplier A- and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.tenantReference, DeviceMessageConstants.tenantReferenceDefaultTranslation).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.supplierReference, DeviceMessageConstants.supplierReferenceDefaultTranslation).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChangeOfSupplierID, DeviceMessageConstants.ChangeOfSupplierIDDefaultTranslation),
                    this.stringSpecBuilder(service, DeviceMessageConstants.scriptExecuted, DeviceMessageConstants.scriptExecutedDefaultTranslation).finish(),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    SET_ENGINEER_PIN(63, "Set Engineer PIN") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecOfExactLength(service, DeviceMessageConstants.engineerPin, DeviceMessageConstants.engineerPinDefaultTranslation, 8),
                    this.temporalAmountSpec(service, DeviceMessageConstants.engineerPinTimeout, DeviceMessageConstants.engineerPinTimeoutDefaultTranslation, Duration.ofSeconds(30))
            );
        }
    },
    SET_ENGINEER_PIN_AND_ACTIVATION_DATE(64, "Set Engineer PIN and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecOfExactLength(service, DeviceMessageConstants.engineerPin, DeviceMessageConstants.engineerPinDefaultTranslation, 8),
                    this.temporalAmountSpec(service, DeviceMessageConstants.engineerPinTimeout, DeviceMessageConstants.engineerPinTimeoutDefaultTranslation, Duration.ofSeconds(30)),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    SetCalorificValue(65, "Set calorific value") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.CalorificValue, DeviceMessageConstants.CalorificValueDefaultTranslation));
        }
    },
    SetConversionFactor(66, "Set conversion factor") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.ConversionFactor, DeviceMessageConstants.ConversionFactorDefaultTranslation));
        }
    },
    ConfigureAllGasParameters(67, "Configure all gas parameters") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.gasDensityAttributeName, DeviceMessageConstants.gasDensityAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.relativeDensityAttributeName, DeviceMessageConstants.relativeDensityAttributeDefaultTranslation),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.molecularNitrogenPercentageAttributeName, DeviceMessageConstants.molecularNitrogenPercentageAttributeDefaultTranslation,
                            BigDecimal.ZERO, new BigDecimal(100)),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.carbonDioxidePercentageAttributeName, DeviceMessageConstants.carbonDioxidePercentageAttributeDefaultTranslation,
                            BigDecimal.ZERO, new BigDecimal(100)),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.carbonOxidePercentageAttributeName, DeviceMessageConstants.carbonOxidePercentageAttributeDefaultTranslation,
                            BigDecimal.ZERO, new BigDecimal(100)),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.molecularHydrogenPercentageAttributeName, DeviceMessageConstants.molecularHydrogenPercentageAttributeDefaultTranslation,
                            BigDecimal.ZERO, new BigDecimal(100)),
                    this.boundedBigDecimalSpec(
                            service, DeviceMessageConstants.methanePercentageAttributeName, DeviceMessageConstants.methanePercentageAttributeDefaultTranslation,
                            BigDecimal.ZERO, new BigDecimal(100)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.higherCalorificValueAttributeName, DeviceMessageConstants.higherCalorificValueAttributeDefaultTranslation)
            );
        }
    },
    ChangeMeterLocation(68, "Change meter location") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.meterLocationAttributeName, DeviceMessageConstants.meterLocationAttributeDefaultTranslation).finish());
        }
    },
    SendShortDisplayMessage(69, "Send short display message") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SHORT_DISPLAY_MESSAGE, DeviceMessageConstants.SHORT_DISPLAY_MESSAGE_DEFAULT_TRANSLATION).finish());
        }
    },
    SendLongDisplayMessage(70, "Send long display message") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.LONG_DISPLAY_MESSAGE, DeviceMessageConstants.LONG_DISPLAY_MESSAGE_DEFAULT_TRANSLATION).finish());
        }
    },
    ResetDisplayMessage(71, "Reset display message") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    ConfigureLCDDisplay(72, "Configure LCD display") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service,
                            DeviceMessageConstants.NUMBER_OF_DIGITS_BEFORE_COMMA, DeviceMessageConstants.NUMBER_OF_DIGITS_BEFORE_COMMA,
                            new BigDecimal(5),
                            new BigDecimal(6),
                            new BigDecimal(7)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.NUMBER_OF_DIGITS_AFTER_COMMA, DeviceMessageConstants.NUMBER_OF_DIGITS_AFTER_COMMA_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO,
                            BigDecimal.ONE,
                            new BigDecimal(2),
                            new BigDecimal(3)),
                    this.stringSpecBuilder(service, DeviceMessageConstants.DISPLAY_SEQUENCE, DeviceMessageConstants.DISPLAY_SEQUENCE_DEFAULT_TRANSLATION).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.DISPLAY_CYCLE_TIME, DeviceMessageConstants.DISPLAY_CYCLE_TIME_DEFAULT_TRANSLATION)
            );
        }
    },
    ConfigureLoadProfileDataRecording(73, "Configure load profile data recording") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.ENABLE_DISABLE, DeviceMessageConstants.ENABLE_DISABLE_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO, BigDecimal.ONE),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CONFIG_LOAD_PROFILE_INTERVAL, DeviceMessageConstants.CONFIG_LOAD_PROFILE_INTERVAL_DEFAULT_TRANSLATION,
                            BigDecimal.ONE,
                            new BigDecimal(2),
                            new BigDecimal(3),
                            new BigDecimal(5),
                            new BigDecimal(6),
                            BigDecimal.TEN,
                            new BigDecimal(12),
                            new BigDecimal(15),
                            new BigDecimal(20),
                            new BigDecimal(30),
                            new BigDecimal(60),
                            new BigDecimal(120),
                            new BigDecimal(240)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.MAX_NUMBER_RECORDS, DeviceMessageConstants.MAX_NUMBER_RECORDS_DEFAULT_TRANSLATION)
            );
        }
    },

    ConfigureSpecialDataMode(74, "Configure special data mode") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DAYS, DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DAYS_DEFAULT_TRANSLATION),
                    this.dateSpec(service, DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DATE, DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DATE_DEFAULT_TRANSLATION),
                    this.bigDecimalSpec(service, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO,
                            BigDecimal.ONE),
                    this.bigDecimalSpec(service, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_INTERVAL, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_INTERVAL_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO,
                            BigDecimal.ONE,
                            new BigDecimal(2)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_MAX_NUMBER_RECORDS, DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_MAX_NUMBER_RECORDS_DEFAULT_TRANSLATION,
                            BigDecimal.ONE, new BigDecimal(65535)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.SPECIAL_LOAD_PROFILE, DeviceMessageConstants.SPECIAL_LOAD_PROFILE_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO,
                            BigDecimal.ONE),
                    this.bigDecimalSpec(service, DeviceMessageConstants.SPECIAL_LOAD_PROFILE_INTERVAL, DeviceMessageConstants.SPECIAL_LOAD_PROFILE_INTERVAL_DEFAULT_TRANSLATION,
                            BigDecimal.ONE,
                            new BigDecimal(2),
                            new BigDecimal(3),
                            new BigDecimal(5),
                            new BigDecimal(6),
                            BigDecimal.TEN,
                            new BigDecimal(12),
                            new BigDecimal(15),
                            new BigDecimal(20),
                            new BigDecimal(30),
                            new BigDecimal(60),
                            new BigDecimal(120),
                            new BigDecimal(240)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.SPECIAL_LOAD_PROFILE_MAX_NO, DeviceMessageConstants.SPECIAL_LOAD_PROFILE_MAX_NO_DEFAULT_TRANSLATION,
                            BigDecimal.ONE, new BigDecimal(65535))
            );
        }
    },
    ConfigureMaxDemandSettings(75, "Configure maximum demand settings") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.ACTIVE_REGISTERS_0_OR_REACTIVE_REGISTERS_1, DeviceMessageConstants.ACTIVE_REGISTERS_0_OR_REACTIVE_REGISTERS_1_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO,
                            BigDecimal.ONE),
                    this.bigDecimalSpec(service, DeviceMessageConstants.NUMBER_OF_SUBINTERVALS, DeviceMessageConstants.NUMBER_OF_SUBINTERVALS_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO,
                            BigDecimal.ONE,
                            new BigDecimal(2),
                            new BigDecimal(3),
                            new BigDecimal(4),
                            new BigDecimal(5),
                            BigDecimal.TEN,
                            new BigDecimal(15)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.SUB_INTERVAL_DURATION, DeviceMessageConstants.SUB_INTERVAL_DURATION_DEFAULT_TRANSLATION,
                            new BigDecimal(30),
                            new BigDecimal(60),
                            new BigDecimal(300),
                            new BigDecimal(600),
                            new BigDecimal(900),
                            new BigDecimal(1200),
                            new BigDecimal(1800),
                            new BigDecimal(3600))
            );
        }
    },
    ConfigureConsumptionLimitationsSettings(76, "Configure consumption limitation settings") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.NUMBER_OF_SUBINTERVALS, DeviceMessageConstants.NUMBER_OF_SUBINTERVALS_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO,
                            BigDecimal.ONE,
                            new BigDecimal(2),
                            new BigDecimal(3),
                            new BigDecimal(4),
                            new BigDecimal(5),
                            BigDecimal.TEN,
                            new BigDecimal(15)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.SUB_INTERVAL_DURATION, DeviceMessageConstants.SUB_INTERVAL_DURATION_DEFAULT_TRANSLATION,
                            new BigDecimal(30),
                            new BigDecimal(60),
                            new BigDecimal(300),
                            new BigDecimal(600),
                            new BigDecimal(900),
                            new BigDecimal(1200),
                            new BigDecimal(1800),
                            new BigDecimal(3600)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.OVERRIDE_RATE, DeviceMessageConstants.OVERRIDE_RATE_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO, new BigDecimal(4)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.ALLOWED_EXCESS_TOLERANCE, DeviceMessageConstants.ALLOWED_EXCESS_TOLERANCE_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO, new BigDecimal(100)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.THRESHOLD_SELECTION, DeviceMessageConstants.THRESHOLD_SELECTION_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO,
                            BigDecimal.ONE),
                    this.stringSpecBuilder(service, DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE0, DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE0_DEFAULT_TRANSLATION).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE0, DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE0_DEFAULT_TRANSLATION).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.THRESHOLDS_MOMENTS, DeviceMessageConstants.THRESHOLDS_MOMENTS_DEFAULT_TRANSLATION).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE0, DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE0_DEFAULT_TRANSLATION).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE1, DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE1_DEFAULT_TRANSLATION).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE1, DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE1_DEFAULT_TRANSLATION).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.THRESHOLDS_MOMENTS, DeviceMessageConstants.THRESHOLDS_MOMENTS_DEFAULT_TRANSLATION).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE1, DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE1_DEFAULT_TRANSLATION).finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.DAY_PROFILES, DeviceMessageConstants.DAY_PROFILES_DEFAULT_TRANSLATION).finish(),
                    this.dateTimeSpec(service, DeviceMessageConstants.ACTIVATION_DATE, DeviceMessageConstants.ACTIVATION_DATE_DEFAULT_TRANSLATION)
            );
        }
    },
    ConfigureEmergencyConsumptionLimitation(77, "Configure emergency consumption limitation") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.DURATION_MINUTES, DeviceMessageConstants.DURATION_MINUTES_DEFAULT_TRANSLATION,
                            BigDecimal.ONE, new BigDecimal(65535)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.TRESHOLD_VALUE, DeviceMessageConstants.TRESHOLD_VALUE_DEFAULT_TRANSLATION),
                    this.bigDecimalSpec(service, DeviceMessageConstants.TRESHOLD_UNIT, DeviceMessageConstants.TRESHOLD_UNIT_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO,
                            BigDecimal.ONE),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.OVERRIDE_RATE, DeviceMessageConstants.OVERRIDE_RATE_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO, new BigDecimal(4))
            );
        }
    },
    ConfigureTariffSettings(78, "Configure tariff settings") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.UNIQUE_TARIFF_ID_NO, DeviceMessageConstants.UNIQUE_TARIFF_ID_NO_DEFAULT_TRANSLATION),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.NUMBER_OF_TARIFF_RATES, DeviceMessageConstants.NUMBER_OF_TARIFF_RATES_DEFAULT_TRANSLATION,
                            BigDecimal.ZERO, new BigDecimal(4)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.CODE_TABLE_ID, DeviceMessageConstants.CODE_TABLE_ID_DEFAULT_TRANSLATION)
            );
        }
    },
    EnableGzipCompression(79, "Enable GZIP Compression") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.ENABLE_GZIP_COMPRESSION, DeviceMessageConstants.ENABLE_GZIP_COMPRESSION_DEFAULT_TRANSLATION));
        }
    },
    SetAuthenticationMechanism(80, "Set Authentication mechanism") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.SET_AUTHENTICATION_MECHANISM, DeviceMessageConstants.SET_AUTHENTICATION_MECHANISM_DEFAULT_TRANSLATION)
                            .addValues(AuthenticationMechanism.getAuthNames())
                            .markExhaustive()
                            .finish());
        }
    },
    SetMaxLoginAttempts(81, "Set Max Login Attempts") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SET_MAX_LOGIN_ATTEMPTS, DeviceMessageConstants.SET_MAX_LOGIN_ATTEMPTS_DEFAULT_TRANSLATION).finish());
        }
    },
    SetLockoutDuration(82, "Set Lockout Duration") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.durationSpec(service, DeviceMessageConstants.SET_LOCKOUT_DURATION, DeviceMessageConstants.SET_LOCKOUT_DURATION_DEFAULT_TRANSLATION, Duration.ofMillis(10000)));
        }
    },
    ConfigureGeneralLocalPortReadout(83, "Configure general local port readout") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.objectDefinitionsAttributeName, DeviceMessageConstants.objectDefinitionsAttributeDefaultTranslation).finish());
        }
    },
    DISABLE_PUSH_ON_INSTALLATION(84, "Disable push on installation") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    ENABLE_PUSH_ON_INTERVAL_OBJECTS(85, "Write execution time for push on interval") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.typeAttributeName, DeviceMessageConstants.typeAttributeDefaultTranslation)
                        .addValues(PushType.getTypes())
                        .markExhaustive()
                            .finish(),
                    this.stringSpecBuilder(service, DeviceMessageConstants.executionMinutesForEachHour, DeviceMessageConstants.executionMinutesForEachHourDefaultTranslation).finish()
            );
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    ConfigurationChangeDeviceMessage(int id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec dateSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec timeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .timeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec durationSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, Duration defaultValue) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .durationSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpecBuilder<TemporalAmount> temporalAmountSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .temporalAmountSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec temporalAmountSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.temporalAmountSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec temporalAmountSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, TemporalAmount defaultValue) {
        return this.temporalAmountSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).setDefaultValue(defaultValue).finish();
    }

    protected PropertySpec boundedBigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal lowerLimit, BigDecimal upperLimit) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .boundedBigDecimalSpec(lowerLimit, upperLimit)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec stringSpecOfExactLength(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, int length) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpecOfExactLength(length)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec hexStringSpecOfExactLength(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, int length) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .hexStringSpecOfExactLength(length)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    private PropertySpecBuilder<Boolean> booleanSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.booleanSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, Boolean defaultValue) {
        return this.booleanSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).setDefaultValue(defaultValue).finish();
    }

    protected PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... exhaustiveValues) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(exhaustiveValues)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec deviceMessageFileSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(DeviceMessageFile.class)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    private String getNameResourceKey() {
        return ConfigurationChangeDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.CONFIGURATION_CHANGE,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

    private static class Constants {

        private static final Date DEFAULT_DATE = new Date(978307200000l);   // 01/01/2001
        private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
    }

    public enum PushType {
        Interval_1(1),
        Interval_2(2),
        Interval_3(3);

        private final int id;

        PushType(int id) {
            this.id = id;
        }

        public static String[] getTypes() {
            return Stream.of(values()).map(PushType::name).toArray(String[]::new);
        }

        public int getId() {
            return id;
        }
    }

}