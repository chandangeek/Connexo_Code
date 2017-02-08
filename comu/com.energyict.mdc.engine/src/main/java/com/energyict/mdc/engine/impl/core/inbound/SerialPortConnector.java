package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.modemproperties.postdialcommand.ModemComponent;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.interval.Temporals;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannelImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.upl.io.SerialComponentService;
import com.energyict.mdc.upl.io.SerialPortException;

import java.io.IOException;
import java.time.Clock;

/**
 * Implementation of an {@link InboundComPortConnector}
 * for a {@link com.energyict.mdc.engine.config.ComPort}
 * of the type {@link ComPortType#SERIAL}
 * <p>
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
    private final DeviceMessageService deviceMessageService;

    /**
     * The number of consecutive rings already received.
     */
    private int currentRingCount;
    private ComPortRelatedComChannel comChannel;

    public SerialPortConnector(ModemBasedInboundComPort comPort, SerialComponentService serialComponentService, HexService hexService, EventPublisher eventPublisher, Clock clock, DeviceMessageService deviceMessageService) {
        super();
        this.comPort = comPort;
        this.serialComponentService = serialComponentService;
        this.hexService = hexService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.deviceMessageService = deviceMessageService;
    }

    @Override
    public ComPortRelatedComChannel accept() {
        SerialPortComChannel comChannel = getNewComChannel();
        ModemComponent modemComponent = initializeAtModemComponent(comChannel);

        waitForNumberOfRings(comChannel, modemComponent);
        acceptCallAndConnect(comChannel, modemComponent);

        this.comChannel = new ComPortRelatedComChannelImpl(comChannel, this.comPort, this.clock, this.deviceMessageService, this.hexService, eventPublisher);
        return this.comChannel;
    }

    @Override
    public void close() throws Exception {
        if (this.comChannel != null) {
            this.comChannel.close();
        }
    }

    private void waitForNumberOfRings(SerialPortComChannel comChannel, ModemComponent modemComponent) {
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
            } catch (com.energyict.mdc.upl.io.ModemException e) {
                currentRingCount = 0;
            }
        } while (currentRingCount < comPort.getRingCount());
    }

    private void acceptCallAndConnect(SerialPortComChannel comChannel, ModemComponent modemComponent) {
        modemComponent.write(comChannel, "ATA");
        if (!modemComponent.readAndVerify(comChannel, "CONNECT", comPort.getConnectTimeout().getMilliSeconds())) {
            throw new ModemException(MessageSeeds.MODEM_COULD_NOT_ESTABLISH_CONNECTION, comPort.getName(), comPort.getConnectTimeout().getMilliSeconds());
        }

        modemComponent.initializeAfterConnect(comChannel);
    }

    private ModemComponent initializeAtModemComponent(SerialPortComChannel comChannel) {
        ModemComponent atModemComponent = this.serialComponentService.newModemComponent(
                "",
                "",
                Temporals.toTemporalAmount(comPort.getConnectTimeout()),
                Temporals.toTemporalAmount(comPort.getDelayAfterConnect()),
                Temporals.toTemporalAmount(comPort.getDelayBeforeSend()),
                Temporals.toTemporalAmount(comPort.getAtCommandTimeout()),
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
    SerialPortComChannel getNewComChannel() {
        SerialPortConfiguration serialPortConfiguration = comPort.getSerialPortConfiguration();
        serialPortConfiguration.setComPortName(comPort.getName());
        return newSioSerialConnection(serialPortConfiguration);
    }

    /**
     * Creates a new {@link ComChannel}
     * that uses a {@link ServerSerialPort} as the interface with the physical ComPort.
     *
     * @param serialPortConfiguration the configuration of the serialPort
     * @return the ComChannel
     * @throws CommunicationException if an exception occurred during the creation or initialization of the ComChannel
     */
    private SerialPortComChannel newSioSerialConnection(final SerialPortConfiguration serialPortConfiguration) {
        try {
            ServerSerialPort serialPort = this.serialComponentService.newSerialPort(serialPortConfiguration);
            serialPort.openAndInit();
            return serialComponentService.newSerialComChannel(serialPort, ComChannelType.SerialComChannel);
        } catch (SerialPortException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, (IOException) e.getCause());
        } catch (UnsatisfiedLinkError e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, new NestedIOException(e));
        }
    }

    int getCurrentRingCount() {
        return currentRingCount;
    }

}

// 5. Create the tests based on UDPPortConnectorTest
//    Do both software as hardware tests.
//    Software testing: implement your own SerInputStream, so you can code what comes available on the stream (E.g.: rings / rubbish / timeouts)
//    Hardware testing: use TRON modem - use AT commandos to ring your cell phone, so you know the phone number of modem. Then you can call back to modem, testing out the various situations.
