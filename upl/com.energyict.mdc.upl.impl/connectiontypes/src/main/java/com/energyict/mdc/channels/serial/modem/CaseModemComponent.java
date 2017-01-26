package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.channels.serial.SignalController;
import com.energyict.mdc.channels.serial.modem.postdialcommand.ModemComponent;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.io.ModemException;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Modem component, similar to {@link AtModemComponent}, but to be used for 'Case' modem communications.
 *
 * @author sva
 * @since 30/04/13 - 11:45
 */
public class CaseModemComponent implements ModemComponent {

    // Case commands and corresponding response
    public static final String CONFIRM = "\r\n";
    public static final String SEPARATOR = ";";
    public static final String DIAL_SEQUENCE = "D";
    public static final String LINK_ESTABLISHED = "LINK ESTABLISHED";
    public static final String CALL_ABORTED = "CALL ABORTED";

    // Initialization strings
    public static final String ECHO_OFF_COMMAND = "L0";
    public static final String ECHO_OFF = "ECHO OFF";
    public static final String DTR_NORMAL_COMMAND = "O0";
    public static final String DTR_NORMAL = "DTR NORMAL";
    public static final String ERROR_CORRECTING_MODE_COMMAND = "V0";
    public static final String ERROR_CORRECTING_MODE = "ERROR CORRECTING MODE";

    private String comPortName = "UnKnown";
    private String lastCommandSend = "";
    private String lastResponseReceived = "";

    private AbstractCaseModemProperties modemProperties;

    public CaseModemComponent(AbstractCaseModemProperties modemProperties) {
        super();
        this.modemProperties = modemProperties;
    }

    public void connect(String name, SerialPortComChannel comChannel) {
        this.initializeModem(name, comChannel);

        if (!dialModem(comChannel)) {
            throw ModemException.connectTimeOutException(this.comPortName, modemProperties.getConnectTimeout().get(ChronoUnit.MILLIS));
        }

        initializeAfterConnect(comChannel);
    }

    @Override
    public void initializeModem(String name, SerialPortComChannel comChannel) {
        this.comPortName = name;

        disconnectModemBeforeNewSession(comChannel);

        if (!sendInitStrings(comChannel)) {
            throw ModemException.failedToWriteInitString(this.comPortName, lastCommandSend, lastResponseReceived);
        }
    }

    /**
     * Perform the actual dial to the modem of the Device.
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if a CONNECT has been received within the expected timeout, false otherwise
     */
    public boolean dialModem(ComChannel comChannel) {
        write(comChannel, CaseModemComponent.DIAL_SEQUENCE + modemProperties.getCommandPrefix() + modemProperties.getPhoneNumber());
        return readAndVerify(comChannel, CaseModemComponent.LINK_ESTABLISHED, modemProperties.getConnectTimeout().get(ChronoUnit.MILLIS));
    }

    @Override
    public void initializeAfterConnect(ComChannel comChannel) {
        this.delay(modemProperties.getDelayAfterConnect().get(ChronoUnit.MILLIS));
        flushInputStream(comChannel);
        if (!modemProperties.getAddressSelector().isEmpty()) {
            sendAddressSelector(comChannel);
            flushInputStream(comChannel);
        }
    }

    /**
     * Send all initialization strings to the modem
     *
     * @param comChannel the comChannel to send the commands to
     * @return true if all commands succeeded, false otherwise
     */
    public boolean sendInitStrings(ComChannel comChannel) {
        boolean sendNext = writeSingleInitString(comChannel, ECHO_OFF_COMMAND, ECHO_OFF);
        if (sendNext) {
            sendNext = writeSingleInitString(comChannel, DTR_NORMAL_COMMAND, DTR_NORMAL);
        }
        if (sendNext) {
            sendNext = writeSingleInitString(comChannel, ERROR_CORRECTING_MODE_COMMAND, ERROR_CORRECTING_MODE);
        }

        List<String> modemInitStringValues = new ArrayList<>();
        modemInitStringValues.addAll(modemProperties.getGlobalModemInitStrings());
        modemInitStringValues.addAll(modemProperties.getModemInitStrings());
        Iterator<String> initStringIterator = modemInitStringValues.listIterator();
        while (initStringIterator.hasNext() && sendNext) {
            sendNext = writeSingleInitString(comChannel, initStringIterator.next(), CONFIRM);
        }
        return sendNext;
    }

    public boolean writeSingleInitString(ComChannel comChannel, String initString, String expectedAnswer) {
        write(comChannel, initString);
        return readAndVerifyWithRetries(comChannel, expectedAnswer);
    }

    /**
     * Terminate all current sessions on the modem
     *
     * @param comChannel the serialComChannel
     * @return true if all commands succeeded, false otherwise
     */
    public void disconnectModemBeforeNewSession(SerialPortComChannel comChannel) {
        disconnect(comChannel);
        flushInputStream(comChannel);
    }

    /**
     * Terminate all current sessions on the modem
     *
     * @param comChannel the serialComChannel
     * @return true if all commands succeeded, false otherwise
     */
    public void disconnect(SerialPortComChannel comChannel) {
        comChannel.startWriting();
        toggleDTR(comChannel, modemProperties.getLineToggleDelay().get(ChronoUnit.MILLIS));
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
        this.lastCommandSend = modemProperties.getAddressSelector();
        comChannel.write((modemProperties.getAddressSelector()).getBytes());
        this.delay(modemProperties.getCommandTimeOut().get(ChronoUnit.MILLIS));
    }

    /**
     * Toggle the DTR signal line, to ensure the current session is terminated
     *
     * @param comChannel          the serialComChannel
     * @param delayInMilliSeconds the delay to wait after each DTR signal switch
     */
    protected void toggleDTR(SerialPortComChannel comChannel, long delayInMilliSeconds) {
        SignalController signalController = comChannel.getSerialPort().getSerialPortSignalController();
        signalController.setDTR(false);
        delay(delayInMilliSeconds);
        signalController.setDTR(true);
        delay(delayInMilliSeconds);
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

    @Override
    public void write(ComChannel comChannel, String dataToWrite, boolean confirm) {
        delayBeforeSend();
        comChannel.startWriting();
        this.lastCommandSend = dataToWrite;
        comChannel.write((dataToWrite + (confirm ? CaseModemComponent.CONFIRM : "")).getBytes());
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
        while (currentTry++ < modemProperties.getCommandTry().intValue()) {
            try {
                if (readAndVerify(comChannel, expectedAnswer, modemProperties.getCommandTimeOut().get(ChronoUnit.MILLIS))) {
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

    @Override
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
                    this.delay(25);
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
            this.lastResponseReceived = responseBuilder.toString();
        }
        return true;
    }

    /**
     * The validateResponse will check if the answer we received from the meter contains:
     * <ul>
     * <li>Any error messages (see {@link com.energyict.mdc.channels.serial.modem.CaseModemComponent.ExceptionAnswers}</li>
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
        for (CaseModemComponent.ExceptionAnswers exceptionAnswer : CaseModemComponent.ExceptionAnswers.values()) {
            if (response.contains(exceptionAnswer.getError())) {
                throw ModemException.dialingError(this.comPortName, exceptionAnswer.getDialErrorType(), this.lastCommandSend);
            }
        }
        return response.contains(expectedAnswer);
    }


    /**
     * Some devices/modems can not keep up with the current <i>HighSpeed</i> communications,
     * so we can wait a little while until the catch up.
     */
    private void delayBeforeSend() {
        this.delay(modemProperties.getDelayBeforeSend().get(ChronoUnit.MILLIS));
    }

    /**
     * Flushes all data on the comChannel and waits for a period of silence.
     *
     * @param comChannel            the comChannel to flush
     * @param milliSecondsOfSilence the number of milliseconds of silence to search for
     */
    public void flush(ComChannel comChannel, long milliSecondsOfSilence) {
        try {
            long flushTimeOut = System.currentTimeMillis() + milliSecondsOfSilence;
            long globalTimeOut = System.currentTimeMillis() + modemProperties.getCommandTimeOut().get(ChronoUnit.MILLIS) + flushTimeOut;

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

    public void delay(long milliSecondsToSleep) {
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
        ERROR_CALL_ABORTED(CALL_ABORTED, com.energyict.mdc.upl.io.ModemException.DialErrorType.MODEM_CALL_ABORTED);

        private final String error;
        private final com.energyict.mdc.upl.io.ModemException.DialErrorType dialErrorType;

        ExceptionAnswers(String error, com.energyict.mdc.upl.io.ModemException.DialErrorType dialErrorType) {
            this.error = error;
            this.dialErrorType = dialErrorType;
        }

        public String getError() {
            return error;
        }

        public com.energyict.mdc.upl.io.ModemException.DialErrorType getDialErrorType() {
            return dialErrorType;
        }
    }
}