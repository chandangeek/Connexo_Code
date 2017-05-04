package com.energyict.mdc.io.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channel.serial.SerialComChannelImpl;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.channel.serial.modemproperties.AbstractAtModemProperties;
import com.energyict.mdc.channel.serial.modemproperties.AtModemComponent;
import com.energyict.mdc.channel.serial.modemproperties.postdialcommand.ModemComponent;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.io.SerialComponentService;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.temporal.TemporalAmount;
import java.util.List;

/**
 * Provides an implementation for the {@link SerialComponentService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-31 (16:43)
 */
public abstract class AbstractSerialComponentServiceImpl implements SerialComponentService {

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework only
    protected AbstractSerialComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    protected AbstractSerialComponentServiceImpl(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setThesaurus(thesaurus);
    }

    @Override
    public SerialPortComChannel newSerialComChannel(ServerSerialPort serialPort, ComChannelType comChannelType) {
        return new SerialComChannelImpl(serialPort, comChannelType);
    }

    protected AbstractAtModemProperties newAtModemProperties(String phoneNumber, String atCommandPrefix, TemporalAmount connectTimeout, TemporalAmount delayAfterConnect, TemporalAmount delayBeforeSend, TemporalAmount atCommandTimeout, BigDecimal atCommandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, String addressSelector, TemporalAmount lineToggleDelay, String postDialCommands) {
        return new SimpleAtModemProperties(phoneNumber, atCommandPrefix, connectTimeout, delayAfterConnect, delayBeforeSend, atCommandTimeout, atCommandTry, modemInitStrings, addressSelector, lineToggleDelay, postDialCommands, globalModemInitStrings);
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected abstract void setPropertySpecService(PropertySpecService propertySpecService);

    protected void doSetPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected abstract void setNlsService(NlsService nlsService);

    protected void setThesaurusWith(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(SerialComponentService.COMPONENT_NAME, Layer.DOMAIN));
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public ModemComponent newModemComponent(String phoneNumber, String commandPrefix, TemporalAmount connectTimeout, TemporalAmount delayAfterConnect, TemporalAmount delayBeforeSend, TemporalAmount commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, String addressSelector, TemporalAmount lineToggleDelay, String postDialCommands) {
        return new AtModemComponent(
                this.newAtModemProperties(
                        phoneNumber,
                        commandPrefix,
                        connectTimeout,
                        delayAfterConnect,
                        delayBeforeSend,
                        commandTimeout,
                        commandTry,
                        modemInitStrings,
                        globalModemInitStrings,
                        addressSelector,
                        lineToggleDelay,
                        postDialCommands));
    }
}