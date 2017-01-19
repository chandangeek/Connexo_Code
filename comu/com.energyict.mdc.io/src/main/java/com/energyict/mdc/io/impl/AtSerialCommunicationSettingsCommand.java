package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.io.NrOfDataBits;
import com.energyict.mdc.io.NrOfStopBits;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.protocol.ComChannel;

import java.math.BigDecimal;

/**
 * @author sva
 * @since 22/04/13 - 10:04
 */
public class AtSerialCommunicationSettingsCommand extends AbstractAtPostDialCommand {

    public static final char SERIAL_COMMUNICATION_SETTINGS_COMMAND = 'S';

    private Parities parity;
    private NrOfDataBits nrOfDataBits;
    private NrOfStopBits nrOfStopBits;

    public AtSerialCommunicationSettingsCommand(String command) {
        super(command);
    }

    @Override
    public void initAndVerifyCommand() {
        String settings = getCommand().trim().toUpperCase();
        if (settings.length() != 3) {
            throw new ApplicationException("The provided post dial commands string is not valid.");
        }

        try {
            setParity(validateAndGetParity(settings));
            setNrOfDataBits(validateAndGetDataBits(settings));
            setNrOfStopBits(validateAndGetStopBits(settings));
        } catch (NumberFormatException e) {
            throw new ApplicationException("The provided post dial commands string is not valid.");
        }
    }

    @Override
    public void execute(AtModemComponent modemComponent, ComChannel comChannel) {

        // Do the actual parity switch. Baud rate is not changed and data is not flushed.
        SerialPortConfiguration portConfiguration = ((SerialComChannel) comChannel).getSerialPort().getSerialPortConfiguration();
        portConfiguration.setParity(getParity());
        portConfiguration.setNrOfDataBits(getNrOfDataBits());
        portConfiguration.setNrOfStopBits(getNrOfStopBits());
    }

    /**
     * Extract the number of data bits from the settings string and validate the value.
     *
     * @param settings The settings string provided to the command as parameter
     * @return The correct number of data bits
     * @throws ApplicationException If the number of data bits is invalid
     */
    private NrOfDataBits validateAndGetDataBits(String settings) {
        BigDecimal dataBits = new BigDecimal(settings.substring(0, 1));
        NrOfDataBits nrOfDataBits = NrOfDataBits.valueFor(dataBits);
        if (nrOfDataBits == null) {
            throw new ApplicationException("The provided post dial commands string is not valid.");
        }
        return nrOfDataBits;
    }

    /**
     * Extract the number of stop bits from the settings string and validate the value.
     *
     * @param settings The settings string provided to the command as parameter
     * @return The correct number of stop bits
     * @throws ApplicationException If the number of stop bits is invalid
     */
    private NrOfStopBits validateAndGetStopBits(String settings) {
        BigDecimal stopBits = new BigDecimal(settings.substring(2, 3));
        NrOfStopBits nrOfStopBits = NrOfStopBits.valueFor(stopBits);
        if (nrOfStopBits == null) {
            throw new ApplicationException("The provided post dial commands string is not valid.");
        }
        return nrOfStopBits;
    }

    /**
     * Convert and validate a settings parity value to a 'enum' value from {@link Parities}.
     *
     * @param settings The settings string provided in the command containing the dataBits, parity and stopBits
     * @return the correct Parities value
     * @throws ApplicationException If the parity char was invalid
     */
    private Parities validateAndGetParity(String settings) {
        Parities parities = Parities.valueFor(settings.charAt(1));
        if (parities == null) {
            throw new ApplicationException("The provided post dial commands string is not valid.");
        }
        return parities;
    }

    public Parities getParity() {
        return parity;
    }

    public void setParity(Parities parity) {
        this.parity = parity;
    }

    public NrOfDataBits getNrOfDataBits() {
        return nrOfDataBits;
    }

    public void setNrOfDataBits(NrOfDataBits nrOfDataBits) {
        this.nrOfDataBits = nrOfDataBits;
    }

    public NrOfStopBits getNrOfStopBits() {
        return nrOfStopBits;
    }

    public void setNrOfStopBits(NrOfStopBits nrOfStopBits) {
        this.nrOfStopBits = nrOfStopBits;
    }
}