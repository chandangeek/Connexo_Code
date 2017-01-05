package com.energyict.mdc.channels.serial.rf;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WaveModuleLinkAdaptor;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.comchannels.WavenisStackUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ConnectionType for a Waveport connected to a serial comport
 * This initializes the Wavenis stack and creates a link to the RF device.
 * <p>
 * Copyrights EnergyICT
 * Date: 27/05/13
 * Time: 14:30
 * Author: khe
 */
@XmlRootElement
public class WavenisSerialConnectionType extends SioSerialConnectionType {

    private static final String RF_ADDRESS = "RFAddress";
    WavenisStack wavenisStack;

    @Override
    public ComChannel connect() throws ConnectionException {
        SerialPortConfiguration serialConfiguration = super.createSerialConfiguration(getComPortName(getAllProperties()), getAllProperties());
        serialConfiguration.setFlowControl(FlowControl.NONE);
        SerialComponentFactory serialComponentFactory = ManagerFactory.getCurrent().getSerialComponentFactory();
        ServerSerialPort serialPort = serialComponentFactory.newSioSerialPort(serialConfiguration);
        serialPort.openAndInit();

        try {
            wavenisStack = WavenisStackUtils.start(serialPort.getInputStream(), serialPort.getOutputStream());
            WaveModuleLinkAdaptor waveModuleLinkAdaptor = WavenisStackUtils.createLink(getRFAddress(), wavenisStack);
            ComChannel comChannel = new WavenisSerialComChannel(waveModuleLinkAdaptor.getInputStream(), waveModuleLinkAdaptor.getOutputStream(), serialPort);
            comChannel.addProperties(createTypeProperty(ComChannelType.WavenisSerialComChannel));
            return comChannel;
        } catch (IOException e) {
            wavenisStack.stop();
            throw new ConnectionException("Error while starting the Wavenis stack", e);
        }
    }

    /**
     * Stop the Wavenis stack. The serial port will be closed in the comchannel implementation
     */
    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        super.disconnect(comChannel);
        if (wavenisStack != null) {
            wavenisStack.stop();
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    private String getRFAddress() {
        return getAllProperties().getTypedProperty(RF_ADDRESS);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(rfAddressPropertySpec());
        return propertySpecs;
    }

    protected PropertySpec rfAddressPropertySpec() {
        return UPLPropertySpecFactory.string(RF_ADDRESS, true);
    }

    @Override
    protected PropertySpec baudRatePropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(SerialPortConfiguration.BAUDRATE_NAME, true, BaudrateValue.BAUDRATE_57600.getBaudrate(),
                BaudrateValue.BAUDRATE_9600.getBaudrate(),
                BaudrateValue.BAUDRATE_19200.getBaudrate(),
                BaudrateValue.BAUDRATE_38400.getBaudrate(),
                BaudrateValue.BAUDRATE_57600.getBaudrate(),
                BaudrateValue.BAUDRATE_115200.getBaudrate());
    }
}