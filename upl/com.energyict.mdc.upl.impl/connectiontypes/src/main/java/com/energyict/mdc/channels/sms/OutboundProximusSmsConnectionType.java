package com.energyict.mdc.channels.sms;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation of the {@link com.energyict.mdc.io.ConnectionType} interface
 * specific for outbound SMS communication using Proximus as carrier.
 *
 * @author sva
 * @since 19/06/13 - 9:12
 */
@XmlRootElement
public class OutboundProximusSmsConnectionType extends ConnectionTypeImpl {

    public static final String PHONE_NUMBER_PROPERTY_NAME = "SMS_phoneNumber";
    public static final String CONNECTION_URL_PROPERTY_NAME = "API_connectionURL";
    public static final String SOURCE_PROPERTY_NAME = "API_source";
    public static final String AUTHENTICATION_PROPERTY_NAME = "API_authentication";
    public static final String SERVICE_CODE_PROPERTY_NAME = "API_serviceCode";
    private final PropertySpecService propertySpecService;

    public OutboundProximusSmsConnectionType(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    private PropertySpec phoneNumberPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PHONE_NUMBER_PROPERTY_NAME, true,this.propertySpecService::stringSpec).finish();
    }

    protected String phoneNumberPropertyValue() {
        return (String) this.getProperty(PHONE_NUMBER_PROPERTY_NAME);
    }

    private PropertySpec connectionURLPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(CONNECTION_URL_PROPERTY_NAME, true, this.propertySpecService::stringSpec).finish();
    }

    protected String connectionURLPropertyValue() {
        return (String) this.getProperty(CONNECTION_URL_PROPERTY_NAME);
    }

    private PropertySpec sourcePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(SOURCE_PROPERTY_NAME, true, this.propertySpecService::stringSpec).finish();
    }

    protected String sourcePropertyValue() {
        return (String) this.getProperty(SOURCE_PROPERTY_NAME);
    }

    private PropertySpec authenticationPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(AUTHENTICATION_PROPERTY_NAME, true, this.propertySpecService::stringSpec).finish();
    }

    protected String authenticationPropertyValue() {
        return (String) this.getProperty(AUTHENTICATION_PROPERTY_NAME);
    }

    private PropertySpec serviceCodePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(SERVICE_CODE_PROPERTY_NAME, true, this.propertySpecService::stringSpec).finish();
    }

    protected String serviceCodePropertyValue() {
        return (String) this.getProperty(SERVICE_CODE_PROPERTY_NAME);
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        ProximusSmsComChannel smsComChannel = new ProximusSmsComChannel(this.phoneNumberPropertyValue(),
                this.connectionURLPropertyValue(), this.sourcePropertyValue(), this.authenticationPropertyValue(), this.serviceCodePropertyValue());
        smsComChannel.addProperties(createTypeProperty(ComChannelType.ProximusSmsComChannel));
        return smsComChannel;
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
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(this.phoneNumberPropertySpec());
        propertySpecs.add(this.connectionURLPropertySpec());
        propertySpecs.add(this.authenticationPropertySpec());
        propertySpecs.add(this.serviceCodePropertySpec());
        propertySpecs.add(this.sourcePropertySpec());
        return propertySpecs;
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }
}
