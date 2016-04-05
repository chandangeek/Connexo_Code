package com.energyict.mdc.channels.dlms;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 10/10/13
 * Time: 14:48
 */
@XmlRootElement
public class LegacyTCPDlmsConnectionType extends DlmsConnectionType {

    public LegacyTCPDlmsConnectionType() {
        super(new OutboundTcpIpConnectionType());
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return getActualConnectionType().allowsSimultaneousConnections();
    }

    @Override
    public boolean supportsComWindow() {
        return getActualConnectionType().supportsComWindow();
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return getActualConnectionType().getSupportedComPortTypes();
    }

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        return getActualConnectionType().connect(comPort, properties);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case PROPERTY_NAME_ADDRESSING_MODE:
                return this.getAddressingModePropertySpec();
            case PROPERTY_NAME_CONNECTION:
                return this.getConnectionPropertySpec();
            case PROPERTY_NAME_SERVER_MAC_ADDRESS:
                return this.getServerMacAddress();
            case PROPERTY_NAME_SERVER_LOWER_MAC_ADDRESS:
                return this.getServerLowerMacAddress();
            case PROPERTY_NAME_SERVER_UPPER_MAC_ADDRESS:
                return this.getServerUpperMacAddress();
            default:
                return getActualConnectionType().getPropertySpec(name);
        }
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return getActualConnectionType().isRequiredProperty(name);
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return getActualConnectionType().getRequiredProperties();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = getActualConnectionType().getOptionalProperties();
        optionalProperties.add(getAddressingModePropertySpec());
        optionalProperties.add(getConnectionPropertySpec());
        optionalProperties.add(getServerMacAddress());
        optionalProperties.add(getServerLowerMacAddress());
        optionalProperties.add(getServerUpperMacAddress());
        return optionalProperties;
    }

    PropertySpec getServerUpperMacAddress() {
        return PropertySpecFactory.bigDecimalPropertySpec(PROPERTY_NAME_SERVER_UPPER_MAC_ADDRESS, new BigDecimal(1));
    }

    PropertySpec getServerLowerMacAddress() {
        return PropertySpecFactory.bigDecimalPropertySpec(PROPERTY_NAME_SERVER_LOWER_MAC_ADDRESS, new BigDecimal(0));
    }
}
