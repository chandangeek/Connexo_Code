/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;
import com.energyict.protocols.exception.ProtocolEncryptionException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import com.energyict.dlms.DLMSUtils;
import com.energyict.protocolimplv2.common.BasicDynamicPropertySupport;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.DELAY_AFTER_ERROR;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.TIMEOUT;

/**
 * @author sva
 * @since 17/06/2014 - 13:40
 */
public class GarnetProperties implements HasDynamicProperties {

    public static final String DEVICE_ID = "DeviceId";
    public static final BigDecimal DEFAULT_DEVICE_ID = BigDecimal.ZERO;
    public static final TimeDuration DEFAULT_TIMEOUT = new TimeDuration(10, TimeDuration.TimeUnit.SECONDS);
    public static final TimeDuration DEFAULT_FORCED_DELAY = new TimeDuration(0, TimeDuration.TimeUnit.MILLISECONDS);
    public static final TimeDuration DEFAULT_DELAY_AFTER_ERROR = new TimeDuration(100, TimeDuration.TimeUnit.MILLISECONDS);

    private final PropertySpecService propertySpecService;
    private TypedProperties properties;
    private DeviceProtocolSecurityPropertySet securityPropertySet;

    private byte[] manufacturerKey;
    private byte[] customerKey;

    public GarnetProperties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    /**
     * The security set of a device. It contains all properties related to security.
     */
    public DeviceProtocolSecurityPropertySet getSecurityPropertySet() {
        return securityPropertySet;
    }

    /**
     * Setter for the device's security set
     */
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        this.securityPropertySet = deviceProtocolSecurityPropertySet;
    }

    /**
     * The SerialNumber of the concentrator
     */
    public String getSerialNumber() {
        return getProperties().getTypedProperty(MeterProtocol.SERIALNUMBER);
    }

    /**
     * Getter for the device ID.
     */
    public int getDeviceId() {
        return parseBigDecimalProperty(DEVICE_ID, DEFAULT_DEVICE_ID);
    }

    /**
     * The device timezone
     */
    public TimeZone getTimeZone() {
        TimeZoneInUse timeZoneInUse = getProperties().getTypedProperty(BasicDynamicPropertySupport.TIMEZONE);
        if (timeZoneInUse == null || timeZoneInUse.getTimeZone() == null) {
            return TimeZone.getTimeZone(BasicDynamicPropertySupport.DEFAULT_TIMEZONE);
        } else {
            return timeZoneInUse.getTimeZone();
        }
    }

    /**
     * The timeout interval of the communication session, expressed in milliseconds
     */
    public long getTimeout() {
        return getProperties().getTypedProperty(TIMEOUT, DEFAULT_TIMEOUT).getMilliSeconds();
    }

    /**
     * The number of retries
     */
    public int getRetries() {
        return parseBigDecimalProperty(BasicDynamicPropertySupport.RETRIES, BasicDynamicPropertySupport.DEFAULT_RETRIES);
    }

    /**
     * The delay before sending the requests, expressed in milliseconds
     */
    public long getForcedDelay() {
        return getProperties().getTypedProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY).getMilliSeconds();
    }

    public long getDelayAfterError() {
        return getProperties().getTypedProperty(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR).getMilliSeconds();
    }

    public byte[] getManufacturerKey() {
        if (this.manufacturerKey == null) {
            String hex = getProperties().getTypedProperty(SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.getKey());
            if (hex != null) {
                this.manufacturerKey = DLMSUtils.hexStringToByteArray(hex);
                if (this.manufacturerKey.length != 16) {
                    throw new ProtocolEncryptionException(new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER + " has an invalid length!"),
                            MessageSeeds.ENCRYPTION_ERROR);
                }
            } else {
                throw new ProtocolEncryptionException(new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER + " is missing!"),
                        MessageSeeds.ENCRYPTION_ERROR);
            }
        }
        return this.manufacturerKey;
    }

    public byte[] getCustomerKey() {
        if (this.customerKey == null) {
            String hex = getProperties().getTypedProperty(SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER.getKey());
            if (hex != null) {
                this.customerKey = DLMSUtils.hexStringToByteArray(hex);
                if (this.customerKey.length != 16) {
                    throw new ProtocolEncryptionException(new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER + " has an invalid length!"),
                            MessageSeeds.ENCRYPTION_ERROR);
                }
            } else {
                throw new ProtocolEncryptionException(new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER + " is missing!"),
                        MessageSeeds.ENCRYPTION_ERROR);
            }
        }
        return this.customerKey;
    }

    private int parseBigDecimalProperty(String key, BigDecimal defaultValue) {
        return getProperties().getTypedProperty(key, defaultValue).intValue();
    }

    /**
     * Add properties
     */
    public void addProperties(TypedProperties properties) {
        if (properties.getInheritedProperties() != null) {
            addProperties(properties.getInheritedProperties());
        }
        this.getProperties().setAllProperties(properties);
    }

    /**
     * Return all properties
     */
    public TypedProperties getProperties() {
        if (this.properties == null) {
            this.properties = TypedProperties.empty();
        }
        return this.properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(DEVICE_ID, DEVICE_ID)
                        .describedAs(DEVICE_ID)
                        .markRequired()
                        .setDefaultValue(DEFAULT_DEVICE_ID)
                        .finish(),
                this.propertySpecService
                        .referenceSpec(TimeZoneInUse.class)
                        .named(TIMEOUT, TIMEOUT)
                        .describedAs(TIMEOUT)
                        .finish());
    }

}