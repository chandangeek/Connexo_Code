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
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides a summary of all messages related to configuring alarms.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum AlarmConfigurationMessage implements DeviceMessageSpecSupplier {

    RESET_ALL_ALARM_BITS(2001, "Reset all alarm bits") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    WRITE_ALARM_FILTER(2002, "Write alarm filter") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.bigDecimalSpec(service, DeviceMessageConstants.alarmFilterAttributeName, DeviceMessageConstants.alarmFilterAttributeDefaultTranslation));
        }
    },
    CONFIGURE_PUSH_EVENT_NOTIFICATION(2004, "Configure push event notification") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.transportTypeAttributeName, DeviceMessageConstants.transportTypeAttributeDefaultTranslation, TransportType.getTypes()),
                    this.stringSpec(service, DeviceMessageConstants.destinationAddressAttributeName, DeviceMessageConstants.destinationAddressAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.messageTypeAttributeName, DeviceMessageConstants.messageTypeAttributeDefaultTranslation, MessageType.getTypes())
            );
        }
    },
    RESET_ALL_ERROR_BITS(2003, "Reset all error bits") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }
    },
    RESET_DESCRIPTOR_FOR_ALARM_REGISTER_1_OR_2(2005, "Reset alarm descriptor") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.alarmRegisterAttribute(service),
                    this.bigDecimalSpec(service, DeviceMessageConstants.alarmBitMaskAttributeName, DeviceMessageConstants.alarmBitMaskAttributeDefaultTranslation)
            );
        }
    },
    RESET_BITS_IN_ALARM_REGISTER_1_OR_2(2006, "Reset alarm bits") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.alarmRegisterAttribute(service));
        }
    },
    WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2(2007, "Write alarm filter") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.alarmRegisterAttribute(service),
                    this.bigDecimalSpec(service, DeviceMessageConstants.alarmFilterAttributeName, DeviceMessageConstants.alarmFilterAttributeDefaultTranslation)
            );
        }
    },
    FULLY_CONFIGURE_PUSH_EVENT_NOTIFICATION(2008, "Configure push event notifications") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.typeAttributeName, DeviceMessageConstants.typeAttributeDefaultTranslation, PushType.getTypes()),
                    this.stringSpec(service, DeviceMessageConstants.objectDefinitionsAttributeName, DeviceMessageConstants.objectDefinitionsAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.transportTypeAttributeName, DeviceMessageConstants.transportTypeAttributeDefaultTranslation, TransportType.getTypes()),
                    this.stringSpec(service, DeviceMessageConstants.destinationAddressAttributeName, DeviceMessageConstants.destinationAddressAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.messageTypeAttributeName, DeviceMessageConstants.messageTypeAttributeDefaultTranslation, MessageType.getTypes())
            );
        }
    },
    CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS(2009, "Configure push event notification object definitions") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.typeAttributeName, DeviceMessageConstants.typeAttributeDefaultTranslation, PushType.getTypes()),
                    this.stringSpec(service, DeviceMessageConstants.objectDefinitionsAttributeName, DeviceMessageConstants.objectDefinitionsAttributeDefaultTranslation)
            );
        }
    },
    CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION(2010, "Configure push event notification destination") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.typeAttributeName, DeviceMessageConstants.typeAttributeDefaultTranslation, PushType.getTypes()),
                    this.stringSpec(service, DeviceMessageConstants.transportTypeAttributeName, DeviceMessageConstants.transportTypeAttributeDefaultTranslation, TransportType.getTypes()),
                    this.stringSpec(service, DeviceMessageConstants.destinationAddressAttributeName, DeviceMessageConstants.destinationAddressAttributeDefaultTranslation),
                    this.stringSpec(service, DeviceMessageConstants.messageTypeAttributeName, DeviceMessageConstants.messageTypeAttributeDefaultTranslation, MessageType.getTypes())
            );
        }
    },
    ENABLE_EVENT_NOTIFICATIONS(2011, "Enable event notifications") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.booleanSpec(service, DeviceMessageConstants.EnableEventNotifications, DeviceMessageConstants.EnableEventNotificationsDefaultTranslation));
        }
    },
    RESET_DESCRIPTOR_FOR_ALARM_REGISTER(2012, "Reset alarm descriptor") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.alarmRegisterAttributeFor3Objects(service),
                    this.bigDecimalSpec(service, DeviceMessageConstants.alarmBitMaskAttributeName, DeviceMessageConstants.alarmBitMaskAttributeDefaultTranslation)
            );
        }
    },
    RESET_BITS_IN_ALARM_REGISTER(2013, "Reset alarm bits") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.alarmRegisterAttributeFor3Objects(service));
        }
    },
    WRITE_FILTER_FOR_ALARM_REGISTER(2014, "Write alarm filter") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.alarmRegisterAttributeFor3Objects(service),
                    this.bigDecimalSpec(service, DeviceMessageConstants.alarmFilterAttributeName, DeviceMessageConstants.alarmFilterAttributeDefaultTranslation)
            );
        }
    };

    public enum PushType {
        Interval_1(1),
        Interval_2(2),
        Interval_3(3),
        On_Alarm(4),
        On_Connectivity(0),
        On_Installation(7),
        On_Power_Down(5),
        Consumer_Information(6);

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

    public enum MessageType {
        AXDR(0),
        XML(1);

        private final int id;

        MessageType(int id) {
            this.id = id;
        }

        public static String[] getTypes() {
            return Stream.of(values()).map(MessageType::name).toArray(String[]::new);
        }

        public static String getStringValue(int id) {
            return Stream
                    .of(values())
                    .filter(each -> each.getId() == id)
                    .findFirst()
                    .map(MessageType::name)
                    .orElse("Unknown message type");
        }

        public int getId() {
            return id;
        }
    }

    public enum TransportType {
        TCP(0),
        UDP(1);

        private final int id;

        TransportType(int id) {
            this.id = id;
        }

        public static String[] getTypes() {
            return Stream.of(values()).map(TransportType::name).toArray(String[]::new);
        }

        public static String getStringValue(int id) {
            return Stream
                        .of(values())
                        .filter(each -> each.getId() == id)
                        .findFirst()
                        .map(TransportType::name)
                        .orElse("Unknown transport type");
        }

        public int getId() {
            return id;
        }
    }

    private final long id;
    private final String defaultNameTranslation;

    AlarmConfigurationMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    protected PropertySpec alarmRegisterAttribute(PropertySpecService service) {
        return this.bigDecimalSpec(
                service,
                DeviceMessageConstants.alarmRegisterAttributeName, DeviceMessageConstants.alarmRegisterAttributeDefaultTranslation,
                BigDecimal.ONE,
                BigDecimal.valueOf(2));
    }

    protected PropertySpec alarmRegisterAttributeFor3Objects(PropertySpecService service) {
        return this.bigDecimalSpec(service,
                    DeviceMessageConstants.alarmRegisterAttributeName, DeviceMessageConstants.alarmRegisterAttributeDefaultTranslation,
                    BigDecimal.ONE,
                    BigDecimal.valueOf(2),
                    BigDecimal.valueOf(3));
    }

    private PropertySpecBuilder<BigDecimal> bigDecimalSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... exhaustiveValues) {
        return this.bigDecimalSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).addValues(exhaustiveValues).markExhaustive().finish();
    }

    protected PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... exhaustiveValues) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(exhaustiveValues)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private String getNameResourceKey() {
        return AlarmConfigurationMessage.class.getSimpleName() + "." + this.toString();
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.ALARM_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}