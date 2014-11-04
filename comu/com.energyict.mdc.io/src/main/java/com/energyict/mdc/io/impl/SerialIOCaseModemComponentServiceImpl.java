package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.LibraryType;
import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.ModemType;
import com.energyict.mdc.io.SerialComponentService;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import org.osgi.service.component.annotations.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the serialio library and creates {@link CaseModemComponent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:10)
 */
@Component(name = "com.energyict.mdc.io.serialio.case", service = SerialComponentService.class, property = {"library=" + LibraryType.Target.SERIALIO, "modem-type=" + ModemType.Target.CASE})
@SuppressWarnings("unused")
public class SerialIOCaseModemComponentServiceImpl extends SerialIOComponentServiceImpl {

    @Override
    public ModemComponent newModemComponent(TypedProperties properties) {
        return new CaseModemComponent(this.newCaseModemProperties(properties));
    }

    @Override
    public ModemComponent newModemComponent(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, String addressSelector, TimeDuration lineToggleDelay, String postDialCommands) {
        return new CaseModemComponent(
                this.newCaseModemProperties(
                        phoneNumber,
                        commandPrefix,
                        connectTimeout,
                        delayAfterConnect,
                        delayBeforeSend,
                        commandTimeout,
                        commandTry,
                        modemInitStrings,
                        addressSelector,
                        lineToggleDelay));
    }

    @Override
    protected void addModemComponentProperties(List<PropertySpec> propertySpecs) {
        propertySpecs.addAll(new TypedCaseModemProperties(this.getPropertySpecService()).getPropertySpecs());
    }

}