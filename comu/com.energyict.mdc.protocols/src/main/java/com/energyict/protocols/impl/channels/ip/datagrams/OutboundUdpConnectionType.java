package com.energyict.protocols.impl.channels.ip.datagrams;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionType;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link ConnectionType} interface for UDP.
 * <p>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 11:20
 */
public class OutboundUdpConnectionType extends OutboundIpConnectionType {

    public static final String BUFFER_SIZE_NAME = "udpdatagrambuffersize";

    private final SocketService socketService;

    @Inject
    public OutboundUdpConnectionType(PropertySpecService propertySpecService, SocketService socketService) {
        super(propertySpecService);
        this.socketService = socketService;
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

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        properties
            .stream()
            .filter(property -> property.getValue() != null)
            .forEach(property -> this.setProperty(property.getName(), property.getValue()));
        return this.newUDPConnection(this.socketService, this.getBufferSizePropertyValue(), this.hostPropertyValue(), this.portNumberPropertyValue());
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // no explicit disconnect for this OutboundUdpConnectionType
    }

    private int getBufferSizePropertyValue() {
        BigDecimal value = (BigDecimal) this.getProperty(BUFFER_SIZE_NAME);
        return this.intProperty(value);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(this.bufferSizePropertySpec());
        return propertySpecs;
    }

    private PropertySpec bufferSizePropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(BUFFER_SIZE_NAME, true, new BigDecimalFactory());
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-04-12 15:03:44 +0200 (vr, 12 apr 2013) $";
    }

}