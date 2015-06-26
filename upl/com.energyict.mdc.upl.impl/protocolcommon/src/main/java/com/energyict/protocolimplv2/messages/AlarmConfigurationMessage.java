package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all messages related to configuring alarms
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum AlarmConfigurationMessage implements DeviceMessageSpec {

    RESET_ALL_ALARM_BITS(0),
    WRITE_ALARM_FILTER(1, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.alarmFilterAttributeName)),
    CONFIGURE_PUSH_EVENT_NOTIFICATION(2,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.transportTypeAttributeName, TransportType.getTypes()),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.destinationAddressAttributeName),
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.messageTypeAttributeName, MessageType.getTypes())
    ),
    RESET_ALL_ERROR_BITS(3),
    RESET_DESCRIPTOR_FOR_ALARM_REGISTER_1_OR_2(4,
            alarmRegisterAttribute(),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.alarmBitMaskAttributeName)),
    RESET_BITS_IN_ALARM_REGISTER_1_OR_2(5, alarmRegisterAttribute()),
    WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2(6,
            alarmRegisterAttribute(),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.alarmFilterAttributeName)),

    FULLY_CONFIGURE_PUSH_EVENT_NOTIFICATION(7,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.typeAttributeName, PushType.getTypes()),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.objectDefinitionsAttributeName),
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.transportTypeAttributeName, TransportType.getTypes()),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.destinationAddressAttributeName),
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.messageTypeAttributeName, MessageType.getTypes())
    ),
    CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS(8,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.typeAttributeName, PushType.getTypes()),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.objectDefinitionsAttributeName)
    ),
    CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION(9,
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.typeAttributeName, PushType.getTypes()),
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.transportTypeAttributeName, TransportType.getTypes()),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.destinationAddressAttributeName),
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.messageTypeAttributeName, MessageType.getTypes())
    ),
    ENABLE_EVENT_NOTIFICATIONS(10,
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.EnableEventNotifications)
    ),
    ;


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

        private PushType(int id) {
            this.id = id;
        }

        public static String[] getTypes() {
            PushType[] allTypes = values();
            String[] result = new String[allTypes.length];
            for (int index = 0; index < allTypes.length; index++) {
                result[index] = allTypes[index].name();
            }
            return result;
        }

        public int getId() {
            return id;
        }
    }

    public enum MessageType {
        AXDR(0),
        XML(1);

        private final int id;

        private MessageType(int id) {
            this.id = id;
        }

        public static String[] getTypes() {
            MessageType[] allTypes = values();
            String[] result = new String[allTypes.length];
            for (int index = 0; index < allTypes.length; index++) {
                result[index] = allTypes[index].name();
            }
            return result;
        }

        public int getId() {
            return id;
        }
    }

    public enum TransportType {
        TCP(0),
        UDP(1);

        private final int id;

        private TransportType(int id) {
            this.id = id;
        }

        public static String[] getTypes() {
            TransportType[] allTypes = values();
            String[] result = new String[allTypes.length];
            for (int index = 0; index < allTypes.length; index++) {
                result[index] = allTypes[index].name();
            }
            return result;
        }

        public int getId() {
            return id;
        }
    }

    private static PropertySpec<BigDecimal> alarmRegisterAttribute() {
        return PropertySpecFactory.bigDecimalPropertySpecWithValues(
                DeviceMessageConstants.alarmRegisterAttributeName,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(2));
    }

    private static final DeviceMessageCategory displayCategory = DeviceMessageCategories.ALARM_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private AlarmConfigurationMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return displayCategory;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return AlarmConfigurationMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public int getMessageId() {
        return id;
    }
}
