package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides a summary of all <i>Contactor</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:00
 */
public enum ChargeDeviceMessage implements DeviceMessageSpecSupplier {

    ACTIVATE_PASSIVE_UNIT_CHARGE(41001, "Activate passive unit charge") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeType, DeviceMessageConstants.chargeTypeDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues())
            );
        }
    },
    CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION(41002, "Change unit charge passive with(out) activation") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeType, DeviceMessageConstants.chargeTypeDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues()),
                    this.booleanSpec(service, DeviceMessageConstants.passiveImmediateActivation, DeviceMessageConstants.passiveImmediateActivationDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeCommodityScale, DeviceMessageConstants.chargeCommodityScaleDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargePriceScale, DeviceMessageConstants.chargePriceScaleDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime1, DeviceMessageConstants.chargeTableTime1DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit1, DeviceMessageConstants.chargeTableUnit1DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime2, DeviceMessageConstants.chargeTableTime2DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit2, DeviceMessageConstants.chargeTableUnit2DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime3, DeviceMessageConstants.chargeTableTime3DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit3, DeviceMessageConstants.chargeTableUnit3DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime4, DeviceMessageConstants.chargeTableTime4DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit4, DeviceMessageConstants.chargeTableUnit4DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime5, DeviceMessageConstants.chargeTableTime5DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit5, DeviceMessageConstants.chargeTableUnit5DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime6, DeviceMessageConstants.chargeTableTime6DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit6, DeviceMessageConstants.chargeTableUnit6DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime7, DeviceMessageConstants.chargeTableTime7DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit7, DeviceMessageConstants.chargeTableUnit7DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime8, DeviceMessageConstants.chargeTableTime8DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit8, DeviceMessageConstants.chargeTableUnit8DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime9, DeviceMessageConstants.chargeTableTime9DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit9, DeviceMessageConstants.chargeTableUnit9DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime10, DeviceMessageConstants.chargeTableTime10DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit10, DeviceMessageConstants.chargeTableUnit10DefaultTranslation)

                    );
        }
    },
    CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION_DATE(41003, "Change unit charge passive with activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeType, DeviceMessageConstants.chargeTypeDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues()),
                    this.dateTimeSpec(service, DeviceMessageConstants.passiveUnitChargeActivationTime, DeviceMessageConstants.passiveUnitChargeActivationTimeDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeCommodityScale, DeviceMessageConstants.chargeCommodityScaleDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargePriceScale, DeviceMessageConstants.chargePriceScaleDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime1, DeviceMessageConstants.chargeTableTime1DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit1, DeviceMessageConstants.chargeTableUnit1DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime2, DeviceMessageConstants.chargeTableTime2DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit2, DeviceMessageConstants.chargeTableUnit2DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime3, DeviceMessageConstants.chargeTableTime3DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit3, DeviceMessageConstants.chargeTableUnit3DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime4, DeviceMessageConstants.chargeTableTime4DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit4, DeviceMessageConstants.chargeTableUnit4DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime5, DeviceMessageConstants.chargeTableTime5DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit5, DeviceMessageConstants.chargeTableUnit5DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime6, DeviceMessageConstants.chargeTableTime6DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit6, DeviceMessageConstants.chargeTableUnit6DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime7, DeviceMessageConstants.chargeTableTime7DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit7, DeviceMessageConstants.chargeTableUnit7DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime8, DeviceMessageConstants.chargeTableTime8DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit8, DeviceMessageConstants.chargeTableUnit8DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime9, DeviceMessageConstants.chargeTableTime9DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit9, DeviceMessageConstants.chargeTableUnit9DefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.chargeTableTime10, DeviceMessageConstants.chargeTableTime10DefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeTableUnit10, DeviceMessageConstants.chargeTableUnit10DefaultTranslation)
                    );
        }
    },
    UPDATE_UNIT_CHARGE(41004, "Update unit charge") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeType, DeviceMessageConstants.chargeTypeDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues())
            );
        }
    },
    CHANGE_CHARGE_PERIOD(41005, "Change charge period") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeType, DeviceMessageConstants.chargeTypeDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargePeriod, DeviceMessageConstants.chargePeriodDefaultTranslation)
            );
        }
    },
    CHANGE_CHARGE_PROPORTION(41006, "Change charge proportion") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeType, DeviceMessageConstants.chargeTypeDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeProportion, DeviceMessageConstants.chargeProportionDefaultTranslation)
            );
        }
    },
    CHANGE_STEP_TARIFF(41007, "Change passive step tariff") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.tariffCode, DeviceMessageConstants.tariffCodeDefaultTranslation),
                    this.stringDefSpec(service, DeviceMessageConstants.additionalTaxesType, DeviceMessageConstants.additionalTaxesTypeDefaultTranslation, ChargeDeviceMessage.AdditionalTaxesType.getDescriptionValues()),
                    this.stringDefSpec(service, DeviceMessageConstants.graceRecalculationType, DeviceMessageConstants.graceRecalculationTypeDefaultTranslation, ChargeDeviceMessage.GraceRecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceRecalculationValue, DeviceMessageConstants.graceRecalculationValueDefaultTranslation, new BigDecimal(0)),

                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeStep1, DeviceMessageConstants.chargeStep1DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.priceStep1, DeviceMessageConstants.priceStep1DefaultTranslation, new BigDecimal(0)),
                    this.stringDefSpec(service, DeviceMessageConstants.recalculationTypeStep1, DeviceMessageConstants.recalculationTypeStep1DefaultTranslation, ChargeDeviceMessage.RecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceWarningStep1, DeviceMessageConstants.graceWarningStep1DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.additionalTaxStep1, DeviceMessageConstants.additionalTaxStep1DefaultTranslation, new BigDecimal(0)),

                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeStep2, DeviceMessageConstants.chargeStep2DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.priceStep2, DeviceMessageConstants.priceStep2DefaultTranslation, new BigDecimal(0)),
                    this.stringDefSpec(service, DeviceMessageConstants.recalculationTypeStep2, DeviceMessageConstants.recalculationTypeStep2DefaultTranslation, ChargeDeviceMessage.RecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceWarningStep2, DeviceMessageConstants.graceWarningStep2DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.additionalTaxStep2, DeviceMessageConstants.additionalTaxStep2DefaultTranslation, new BigDecimal(0)),

                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeStep3, DeviceMessageConstants.chargeStep3DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.priceStep3, DeviceMessageConstants.priceStep3DefaultTranslation, new BigDecimal(0)),
                    this.stringDefSpec(service, DeviceMessageConstants.recalculationTypeStep3, DeviceMessageConstants.recalculationTypeStep3DefaultTranslation, ChargeDeviceMessage.RecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceWarningStep3, DeviceMessageConstants.graceWarningStep3DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.additionalTaxStep3, DeviceMessageConstants.additionalTaxStep3DefaultTranslation, new BigDecimal(0)),

                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeStep4, DeviceMessageConstants.chargeStep4DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.priceStep4, DeviceMessageConstants.priceStep4DefaultTranslation, new BigDecimal(0)),
                    this.stringDefSpec(service, DeviceMessageConstants.recalculationTypeStep4, DeviceMessageConstants.recalculationTypeStep4DefaultTranslation, ChargeDeviceMessage.RecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceWarningStep4, DeviceMessageConstants.graceWarningStep4DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.additionalTaxStep4, DeviceMessageConstants.additionalTaxStep4DefaultTranslation, new BigDecimal(0)),

                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeStep5, DeviceMessageConstants.chargeStep5DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.priceStep5, DeviceMessageConstants.priceStep5DefaultTranslation, new BigDecimal(0)),
                    this.stringDefSpec(service, DeviceMessageConstants.recalculationTypeStep5, DeviceMessageConstants.recalculationTypeStep5DefaultTranslation, ChargeDeviceMessage.RecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceWarningStep5, DeviceMessageConstants.graceWarningStep5DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.additionalTaxStep5, DeviceMessageConstants.additionalTaxStep5DefaultTranslation, new BigDecimal(0)),

                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeStep6, DeviceMessageConstants.chargeStep6DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.priceStep6, DeviceMessageConstants.priceStep6DefaultTranslation, new BigDecimal(0)),
                    this.stringDefSpec(service, DeviceMessageConstants.recalculationTypeStep6, DeviceMessageConstants.recalculationTypeStep6DefaultTranslation, ChargeDeviceMessage.RecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceWarningStep6, DeviceMessageConstants.graceWarningStep6DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.additionalTaxStep6, DeviceMessageConstants.additionalTaxStep6DefaultTranslation, new BigDecimal(0)),

                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeStep7, DeviceMessageConstants.chargeStep7DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.priceStep7, DeviceMessageConstants.priceStep7DefaultTranslation, new BigDecimal(0)),
                    this.stringDefSpec(service, DeviceMessageConstants.recalculationTypeStep7, DeviceMessageConstants.recalculationTypeStep7DefaultTranslation, ChargeDeviceMessage.RecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceWarningStep7, DeviceMessageConstants.graceWarningStep7DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.additionalTaxStep7, DeviceMessageConstants.additionalTaxStep7DefaultTranslation, new BigDecimal(0)),

                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeStep8, DeviceMessageConstants.chargeStep8DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.priceStep8, DeviceMessageConstants.priceStep8DefaultTranslation, new BigDecimal(0)),
                    this.stringDefSpec(service, DeviceMessageConstants.recalculationTypeStep8, DeviceMessageConstants.recalculationTypeStep8DefaultTranslation, ChargeDeviceMessage.RecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceWarningStep8, DeviceMessageConstants.graceWarningStep8DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.additionalTaxStep8, DeviceMessageConstants.additionalTaxStep8DefaultTranslation, new BigDecimal(0)),

                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeStep9, DeviceMessageConstants.chargeStep9DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.priceStep9, DeviceMessageConstants.priceStep9DefaultTranslation, new BigDecimal(0)),
                    this.stringDefSpec(service, DeviceMessageConstants.recalculationTypeStep9, DeviceMessageConstants.recalculationTypeStep9DefaultTranslation, ChargeDeviceMessage.RecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceWarningStep9, DeviceMessageConstants.graceWarningStep9DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.additionalTaxStep9, DeviceMessageConstants.additionalTaxStep9DefaultTranslation, new BigDecimal(0)),

                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeStep10, DeviceMessageConstants.chargeStep10DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.priceStep10, DeviceMessageConstants.priceStep10DefaultTranslation, new BigDecimal(0)),
                    this.stringDefSpec(service, DeviceMessageConstants.recalculationTypeStep10, DeviceMessageConstants.recalculationTypeStep10DefaultTranslation, ChargeDeviceMessage.RecalculationType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.graceWarningStep10, DeviceMessageConstants.graceWarningStep10DefaultTranslation, new BigDecimal(0)),
                    this.bigDecimalSpec(service, DeviceMessageConstants.additionalTaxStep10, DeviceMessageConstants.additionalTaxStep10DefaultTranslation, new BigDecimal(0))
            );
        }
    },
    CHANGE_TAX_RATES(41008, "Change passive tax rates") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.monthlyTax, DeviceMessageConstants.monthlyTaxDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.zeroConsumptionTax, DeviceMessageConstants.zeroConsumptionTaxDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.consumptionTax, DeviceMessageConstants.consumptionTaxDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.consumptionAmount, DeviceMessageConstants.consumptionAmountDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.consumptionLimit, DeviceMessageConstants.consumptionLimitDefaultTranslation)
            );
        }
    },
    SWITCH_CHARGE_MODE(41009, "Switch charge mode prepaid - postpaid") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeMode, DeviceMessageConstants.chargeModeDefaultTranslation, ChargeDeviceMessage.ChargeMode.getDescriptionValues()),
                    this.dateTimeSpec(service, DeviceMessageConstants.activationDate, DeviceMessageConstants.activationDateDefaultTranslation)
            );
        }
    },
    SWITCH_TAX_AND_STEP_TARIFF(41010, "Activate the passive tax and step tariff") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.tariffType, DeviceMessageConstants.tariffTypeDefaultTranslation, ChargeDeviceMessage.TariffType.getDescriptionValues()),
                    this.dateTimeSpec(service, DeviceMessageConstants.activationDate, DeviceMessageConstants.activationDateDefaultTranslation)
            );
        }
    },
    FRIENDLY_DAY_PERIOD_UPDATE(41011, "Update the friendly day period") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.friendlyHourStart, DeviceMessageConstants.friendlyHourStartDefaultTranslation, new BigDecimal(0), new BigDecimal(0), new BigDecimal(23)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.friendlyMinuteStart, DeviceMessageConstants.friendlyMinuteStartDefaultTranslation, new BigDecimal(0), new BigDecimal(0), new BigDecimal(59)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.friendlySecondStart, DeviceMessageConstants.friendlySecondStartDefaultTranslation, new BigDecimal(0), new BigDecimal(0), new BigDecimal(59)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.friendlyHundredthsStart, DeviceMessageConstants.friendlyHundredthsStartDefaultTranslation, new BigDecimal(0), new BigDecimal(0), new BigDecimal(99)),

                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.friendlyHourStop, DeviceMessageConstants.friendlyHourStopDefaultTranslation, new BigDecimal(0), new BigDecimal(0), new BigDecimal(23)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.friendlyMinuteStop, DeviceMessageConstants.friendlyMinuteStopDefaultTranslation, new BigDecimal(0), new BigDecimal(0), new BigDecimal(59)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.friendlySecondStop, DeviceMessageConstants.friendlySecondStopDefaultTranslation, new BigDecimal(0), new BigDecimal(0), new BigDecimal(59)),
                    this.boundedBigDecimalSpec(service, DeviceMessageConstants.friendlyHundredthsStop, DeviceMessageConstants.friendlyHundredthsStopDefaultTranslation, new BigDecimal(0), new BigDecimal(0), new BigDecimal(99))

            );
        }
    },
    FRIENDLY_WEEKDAYS_UPDATE(41012, "Update the friendly week days") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpecWithDef(service, DeviceMessageConstants.friendlyWeekdays, DeviceMessageConstants.friendlyWeekdaysDefaultTranslation, "0000000")
            );
        }
    }
   ;
    private final long id;
    private final String defaultNameTranslation;

    ChargeDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... exhaustiveValues) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(exhaustiveValues)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec stringDefSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... exhaustiveValues) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(exhaustiveValues)
                .setDefaultValue(exhaustiveValues.length > 0 ? exhaustiveValues[0] : null)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec stringSpecWithDef(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String defaultValue) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
                .markRequired()
                .finish();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal defValue) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).setDefaultValue(defValue).finish();
    }

    protected PropertySpec boundedBigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal defValue, BigDecimal minValue, BigDecimal maxValue) {
        return this.boundedBigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation, minValue, maxValue).setDefaultValue(defValue).finish();
    }

    protected PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpecBuilder<BigDecimal> boundedBigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal lowerLimit, BigDecimal upperLimit) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .boundedBigDecimalSpec(lowerLimit, upperLimit)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    private String getNameResourceKey() {
        return ChargeDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.CHARGE_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

    public enum ChargeMode {
        Postpaid_charge(1, "Postpaid"),
        Prepaid_charge(2, "Prepaid");


        private final int id;
        private final String description;

        ChargeMode(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static ChargeMode entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String getDescriptionValue(int id) {
            return Stream
                    .of(values())
                    .filter(each -> each.getId() == id)
                    .findFirst()
                    .get().getDescription();
        }

        public static String[] getDescriptionValues() {
            ChargeMode[] allObjects = values();
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

    public enum TariffType {
        Passive_step_tariff(3, "Passive step tariff"),
        Passive_TOU_tariff(4, "Passive TOU tariff");

        private final int id;
        private final String description;

        TariffType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static TariffType entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String[] getDescriptionValues() {
            TariffType[] allObjects = values();
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

    public enum AdditionalTaxesType {
        Enum0(0, "Enum 0"),
        Enum1(1, "Enum 1"),
        Enum2(2, "Enum 2");

        private final int id;
        private final String description;

        AdditionalTaxesType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static AdditionalTaxesType entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String getDescriptionValue(int id) {
            return Stream
                    .of(values())
                    .filter(each -> each.getId() == id)
                    .findFirst()
                    .get().getDescription();
        }

        public static String[] getDescriptionValues() {
            AdditionalTaxesType[] allObjects = values();
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

    public enum RecalculationType {
        Disable(0, "Disable"),
        Recalculate_Only(1, "Recalculate Only"),
        Recalculate_With_Grace(2, "Recalculate With Grace");

        private final int id;
        private final String description;

        RecalculationType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static RecalculationType entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String getDescriptionValue(int id) {
            return Stream
                    .of(values())
                    .filter(each -> each.getId() == id)
                    .findFirst()
                    .get().getDescription();
        }

        public static String[] getDescriptionValues() {
            RecalculationType[] allObjects = values();
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

    public enum GraceRecalculationType {
        ValueInDays(0, "Value in days"),
        ValueInConsumption(1, "Value in consumption");

        private final int id;
        private final String description;

        GraceRecalculationType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static GraceRecalculationType entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String getDescriptionValue(int id) {
            return Stream
                    .of(values())
                    .filter(each -> each.getId() == id)
                    .findFirst()
                    .get().getDescription();
        }

        public static String[] getDescriptionValues() {
            GraceRecalculationType[] allObjects = values();
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

    public enum ChargeType {
        Consumption_tax_charge(0, "Consumption Tax Charge"),
        TOU_import_charge(1, "TOU Import Charge"),
        Monthly_tax_charge(2, "Monthly Tax Charge");

        private final int id;
        private final String description;

        ChargeType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static ChargeType entryForDescription(String description) {
            return Stream
                    .of(values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String[] getDescriptionValues() {
            ChargeType[] allObjects = values();
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
}