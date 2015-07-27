package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.OpticalComChannel;
import com.energyict.mdc.io.PEMPModemConfiguration;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.ServerSerialPort;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Checks;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link SerialComponentService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-31 (16:43)
 */
public abstract class AbstractSerialComponentServiceImpl implements SerialComponentService {

    private volatile PropertySpecService propertySpecService;

    // For OSGi framework only
    protected AbstractSerialComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    protected AbstractSerialComponentServiceImpl(PropertySpecService propertySpecService) {
        this();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public SerialComChannel newSerialComChannel(ServerSerialPort serialPort) {
        return new SerialComChannelImpl(serialPort);
    }

    @Override
    public OpticalComChannel createOpticalFromSerialComChannel(SerialComChannel serialComChannel) {
        return new OpticalComChannelImpl(serialComChannel);
    }

    protected AtModemProperties newAtModemProperties(TypedProperties properties) {
        TypedProperties simpleProperties = TypedProperties.empty();
        List<AtPostDialCommand> postDialCommands = Collections.emptyList();
        for (String propertyName : properties.propertyNames()) {
            switch (propertyName) {
                case TypedAtModemProperties.AT_MODEM_POST_DIAL_COMMANDS:
                    postDialCommands = this.parseAndValidatePostDialCommands((String) properties.getProperty(propertyName));
                    break;
                default:
                    simpleProperties.setProperty(propertyName, properties.getProperty(propertyName));
                    break;
            }
        }
        return new TypedAtModemProperties(simpleProperties, postDialCommands, this.propertySpecService);
    }

    protected AtModemProperties newAtModemProperties(String phoneNumber, String atCommandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration atCommandTimeout, BigDecimal atCommandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, String addressSelector, TimeDuration lineToggleDelay, List<AtPostDialCommand> postDialCommands) {
        return new SimpleAtModemProperties(phoneNumber, atCommandPrefix, connectTimeout, delayAfterConnect, delayBeforeSend, atCommandTimeout, atCommandTry, modemInitStrings, addressSelector, lineToggleDelay, postDialCommands, globalModemInitStrings);
    }

    protected CaseModemProperties newCaseModemProperties(TypedProperties properties) {
        return new TypedCaseModemProperties(TypedProperties.copyOf(properties), this.propertySpecService);
    }

    protected CaseModemProperties newCaseModemProperties(String phoneNumber, String atCommandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration atCommandTimeout, BigDecimal atCommandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, String addressSelector, TimeDuration lineToggleDelay) {
        return new SimpleCaseModemProperties(phoneNumber, atCommandPrefix, connectTimeout, delayAfterConnect, delayBeforeSend, atCommandTimeout, atCommandTry, modemInitStrings, addressSelector, lineToggleDelay, globalModemInitStrings);
    }

    protected PaknetModemProperties newPaknetModemProperties(TypedProperties properties) {
        return new TypedPaknetModemProperties(TypedProperties.copyOf(properties), this.propertySpecService);
    }

    protected PaknetModemProperties newPaknetModemProperties(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, TimeDuration lineToggleDelay) {
        return new SimplePaknetModemProperties(phoneNumber, commandPrefix, connectTimeout, delayAfterConnect, delayBeforeSend, commandTimeout, commandTry, modemInitStrings, lineToggleDelay, globalModemInitStrings);
    }

    protected PEMPModemProperties newPEMPModemProperties(TypedProperties properties) {
        return new TypedPEMPModemProperties(TypedProperties.copyOf(properties), this.propertySpecService);
    }

    protected PEMPModemProperties newPEMPModemProperties(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, TimeDuration lineToggleDelay, PEMPModemConfiguration modemConfiguration) {
        return new SimplePEMPModemProperties(phoneNumber, commandPrefix, connectTimeout, delayAfterConnect, delayBeforeSend, commandTimeout, commandTry, modemInitStrings, lineToggleDelay, modemConfiguration, globalModemInitStrings);
    }

    protected List<AtPostDialCommand> parseAndValidatePostDialCommands(String commands) {
        List<AtPostDialCommand> postDialCommands = new ArrayList<>();
        String[] splittedCommands = this.split(commands);
        for (String splittedCommand : splittedCommands) {
            AtPostDialCommand postDialCommand = this.getCommand(splittedCommand);
            postDialCommand.initAndVerifyCommand();
            postDialCommands.add(postDialCommand);
        }
        return postDialCommands;
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
            if (!Checks.is(command).empty()) {
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

    private AtPostDialCommand getCommand(String splittedCommand) {
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

    protected void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return this.getPropertySpecs()
                .stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

}