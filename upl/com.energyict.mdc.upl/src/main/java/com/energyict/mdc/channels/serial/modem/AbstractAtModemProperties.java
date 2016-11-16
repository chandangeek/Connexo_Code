package com.energyict.mdc.channels.serial.modem;

import com.energyict.mdc.channels.serial.modem.PostDialCommand.AbstractAtPostDialCommand;
import com.energyict.mdc.channels.serial.modem.PostDialCommand.AtDelayCommand;
import com.energyict.mdc.channels.serial.modem.PostDialCommand.AtFlushCommand;
import com.energyict.mdc.channels.serial.modem.PostDialCommand.AtSerialCommunicationSettingsCommand;
import com.energyict.mdc.channels.serial.modem.PostDialCommand.AtWriteCommand;

import com.energyict.cbo.ApplicationException;
import com.energyict.comserver.tools.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 23/11/12 (8:53)
 */
public abstract class AbstractAtModemProperties extends AbstractModemProperties {

    /**
     * Getter for the address selector to use
     *
     * @return the address selector to use after a physical connect
     */
    protected abstract String getAddressSelector();

    /**
     * Getter for the list of post dial command(s) to use
     *
     * @return the post dial command(s) to execute after a physical connect
     */
    protected abstract String getPostDialCommands();

    // --- COMMON PARSING METHODS ---
    /**
     * Parse the given commands string, split it up in separate {@link AbstractAtPostDialCommand PostDialCommands} and validate them.
     * @param commands the complete commands string, to be splitted into AbstractAtPostDialCommands.
     * @return the list of AbstractAtPostDialCommands
     */
    public List<AbstractAtPostDialCommand> parseAndValidatePostDialCommands(String commands) {
        ArrayList<AbstractAtPostDialCommand> postDialCommands = new ArrayList<>();
        String[] splittedCommands = split(commands);
        for (String splittedCommand : splittedCommands) {
            AbstractAtPostDialCommand postDialCommand = getCommand(splittedCommand);
            postDialCommand.initAndVerifyCommand();
            postDialCommands.add(postDialCommand);
        }
        return postDialCommands;
    }

    private AbstractAtPostDialCommand getCommand(String splittedCommand) {
        char command = splittedCommand.charAt(0);
        switch (command) {
            case AtWriteCommand.WRITE_COMMAND:
                return new AtWriteCommand(splittedCommand);
            case AtDelayCommand.DELAY_COMMAND:
                return new AtDelayCommand(splittedCommand);
            case AtFlushCommand.FLUSH_COMMAND:
                return new AtFlushCommand(splittedCommand);
            case AtSerialCommunicationSettingsCommand.SERIAL_COMMUNICATION_SETTINGS_COMMAND:
                return new AtSerialCommunicationSettingsCommand(splittedCommand);
            default:
                throw new ApplicationException("The provided post dial commands string is not valid.");
        }
    }

    private String[] split(String commands) {
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

    private String[] unEscapeCommands(String[] commands) {
        List<String> unEscapedCommands = new ArrayList<>();
        for (String command : commands) {
            if (!Strings.isEmpty(command)) {
                unEscapedCommands.add(this.unEscapeCommand(command));
            }
        }
        return unEscapedCommands.toArray(new String[unEscapedCommands.size()]);
    }

    private String unEscapeCommand (String command) {
        StringBuilder newValueBuilder = new StringBuilder();
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (c == '\\') {
                i++;
                if (i < command.length()) {
                    newValueBuilder.append(this.unEscape(command.charAt(i)));
                }
            } else {
                newValueBuilder.append(c);
            }
        }
        return newValueBuilder.toString();
    }

    private String unEscape (char escaped) {
        switch (escaped) {
            case '0':  return "\0";
            case '1':  return "\1";
            case '2':  return "\2";
            case '3':  return "\3";
            case '4':  return "\4";
            case '5':  return "\5";
            case '6':  return "\6";
            case '7':  return "\7";
            case 'b':  return "\b";
            case 't':  return "\t";
            case 'n':  return "\n";
            case 'f':  return "\f";
            case 'r':  return "\r";
            case '(':  return "(";
            case ')':  return ")";
            case ':':  return ":";
            case '\\':  return "\\";
            default:  throw new ApplicationException("The provided post dial commands string is not valid.");
        }
    }

}