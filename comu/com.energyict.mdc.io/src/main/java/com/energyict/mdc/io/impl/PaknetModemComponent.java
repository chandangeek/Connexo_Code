package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.ModemProperties;
import com.energyict.mdc.io.SignalController;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.io.ModemTimeoutException;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.io.SerialComChannel;

import java.util.List;

/**
* @author sva
* @since 18/03/13 - 16:26
*/
public class PaknetModemComponent implements ModemComponent {

    public static final String COMMAND_PROMPT_REQUEST = "\r";  // The carriage return character
    public static final String PARAMETER_SET_REQUEST = "SET";  // Parameter set
    public static final String COMMAND_PROMPT_OK = "*";// Carriage return - line feed - * - carriage return - line feed
    public static final String CONNECTION_PROMPT_OK = "COM";

    private String comPortName = "UnKnown";
    private String lastCommandSend = "";
    private String lastResponseReceived = "";

    private ModemProperties modemProperties;

    public PaknetModemComponent(ModemProperties properties) {
        super();
        this.modemProperties = properties;
    }

    @Override
    public void connect(String name, SerialComChannel comChannel) {
        this.initializeModem(name, comChannel);

        if (!dialModem(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_CONNECT_TIMEOUT, this.comPortName, modemProperties.getConnectTimeout().getMilliSeconds());
        }

        initializeAfterConnect(comChannel);
    }

    @Override
    public void initializeModem(String name, SerialComChannel comChannel) {
        setComPortName(name);

        disconnectModemBeforeNewSession(comChannel);

        if (!initializeCommandState(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE, this.comPortName, lastResponseReceived);
        }

        if (!sendParameters(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_COULD_NOT_SEND_INIT_STRING, this.comPortName, lastCommandSend, lastResponseReceived);
        }
    }

    /**
     * Terminate all current sessions on the modem
     *
     * @param comChannel the serialComChannel
     */
    public void disconnectModemBeforeNewSession(SerialComChannel comChannel) {
        disconnect(comChannel);
        flushInputStream(comChannel);
    }

    /**
     * Terminate all current sessions on the modem
     *
     * @param comChannel the serialComChannel
     */
    public void disconnect(SerialComChannel comChannel) {
        comChannel.startWriting();
        toggleDTR(comChannel, modemProperties.getLineToggleDelay().getMilliSeconds());
    }

    /**
     * Initialize the paknet modem to ensure it is in the command state.
     * This can be done by requesting the X.28 command prompt.
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if all commands succeeded, false otherwise
     */
    public boolean initializeCommandState(ComChannel comChannel) {
        setLastCommandSend(PaknetModemComponent.COMMAND_PROMPT_REQUEST);
        write(comChannel, "");
        return readAndVerifyWithRetries(comChannel, PaknetModemComponent.COMMAND_PROMPT_OK, modemProperties.getCommandTimeOut().getMilliSeconds());
    }

    /**
     * Set the Radio-Pad parameters on the modem
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if all commands succeeded, false otherwise
     */
    public boolean sendParameters(ComChannel comChannel) {
        List<String> modemInitStrings = modemProperties.getModemInitStrings();
        String modemInitString = (modemInitStrings == null || modemInitStrings.isEmpty()) ? null : modemInitStrings.get(0);
        if (modemInitString != null && !modemInitString.isEmpty()) {
            String modemParameters = PARAMETER_SET_REQUEST + modemInitString;

            write(comChannel, modemParameters);
            return readAndVerifyWithRetries(comChannel, PaknetModemComponent.COMMAND_PROMPT_OK, modemProperties.getCommandTimeOut().getMilliSeconds());
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
        return readAndVerify(comChannel, PaknetModemComponent.CONNECTION_PROMPT_OK, modemProperties.getConnectTimeout().getMilliSeconds());
    }

    /**
     * Initialization method to be performed right after the modem of the device has established a connection.
     *
     * @param comChannel to initialize
     */
    public void initializeAfterConnect(ComChannel comChannel) {
        PaknetModemComponent.delay(modemProperties.getDelayAfterConnect().getMilliSeconds());
        flushInputStream(comChannel);
    }

    /**
     * Toggle the DTR signal line, to ensure the current session is terminated
     *
     * @param comChannel the serialComChannel
     * @param delayInMilliSeconds the delay to wait after each DTR signal switch
     */
    protected void toggleDTR(SerialComChannel comChannel, long delayInMilliSeconds) {
        SignalController signalController = comChannel.getSerialPort().getSerialPortSignalController();
        signalController.setDTR(false);
        delay(delayInMilliSeconds);
        signalController.setDTR(true);
        delay(delayInMilliSeconds);
    }

    /**
     * Toggle the DTR signal line, to ensure the current session is terminated
     *
     * @param comChannel the serialComChannel
     */
    protected void toggleDTR(SerialComChannel comChannel) {
        SignalController signalController = comChannel.getSerialPort().getSerialPortSignalController();
        signalController.setDTR(false);
        signalController.setDTR(true);
    }

    /**
     * Write the given data to the comChannel
     *
     * @param comChannel  the comChannel to write to
     * @param dataToWrite the data to write
     */
    public void write(ComChannel comChannel, String dataToWrite) {
        delayBeforeSend();
        comChannel.startWriting();
        setLastCommandSend(dataToWrite);
        comChannel.write((dataToWrite + COMMAND_PROMPT_REQUEST).getBytes());
    }

    /**
     * Read bytes from the comChannel and verifies against the given expected value.
     * If the value doesn't match, then we retry until the maximum number of tries is reached.
     *
     * @param comChannel     the comChannel to read
     * @param expectedAnswer the expected response
     * @param timeOutInMillis the timeOut in milliseconds to wait before throwing a TimeOutException
     *
     */
    protected boolean readAndVerifyWithRetries(ComChannel comChannel, String expectedAnswer, long timeOutInMillis) {
        int currentTry = 0;
        while (currentTry++ < modemProperties.getCommandTry().intValue()) {
            try {
                if (readAndVerify(comChannel, expectedAnswer, timeOutInMillis)) {
                    return true;
                }
            } catch (ModemTimeoutException e) {
                // Ignore timeouts
            }
        }
        return false;
    }

    /**
     * Reads bytes from the comChannel and verifies against the given expected value.
     * No retries are performed, just once.
     *
     * @param comChannel      the ComChannel to read
     * @param expectedAnswer  the expected response
     * @param timeOutInMillis the timeOut in milliseconds to wait before throwing a TimeOutException
     * @return true if the answer matches the expected answer, false otherwise
     */
    public boolean readAndVerify(ComChannel comChannel, String expectedAnswer, long timeOutInMillis) {
        comChannel.startReading();
        StringBuilder responseBuilder = new StringBuilder();
        long max = System.currentTimeMillis() + timeOutInMillis;
        try {
            do {
                int available = comChannel.available();
                if (available > 0) {
                    responseBuilder.append((char) comChannel.read());
                } else {
                    PaknetModemComponent.delay(25);
                }
                if (System.currentTimeMillis() > max) {
                    if (responseBuilder.length() == 0) { // indication that we did not read anything
                        throw new ModemTimeoutException(MessageSeeds.MODEM_READ_TIMEOUT, this.comPortName, timeOutInMillis, lastCommandSend);
                    } else {
                        return false;
                    }
                }

            } while (!validateResponse(responseBuilder.toString(), expectedAnswer));
        } finally {
            setLastResponseReceived(responseBuilder.toString());
        }
        return true;
    }

    /**
     * The validateResponse will check if the answer we received from the meter contains:
     * <ul>
     * <li>Any error messages (see {@link AtModemComponent.ExceptionAnswers}</li>
     * <li>The expected Answer</li>
     * <li>A copy of our request</li>
     * <li>Any other value ...</li>
     * </ul>
     * An exception is throw if the response contains any of the error messages.
     *
     * @param response       the response from the modem
     * @param expectedAnswer the answer we expect from the modem
     * @return true if the response matches the expected, false otherwise
     */
    protected boolean validateResponse(String response, String expectedAnswer) {
        for (PaknetModemComponent.ExceptionAnswers exceptionAnswer : PaknetModemComponent.ExceptionAnswers.values()) {
            if (response.contains(exceptionAnswer.getError())) {
                throw new ModemException(exceptionAnswer.getMessageSeed(), this.comPortName, this.lastCommandSend);
            }
        }
        return response.contains(expectedAnswer);
    }

    public String getComPortName() {
        return comPortName;
    }

    public void setComPortName(String comPortName) {
        this.comPortName = comPortName;
    }

    public String getLastCommandSend() {
        return lastCommandSend;
    }

    public void setLastCommandSend(String lastCommandSend) {
        this.lastCommandSend = lastCommandSend;
    }

    public String getLastResponseReceived() {
        return lastResponseReceived;
    }

    public void setLastResponseReceived(String lastResponseReceived) {
        this.lastResponseReceived = lastResponseReceived;
    }

    /**
     * Flush all bytes from the inputStream. The data which is currently on the
     * inputStream <b>WILL BE LOST!</b>
     */
    protected void flushInputStream(ComChannel comChannel) {
        comChannel.startReading();
        while (comChannel.available() > 0) {
            comChannel.read();
        }
    }

    /**
     * Some devices/modems can not keep up with the current <i>HighSpeed</i> communications,
     * so we can wait a little while until the catch up.
     */
    protected void delayBeforeSend() {
        PaknetModemComponent.delay(modemProperties.getDelayBeforeSend().getMilliSeconds());
    }

    public static void delay(long milliSecondsToSleep) {
        try {
            Thread.sleep(milliSecondsToSleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Exception values which can occur during communication with the modem.
     */
    public enum ExceptionAnswers {
        ;   // ToDO: No exception answers registered yet!

        private final String error;
        private final MessageSeed messageSeed;

        ExceptionAnswers(String error, MessageSeed messageSeed) {
            this.error = error;
            this.messageSeed = messageSeed;
        }

        public String getError() {
            return this.error;
        }

        public MessageSeed getMessageSeed() {
            return this.messageSeed;
        }
    }

}