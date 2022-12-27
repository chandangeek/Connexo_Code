/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.inbound;

import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@XmlRootElement
public class CoapConnectionType implements ConnectionType {

    public static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";
    private PropertySpecService propertySpecService;

    private TypedProperties properties = com.energyict.mdc.upl.TypedProperties.empty();

    public CoapConnectionType(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected TypedProperties getAllProperties() {
        return this.properties;
    }

    protected Object getProperty(String propertyName) {
        return this.getAllProperties().getProperty(propertyName);
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        throw new UnsupportedOperationException("Calling connect is not allowed on an CoapConnectionType");
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return true;
    }

    @Override
    public boolean supportsComWindow() {
        return true;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.COAP);
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-10-30";
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(this.ipAddressPropertySpec());
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties = properties;
    }

    private PropertySpec ipAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(IP_ADDRESS_PROPERTY_NAME, false, PropertyTranslationKeys.EIWEB_IP_ADDRESS, this.propertySpecService::stringSpec).finish();
    }
}