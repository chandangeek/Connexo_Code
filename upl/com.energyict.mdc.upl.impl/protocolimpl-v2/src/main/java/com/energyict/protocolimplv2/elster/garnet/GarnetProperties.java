package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSUtils;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * @author sva
 * @since 17/06/2014 - 13:40
 */
public class GarnetProperties implements ConfigurationSupport {

    public static final String DEVICE_ID = "DeviceId";
    public static final BigDecimal DEFAULT_DEVICE_ID = new BigDecimal(0);
    public static final TimeDuration DEFAULT_TIMEOUT = new TimeDuration(10, TimeDuration.SECONDS);
    public static final TimeDuration DEFAULT_FORCED_DELAY = new TimeDuration(0, TimeDuration.MILLISECONDS);
    public static final TimeDuration DEFAULT_DELAY_AFTER_ERROR = new TimeDuration(100, TimeDuration.MILLISECONDS);

    private TypedProperties properties;
    private DeviceProtocolSecurityPropertySet securityPropertySet;

    private byte[] manufacturerKey;
    private byte[] customerKey;

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
        return getProperties().getTypedProperty(TIMEOUT, DEFAULT_TIMEOUT).getMilliSeconds();
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
        return getProperties().getTypedProperty(FORCED_DELAY, DEFAULT_FORCED_DELAY).getMilliSeconds();
    }

    public long getDelayAfterError() {
        return getProperties().getTypedProperty(DELAY_AFTER_ERROR, DEFAULT_DELAY_AFTER_ERROR).getMilliSeconds();
    }

    public byte[] getManufacturerKey() {
        if (this.manufacturerKey == null) {
            String hex = getProperties().getTypedProperty(SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER.toString());
            if (hex != null) {
                this.manufacturerKey = DLMSUtils.hexStringToByteArray(hex);
                if (this.manufacturerKey.length != 16) {
                    throw MdcManager.getComServerExceptionFactory().createCipheringException(
                            new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_MANUFACTURER + " has an invalid length!")
                    );
                }
            } else {
                throw MdcManager.getComServerExceptionFactory().createCipheringException(
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
                    throw MdcManager.getComServerExceptionFactory().createCipheringException(
                            new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER + " has an invalid length!")
                    );
                }
            } else {
                GarnetException exception = new GarnetException("Invalid security set used - the " + SecurityPropertySpecName.ENCRYPTION_KEY_CUSTOMER + " is missing!");
                throw MdcManager.getComServerExceptionFactory().createCipheringException(exception);
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
    public List<PropertySpec> getRequiredProperties() {
        return Arrays.asList(
                this.deviceIdPropertySpec()
        );
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.timeZonePropertySpec()
        );
    }

    private PropertySpec deviceIdPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DEVICE_ID, DEFAULT_DEVICE_ID);
    }

    private PropertySpec timeZonePropertySpec() {
        return PropertySpecFactory.timeZoneInUseReferencePropertySpec(TIMEZONE);
    }
}