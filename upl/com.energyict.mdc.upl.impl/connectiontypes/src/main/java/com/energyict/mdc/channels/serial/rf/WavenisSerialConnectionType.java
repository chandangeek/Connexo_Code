package com.energyict.mdc.channels.serial.rf;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.mdc.channel.serial.BaudrateValue;
import com.energyict.mdc.channel.serial.FlowControl;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.channel.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.comchannels.WavenisStackUtils;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

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

    public WavenisSerialConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public SerialPortComChannel connect() throws ConnectionException {
        SerialPortConfiguration serialConfiguration = super.createSerialConfiguration(getComPortName(getAllProperties()), getAllProperties());
        serialConfiguration.setFlowControl(FlowControl.NONE);
        ServerSerialPort serialPort = new SioSerialPort(serialConfiguration);
        serialPort.openAndInit();

        try {
            wavenisStack = WavenisStackUtils.start(serialPort.getInputStream(), serialPort.getOutputStream());
            WavenisStackUtils.WavenisInputOutStreams inOutStreams = WavenisStackUtils.createInputOutStreams(getRFAddress(), wavenisStack);
            return new WavenisSerialComChannel(inOutStreams.inputStream, inOutStreams.outputStream, serialPort);
        } catch (IOException e) {
            wavenisStack.stop();
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.WavenisStackSetupError, e);
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
        return UPLPropertySpecFactory.specBuilder(RF_ADDRESS, true, PropertyTranslationKeys.SERIAL_RF_ADDRESS, this.getPropertySpecService()::stringSpec).finish();
    }

    @Override
    protected PropertySpec baudRatePropertySpec() {
        return this.bigDecimalSpec(SerialPortConfiguration.BAUDRATE_NAME, PropertyTranslationKeys.SERIAL_BAUDRATE, true, BaudrateValue.BAUDRATE_57600.getBaudrate(),
                BaudrateValue.BAUDRATE_9600.getBaudrate(),
                BaudrateValue.BAUDRATE_19200.getBaudrate(),
                BaudrateValue.BAUDRATE_38400.getBaudrate(),
                BaudrateValue.BAUDRATE_57600.getBaudrate(),
                BaudrateValue.BAUDRATE_115200.getBaudrate());
    }
}