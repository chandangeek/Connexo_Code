package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.mdc.upl.properties.*;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.dlms.DLMSUtils;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.DELAY_AFTER_ERROR;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.RETRIES;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEOUT;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;

/**
 * @author sva
 * @since 17/06/2014 - 13:40
 */
public class GarnetProperties implements HasDynamicProperties {

    public static final String DEVICE_ID = "DeviceId";
    public static final BigDecimal DEFAULT_DEVICE_ID = new BigDecimal(0);
    public static final TemporalAmount DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    public static final TemporalAmount DEFAULT_FORCED_DELAY = Duration.ofMillis(0);
    public static final TemporalAmount DEFAULT_DELAY_AFTER_ERROR = Duration.ofMillis(100);

    private TypedProperties properties;
    private DeviceProtocolSecurityPropertySet securityPropertySet;

    private byte[] manufacturerKey;
    private byte[] customerKey;

    private final PropertySpecService propertySpecService;

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
        return getProperties().getTypedProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName());
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
        TimeZoneInUse timeZoneInUse = getProperties().getTypedProperty(TIMEZONE);
        if (timeZoneInUse == null || timeZoneInUse.getTimeZone() == null) {
            return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
        } else {
            return timeZoneInUse.getTimeZone();
        }
    }

    /**
     * The timeout interval of the communication session, expressed in milliseconds
     */
    public long getTimeout() {
        return Temporals.toMilliSeconds(getProperties().getTypedProperty(TIMEOUT, DEFAULT_TIMEOUT));
    }

    /**
     * The number of retries
     */
    public int getRetries() {
        return parseBigDecimalProperty(RETRIES, DEFAULT_RETRIES);
    }

    /**
     * The delay before sending the requests, expressed in milliseconds
     */
    public long getForcedDelay() {
        return Temporals.toMilliSeconds(getProperties().getTypedProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY));
    }

    public long getDelayAfterError() {
        return Temporals.toMilliSeconds(getProperties().getTypedProperty(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR));
    }

    public byte[] getManufacturerKey() {
        if (this.manufacturerKey == null) {
            String hex = getProperties().getTypedProperty(SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.toString());
            if (hex != null) {
                this.manufacturerKey = DLMSUtils.hexStringToByteArray(hex);
                if (this.manufacturerKey.length != 16) {
                    throw ConnectionCommunicationException.cipheringException(
                            new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER + " has an invalid length!")
                    );
                }
            } else {
                throw  ConnectionCommunicationException.cipheringException(
                        new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER + " is missing!")
                );
            }
        }
        return this.manufacturerKey;
    }

    public byte[] getCustomerKey() {
        if (this.customerKey == null) {
            String hex = getProperties().getTypedProperty(SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER.toString());
            if (hex != null) {
                this.customerKey = DLMSUtils.hexStringToByteArray(hex);
                if (this.customerKey.length != 16) {
                    throw  ConnectionCommunicationException.cipheringException(
                            new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER + " has an invalid length!")
                    );
                }
            } else {
                GarnetException exception = new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER + " is missing!");
                throw  ConnectionCommunicationException.cipheringException(exception);
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
            this.properties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        }
        return this.properties;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(this.deviceIdPropertySpec(), this.timeZonePropertySpec());
    }

    private PropertySpec deviceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DEVICE_ID, true, this.propertySpecService::bigDecimalSpec)
                .setDefaultValue(DEFAULT_DEVICE_ID)
                .finish();
    }

    private PropertySpec timeZonePropertySpec() {
        return this.propertySpecService
                .timeZoneSpec()
                .named(TIMEZONE, TIMEZONE).describedAs("Description for " + TIMEOUT)
                .finish();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.addProperties(com.energyict.protocolimpl.properties.TypedProperties.copyOf(properties));
    }

}