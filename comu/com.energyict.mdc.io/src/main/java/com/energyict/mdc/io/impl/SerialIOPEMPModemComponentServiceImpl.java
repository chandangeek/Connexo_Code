package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.LibraryType;
import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.ModemType;
import com.energyict.mdc.io.PEMPModemConfiguration;
import com.energyict.mdc.io.SerialComponentService;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the serialio library and creates {@link PEMPModemComponent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:10)
 */
@Component(name = "com.energyict.mdc.io.serialio.pemp", service = SerialComponentService.class, property = {"library=" + LibraryType.Target.SERIALIO, "modem-type=" + ModemType.Target.PEMP})
@SuppressWarnings("unused")
public class SerialIOPEMPModemComponentServiceImpl extends SerialIOComponentServiceImpl {

    // For OSGi framework only
    public SerialIOPEMPModemComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    @Inject
    public SerialIOPEMPModemComponentServiceImpl(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Reference
    @Override
    @SuppressWarnings("unused")
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Override
    public ModemComponent newModemComponent(TypedProperties properties) {
        return new PEMPModemComponent(this.newPEMPModemProperties(properties));
    }

    @Override
    public ModemComponent newModemComponent(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, String addressSelector, TimeDuration lineToggleDelay, String postDialCommands) {
        return new PEMPModemComponent(
                this.newPEMPModemProperties(
                        phoneNumber,
                        commandPrefix,
                        connectTimeout,
                        delayAfterConnect,
                        delayBeforeSend,
                        commandTimeout,
                        commandTry,
                        modemInitStrings,
                        globalModemInitStrings,
                        lineToggleDelay,
                        PEMPModemConfiguration.getPEMPModemConfiguration("TODO")));
    }

    @Override
    protected void addModemComponentProperties(List<PropertySpec> propertySpecs) {
        propertySpecs.addAll(new TypedPEMPModemProperties(this.getPropertySpecService()).getPropertySpecs());
    }

}