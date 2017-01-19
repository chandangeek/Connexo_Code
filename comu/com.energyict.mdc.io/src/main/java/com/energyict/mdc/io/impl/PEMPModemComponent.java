package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.protocol.ComChannel;

/**
 * Modem component for PEMP communication, which is based on the {@link PaknetModemComponent}.
 *
 * @author sva
 * @since 18/03/13 - 16:26
 */
public class PEMPModemComponent extends PaknetModemComponent {

    private PEMPModemProperties modemProperties;

    public PEMPModemComponent(PEMPModemProperties properties) {
        super(properties);
        this.modemProperties = properties;
    }

    @Override
    public void connect(String name, SerialComChannel comChannel) {
        this.initializeModem(name, comChannel);

        if (!dialModem(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_CONNECT_TIMEOUT, getComPortName(), modemProperties.getConnectTimeout().getMilliSeconds());
        }

        initializeAfterConnect(comChannel);
    }

    @Override
    public void initializeModem(String name, SerialComChannel comChannel) {
        setComPortName(name);

        disconnectModemBeforeNewSession(comChannel);

        if (!initializeCommandState(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE, getComPortName(), getLastResponseReceived());
        }

        if (!initializePEMPCommandState(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE, getComPortName(), getLastResponseReceived());
        }

        if (!sendParameters(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_COULD_NOT_SEND_INIT_STRING, getComPortName(), getLastCommandSend(), getLastResponseReceived());
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
        String[] addresses = modemProperties.getConfiguration().getAddresses();
        String promptResponse = modemProperties.getConfiguration().getPromptResponse();
        for (String address : addresses) {
            setLastCommandSend(address);
            write(comChannel, address);
            delay(200);
            toggleDTR((SerialComChannel) comChannel);
            delay(1000);
            if (readAndVerify(comChannel, promptResponse, modemProperties.getConnectTimeout().getMilliSeconds())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Perform the actual dial to the modem of the Device.
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if a CONNECT has been received within the expected timeout, false otherwise
     */
    public boolean dialModem(ComChannel comChannel) {
        write(comChannel, modemProperties.getCommandPrefix() + modemProperties.getPhoneNumber());
        String expectedConnectionResponse = modemProperties.getConfiguration().getConnectionResponse();
        long timeOutInMillis = modemProperties.getConnectTimeout().getMilliSeconds();
        return readAndVerifyWithRetries(comChannel, expectedConnectionResponse, timeOutInMillis);
    }
}
