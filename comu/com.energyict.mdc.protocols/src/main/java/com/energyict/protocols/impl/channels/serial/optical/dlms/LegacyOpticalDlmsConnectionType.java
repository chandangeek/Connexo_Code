package com.energyict.protocols.impl.channels.serial.optical.dlms;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.impl.ConnectionTypeServiceImpl;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 10/7/13
 * Time: 1:32 PM
 */
public class LegacyOpticalDlmsConnectionType extends DlmsConnectionType {

    @Inject
    public LegacyOpticalDlmsConnectionType(@Named(ConnectionTypeServiceImpl.SERIAL_PLAIN_GUICE_INJECTION_NAME) SerialComponentService serialComponentService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, new SioOpticalConnectionType(serialComponentService, thesaurus));
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
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return getActualConnectionType().connect(properties);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        this.getActualConnectionType().disconnect(comChannel);
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(this.getActualConnectionType().getPropertySpecs());
        propertySpecs.add(this.getAddressingModePropertySpec());
        propertySpecs.add(this.getConnectionPropertySpec());
        propertySpecs.add(this.getServerMacAddress());
        propertySpecs.add(this.getServerLowerMacAddress());
        propertySpecs.add(this.getServerUpperMacAddress());
        return propertySpecs;
    }

    @Override
    PropertySpec getServerLowerMacAddress () {
        return this.getServerLowerMacAddress(false);
    }

    @Override
    PropertySpec getServerUpperMacAddress () {
        return this.getServerUpperMacAddress(false);
    }

    @Override
    PropertySpec getServerMacAddress () {
        return this.getServerMacAddress(false);
    }

    @Override
    PropertySpec getAddressingModePropertySpec () {
        return this.getAddressingModePropertySpec(false);
    }

    PropertySpec getConnectionPropertySpec() {
        return this.getPropertySpecService().bigDecimalPropertySpec(PROPERTY_NAME_CONNECTION, false, BigDecimal.ZERO);
    }

}