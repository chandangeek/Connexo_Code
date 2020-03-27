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
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues())
            );
        }
    },
    CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION(41002, "Change unit charge passive with(out) activation") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues()),
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
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues()),
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
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues())
            );
        }
    },
    CHANGE_CHARGE_PERIOD(41005, "Change charge period") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargePeriod, DeviceMessageConstants.chargePeriodDefaultTranslation)
            );
        }
    },
    CHANGE_CHARGE_PROPORTION(41006, "Change charge proportion") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargeProportion, DeviceMessageConstants.chargeProportionDefaultTranslation)
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

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
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