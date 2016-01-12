package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a summary of all messages that have no unique goal.
 * For example, this can be a message that writes a general value to a certain DLMS object, chosen by the user (obiscode).
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum GeneralDeviceMessage implements DeviceMessageSpecEnum {

    WRITE_RAW_IEC1107_CLASS(DeviceMessageId.GENERAL_WRITE_RAW_IEC1107_CLASS, "Write raw IEC1107 class") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .boundedBigDecimalSpec(BigDecimal.ZERO, Constants.UPPER_LIMIT)
                            .named(DeviceMessageAttributes.IEC1107ClassIdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .boundedBigDecimalSpec(BigDecimal.ZERO, Constants.UPPER_LIMIT)
                            .named(DeviceMessageAttributes.OffsetAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(DeviceMessageAttributes.RawDataAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    WRITE_FULL_CONFIGURATION(DeviceMessageId.GENERAL_WRITE_FULL_CONFIGURATION, "Write full configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(UserFile.class)
                            .named(DeviceMessageAttributes.configUserFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    GeneralDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return GeneralDeviceMessage.class.getSimpleName() + "." + this.toString();
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

    private static class Constants {
        private static final BigDecimal UPPER_LIMIT = BigDecimal.valueOf(9999);
    }

}