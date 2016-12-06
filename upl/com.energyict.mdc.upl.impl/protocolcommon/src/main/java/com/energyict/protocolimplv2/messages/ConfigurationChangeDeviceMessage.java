package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.messages.enums.AuthenticationMechanism;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    WriteExchangeStatus(0) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.WriteExchangeStatus, DeviceMessageConstants.WriteExchangeStatusDefaultTranslation));
        }
    },
    WriteRadioAcknowledge(1) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.WriteRadioAcknowledge, DeviceMessageConstants.WriteRadioAcknowledgeDefaultTranslation));
        }
    },
    WriteRadioUserTimeout(2) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.temporalAmountSpec(service, DeviceMessageConstants.WriteRadioUserTimeout, DeviceMessageConstants.WriteRadioUserTimeoutDefaultTranslation));
        }
    },
    WriteNewPDRNumber(3) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecOfExactLength(service, DeviceMessageConstants.newPDRAttributeName, DeviceMessageConstants.newPDRAttributeNameDefaultTranslation, 14));
        }
    },
    ConfigureConverterMasterData(4) {
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
    ConfigureGasMeterMasterData(5) {
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
    ConfigureGasParameters(6) {
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
    SetDescription(7) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetDescriptionAttributeName, DeviceMessageConstants.SetDescriptionAttributeDefaultTranslation).finish());
        }
    },
    SetIntervalInSeconds(8) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetIntervalInSecondsAttributeName, DeviceMessageConstants.SetIntervalInSecondsAttributeDefaultTranslation).finish());
        }
    },
    SetUpgradeUrl(9) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetUpgradeUrlAttributeName, DeviceMessageConstants.SetUpgradeUrlAttributeDefaultTranslation).finish());
        }
    },
    SetUpgradeOptions(10) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetUpgradeOptionsAttributeName, DeviceMessageConstants.SetUpgradeOptionsAttributeDefaultTranslation).finish());
        }
    },
    SetDebounceTreshold(11) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetDebounceTresholdAttributeName, DeviceMessageConstants.SetDebounceTresholdAttributeDefaultTranslation).finish());
        }
    },
    SetTariffMoment(12) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetTariffMomentAttributeName, DeviceMessageConstants.SetTariffMomentAttributeDefaultTranslation).finish());
        }
    },
    SetCommOffset(13) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetCommOffsetAttributeName, DeviceMessageConstants.SetCommOffsetAttributeDefaultTranslation).finish());
        }
    },
    SetAggIntv(14) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetAggIntvAttributeName, DeviceMessageConstants.SetAggIntvAttributeDefaultTranslation).finish());
        }
    },
    SetPulseTimeTrue(15) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetPulseTimeTrueAttributeName, DeviceMessageConstants.SetPulseTimeTrueAttributeDefaultTranslation).finish());
        }
    },
    SetDukePowerID(16) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetDukePowerIDAttributeName, DeviceMessageConstants.SetDukePowerIDAttributeDefaultTranslation).finish());
        }
    },
    SetDukePowerPassword(17) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetDukePowerPasswordAttributeName, DeviceMessageConstants.SetDukePowerPasswordAttributeDefaultTranslation).finish());
        }
    },
    SetDukePowerIdleTime(18) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.SetDukePowerIdleTimeAttributeName, DeviceMessageConstants.SetDukePowerIdleTimeAttributeDefaultTranslation).finish());
        }
    },
    UploadMeterScheme(19) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.MeterScheme, DeviceMessageConstants.MeterSchemeDefaultTranslation));
        }
    },
    UploadSwitchPointClockSettings(20) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.SwitchPointClockSettings, DeviceMessageConstants.SwitchPointClockSettingsDefaultTranslation));
        }
    },
    UploadSwitchPointClockUpdateSettings(21) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.deviceMessageFileSpec(service, DeviceMessageConstants.SwitchPointClockUpdateSettings, DeviceMessageConstants.SwitchPointClockUpdateSettingsDefaultTranslation));
        }
    },
    ProgramBatteryExpiryDate(22) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateSpec(service, DeviceMessageConstants.ConfigurationChangeDate, DeviceMessageConstants.ConfigurationChangeDateDefaultTranslation));
        }
    },
    ChangeOfSupplier(23) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecBuilder(service, DeviceMessageConstants.ChangeOfSupplierName, DeviceMessageConstants.ChangeOfSupplierDefaultTranslation).finish(),
                    this.bigDecimalSpec(service, DeviceMessageConstants.ChangeOfSupplierID, DeviceMessageConstants.ChangeOfSupplierIDDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    ChangeOfTenancy(24) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation));
        }
    },
    SetCalorificValueAndActivationDate(25) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.CalorificValue, DeviceMessageConstants.CalorificValueDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    SetConversionFactorAndActivationDate(26) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.ConversionFactor, DeviceMessageConstants.ConversionFactorDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.ConfigurationChangeActivationDate, DeviceMessageConstants.ConfigurationChangeActivationDateDefaultTranslation)
            );
        }
    },
    SetAlarmFilter(27) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.hexStringSpecOfExactLength(
                            service, DeviceMessageConstants.AlarmFilterAttributeName, DeviceMessageConstants.AlarmFilterAttributeDefaultTranslation,
                            4));
        }
    },
    ChangeDefaultResetWindow(28) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.DefaultResetWindowAttributeName, DeviceMessageConstants.DefaultResetWindowAttributeDefaultTranslation));
        }
    },
    ChangeAdministrativeStatus(29) {
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
    BootSyncEnable(30) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.enableBootSync, DeviceMessageConstants.enableBootSyncDefaultTranslation).finish());
        }
    },
    WhitelistedPhoneNumbers(31) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.whiteListPhoneNumbersAttributeName, DeviceMessageConstants.whiteListPhoneNumbersAttributeDefaultTranslation).finish());
        }
    },
    EnableFW(32) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    DisableFW(33) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    EnableSSL(35) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.enableSSL, DeviceMessageConstants.enableSSLDefaultTranslation));
        }
    },
    SetDeviceName(36) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.deviceName, DeviceMessageConstants.deviceNameDefaultTranslation).finish());
        }
    },
    SetNTPAddress(37) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.ntpAddress, DeviceMessageConstants.ntpAddressDefaultTranslation).finish());
        }
    },
    Clear_Faults_Flags(38) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    Clear_Statistical_Values(39) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    SyncNTPServer(40) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    ConfigureAutomaticDemandReset(41) {
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
    ENABLE_DISCOVERY_ON_POWER_UP(43) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    DISABLE_DISCOVERY_ON_POWER_UP(44) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    ConfigureMasterBoardParameters(45) {
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
    UpgradeSetOption(46) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation).finish());
        }
    },
    UpgradeClrOption(47) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpecBuilder(service, DeviceMessageConstants.singleOptionAttributeName, DeviceMessageConstants.singleOptionAttributeDefaultTranslation).finish());
        }
    },
    ConfigureBillingPeriodStartDate(48) {
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
    ConfigureBillingPeriodLength(49) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.billingPeriodLengthAttributeName, DeviceMessageConstants.billingPeriodLengthAttributeDefaultTranslation));
        }
    },
    WriteNewOnDemandBillingDate(50) {
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
    ChangeUnitStatus(51) {
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
    ConfigureStartOfGasDaySettings(52) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.booleanSpec(
                            service, DeviceMessageConstants.IgnoreDSTAttributeName, DeviceMessageConstants.IgnoreDSTAttributeDefaultTranslation,
                            Boolean.FALSE));
        }
    },
    ConfigureStartOfGasDay(53) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.timeSpec(service, DeviceMessageConstants.StartOfGasDayAttributeName, DeviceMessageConstants.StartOfGasDayAttributeDefaultTranslation));
        }
    },
    ConfigureRSSIMultipleSampling(54) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.booleanSpec(
                            service, DeviceMessageConstants.enableRSSIMultipleSampling, DeviceMessageConstants.enableRSSIMultipleSamplingDefaultTranslation,
                            Boolean.TRUE));
        }
    },
    CHANGE_OF_TENANT(55,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.tenantReference),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.supplierReference),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ChangeOfSupplierID),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.scriptExecuted)
    ),
    CHANGE_OF_TENANT_AND_ACTIVATION_DATE(56,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.tenantReference),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.supplierReference),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ChangeOfSupplierID),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.scriptExecuted),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    CHANGE_OF_SUPPLIER(57,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.tenantReference),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.supplierReference),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ChangeOfSupplierID),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.scriptExecuted)
    ),
    CHANGE_OF_SUPPLIER_AND_ACTIVATION_DATE(58,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.tenantReference),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.supplierReference),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ChangeOfSupplierID),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.scriptExecuted),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    CHANGE_OF_SUPPLIER_IMPORT_ENERGY(59,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.tenantReference),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.supplierReference),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ChangeOfSupplierID),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.scriptExecuted)
    ),
    CHANGE_OF_SUPPLIER_IMPORT_ENERGY_AND_ACTIVATION_DATE(60,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.tenantReference),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.supplierReference),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ChangeOfSupplierID),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.scriptExecuted),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    CHANGE_OF_SUPPLIER_EXPORT_ENERGY(61,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.tenantReference),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.supplierReference),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ChangeOfSupplierID),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.scriptExecuted)
    ),
    CHANGE_OF_SUPPLIER_EXPORT_ENERGY_AND_ACTIVATION_DATE(62,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.tenantReference),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.supplierReference),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ChangeOfSupplierID),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.scriptExecuted),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    SET_ENGINEER_PIN(63,
            PropertySpecFactory.fixedLengthStringPropertySpec(DeviceMessageConstants.engineerPin, 8),
            PropertySpecFactory.timeDurationPropertySpec(DeviceMessageConstants.engineerPinTimeout, TimeDuration.seconds(30))
    ),
    SET_ENGINEER_PIN_AND_ACTIVATION_DATE(64,
            PropertySpecFactory.fixedLengthStringPropertySpec(DeviceMessageConstants.engineerPin, 8),
            PropertySpecFactory.timeDurationPropertySpec(DeviceMessageConstants.engineerPinTimeout, TimeDuration.seconds(30)),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ConfigurationChangeActivationDate)
    ),
    SetCalorificValue(65,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CalorificValue)
    ),
    SetConversionFactor(66,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ConversionFactor)
    ),
    ConfigureAllGasParameters(67,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.gasDensityAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.relativeDensityAttributeName),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.molecularNitrogenPercentageAttributeName, new BigDecimal(0), new BigDecimal(100)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.carbonDioxidePercentageAttributeName, new BigDecimal(0), new BigDecimal(100)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.carbonOxidePercentageAttributeName, new BigDecimal(0), new BigDecimal(100)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.molecularHydrogenPercentageAttributeName, new BigDecimal(0), new BigDecimal(100)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.methanePercentageAttributeName, new BigDecimal(0), new BigDecimal(100)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.higherCalorificValueAttributeName)),
    ChangeMeterLocation(68, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.meterLocationAttributeName)),
    SendShortDisplayMessage(69,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SHORT_DISPLAY_MESSAGE)),
    SendLongDisplayMessage(70,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.LONG_DISPLAY_MESSAGE)),
    ResetDisplayMessage(71) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    ConfigureLCDDisplay(72,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.NUMBER_OF_DIGITS_BEFORE_COMMA,
                    new BigDecimal(5),
                    new BigDecimal(6),
                    new BigDecimal(7)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.NUMBER_OF_DIGITS_AFTER_COMMA,
                    new BigDecimal(0),
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.DISPLAY_SEQUENCE),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.DISPLAY_CYCLE_TIME)
    ),
    ConfigureLoadProfileDataRecording(73,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.ENABLE_DISABLE,
                    new BigDecimal(0),
                    new BigDecimal(1)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.CONFIG_LOAD_PROFILE_INTERVAL,
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3),
                    new BigDecimal(5),
                    new BigDecimal(6),
                    new BigDecimal(10),
                    new BigDecimal(12),
                    new BigDecimal(15),
                    new BigDecimal(20),
                    new BigDecimal(30),
                    new BigDecimal(60),
                    new BigDecimal(120),
                    new BigDecimal(240)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MAX_NUMBER_RECORDS)
    ),

    ConfigureSpecialDataMode(74,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DAYS),
            PropertySpecFactory.datePropertySpec(DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DATE),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING,
                    new BigDecimal(0),
                    new BigDecimal(1)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_INTERVAL,
                    new BigDecimal(0),
                    new BigDecimal(1),
                    new BigDecimal(2)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_MAX_NUMBER_RECORDS, new BigDecimal(1), new BigDecimal(65535)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SPECIAL_LOAD_PROFILE,
                    new BigDecimal(0),
                    new BigDecimal(1)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SPECIAL_LOAD_PROFILE_INTERVAL,
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3),
                    new BigDecimal(5),
                    new BigDecimal(6),
                    new BigDecimal(10),
                    new BigDecimal(12),
                    new BigDecimal(15),
                    new BigDecimal(20),
                    new BigDecimal(30),
                    new BigDecimal(60),
                    new BigDecimal(120),
                    new BigDecimal(240)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.SPECIAL_LOAD_PROFILE_MAX_NO, new BigDecimal(1), new BigDecimal(65535))
    ),
    ConfigureMaxDemandSettings(75,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.ACTIVE_REGISTERS_0_OR_REACTIVE_REGISTERS_1,
                    new BigDecimal(0),
                    new BigDecimal(1)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.NUMBER_OF_SUBINTERVALS,
                    new BigDecimal(0),
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3),
                    new BigDecimal(4),
                    new BigDecimal(5),
                    new BigDecimal(10),
                    new BigDecimal(15)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SUB_INTERVAL_DURATION,
                    new BigDecimal(30),
                    new BigDecimal(60),
                    new BigDecimal(300),
                    new BigDecimal(600),
                    new BigDecimal(900),
                    new BigDecimal(1200),
                    new BigDecimal(1800),
                    new BigDecimal(3600))
    ),
    ConfigureConsumptionLimitationsSettings(76,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.NUMBER_OF_SUBINTERVALS,
                    new BigDecimal(0),
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3),
                    new BigDecimal(4),
                    new BigDecimal(5),
                    new BigDecimal(10),
                    new BigDecimal(15)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SUB_INTERVAL_DURATION,
                    new BigDecimal(30),
                    new BigDecimal(60),
                    new BigDecimal(300),
                    new BigDecimal(600),
                    new BigDecimal(900),
                    new BigDecimal(1200),
                    new BigDecimal(1800),
                    new BigDecimal(3600)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.OVERRIDE_RATE, new BigDecimal(0), new BigDecimal(4)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.ALLOWED_EXCESS_TOLERANCE, new BigDecimal(0), new BigDecimal(100)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.THRESHOLD_SELECTION,
                    new BigDecimal(0),
                    new BigDecimal(1)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE0),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE0),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.THRESHOLDS_MOMENTS),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE0),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE1),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE1),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.THRESHOLDS_MOMENTS),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE1),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.DAY_PROFILES),
            PropertySpecFactory.dateTimePropertySpec(DeviceMessageConstants.ACTIVATION_DATE)
    ),
    ConfigureEmergencyConsumptionLimitation(77,
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.DURATION_MINUTES, new BigDecimal(1), new BigDecimal(65535)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.TRESHOLD_VALUE),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.TRESHOLD_UNIT,
                    new BigDecimal(0),
                    new BigDecimal(1)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.OVERRIDE_RATE, new BigDecimal(0), new BigDecimal(4))
    ),
    ConfigureTariffSettings(78,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.UNIQUE_TARIFF_ID_NO),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.NUMBER_OF_TARIFF_RATES, new BigDecimal(0), new BigDecimal(4)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CODE_TABLE_ID)),
    EnableGzipCompression(79, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.ENABLE_GZIP_COMPRESSION)),
    SetAuthenticationMechanism(80, PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.SET_AUTHENTICATION_MECHANISM, AuthenticationMechanism.getAuthNames())),
    SetMaxLoginAttempts(81, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SET_MAX_LOGIN_ATTEMPTS)),
    SetLockoutDuration(82, PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DeviceMessageConstants.SET_LOCKOUT_DURATION, new TimeDuration(10000, TimeDuration.MILLISECONDS))),
    ConfigureGeneralLocalPortReadout(83, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.objectDefinitionsAttributeName)),
    DISABLE_PUSH_ON_INSTALLATION(84) {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList() ;
        }
    },
    ENABLE_PUSH_ON_INTERVAL_OBJECTS(85,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.typeAttributeName, PushType.getTypes()),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.executionMinutesForEachHour));

    private final long id;

    ConfigurationChangeDeviceMessage(int id) {
        this.id = id;
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

    protected PropertySpec temporalAmountSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .temporalAmountSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
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