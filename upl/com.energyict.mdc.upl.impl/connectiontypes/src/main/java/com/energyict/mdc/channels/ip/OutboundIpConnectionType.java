package com.energyict.mdc.channels.ip;

import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.protocols.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlTransient;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Groups common behavior for outbound IP related connectionTypes.
 * <p>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 11:28
 */
public abstract class OutboundIpConnectionType extends ConnectionTypeImpl {

    public static final String HOST_PROPERTY_NAME = "host";
    public static final String PORT_PROPERTY_NAME = "portNumber";
    public static final String CONNECTION_TIMEOUT_PROPERTY_NAME = "connectionTimeout";
    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(10);
    private PropertySpecService propertySpecService;

    public OutboundIpConnectionType() {
        super();
    }

    public OutboundIpConnectionType(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec hostPropertySpec;

    private PropertySpec getHostPropertySpec() {
        if (hostPropertySpec == null && propertySpecService != null)
            hostPropertySpec = UPLPropertySpecFactory.specBuilder(HOST_PROPERTY_NAME, false, PropertyTranslationKeys.OUTBOUND_IP_HOST, this.propertySpecService::stringSpec).finish();
        return hostPropertySpec;
    }

    protected String hostPropertyValue() {
        return (String) this.getProperty(HOST_PROPERTY_NAME);
    }

    private PropertySpec portNumberPropertySpec;

    private PropertySpec getPortNumberPropertySpec() {
        if (portNumberPropertySpec == null && propertySpecService != null)
            portNumberPropertySpec = UPLPropertySpecFactory.specBuilder(PORT_PROPERTY_NAME, false, PropertyTranslationKeys.OUTBOUND_IP_PORT_NUMBER, this.propertySpecService::bigDecimalSpec).finish();
        return portNumberPropertySpec;
    }

    private PropertySpec connectionTimeOutPropertySpec;

    private PropertySpec getConnectionTimeOutPropertySpec() {
        if (connectionTimeOutPropertySpec == null && propertySpecService != null)
            connectionTimeOutPropertySpec = this.durationSpec(CONNECTION_TIMEOUT_PROPERTY_NAME, PropertyTranslationKeys.OUTBOUND_IP_CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
        return connectionTimeOutPropertySpec;
    }

    protected int portNumberPropertyValue() {
        BigDecimal value = (BigDecimal) this.getProperty(PORT_PROPERTY_NAME);
        return this.intProperty(value);
    }

    protected int connectionTimeOutPropertyValue() {
        Duration value = (Duration) this.getProperty(CONNECTION_TIMEOUT_PROPERTY_NAME);
        return value != null ? this.intProperty(value) : (int) DEFAULT_CONNECTION_TIMEOUT.toMillis();
    }

    protected int intProperty(BigDecimal value) {
        if (value == null) {
            return 0;
        } else {
            return value.intValue();
        }
    }

    protected int intProperty(Duration value) {
        if (value == null) {
            return 0;
        } else {
            return (int) value.toMillis();
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.getHostPropertySpec(),
                this.getPortNumberPropertySpec(),
                this.getConnectionTimeOutPropertySpec());
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

    private PropertySpec durationSpec(String name, TranslationKey translationKey, Duration defaultDuration) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::durationSpec)
                .setDefaultValue(defaultDuration)
                .finish();
    }

    @JsonIgnore
    @XmlTransient
    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }
}
