package com.energyict.mdc.channels.ip.datagrams;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.ip.OutboundIpConnectionType;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link com.energyict.mdc.io.ConnectionType} interface for UDP.
 * <p>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 11:20
 */
@XmlRootElement
public class OutboundUdpConnectionType extends OutboundIpConnectionType {

    public static final String BUFFER_SIZE_NAME = "udpdatagrambuffersize";

    public OutboundUdpConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        ComChannel comChannel = this.newUDPConnection(this.getBufferSizePropertyValue(), this.hostPropertyValue(), this.portNumberPropertyValue());
        comChannel.addProperties(createTypeProperty(ComChannelType.DatagramComChannel));
        return comChannel;
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
        return EnumSet.of(ComPortType.UDP);
    }

    private int getBufferSizePropertyValue() {
        BigDecimal value = (BigDecimal) this.getProperty(BUFFER_SIZE_NAME);
        return this.intProperty(value);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(bufferSizePropertySpec());
        return propertySpecs;
    }

    private PropertySpec bufferSizePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(BUFFER_SIZE_NAME, true, this.getPropertySpecService()::bigDecimalSpec).finish();
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-04-12 15:03:44 +0200 (vr, 12 apr 2013) $";
    }
}
