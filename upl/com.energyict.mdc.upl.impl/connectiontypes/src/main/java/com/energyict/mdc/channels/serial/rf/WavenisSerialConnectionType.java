package com.energyict.mdc.channels.serial.rf;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WaveModuleLinkAdaptor;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecBuilder;
import com.energyict.dynamicattributes.BigDecimalFactory;
import com.energyict.dynamicattributes.StringFactory;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.channels.serial.*;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.protocolimplv2.comchannels.WavenisStackUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ConnectionType for a Waveport connected to a serial comport
 * This initializes the Wavenis stack and creates a link to the RF device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 27/05/13
 * Time: 14:30
 * Author: khe
 */
public class WavenisSerialConnectionType extends SioSerialConnectionType {

    private static final String RF_ADDRESS = "RFAddress";
    WavenisStack wavenisStack;

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        SerialPortConfiguration serialConfiguration = super.createSerialConfiguration(comPort, properties);
        serialConfiguration.setFlowControl(FlowControl.NONE);
        SerialComponentFactory serialComponentFactory = ManagerFactory.getCurrent().getSerialComponentFactory();
        SioSerialPort serialPort = serialComponentFactory.newSioSerialPort(serialConfiguration);
        serialPort.openAndInit();

        try {
            wavenisStack = WavenisStackUtils.start(serialPort.getInputStream(), serialPort.getOutputStream());
            WaveModuleLinkAdaptor waveModuleLinkAdaptor = WavenisStackUtils.createLink(getRFAddress(), wavenisStack);
            return new WavenisSerialComChannel(waveModuleLinkAdaptor.getInputStream(), waveModuleLinkAdaptor.getOutputStream(), serialPort);
        } catch (IOException e) {
            wavenisStack.stop();
            throw new ConnectionException("Error while starting the Wavenis stack", e);
        }
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        PropertySpec superPropertySpec = super.getPropertySpec(name);
        if (superPropertySpec != null) {
            return superPropertySpec;
        } else if (RF_ADDRESS.equals(name)) {
            return this.rfAddressPropertySpec();
        } else {
            return null;
        }
    }

    /**
     * Stop the Wavenis stack. The serial port will be closed in the comchannel implementation
     */
    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        if (wavenisStack != null) {
            wavenisStack.stop();
        }
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return false;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    private String getRFAddress() {
        return getAllProperties().getStringProperty(RF_ADDRESS);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>(1);
        propertySpecs.add(this.rfAddressPropertySpec());
        return propertySpecs;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>(4);
        propertySpecs.add(this.baudRatePropertySpec());
        propertySpecs.add(this.nrOfStopBitsPropertySpec());
        propertySpecs.add(this.parityPropertySpec());
        propertySpecs.add(this.nrOfDataBitsPropertySpec());
        return propertySpecs;
    }

    protected PropertySpec<String> rfAddressPropertySpec() {
        return PropertySpecBuilder.
                forClass(String.class, new StringFactory()).
                name(RF_ADDRESS).
                setDefaultValue("").
                finish();
    }

    @Override
    protected PropertySpec<BigDecimal> baudRatePropertySpec() {
        return
                PropertySpecBuilder.
                        forClass(BigDecimal.class, new BigDecimalFactory()).
                        name(SerialPortConfiguration.BAUDRATE_NAME).
                        markExhaustive().
                        addValues(BaudrateValue.BAUDRATE_9600.getBaudrate()).
                        addValues(BaudrateValue.BAUDRATE_19200.getBaudrate()).
                        addValues(BaudrateValue.BAUDRATE_38400.getBaudrate()).
                        addValues(BaudrateValue.BAUDRATE_57600.getBaudrate()).
                        addValues(BaudrateValue.BAUDRATE_115200.getBaudrate()).
                        finish();
    }
}