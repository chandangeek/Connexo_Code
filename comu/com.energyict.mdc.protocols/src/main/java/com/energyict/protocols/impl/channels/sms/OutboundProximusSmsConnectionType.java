package com.energyict.protocols.impl.channels.sms;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation of the {@link ConnectionType} interface
 * that supports outbound SMS communication using Proximus as carrier.
 *
 * @author sva
 * @since 19/06/13 - 9:12
 */
public class OutboundProximusSmsConnectionType extends ConnectionTypeImpl {

    public static final String PHONE_NUMBER_PROPERTY_NAME = "SMS_phoneNumber";
    public static final String CONNECTION_URL_PROPERTY_NAME = "API_connectionURL";
    public static final String SOURCE_PROPERTY_NAME = "API_source";
    public static final String AUTHENTICATION_PROPERTY_NAME = "API_authentication";
    public static final String SERVICE_CODE_PROPERTY_NAME = "API_serviceCode";

    private final PropertySpecService propertySpecService;

    @Inject
    public OutboundProximusSmsConnectionType(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec phoneNumberPropertySpec() {
        return this.propertySpecService.basicPropertySpec(PHONE_NUMBER_PROPERTY_NAME, true, new StringFactory());
    }

    protected String phoneNumberPropertyValue() {
        return (String) this.getProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    private PropertySpec connectionURLPropertySpec() {
        return this.propertySpecService.basicPropertySpec(CONNECTION_URL_PROPERTY_NAME, true, new StringFactory());
    }

    protected String connectionURLPropertyValue() {
        return (String) this.getProperty(CONNECTION_URL_PROPERTY_NAME);
    }

    private PropertySpec sourcePropertySpec() {
        return this.propertySpecService.basicPropertySpec(SOURCE_PROPERTY_NAME, true, new StringFactory());
    }

    protected String sourcePropertyValue() {
        return (String) this.getProperty(SOURCE_PROPERTY_NAME);
    }

    private PropertySpec authenticationPropertySpec() {
        return this.propertySpecService.basicPropertySpec(AUTHENTICATION_PROPERTY_NAME, true, new StringFactory());
    }

    protected String authenticationPropertyValue() {
        return (String) this.getProperty(AUTHENTICATION_PROPERTY_NAME);
    }

    private PropertySpec serviceCodePropertySpec() {
        return this.propertySpecService.basicPropertySpec(SERVICE_CODE_PROPERTY_NAME, true, new StringFactory());
    }

    protected String serviceCodePropertyValue() {
        return (String) this.getProperty(SERVICE_CODE_PROPERTY_NAME);
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return true;
    }

    @Override
    public boolean supportsComWindow() {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.TCP, ComPortType.UDP);
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        for (ConnectionProperty property : properties) {
            if (property.getValue() != null) {
                this.setProperty(property.getName(), property.getValue());
            }
        }
        return new ProximusSmsComChannel(
                this.phoneNumberPropertyValue(),
                this.connectionURLPropertyValue(),
                this.sourcePropertyValue(),
                this.authenticationPropertyValue(),
                this.serviceCodePropertyValue());
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for OutboundProximusSmsConnectionType
    }

    @Override
    @Obsolete
    public List<PropertySpec> getPropertySpecs () {
        return Arrays.asList(
                this.phoneNumberPropertySpec(),
                this.connectionURLPropertySpec(),
                this.sourcePropertySpec(),
                this.authenticationPropertySpec(),
                this.serviceCodePropertySpec());
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-06-26 15:15:49 +0200 (Mit, 26 Jun 2013) $";
    }

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }

}