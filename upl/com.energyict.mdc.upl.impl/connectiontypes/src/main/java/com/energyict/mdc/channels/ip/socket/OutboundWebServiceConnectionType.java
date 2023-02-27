package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.channel.ip.socket.WebServiceComChannel;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.exceptions.ConnectionException;


public class OutboundWebServiceConnectionType extends OutboundTcpIpConnectionType {

    public OutboundWebServiceConnectionType() {
        super();
    }

    public OutboundWebServiceConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getVersion() {
        return "$Date: 2023-02-24 $";
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        ComChannel comChannel = new WebServiceComChannel();
        TypedProperties typedProperties = getAllProperties();
        comChannel.addProperties(typedProperties);
        return comChannel;
    }
}
