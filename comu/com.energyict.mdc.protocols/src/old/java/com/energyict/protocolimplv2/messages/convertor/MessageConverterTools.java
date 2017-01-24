package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;

public class MessageConverterTools {

    /**
     * Searches for the {@link OfflineDeviceMessageAttribute}
     * in the given {@link OfflineDeviceMessage} which corresponds
     * with the provided name. If no match is found, then an empty
     * {@link OfflineDeviceMessageAttribute} is returned.
     *
     * @param offlineDeviceMessage the offlineDeviceMessage to search in
     * @param attributeName        the name of the OfflineDeviceMessageAttribute to return
     * @return the requested OfflineDeviceMessageAttribute or an empty OfflineDeviceMessageAttribute
     */
    public static OfflineDeviceMessageAttribute getDeviceMessageAttribute(OfflineDeviceMessage offlineDeviceMessage, String attributeName) {
        for (OfflineDeviceMessageAttribute offlineDeviceMessageAttribute : offlineDeviceMessage.getDeviceMessageAttributes()) {
            if (offlineDeviceMessageAttribute.getName().equals(attributeName)) {
                return offlineDeviceMessageAttribute;
            }
        }
        return new EmptyOfflineDeviceMessageAttribute(offlineDeviceMessage);
    }

    private static class EmptyOfflineDeviceMessageAttribute implements OfflineDeviceMessageAttribute {
        private final OfflineDeviceMessage offlineDeviceMessage;

        private EmptyOfflineDeviceMessageAttribute(OfflineDeviceMessage offlineDeviceMessage) {
            this.offlineDeviceMessage = offlineDeviceMessage;
        }

        @Override
        public PropertySpec getPropertySpec() {
            return new EmptyAttributePropertySpec();
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getDeviceMessageAttributeValue() {
            return "";
        }

    }

    private static class EmptyAttributePropertySpec implements PropertySpec {
        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getDisplayName() {
            return this.getName();
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public ValueFactory<String> getValueFactory() {
            return new StringFactory();
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public boolean isReference() {
            return false;
        }

        @Override
        public boolean supportsMultiValues() {
            return false;
        }

        @Override
        public boolean validateValue(Object s) throws InvalidValueException {
            return s instanceof String;
        }

        @Override
        public boolean validateValueIgnoreRequired(Object s) throws InvalidValueException {
            return s instanceof String;
        }

        @Override
        public PropertySpecPossibleValues getPossibleValues() {
            return null;
        }
    }

}