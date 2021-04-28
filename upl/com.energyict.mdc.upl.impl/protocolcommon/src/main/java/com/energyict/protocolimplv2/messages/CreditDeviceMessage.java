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
public enum CreditDeviceMessage implements DeviceMessageSpecSupplier {

    UPDATE_CREDIT_AMOUNT(40001, "Update credit amount") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.creditType, DeviceMessageConstants.creditTypeDefaultTranslation, CreditType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.creditAmount, DeviceMessageConstants.creditAmountDefaultTranslation)
            );
        }
    },
    UPDATE_MONEY_CREDIT_THRESHOLD(40011, "Update money credit threshold") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.creditType, DeviceMessageConstants.creditTypeDefaultTranslation, CreditType.getDescriptionValues()),
                    this.stringSpec(service, DeviceMessageConstants.currency, DeviceMessageConstants.currencyDefaultTranslation, 3),
                    this.bigDecimalSpec(service, DeviceMessageConstants.remainingCreditHigh, DeviceMessageConstants.remainingCreditHighDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.remainingCreditLow, DeviceMessageConstants.remainingCreditLowDefaultTranslation)
            );
        }
    },
    UPDATE_CONSUMPTION_CREDIT_THRESHOLD(40012, "Update consumption credit threshold") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.creditType, DeviceMessageConstants.creditTypeDefaultTranslation, CreditType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.consumedCreditHigh, DeviceMessageConstants.consumedCreditHighDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.consumedCreditLow, DeviceMessageConstants.consumedCreditLowDefaultTranslation)
            );
        }
    },
    UPDATE_TIME_CREDIT_THRESHOLD(40013, "Update time credit threshold") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.creditType, DeviceMessageConstants.creditTypeDefaultTranslation, CreditType.getDescriptionValues()),
                    this.bigDecimalSpec(service, DeviceMessageConstants.remainingTimeHigh, DeviceMessageConstants.remainingTimeHighDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.remainingTimeLow, DeviceMessageConstants.remainingTimeLowDefaultTranslation)
            );
        }
    },
    UPDATE_CREDIT_DAYS_LIMIT(40014, "Update credit days limit") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.bigDecimalSpec(service, DeviceMessageConstants.creditDaysLimitFirst, DeviceMessageConstants.creditDaysLimitFirstDefaultTranslation),
                    this.bigDecimalSpec(service, DeviceMessageConstants.creditDaysLimitScnd, DeviceMessageConstants.creditDaysLimitScndDefaultTranslation)
            );
        }
    }
   ;
    private final long id;
    private final String defaultNameTranslation;

    CreditDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    private String getNameResourceKey() {
        return CreditDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.CREDIT_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

    public enum CreditType {
        Import_credit(0, "Import Credit"),
        Emergency_credit(1, "Emergency Credit");

        private final int id;
        private final String description;

        CreditType(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static CreditType entryForDescription(String description) {
            return Stream
                    .of(CreditType.values())
                    .filter(each -> each.getDescription().equals(description))
                    .findFirst()
                    .get();
        }

        public static String[] getDescriptionValues() {
            CreditType[] allObjects = CreditType.values();
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