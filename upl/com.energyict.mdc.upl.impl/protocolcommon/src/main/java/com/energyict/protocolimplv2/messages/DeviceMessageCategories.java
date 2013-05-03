package com.energyict.protocolimplv2.messages;

import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageCategoryPrimaryKey;
import com.energyict.mdc.messages.DeviceMessageSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Serves as a implementation to summarize <b>all</b> the supported standard
 * {@link DeviceMessageCategory DeviceMessageCategories}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:12)
 */
public enum DeviceMessageCategories implements DeviceMessageCategory {

    /**
     * The category for all messages that relate to the Activity Calendar
     */
    ACTIVITY_CALENDAR {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ActivityCalendarDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to the contactor/breaker/valve of a Device
     */
    CONTACTOR {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(ContactorDeviceMessage.values());
        }
    },
    /**
     * The category for all messages related to resetting values/registers/states/flags
     */
    RESET {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Collections.emptyList();
        }
    },
    /**
     * The category for all messages that will change the connectivity setup of a device.
     */
    NETWORK_AND_CONNECTIVITY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(NetworkConnectivityMessage.values());
        }
    },

    /**
     * The category for all messages that relate to authentication, authorisation and encryption.
     */
    SECURITY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(SecurityMessage.values());
        }
    },

    /**
     * The category for all message that relate to the device's firmware.
     */
    FIRMWARE {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(FirmwareDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to a device action
     */
    DEVICE_ACTIONS {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(DeviceActionMessage.values());
        }
    },
    /**
     * The category for all messages that relate to <i>a</i> display (InHomeDisplay, Display of E-meter, ...)
     */
    DISPLAY {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(DisplayDeviceMessage.values());
        }
    },
    /**
     * The category for all messages that relate to load limiting
     */
    LOAD_BALANCE {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(LoadBalanceDeviceMessage.values());
        }
    },
    /**
     * This category summarizes all advance test messages
     */
    ADVANCED_TEST {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(AdvancedTestMessage.values());
        }
    },
    /**
     * This category summarizes all messages related to a LoadProfile
     */
    LOAD_PROFILES {
        @Override
        public List<DeviceMessageSpec> getMessageSpecifications() {
            return Arrays.<DeviceMessageSpec>asList(LoadProfileMessage.values());
        }
    };


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
        return DeviceMessageCategories.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDescription() {
        return UserEnvironment.getDefault().getTranslation(this.getDescriptionResourceKey());
    }

    /**
     * Gets the resource key that determines the description
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getDescriptionResourceKey() {
        return this.getNameResourceKey() + ".description";
    }

    @Override
    public int getId() {
        return this.ordinal();
    }

    @Override
    public abstract List<DeviceMessageSpec> getMessageSpecifications();

    @Override
    public DeviceMessageCategoryPrimaryKey getPrimaryKey() {
        return new DeviceMessageCategoryPrimaryKey(this, name());
    }
}