package com.energyict.protocols.impl.channels.serial;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.BaudrateValue;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.NrOfDataBits;
import com.energyict.mdc.io.NrOfStopBits;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.SerialConnectionPropertyNames;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 11:39
 */
public abstract class AbstractSerialConnectionType extends ConnectionTypeImpl {

    private final SerialComponentService serialComponentService;

    public AbstractSerialConnectionType(SerialComponentService serialComponentService) {
        super();
        this.serialComponentService = serialComponentService;
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
    public List<PropertySpec> getPropertySpecs() {
        return this.serialComponentService.getPropertySpecs();
    }

    protected Parities getParityValue() {
        return Parities.valueFor((String) getProperty(SerialPortConfiguration.PARITY_NAME, Parities.NONE.value()));
    }

    protected FlowControl getFlowControlValue() {
        return FlowControl.valueFor((String) getProperty(SerialPortConfiguration.FLOW_CONTROL_NAME, FlowControl.NONE.value()));
    }

    protected NrOfStopBits getNrOfStopBitsValue() {
        return NrOfStopBits.valueFor((BigDecimal) getProperty(SerialPortConfiguration.NR_OF_STOP_BITS_NAME, BigDecimal.ONE));
    }

    protected NrOfDataBits getNrOfDataBitsValue() {
        return NrOfDataBits.valueFor((BigDecimal) getProperty(SerialPortConfiguration.NR_OF_DATA_BITS_NAME, new BigDecimal(8)));
    }

    protected BaudrateValue getBaudRateValue() {
        return BaudrateValue.valueFor((BigDecimal)getProperty(SerialPortConfiguration.BAUDRATE_NAME, BaudrateValue.BAUDRATE_57600.value()));
    }

    protected BigDecimal getPortOpenTimeOutValue() {
        return this.nrOfMilliSecondsOfTimeDuration(SerialPortConfiguration.DEFAULT_SERIAL_PORT_OPEN_TIMEOUT);
    }

    protected BigDecimal getPortReadTimeOutValue() {
        return this.nrOfMilliSecondsOfTimeDuration(SerialPortConfiguration.DEFAULT_SERIAL_PORT_READ_TIMEOUT);
    }

    protected BigDecimal nrOfMilliSecondsOfTimeDuration(TimeDuration value) {
        if (value == null) {
            return new BigDecimal(0);
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