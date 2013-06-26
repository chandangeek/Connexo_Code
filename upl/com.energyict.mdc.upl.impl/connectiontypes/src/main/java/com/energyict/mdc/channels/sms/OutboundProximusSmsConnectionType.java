package com.energyict.mdc.channels.sms;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation of the {@link ConnectionType} interface specific for outbound SMS communication using Proximus as carrier.
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

    private PropertySpec phoneNumberPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(PHONE_NUMBER_PROPERTY_NAME);
    }

    protected String phoneNumberPropertyValue() {
        return (String) this.getProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    private PropertySpec connectionURLPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(CONNECTION_URL_PROPERTY_NAME);
    }

    protected String connectionURLPropertyValue() {
        return (String) this.getProperty(CONNECTION_URL_PROPERTY_NAME);
    }

    private PropertySpec sourcePropertySpec() {
        return PropertySpecFactory.stringPropertySpec(SOURCE_PROPERTY_NAME);
    }

    protected String sourcePropertyValue() {
        return (String) this.getProperty(SOURCE_PROPERTY_NAME);
    }

    private PropertySpec authenticationPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(AUTHENTICATION_PROPERTY_NAME);
    }

    protected String authenticationPropertyValue() {
        return (String) this.getProperty(AUTHENTICATION_PROPERTY_NAME);
    }

    private PropertySpec serviceCodePropertySpec() {
        return PropertySpecFactory.stringPropertySpec(SERVICE_CODE_PROPERTY_NAME);
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
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        for (ConnectionTaskProperty property : properties) {
            if(property.getValue() != null){
                this.setProperty(property.getName(), property.getValue());
            }
        }
        ProximusSmsComChannel smsComChannel = new ProximusSmsComChannel(this.phoneNumberPropertyValue(),
                this.connectionURLPropertyValue(), this.sourcePropertyValue(), this.authenticationPropertyValue(), this.serviceCodePropertyValue());
        smsComChannel.setComPort(comPort);
        return smsComChannel;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case PHONE_NUMBER_PROPERTY_NAME:
                return this.phoneNumberPropertySpec();
            case CONNECTION_URL_PROPERTY_NAME:
                return this.connectionURLPropertySpec();
            case SOURCE_PROPERTY_NAME:
                return this.sourcePropertySpec();
            case AUTHENTICATION_PROPERTY_NAME:
                return this.authenticationPropertySpec();
            case SERVICE_CODE_PROPERTY_NAME:
                return this.serviceCodePropertySpec();
            default:
                return null;
        }
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return PHONE_NUMBER_PROPERTY_NAME.equals(name) ||
                CONNECTION_URL_PROPERTY_NAME.equals(name) ||
                SOURCE_PROPERTY_NAME.equals(name) ||
                AUTHENTICATION_PROPERTY_NAME.equals(name) ||
                SERVICE_CODE_PROPERTY_NAME.equals(name);
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Arrays.asList(
                phoneNumberPropertySpec(),
                connectionURLPropertySpec(),
                sourcePropertySpec(),
                authenticationPropertySpec(),
                serviceCodePropertySpec());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return new ArrayList<>();
    }
}
