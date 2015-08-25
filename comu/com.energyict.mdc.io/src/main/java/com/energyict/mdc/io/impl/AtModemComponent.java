package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.io.ModemTimeoutException;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SignalController;

import com.elster.jupiter.util.exception.MessageSeed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link ModemComponent} interface
 * for the standard AT modem type.<br>
 * Copyrights EnergyICT
 * Date: 20/11/12
 * Time: 17:00
 */
public class AtModemComponent implements ModemComponent {

    public static final String CONFIRM = "\r\n";
    public static final String SEPARATOR = ";";
    public static final String DISCONNECT_SEQUENCE = "+++";
    public static final String HANG_UP_SEQUENCE = "ATH";
    public static final String RESTORE_PROFILE_SEQUENCE = "ATZ";
    public static final String DIAL_SEQUENCE = "ATD";
    public static final String OK_ANSWER = "OK";
    public static final String CONNECT = "CONNECT";
    public static final String BUSY = "BUSY";
    public static final String ERROR = "ERROR";
    public static final String NO_DIALTONE = "NO DIALTONE";
    public static final String NO_CARRIER = "NO CARRIER";
    public static final String NO_ANSWER = "NO ANSWER";

    private String comPortName = "UnKnown";
    private String lastCommandSend = "";
    private String lastResponseReceived = "";

    private AtModemProperties atModemProperties;

    public AtModemComponent(AtModemProperties atModemProperties) {
        super();
        this.atModemProperties = atModemProperties;
    }

    @Override
    public void connect(String name, SerialComChannel comChannel) {
        this.initializeModem(name, comChannel);

        if (!dialModem(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_CONNECT_TIMEOUT, this.comPortName, atModemProperties.getConnectTimeout().getMilliSeconds());
        }

        initializeAfterConnect(comChannel);
    }

    @Override
    public void initializeModem(String name, SerialComChannel comChannel) {
        this.comPortName = name;

        if (!hangUpComChannel(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_COULD_NOT_HANG_UP, this.comPortName);
        }

        if (!reStoreProfile(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE, this.comPortName, lastCommandSend, lastResponseReceived);
        }

        if (!sendInitStrings(comChannel)) {
            throw new ModemException(MessageSeeds.MODEM_COULD_NOT_SEND_INIT_STRING, this.comPortName, lastCommandSend, lastResponseReceived);
        }
    }

    /**
     * Perform the actual dial to the modem of the Device.
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if a CONNECT has been received within the expected timeout, false otherwise
     */
    public boolean dialModem(ComChannel comChannel) {
        write(comChannel, AtModemComponent.DIAL_SEQUENCE + getDialCommandPrefix() + atModemProperties.getPhoneNumber());
        return readAndVerify(comChannel, AtModemComponent.CONNECT, atModemProperties.getConnectTimeout().getMilliSeconds());
    }

    private String getDialCommandPrefix() {
        final String commandPrefix = atModemProperties.getCommandPrefix();
        if (commandPrefix != null) {
            return commandPrefix;
        } else {
            return "";
        }
    }

    /**
     * Initialization method to be performed right after the modem of the device has established a connection.
     *
     * @param comChannel The newly created ComChannel
     */
    public void initializeAfterConnect(ComChannel comChannel) {
        AtModemComponent.delay(atModemProperties.getDelayAfterConnect().getMilliSeconds());
        flushInputStream(comChannel);
        if (atModemProperties.getAddressSelector() != null && !atModemProperties.getAddressSelector().isEmpty()) {
            sendAddressSelector(comChannel);
            flushInputStream(comChannel);
        }
        if (atModemProperties.getPostDialCommands() != null && !atModemProperties.getPostDialCommands().isEmpty()) {
            executePostDial(comChannel);
            flushInputStream(comChannel);
        }
    }

    /**
     * Send all initialization strings to the modem.
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if all commands succeeded, false otherwise
     */
    public boolean sendInitStrings(ComChannel comChannel) {
        List<String> modemInitStringValues = new ArrayList<>();
        modemInitStringValues.addAll(atModemProperties.getGlobalModemInitStrings());
        modemInitStringValues.addAll(atModemProperties.getModemInitStrings());
        boolean sendNext = true;
        Iterator<String> initStringIterator = modemInitStringValues.listIterator();
        while (initStringIterator.hasNext() && sendNext) {
            sendNext = writeSingleInitString(comChannel, initStringIterator.next());
        }
        return sendNext;
    }

    public boolean writeSingleInitString(ComChannel comChannel, String initString) {
        write(comChannel, initString);
        return readAndVerifyWithRetries(comChannel, AtModemComponent.OK_ANSWER);
    }

    /**
     * Restore the default profile of the modem.
     * (by calling ATZ)
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if the command succeeded, false otherwise
     */
    public boolean reStoreProfile(ComChannel comChannel) {
        write(comChannel, AtModemComponent.RESTORE_PROFILE_SEQUENCE);
        return readAndVerifyWithRetries(comChannel, AtModemComponent.OK_ANSWER);
    }

    /**
     * Terminate all current sessions on the modem.
     *
     * @param comChannel the serialComChannel
     */
    public void disconnect(SerialComChannel comChannel) {
        hangUpComChannel(comChannel);
        toggleDTR(comChannel, atModemProperties.getLineToggleDelay().getMilliSeconds());
    }

    /**
     * Hangs the current connection of the modem up.
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if the command succeeded, false otherwise
     */
    public boolean hangUpComChannel(ComChannel comChannel) {
        sendDisconnectModem(comChannel);
        comChannel.startWriting();
        write(comChannel, AtModemComponent.HANG_UP_SEQUENCE);
        return readAndVerifyWithRetries(comChannel, AtModemComponent.OK_ANSWER);
    }

    /**
     * Send a '+++' sequence to terminate all current sessions on the modem.
     *
     * @param comChannel the comChannel to send the commands to
     */
    private void sendDisconnectModem(ComChannel comChannel) {
        comChannel.startWriting();
        this.lastCommandSend = AtModemComponent.DISCONNECT_SEQUENCE;
        comChannel.write((AtModemComponent.DISCONNECT_SEQUENCE).getBytes());
        AtModemComponent.delay(atModemProperties.getCommandTimeOut().getMilliSeconds());
        try {
            comChannel.flush();
            flushInputStream(comChannel);
        } catch (IOException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    /**
     * Send the address selector to the given comChannel.
     * Some modems share a line with different devices. Use this command to send a specific address to the modem
     * so it is clear for which which line has to be used. No response is expected
     *
     * @param comChannel the comChannel to send the commands to
     */
    public void sendAddressSelector(ComChannel comChannel) {
        comChannel.startWriting();
        this.lastCommandSend = atModemProperties.getAddressSelector();
        comChannel.write((atModemProperties.getAddressSelector()).getBytes());
        AtModemComponent.delay(atModemProperties.getCommandTimeOut().getMilliSeconds());
    }

    /**
     * Execute the post dial commands on the given comChannel.
     *
     * @param comChannel the comChannel to send the commands to
     */
    public void executePostDial(ComChannel comChannel) {
        List<AtPostDialCommand> postDialCommands = atModemProperties.getPostDialCommands();
        for (AtPostDialCommand command : postDialCommands) {
            command.execute(this, comChannel);
        }
    }

    /**
     * Flush all bytes from the inputStream. The data which is currently on the
     * inputStream <b>WILL BE LOST!</b>
     *
     * @param comChannel the comChannel which contains the inputStream to flush
     */
    private void flushInputStream(ComChannel comChannel) {
        comChannel.startReading();
        while (comChannel.available() > 0) {
            comChannel.read();
        }
    }

    /**
     * Toggle the DTR signal line, to ensure the current session is terminated.
     *
     * @param comChannel          the serialComChannel
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
     * Write the given data to the comChannel.
     *
     * @param comChannel  the comChannel to write to
     * @param dataToWrite the data to write
     */
    public void write(ComChannel comChannel, String dataToWrite) {
        delayBeforeSend();
        comChannel.startWriting();
        this.lastCommandSend = dataToWrite;
        comChannel.write((dataToWrite + AtModemComponent.CONFIRM).getBytes());
    }

    /**
     * Write the given data to the comChannel, without addition of the confirm (\r\n) sequence.
     *
     * @param comChannel  the comChannel to write to
     * @param dataToWrite the data to write
     */
    public void writeRawData(ComChannel comChannel, String dataToWrite) {
        delayBeforeSend();
        comChannel.startWriting();
        this.lastCommandSend = dataToWrite;
        comChannel.write((dataToWrite).getBytes());
    }

    /**
     * Read bytes from the comChannel and verifies against the given expected value.
     * If the value doesn't match, then we retry until the maximum number of tries is reached.
     *
     * @param comChannel     the comChannel to read
     * @param expectedAnswer the expected response
     * @return true if the answer matches the expected answer, false otherwise
     */
    private boolean readAndVerifyWithRetries(ComChannel comChannel, String expectedAnswer) {
        int currentTry = 0;
        while (currentTry++ < atModemProperties.getCommandTry().intValue()) {
            try {
                if (readAndVerify(comChannel, expectedAnswer, atModemProperties.getCommandTimeOut().getMilliSeconds())) {
                    return true;
                }
            }
            catch (ModemTimeoutException e) {
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
                    AtModemComponent.delay(25);
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
            this.lastResponseReceived = responseBuilder.toString();
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
    private boolean validateResponse(String response, String expectedAnswer) {
        for (AtModemComponent.ExceptionAnswers exceptionAnswer : AtModemComponent.ExceptionAnswers.values()) {
            if (response.contains(exceptionAnswer.getError())) {
                throw new ModemException(exceptionAnswer.getMessageSeed(), this.comPortName, this.lastCommandSend);
            }
        }
        return response.contains(expectedAnswer);
    }

    /**
     * Some devices/modems can not keep up with the current <i>HighSpeed</i> communications,
     * so we can wait a little while until the catch up.
     */
    private void delayBeforeSend() {
        AtModemComponent.delay(atModemProperties.getDelayBeforeSend().getMilliSeconds());
    }

    /**
     * Flushes all data on the comChannel and waits for a period of silence.
     *
     * @param comChannel the comChannel to flush
     * @param milliSecondsOfSilence the number of milliseconds of silence to search for
     */
    public void flush(ComChannel comChannel, long milliSecondsOfSilence) {
        comChannel.startReading();
        try {
            long flushTimeOut = System.currentTimeMillis() + milliSecondsOfSilence;
            long globalTimeOut = System.currentTimeMillis() + atModemProperties.getCommandTimeOut().getMilliSeconds() + flushTimeOut;

            while ((System.currentTimeMillis() < flushTimeOut) && (System.currentTimeMillis() < globalTimeOut)) {
                Thread.sleep(10);
                if (comChannel.available() > 0) {
                    flushInputStream(comChannel);
                    flushTimeOut = System.currentTimeMillis() + milliSecondsOfSilence;  // Move the flushTimeOut forwards
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void delay(long milliSecondsToSleep) {
        try {
            Thread.sleep(milliSecondsToSleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionCommunicationException(e);
        }
    }

    /**
     * Exception values which can occur during communication with the modem.
     */
    public enum ExceptionAnswers {
        BUSY_EXCEPTION(BUSY, MessageSeeds.AT_MODEM_BUSY),
        ERROR_EXCEPTION(ERROR, MessageSeeds.AT_MODEM_ERROR),
        NO_ANSWER_EXCEPTION(NO_ANSWER, MessageSeeds.AT_MODEM_NO_ANSWER),
        NO_CARRIER_EXCEPTION(NO_CARRIER, MessageSeeds.AT_MODEM_NO_CARRIER),
        NO_DIALTONE_EXCEPTION(NO_DIALTONE, MessageSeeds.AT_MODEM_NO_DIALTONE);

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