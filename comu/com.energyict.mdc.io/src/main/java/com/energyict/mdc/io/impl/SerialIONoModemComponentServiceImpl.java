package com.energyict.mdc.io.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.LibraryType;
import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.ModemType;
import com.energyict.mdc.io.SerialComponentService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the serialio library and creates {@link AtModemComponent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:10)
 */
@Component(name = "com.energyict.mdc.io.serialio.none", service = SerialComponentService.class, property = {"library=" + LibraryType.Target.SERIALIO, "modem-type=" + ModemType.Target.NONE})
@SuppressWarnings("unused")
public class SerialIONoModemComponentServiceImpl extends SerialIOComponentServiceImpl {

    // For OSGi framework only
    public SerialIONoModemComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    @Inject
    public SerialIONoModemComponentServiceImpl(PropertySpecService propertySpecService) {
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
        // What part of ModemType.Target.NONE did the client code NOT understand?
        throw new UnsupportedOperationException(SerialIONoModemComponentServiceImpl.class.getName() + " does not support modem operations");
    }

    @Override
    public ModemComponent newModemComponent(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, String addressSelector, TimeDuration lineToggleDelay, String postDialCommands) {
        // What part of ModemType.Target.NONE did the client code NOT understand?
        throw new UnsupportedOperationException(SerialIONoModemComponentServiceImpl.class.getName() + " does not support modem operations");
    }

    @Override
    protected void addModemComponentProperties(List<PropertySpec> propertySpecs) {
        // No additional properties to add
    }

}