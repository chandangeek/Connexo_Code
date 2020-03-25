package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
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

    ACTIVATE_PASSIVE_UNIT_CHARGE(39001, "Activate passive unit charge") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getTypes())
            );
        }
    },
    CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION(39002, "Change unit charge passive with(out) activation") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getTypes()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.passiveUnitCharge, DeviceMessageConstants.passiveUnitChargeDefaultTranslation),
                    this.booleanSpec(service, DeviceMessageConstants.passiveImmediateActivation, DeviceMessageConstants.passiveImmediateActivationDefaultTranslation)
                    );
        }
    },
    CHANGE_UNIT_CHARGE_PASSIVE_WITH_ACTIVATION_DATE(39003, "Change unit charge passive with activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getTypes()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.passiveUnitCharge, DeviceMessageConstants.passiveUnitChargeDefaultTranslation),
                    this.dateTimeSpec(service, DeviceMessageConstants.passiveUnitChargeActivationTime, DeviceMessageConstants.passiveUnitChargeActivationTimeDefaultTranslation)
                    );
        }
    },
    UPDATE_UNIT_CHARGE(39004, "Update unit charge") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getTypes())
            );
        }
    },
    CHANGE_CHARGE_PERIOD(39005, "Change charge period") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getTypes()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.chargePeriod, DeviceMessageConstants.chargePeriodDefaultTranslation)
            );
        }
    },
    CHANGE_CHARGE_PROPORTION(39006, "Change charge period") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.chargeTypeAttributeName, DeviceMessageConstants.chargeTypeAttributeNameDefaultTranslation, ChargeDeviceMessage.ChargeType.getTypes()),
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

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .addValues(possibleValues)
                .markExhaustive()
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return ChargeDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.CONTACTOR,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

    public enum ChargeType {
        ACTIVE_REACTIVE(0),
        TOU_IMPORT_CHARGE(1),
        MONTHLY_TAX_CHARGE(2);

        private final int id;

        ChargeType(int id) {
            this.id = id;
        }

        public static String[] getTypes() {
            return Stream.of(values()).map(ChargeType::name).toArray(String[]::new);
        }

        public static String getStringValue(int id) {
            return Stream
                    .of(values())
                    .filter(each -> each.getId() == id)
                    .findFirst()
                    .map(ChargeType::name)
                    .orElse("Unknown charge type");
        }

        public int getId() {
            return id;
        }
    }
}