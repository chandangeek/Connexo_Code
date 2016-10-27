package com.energyict.dialer.coreimpl;

import com.energyict.dialer.core.DialerTimeoutException;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.PostSelect;
import com.energyict.dialer.core.SerialCommunicationChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights
 * Date: 1/07/11
 * Time: 16:06
 */
public class ExtendedATDialer extends ATDialer implements PostSelect {

    private CheckedModemInputStream modemInputStream = null;

    @Override
    protected void dialModem(String strPhoneNR, String selector, int iTimeout) throws IOException, LinkException {
        if ((strDialPrefix != null) && (strDialPrefix.trim().length() != 0)) {
            write("ATD" + strDialPrefix + strPhoneNR + "\r\n", 500);
        } else {
            write("ATD" + strPhoneNR + "\r\n", 500);
        }

        if (modemConnection.expectCommPort("CONNECT", iTimeout) == false) {
            throw new DialerTimeoutException("Timeout waiting for CONNECT to phone " + strPhoneNR);
        } else {
            getStreamConnection().flushInputStream(DELAY_AFTER_CONNECT_AND_FLUSH);
            if ((selector != null) && (selector.trim().length() != 0)) {
                doPostDial(selector, iTimeout);
            }
        }
    }

    private void doPostDial(String selector, int timeOut) throws IOException {
        List<ExtendedATDialerCommand> atDialerCommands = parse(selector);
        for (ExtendedATDialerCommand command : atDialerCommands) {
            command.execute(this, timeOut);
        }
    }

    private List<ExtendedATDialerCommand> parse(String commands) throws IOException {
        ArrayList<ExtendedATDialerCommand> extendedATDialerCommands = new ArrayList<ExtendedATDialerCommand>();
        String[] splittedCommands = split(commands);
        for (String splittedCommand : splittedCommands) {
            extendedATDialerCommands.add(getCommand(splittedCommand));
        }
        return extendedATDialerCommands;
    }

    private ExtendedATDialerCommand getCommand(String splittedCommand) throws IOException {
        char command = splittedCommand.charAt(0);
        switch (command) {
            case WriteCommand.WRITE_COMMAND:
                return new WriteCommand(splittedCommand);
            case DelayCommand.DELAY_COMMAND:
                return new DelayCommand(splittedCommand);
            case FlushCommand.FLUSH_COMMAND:
                return new FlushCommand(splittedCommand);
            case SerialCommunicationSettingsCommand.SERIAL_COMMUNICATION_SETTINGS_COMMAND:
                return new SerialCommunicationSettingsCommand(splittedCommand);
            default:
                throw new IOException("ExtendedATDialer: Unknown command [" + splittedCommand + "]");
        }
    }

    private String[] split(String commands) throws IOException {
        String escapedCommand = commands.replaceAll("([\\\\][\\(])", "--").replaceAll("[\\\\][\\)]", "--");
        escapedCommand = escapedCommand.replaceAll("\\)", "");
        String[] split = escapedCommand.split("\\(");
        int pos = 0;
        for (int i = 0; i < split.length; i++) {
            int commandLength = split[i].length();
            if (commandLength > 0) {
                pos++;
                split[i] = commands.substring(pos, pos + commandLength);
                pos += commandLength + 1;
            }
        }
        return unEscapeCommands(split);
    }

    private String[] unEscapeCommands(String[] array) throws IOException {
        List<String> strings = new ArrayList<String>();
        for (String value : array) {
            String newValue = "";
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '\\') {
                    i++;
                    if (i < value.length()) {
                        c = value.charAt(i);
                        switch (c) {
                            case '0':
                                newValue += "\0";
                                break;
                            case '1':
                                newValue += "\1";
                                break;
                            case '2':
                                newValue += "\2";
                                break;
                            case '3':
                                newValue += "\3";
                                break;
                            case '4':
                                newValue += "\4";
                                break;
                            case '5':
                                newValue += "\5";
                                break;
                            case '6':
                                newValue += "\6";
                                break;
                            case '7':
                                newValue += "\7";
                                break;
                            case 'b':
                                newValue += "\b";
                                break;
                            case 't':
                                newValue += "\t";
                                break;
                            case 'n':
                                newValue += "\n";
                                break;
                            case 'f':
                                newValue += "\f";
                                break;
                            case 'r':
                                newValue += "\r";
                                break;
                            case '(':
                                newValue += "(";
                                break;
                            case ')':
                                newValue += ")";
                                break;
                            case ':':
                                newValue += ":";
                                break;
                            case '\\':
                                newValue += "\\";
                                break;
                            default:
                                throw new IOException("ExtendedATDialer: Invalid character: [\\" + c + "]");
                        }
                    }
                } else {
                    newValue += c;
                }
            }

            if ((value != null) && (value.length() > 0)) {
                strings.add(newValue);
            }
        }
        return strings.toArray(new String[0]);
    }

    public abstract class ExtendedATDialerCommand {

        private final String command;

        public abstract void execute(ExtendedATDialer dialer, int timeOut) throws IOException;

        private ExtendedATDialerCommand(String command) {
            if (command.length() > 2) {
                this.command = command.substring(2);
            } else {
                this.command = "";
            }
        }

        public String getCommand() {
            return command;
        }

    }

    public class WriteCommand extends ExtendedATDialerCommand {

        public static final char WRITE_COMMAND = 'W';

        public WriteCommand(String command) {
            super(command);
        }

        @Override
        public void execute(ExtendedATDialer dialer, int timeOut) throws IOException {
            dialer.getOutputStream().write(getCommand().getBytes());
            dialer.getOutputStream().flush();
        }
    }

    public class DelayCommand extends ExtendedATDialerCommand {

        public static final char DELAY_COMMAND = 'D';

        public DelayCommand(String command) {
            super(command);
        }

        @Override
        public void execute(ExtendedATDialer dialer, int timeOut) throws IOException {
            try {
                long delay = getCommand().trim().equalsIgnoreCase("") ? 1000 : Long.valueOf(getCommand());
                Thread.sleep(delay);
            } catch (NumberFormatException e) {
                throw new IOException("ExtendedATDialer: Invalid command [" + getCommand() + "]! This caused: [" + e.getMessage() + "]");
            } catch (InterruptedException e) {
                throw new IOException("ExtendedATDialer: Delay interrupted! [" + e.getMessage() + "]");
            }
        }

    }

    public class FlushCommand extends ExtendedATDialerCommand {

        public static final char FLUSH_COMMAND = 'F';

        public FlushCommand(String command) {
            super(command);

        }

        @Override
        public void execute(ExtendedATDialer dialer, int timeOut) throws IOException {
            try {
                long delay = getCommand().trim().equalsIgnoreCase("") ? 1000 : Long.valueOf(getCommand());
                long flushTimeOut = System.currentTimeMillis() + delay;
                long globalTimeOut = System.currentTimeMillis() + timeOut + delay;
                while ((System.currentTimeMillis() < flushTimeOut) && (System.currentTimeMillis() < globalTimeOut)) {
                    if (dialer.getModemInputStream().available() > 0) {
                        int bytesIn = dialer.getModemInputStream().read(new byte[1024]);
                        if (bytesIn > 0) {
                            flushTimeOut = System.currentTimeMillis() + delay;
                        }
                    }
                    Thread.sleep(10);
                }
            } catch (IOException e) {
                throw new IOException("ExtendedATDialer: Unable to wait for silence: " + e.getMessage());
            } catch (NumberFormatException e) {
                throw new IOException("ExtendedATDialer: Invalid command [" + getCommand() + "]! This caused: [" + e.getMessage() + "]");
            } catch (InterruptedException e) {
                throw new IOException("ExtendedATDialer: Delay interrupted! [" + e.getMessage() + "]");
            }
        }

    }

    public class SerialCommunicationSettingsCommand extends ExtendedATDialerCommand {

        public static final char SERIAL_COMMUNICATION_SETTINGS_COMMAND = 'S';

        public SerialCommunicationSettingsCommand(String command) {
            super(command);
        }

        @Override
        public void execute(ExtendedATDialer dialer, int timeOut) throws IOException {
            String settings = getCommand().trim().toUpperCase();
            if (settings.length() != 3) {
                throw new IOException("ExtendedATDialer: Invalid command [" + getCommand() + "]! Length should be 3 (7E1, 8N1, ...)");
            }

            try {
                // Extract the communication settings
                int dataBits = validateAndGetDataBits(settings);
                int parity = validateAndGetParity(settings);
                int stopBits = validateAndGetStopBits(settings);

                // Do the actual parity switch. Baud rate is not changed and data is not flushed.
                SerialCommunicationChannel serialCommunicationChannel = dialer.getSerialCommunicationChannel();
                if (serialCommunicationChannel != null) {
                    serialCommunicationChannel.setParity(dataBits, parity, stopBits);
                }

            } catch (NumberFormatException e) {
                throw new IOException("ExtendedATDialer: Invalid command [" + getCommand() + "]! This caused: NumberFormatException[" + e.getMessage() + "]");
            }
        }

        /**
         * Extract the number of data bits from the settings string and validate the value.
         *
         * @param settings The settings string provided to the command as parameter
         * @return The correct number of data bits
         * @throws IOException If the number of data bits is invalid
         */
        private int validateAndGetDataBits(String settings) throws IOException {
            int dataBits = Integer.valueOf(settings.substring(0, 1));
            if ((dataBits < 5) || (dataBits > 8)) {
                throw new IOException("ExtendedATDialer: Invalid number of data bits [" + settings + "]");
            }
            return dataBits;
        }

        /**
         * Extract the number of stop bits from the settings string and validate the value.
         *
         * @param settings The settings string provided to the command as parameter
         * @return The correct number of stop bits
         * @throws IOException If the number of stop bits is invalid
         */
        private int validateAndGetStopBits(String settings) throws IOException {
            int stopBits = Integer.valueOf(settings.substring(2, 3));
            if ((stopBits < 1) || (stopBits > 2)) {
                throw new IOException("ExtendedATDialer: Invalid number of stop bits [" + settings + "]");
            }
            return stopBits;
        }

        /**
         * Convert and validate a settings parity value to a 'enum' value from SerialCommunicationChannel.
         * These are the possible return values:
         * <ul>
         * <li>SerialCommunicationChannel.PARITY_NONE</li>
         * <li>SerialCommunicationChannel.PARITY_EVEN</li>
         * <li>SerialCommunicationChannel.PARITY_ODD</li>
         * <li>SerialCommunicationChannel.PARITY_MARK</li>
         * <li>SerialCommunicationChannel.PARITY_SPACE</li>
         * </ul>
         *
         * @param settings The settings string provided in the command containing the dataBits, parity and stopBits
         * @return the correct SerialCommunicationChannel value as an int (PARITY_NONE, PARITY_EVEN or PARITY_ODD)
         * @throws IOException If the parity char was invalid
         */
        private int validateAndGetParity(String settings) throws IOException {
            char parityChar = settings.charAt(1);
            switch (parityChar) {
                case 'n':
                case 'N':
                    return SerialCommunicationChannel.PARITY_NONE;
                case 'e':
                case 'E':
                    return SerialCommunicationChannel.PARITY_EVEN;
                case 'o':
                case 'O':
                    return SerialCommunicationChannel.PARITY_ODD;
                case 'm':
                case 'M':
                    return SerialCommunicationChannel.PARITY_MARK;
                case 's':
                case 'S':
                    return SerialCommunicationChannel.PARITY_SPACE;
                default:
                    throw new IOException("ExtendedATDialer: Invalid parity value ['" + getCommand() + "']. Only 'N', 'E', 'O', 'M' or 'S' is allowed.");
            }
        }
    }

    public CheckedModemInputStream getModemInputStream() {
        if (modemInputStream == null) {
            modemInputStream = new CheckedModemInputStream(getInputStream());
        }
        return modemInputStream;
    }
}