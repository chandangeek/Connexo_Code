package com.energyict.mdc.upl.io;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.modemproperties.postdialcommand.ModemComponent;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;

import java.math.BigDecimal;
import java.time.temporal.TemporalAmount;
import java.util.List;

/**
 * Provides factory services for serial IO components.
 * It comes in as many flavours as the number of serial IO libraries
 * that are supported by the mdc engine and the number of
 * modem types that are supported by the engine
 * As a client, you will use
 * &lt;target="(&(library=value)(modem-type=value))"&gt; on the
 * &#64;Reference annotation.
 * See enum LibraryType for accepted values for library.
 * See enum ModemType for accepted values for modem-type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-03 (17:56)
 */
@ProviderType
public interface SerialComponentService {

    String COMPONENT_NAME = "MIO";

    ServerSerialPort newSerialPort(SerialPortConfiguration configuration);

    SerialPortComChannel newSerialComChannel(ServerSerialPort serialPort, ComChannelType comChannelType);

    ModemComponent newModemComponent(String phoneNumber, String commandPrefix, TemporalAmount connectTimeout, TemporalAmount delayAfterConnect, TemporalAmount delayBeforeSend, TemporalAmount commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, String addressSelector, TemporalAmount lineToggleDelay, String postDialCommands);

}