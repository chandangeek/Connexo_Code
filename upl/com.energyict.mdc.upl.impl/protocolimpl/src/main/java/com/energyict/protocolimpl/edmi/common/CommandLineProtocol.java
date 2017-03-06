package com.energyict.protocolimpl.edmi.common;

import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.connection.CommandLineConnection;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/02/2017 - 12:00
 */
public interface CommandLineProtocol {

    CommandFactory getCommandFactory();

    CommandLineConnection getCommandLineConnection();

    TimeZone getTimeZone();

    boolean useHardCodedInfo();

    boolean useOldProfileFromDate();

    boolean useExtendedCommand();

    int getMaxNrOfRetries();

    String getConfiguredSerialNumber();

}