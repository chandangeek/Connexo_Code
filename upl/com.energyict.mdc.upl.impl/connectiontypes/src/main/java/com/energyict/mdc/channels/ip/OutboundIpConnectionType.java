package com.energyict.mdc.channels.ip;

import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
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
    private final PropertySpecService propertySpecService;

    public OutboundIpConnectionType(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec hostPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(HOST_PROPERTY_NAME, true, this.propertySpecService::stringSpec).finish();
    }

    protected String hostPropertyValue() {
        return (String) this.getProperty(HOST_PROPERTY_NAME);
    }

    private PropertySpec portNumberPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PORT_PROPERTY_NAME, true, this.propertySpecService::bigDecimalSpec).finish();
    }

    private PropertySpec connectionTimeOutPropertySpec() {
        return this.durationSpec(CONNECTION_TIMEOUT_PROPERTY_NAME, DEFAULT_CONNECTION_TIMEOUT);
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
        return new ArrayList<>(Arrays.asList(
                this.hostPropertySpec(),
                this.portNumberPropertySpec(),
                this.connectionTimeOutPropertySpec()
        ));
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

    private PropertySpec durationSpec(String name, Duration defaultDuration) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, this.propertySpecService::durationSpec)
                .setDefaultValue(defaultDuration)
                .finish();
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }
}
