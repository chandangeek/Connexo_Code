package com.energyict.mdc.channels.dlms;


import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 10/7/13
 * Time: 1:32 PM
 */
@XmlRootElement
public class LegacyOpticalDlmsConnectionType extends DlmsConnectionType {

    public LegacyOpticalDlmsConnectionType() {
        super(new SioOpticalConnectionType());
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        return getActualConnectionType().connect();
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
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = getActualConnectionType().getPropertySpecs();
        propertySpecs.addAll(Arrays.asList(getAddressingModePropertySpec(),
                getConnectionPropertySpec(),
                getServerMacAddress(),
                getServerLowerMacAddress(),
                getServerUpperMacAddress()));
        return propertySpecs;
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    PropertySpec getConnectionPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(PROPERTY_NAME_CONNECTION, false, new BigDecimal(0));
    }
}

