package com.energyict.mdc.io;

import com.energyict.mdc.common.TypedProperties;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;

import java.math.BigDecimal;
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

    public static final String COMPONENT_NAME = "MIO";

    public ServerSerialPort newSerialPort(SerialPortConfiguration configuration);

    public SerialComChannel newSerialComChannel(ServerSerialPort serialPort);

    public List<PropertySpec> getPropertySpecs();

    public PropertySpec getPropertySpec(String name);

    public ModemComponent newModemComponent(TypedProperties properties);

    public ModemComponent newModemComponent(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, String addressSelector, TimeDuration lineToggleDelay, String postDialCommands);

    public OpticalComChannel createOpticalFromSerialComChannel(SerialComChannel serialComChannel);

}