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
 * that uses the rxtx library and creates {@link AtModemComponent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:10)
 */
@Component(name = "com.energyict.mdc.io.rxtx.at", service = SerialComponentService.class, property = {"library=" + LibraryType.Target.RXTX, "modem-type=" + ModemType.Target.AT})
@SuppressWarnings("unused")
public class RxTxAtModemComponentServiceImpl extends RxTxSerialComponentServiceImpl {

    @Override
    public ModemComponent newModemComponent(TypedProperties properties) {
        return new AtModemComponent(this.newAtModemProperties(properties));
    }

    @Override
    public ModemComponent newModemComponent(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, String addressSelector, TimeDuration lineToggleDelay, String postDialCommands) {
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
                        addressSelector,
                        lineToggleDelay,
                        this.parseAndValidatePostDialCommands(postDialCommands)));
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return new TypedAtModemProperties(this.getPropertySpecService()).getPropertySpecs();
    }

}