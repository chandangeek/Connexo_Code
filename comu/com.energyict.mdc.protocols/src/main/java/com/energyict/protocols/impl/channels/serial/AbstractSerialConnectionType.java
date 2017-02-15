/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.BaudrateValue;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.NrOfDataBits;
import com.energyict.mdc.io.NrOfStopBits;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.naming.SerialPortConfigurationPropertySpecNames;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.SerialConnectionPropertyNames;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.ConnectionTypeImpl;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractSerialConnectionType extends ConnectionTypeImpl {

    private final String serialComponentServiceId;
    private final SerialComponentService serialComponentService;
    private final Thesaurus thesaurus;

    public AbstractSerialConnectionType(String serialComponentServiceId, SerialComponentService serialComponentService, Thesaurus thesaurus) {
        super();
        this.serialComponentServiceId = serialComponentServiceId;
        this.serialComponentService = serialComponentService;
        this.thesaurus = thesaurus;
    }

    protected SerialComponentService getSerialComponentService() {
        return serialComponentService;
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return false;
    }

    @Override
    public boolean supportsComWindow() {
        return true;
    }

    protected TypedProperties toTypedProperties(List<ConnectionProperty> properties) {
        TypedProperties typedProperties = TypedProperties.empty();
        for (ConnectionProperty property : properties) {
            typedProperties.setProperty(property.getName(), property.getValue());
        }
        return typedProperties;
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.of(new SioSerialCustomPropertySet("CPS-" + this.serialComponentServiceId, this.thesaurus, this.serialComponentService));
    }

    protected Parities getParityValue() {
        return Parities.valueFor((String) getProperty(SerialPortConfigurationPropertySpecNames.PARITY, Parities.NONE.value()));
    }

    protected FlowControl getFlowControlValue() {
        return FlowControl.valueFor((String) getProperty(SerialPortConfigurationPropertySpecNames.FLOW_CONTROL, FlowControl.NONE.value()));
    }

    protected NrOfStopBits getNrOfStopBitsValue() {
        return NrOfStopBits.valueFor((BigDecimal) getProperty(SerialPortConfigurationPropertySpecNames.NR_OF_STOP_BITS, BigDecimal.ONE));
    }

    protected NrOfDataBits getNrOfDataBitsValue() {
        return NrOfDataBits.valueFor((BigDecimal) getProperty(SerialPortConfigurationPropertySpecNames.NR_OF_DATA_BITS, new BigDecimal(8)));
    }

    protected BaudrateValue getBaudRateValue() {
        return BaudrateValue.valueFor((BigDecimal)getProperty(SerialPortConfigurationPropertySpecNames.BAUDRATE, BaudrateValue.BAUDRATE_57600.value()));
    }

    protected BigDecimal getPortOpenTimeOutValue() {
        return this.nrOfMilliSecondsOfTimeDuration(SerialPortConfiguration.DEFAULT_SERIAL_PORT_OPEN_TIMEOUT);
    }

    protected BigDecimal getPortReadTimeOutValue() {
        return this.nrOfMilliSecondsOfTimeDuration(SerialPortConfiguration.DEFAULT_SERIAL_PORT_READ_TIMEOUT);
    }

    protected BigDecimal nrOfMilliSecondsOfTimeDuration(TimeDuration value) {
        if (value == null) {
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(value.getMilliSeconds());
        }
    }

    public String getComPortNameValue () {
        return (String) this.getProperty(SerialConnectionPropertyNames.COMPORT_NAME_PROPERTY_NAME.propertyName());
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.SERIAL);
    }

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }

}