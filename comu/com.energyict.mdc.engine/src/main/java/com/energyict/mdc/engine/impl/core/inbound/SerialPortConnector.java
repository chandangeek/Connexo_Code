package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;

import com.energyict.protocols.mdc.channels.serial.SerialComponentService;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.protocols.mdc.channels.serial.SioSerialPort;
import com.energyict.protocols.mdc.channels.serial.modem.AtModemComponent;
import com.energyict.protocols.mdc.channels.serial.modem.SimpleAtModemProperties;

import com.energyict.mdc.protocol.api.exceptions.ModemException;
import com.energyict.mdc.protocol.api.exceptions.SerialPortException;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;

import java.io.IOException;

/**
 * Implementation of an {@link InboundComPortConnector}
 * for a {@link com.energyict.mdc.engine.model.ComPort}
 * of the type {@link ComPortType#SERIAL}
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/12
 * Time: 13:49
 */
public class SerialPortConnector implements InboundComPortConnector {

    private final ModemBasedInboundComPort comPort;
    private final SerialComponentService serialComponentService;

    /**
     * The number of consecutive rings already received.
     */
    private int currentRingCount;

    public SerialPortConnector(ModemBasedInboundComPort comPort, SerialComponentService serialComponentService) {
        super();
        this.comPort = comPort;
        this.serialComponentService = serialComponentService;
    }

    @Override
    public ComPortRelatedComChannel accept() {
        ComChannel comChannel = getNewComChannel();
        AtModemComponent modemComponent = initializeAtModemComponent(comChannel);

        waitForNumberOfRings(comChannel, modemComponent);
        acceptCallAndConnect(comChannel, modemComponent);

        return new ComPortRelatedComChannelImpl(comChannel);
    }


    private void waitForNumberOfRings(ComChannel comChannel, AtModemComponent modemComponent) {
        do {
            try {
                if (modemComponent.readAndVerify(comChannel, "RING", comPort.getAtCommandTimeout().getMilliSeconds())) {
                    currentRingCount++;
                    if (currentRingCount >= comPort.getRingCount()) {
                        return;
                    }
                } else {
                    currentRingCount = 0;
                }
            } catch (ModemException e) {
                currentRingCount = 0;
            }
        } while (currentRingCount < comPort.getRingCount());
    }

    private void acceptCallAndConnect(ComChannel comChannel, AtModemComponent modemComponent) {
        modemComponent.write(comChannel, "ATA");
        if (!modemComponent.readAndVerify(comChannel, "CONNECT", comPort.getConnectTimeout().getMilliSeconds())) {
            throw ModemException.couldNotEstablishConnection(comPort.getName(), comPort.getConnectTimeout().getMilliSeconds());
        }

        modemComponent.initializeAfterConnect(comChannel);
    }

    protected AtModemComponent initializeAtModemComponent(ComChannel comChannel) {
        SimpleAtModemProperties modemProperties = new SimpleAtModemProperties("",
                "",
                comPort.getConnectTimeout(),
                comPort.getDelayAfterConnect(),
                comPort.getDelayBeforeSend(),
                comPort.getAtCommandTimeout(),
                comPort.getAtCommandTry(),
                comPort.getModemInitStrings(),
                comPort.getAddressSelector(),
                null,   //Use the default line toggle delay
                comPort.getPostDialCommands()
        );

        AtModemComponent atModemComponent = this.serialComponentService.newAtModemComponent(modemProperties);
        atModemComponent.initializeModem(comPort.getName(), comChannel);
        return atModemComponent;
    }

    /**
     * Create the serial ComChannel based on the correct portConfiguration
     * This may throw a {@link CommunicationException} to indicate a failure
     * to create and/or initialize the ComChannel.
     *
     * @return The ComChannel
     */
    protected ComChannel getNewComChannel() {
        SerialPortConfiguration serialPortConfiguration = comPort.getSerialPortConfiguration();
        serialPortConfiguration.setComPortName(comPort.getName());
        return newSioSerialConnection(serialPortConfiguration);
    }

    /**
     * Creates a new {@link ComChannel}
     * that uses a {@link SioSerialPort} as the interface with the physical ComPort
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws CommunicationException if an exception occurred during the creation or initialization of the ComChannel
     */
    protected ComChannel newSioSerialConnection(final SerialPortConfiguration serialPortConfiguration) {
        try {
            SioSerialPort serialPort = this.serialComponentService.newSioSerialPort(serialPortConfiguration);
            serialPort.openAndInit();
            return serialComponentService.newSerialComChannel(serialPort);
        } catch (SerialPortException e) {
            throw new CommunicationException((IOException) e.getCause());
        } catch (UnsatisfiedLinkError e) {
            throw new CommunicationException(new NestedIOException(e));
        }
    }

    public int getCurrentRingCount() {
        return currentRingCount;
    }
}

// 5. Create the tests based on UDPPortConnectorTest
//    Do both software as hardware tests.
//    Software testing: implement your own SerInputStream, so you can code what comes available on the stream (E.g.: rings / rubbish / timeouts)
//    Hardware testing: use TRON modem - use AT commandos to ring your cell phone, so you know the phone number of modem. Then you can call back to modem, testing out the various situations.
