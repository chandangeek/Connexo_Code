package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.channels.serial.SerialComChannel;
import com.energyict.mdc.channels.serial.SignalController;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.protocol.ComChannel;

import java.util.List;

/**
* @author sva
* @since 18/03/13 - 16:26
*/
public class PaknetModemComponent {

    public static final String COMMAND_PROMPT_REQUEST = "\r";  // The carriage return character
    public static final String PARAMETER_SET_REQUEST = "SET";  // Parameter set
    public static final String COMMAND_PROMPT_OK = "*";// Carriage return - line feed - * - carriage return - line feed
    public static final String CONNECTION_PROMPT_OK = "COM";

    private String comPortName = "UnKnown";
    private String lastCommandSend = "";
    private String lastResponseReceived = "";

    private AbstractPaknetModemProperties modemProperties;

    public PaknetModemComponent() {
        this(new TypedPaknetModemProperties());
    }

    public PaknetModemComponent(AbstractPaknetModemProperties properties) {
        super();
        this.modemProperties = properties;
    }

    public void connect(String name, ComChannel comChannel) {
        this.initializeModem(name, comChannel);

        if (!dialModem(comChannel)) {
            throw ModemException.connectTimeOutException(this.comPortName, modemProperties.getConnectTimeout().toMillis());
        }

        initializeAfterConnect(comChannel);
    }

    /**
     * Initialize the modem so it is ready for dialing/receival of a call.
     * During this initialization, several steps are performed:<br></br>
     * <p/>
     * <ul>
     * <li>If present, the current connection of the modem is hung up</li>
     * <li>The command state is initialized.</li>
     * <li>All initialization parameters are send out to the modem</li>
     * </ul>
     *
     * @param name
     * @param comChannel
     */
    public void initializeModem(String name, ComChannel comChannel) {
        setComPortName(name);

        disconnectModemBeforeNewSession(comChannel);

        if (!initializeCommandState(comChannel)) {
            throw ModemException.failedToInitializeCommandStateString(this.comPortName, lastResponseReceived);
        }

        if (!sendParameters(comChannel)) {
            throw ModemException.failedToWriteInitString(this.comPortName, lastCommandSend, lastResponseReceived);
        }
    }

    /**
     * Terminate all current sessions on the modem
     *
     * @param comChannel the serialComChannel
     * @return true if all commands succeeded, false otherwise
     */
    public void disconnectModemBeforeNewSession(ComChannel comChannel) {
        disconnectModem(comChannel);
        flushInputStream(comChannel);
    }

    /**
     * Terminate all current sessions on the modem
     *
     * @param comChannel the serialComChannel
     * @return true if all commands succeeded, false otherwise
     */
    public void disconnectModem(ComChannel comChannel) {
        comChannel.startWriting();
        toggleDTR((SerialComChannel) comChannel, modemProperties.getLineToggleDelay().toMillis());
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
        return readAndVerifyWithRetries(comChannel, PaknetModemComponent.COMMAND_PROMPT_OK, modemProperties.getCommandTimeOut().toMillis());
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
            return readAndVerifyWithRetries(comChannel, PaknetModemComponent.COMMAND_PROMPT_OK, modemProperties.getCommandTimeOut().toMillis());
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
        return readAndVerify(comChannel, PaknetModemComponent.CONNECTION_PROMPT_OK, modemProperties.getConnectTimeout().toMillis());
    }

    /**
     * Initialization method to be performed right after the modem of the device has established a connection.
     *
     * @param comChannel
     */
    public void initializeAfterConnect(ComChannel comChannel) {
        PaknetModemComponent.delay(modemProperties.getDelayAfterConnect().toMillis());
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
            } catch (ModemException e) {
                if (!e.getType().equals(ModemException.Type.MODEM_READ_TIMEOUT)) {
                    throw e;
                }
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
                        throw ModemException.commandTimeoutExceeded(this.comPortName, timeOutInMillis, lastCommandSend);
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
     * <li>Any error messages (see {@link com.energyict.mdc.channels.serial.modem.AtModemComponent.ExceptionAnswers}</li>
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
                throw ModemException.dialingError(this.comPortName, exceptionAnswer.getDialErrorType(), this.lastCommandSend);
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
        PaknetModemComponent.delay(modemProperties.getDelayBeforeSend().toMillis());
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
        private final ModemException.DialErrorType dialErrorType;

        ExceptionAnswers(String error, ModemException.DialErrorType dialErrorType) {
            this.error = error;
            this.dialErrorType = dialErrorType;
        }

        public String getError() {
            return error;
        }

        public ModemException.DialErrorType getDialErrorType() {
            return dialErrorType;
        }
    }
}
