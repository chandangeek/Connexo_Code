package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeOfDayFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetDSTAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetNTPOptionsAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetNTPServerAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetRefreshClockEveryAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetTimeAdjustmentAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetTimezoneAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.TimeZoneOffsetInHoursAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.dayOfMonth;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.dayOfWeek;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.dstEndAlgorithmAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.dstStartAlgorithmAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.enableDSTAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.hour;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.meterTimeAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.month;

/**
 * Provides a summary of all <i>Clock</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ClockDeviceMessage implements DeviceMessageSpecEnum {

    SET_TIME(DeviceMessageId.CLOCK_SET_TIME, "Set time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(meterTimeAttributeName, true, new DateAndTimeFactory()));
        }
    },
    SET_TIMEZONE_OFFSET(DeviceMessageId.CLOCK_SET_TIMEZONE_OFFSET, "Set time zone offset") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(TimeZoneOffsetInHoursAttributeName, true, new BigDecimalFactory()));
        }
    },

    EnableOrDisableDST(DeviceMessageId.CLOCK_ENABLE_OR_DISABLE_DST, "Enable or disable DST") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(enableDSTAttributeName, true, new BooleanFactory()));
        }
    },
    SetEndOfDST(DeviceMessageId.CLOCK_SET_END_OF_DST, "Set end of DST") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(month, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(dayOfMonth, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(dayOfWeek, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(hour, true, new BigDecimalFactory()));
        }
    },
    SetStartOfDST(DeviceMessageId.CLOCK_SET_START_OF_DST, "Set start of DST") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(month, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(dayOfMonth, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(dayOfWeek, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(hour, true, new BigDecimalFactory()));
        }
    },
    SetStartOfDSTWithoutHour(DeviceMessageId.CLOCK_SET_START_OF_DST_WITHOUT_HOUR, "Set start of DST without hour") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(month, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(dayOfMonth, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(dayOfWeek, true, new BigDecimalFactory()));
        }
    },
    SetEndOfDSTWithoutHour(DeviceMessageId.CLOCK_SET_END_OF_DST_WITHOUT_HOUR, "Set end of DST without hour") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(month, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(dayOfMonth, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(dayOfWeek, true, new BigDecimalFactory()));
        }
    },
    SetDSTAlgorithm(DeviceMessageId.CLOCK_SET_DST_ALGORITHM, "Set DST algorithm") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(dstStartAlgorithmAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(dstEndAlgorithmAttributeName, true, new StringFactory()));
        }
    },

    //EIWeb messages
    SetDST(DeviceMessageId.CLOCK_SET_DST, "Set DST") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetDSTAttributeName, true, new StringFactory()));
        }
    },
    SetTimezone(DeviceMessageId.CLOCK_SET_TIMEZONE, "Set time zone") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetTimezoneAttributeName, true, new StringFactory()));
        }
    },
    SetTimeAdjustment(DeviceMessageId.CLOCK_SET_TIME_ADJUSTMENT, "Set time adjustment") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetTimeAdjustmentAttributeName, true, new StringFactory()));
        }
    },
    SetNTPServer(DeviceMessageId.CLOCK_SET_NTP_SERVER, "Set NTP server") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetNTPServerAttributeName, true, new StringFactory()));
        }
    },
    SetRefreshClockEvery(DeviceMessageId.CLOCK_SET_REFRESH_CLOCK_EVERY, "Set refresh clock every") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetRefreshClockEveryAttributeName, true, new StringFactory()));
        }
    },
    SetNTPOptions(DeviceMessageId.CLOCK_SET_NTP_OPTIONS, "Set NTP options") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetNTPOptionsAttributeName, true, new StringFactory()));
        }
    },
    SyncTime(DeviceMessageId.CLOCK_SET_SYNCHRONIZE_TIME, "Synchronize the time"),
    CONFIGURE_DST(DeviceMessageId.CLOCK_SET_CONFIGURE_DST, "Configure DST"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.booleanPropertySpec(DeviceMessageConstants.enableDSTAttributeName, true, false));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.StartOfDSTAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EndOfDSTAttributeName, true, new DateAndTimeFactory()));
        }
    },
    CONFIGURE_DST_WITHOUT_HOUR(DeviceMessageId.CLOCK_SET_CONFIRUE_DST_WITHOUT_HOUR, "Configure DST without hour"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.booleanPropertySpec(DeviceMessageConstants.enableDSTAttributeName, true, false));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.StartOfDSTAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.EndOfDSTAttributeName, true, new DateAndTimeFactory()));
        }
    },
    ;

    private DeviceMessageId id;
    private String defaultTranslation;

    ClockDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return ClockDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}