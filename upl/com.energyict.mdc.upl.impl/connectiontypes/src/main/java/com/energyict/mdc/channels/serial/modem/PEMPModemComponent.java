package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.channels.serial.SerialComChannelImpl;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;

/**
 * Modem component for PEMP communication, which is based on the {@link PaknetModemComponent}.
 *
 * @author sva
 * @since 18/03/13 - 16:26
 */
public class PEMPModemComponent extends PaknetModemComponent {

    private AbstractPEMPModemProperties modemProperties;

    public PEMPModemComponent(AbstractPEMPModemProperties properties) {
        super(properties);
        this.modemProperties = properties;
    }

    public void connect(String name, SerialComChannelImpl comChannel) {
        this.initializeModem(name, comChannel);

        if (!dialModem(comChannel)) {
            throw ModemException.connectTimeOutException(getComPortName(), modemProperties.getConnectTimeout().toMillis());
        }

        initializeAfterConnect(comChannel);
    }

    /**
     * Initialize the modem so it is ready for dialing/receival of a call.
     * During this initialization, several steps are performed:<br></br>
     * <p/>
     * <ul>
     * <li>If present, the current connection of the modem is hung up</li>
     * <li>The Paknet command state is initialized.</li>
     * <li>The PEMP command state is initialized.</li>
     * <li>All initialization parameters are send out to the modem</li>
     * </ul>
     *
     * @param name The port name
     * @param comChannel The ComChannel
     */
    public void initializeModem(String name, SerialComChannelImpl comChannel) {
        setComPortName(name);

        disconnectModemBeforeNewSession(comChannel);

        if (!initializeCommandState(comChannel)) {
            throw ModemException.failedToInitializeCommandStateString(getComPortName(), getLastResponseReceived());
        }

        if (!initializePEMPCommandState(comChannel)) {
            throw ModemException.failedToInitializeCommandStateString(getComPortName(), getLastResponseReceived());
        }

        if (!sendParameters(comChannel)) {
            throw ModemException.failedToWriteInitString(getComPortName(), getLastCommandSend(), getLastResponseReceived());
        }
    }

    /**
     * Initialize the PEMP modem to ensure it is in the command state.
     * This can be done by requesting the X.28 command prompt.
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if all commands succeeded, false otherwise
     */
    public boolean initializePEMPCommandState(ComChannel comChannel) {
        String[] addresses = modemProperties.getPEMPModemConfiguration().getAddresses();
        String promptResponse = modemProperties.getPEMPModemConfiguration().getPromptResponse();
        for (String address : addresses) {
            setLastCommandSend(address);
            write(comChannel, address);
            delay(200);
            toggleDTR((SerialPortComChannel) comChannel);
            delay(1000);
            if (!readAndVerify(comChannel, promptResponse, modemProperties.getConnectTimeout().toMillis())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Perform the actual dial to the modem of the Device.
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if a CONNECT has been received within the expected timeout, false otherwise
     */
    public boolean dialModem(ComChannel comChannel) {
        write(comChannel, modemProperties.getCommandPrefix() + modemProperties.getPhoneNumber());
        String expectedConnectionResponse = modemProperties.getPEMPModemConfiguration().getConnectionResponse();
        long timeOutInMillis = modemProperties.getConnectTimeout().toMillis();
        return readAndVerifyWithRetries(comChannel, expectedConnectionResponse, timeOutInMillis);
    }
}
