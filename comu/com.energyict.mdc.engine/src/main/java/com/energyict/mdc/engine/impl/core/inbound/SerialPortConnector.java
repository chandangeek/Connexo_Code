package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannelImpl;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.SerialPortException;
import com.energyict.mdc.io.ServerSerialPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.services.HexService;

import java.io.IOException;
import java.time.Clock;

/**
 * Implementation of an {@link InboundComPortConnector}
 * for a {@link com.energyict.mdc.engine.config.ComPort}
 * of the type {@link ComPortType#SERIAL}
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/12
 * Time: 13:49
 */
public class SerialPortConnector implements InboundComPortConnector {

    private final ModemBasedInboundComPort comPort;
    private final SerialComponentService serialComponentService;
    private final HexService hexService;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    /**
     * The number of consecutive rings already received.
     */
    private int currentRingCount;

    public SerialPortConnector(ModemBasedInboundComPort comPort, SerialComponentService serialComponentService, HexService hexService, EventPublisher eventPublisher, Clock clock) {
        super();
        this.comPort = comPort;
        this.serialComponentService = serialComponentService;
        this.hexService = hexService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    public ComPortRelatedComChannel accept() {
        SerialComChannel comChannel = getNewComChannel();
        ModemComponent modemComponent = initializeAtModemComponent(comChannel);

        waitForNumberOfRings(comChannel, modemComponent);
        acceptCallAndConnect(comChannel, modemComponent);

        return new ComPortRelatedComChannelImpl(comChannel, this.comPort, this.clock, this.hexService, eventPublisher);
    }


    private void waitForNumberOfRings(SerialComChannel comChannel, ModemComponent modemComponent) {
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

    private void acceptCallAndConnect(SerialComChannel comChannel, ModemComponent modemComponent) {
        modemComponent.write(comChannel, "ATA");
        if (!modemComponent.readAndVerify(comChannel, "CONNECT", comPort.getConnectTimeout().getMilliSeconds())) {
            throw new ModemException(MessageSeeds.MODEM_COULD_NOT_ESTABLISH_CONNECTION, comPort.getName(), comPort.getConnectTimeout().getMilliSeconds());
        }

        modemComponent.initializeAfterConnect(comChannel);
    }

    private ModemComponent initializeAtModemComponent(SerialComChannel comChannel) {
        ModemComponent atModemComponent = this.serialComponentService.newModemComponent(
                "",
                "",
                comPort.getConnectTimeout(),
                comPort.getDelayAfterConnect(),
                comPort.getDelayBeforeSend(),
                comPort.getAtCommandTimeout(),
                comPort.getAtCommandTry(),
                comPort.getModemInitStrings(),
                comPort.getGlobalModemInitStrings(),
                comPort.getAddressSelector(),
                null,//Use the default line toggle delay
                comPort.getPostDialCommands());
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
    protected SerialComChannel getNewComChannel() {
        SerialPortConfiguration serialPortConfiguration = comPort.getSerialPortConfiguration();
        serialPortConfiguration.setComPortName(comPort.getName());
        return newSioSerialConnection(serialPortConfiguration);
    }

    /**
     * Creates a new {@link ComChannel} that uses a {@link ServerSerialPort}
     * as the interface with the physical ComPort.
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws CommunicationException if an exception occurred during the creation or initialization of the ComChannel
     */
    protected SerialComChannel newSioSerialConnection(final SerialPortConfiguration serialPortConfiguration) {
        try {
            ServerSerialPort serialPort = this.serialComponentService.newSerialPort(serialPortConfiguration);
            serialPort.openAndInit();
            return serialComponentService.newSerialComChannel(serialPort);
        } catch (SerialPortException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, (IOException) e.getCause());
        } catch (UnsatisfiedLinkError e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, new NestedIOException(e));
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
