package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.LibraryType;
import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.ModemType;
import com.energyict.mdc.io.SerialComponentService;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    // For OSGi framework only
    public RxTxAtModemComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    @Inject
    public RxTxAtModemComponentServiceImpl(PropertySpecService propertySpecService) {
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
        return new AtModemComponent(this.newAtModemProperties(properties));
    }

    @Override
    public ModemComponent newModemComponent(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, String addressSelector, TimeDuration lineToggleDelay, String postDialCommands) {
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
                        this.parseAndValidatePostDialCommands(postDialCommands)));
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(new TypedAtModemProperties(this.getPropertySpecService()).getPropertySpecs());
        propertySpecs.add(this.baudRatePropertySpec(true));
        propertySpecs.add(this.parityPropertySpec(true));
        propertySpecs.add(this.nrOfStopBitsPropertySpec(true));
        propertySpecs.add(this.nrOfDataBitsPropertySpec(true));
        propertySpecs.add(this.flowControlPropertySpec());
        return propertySpecs;
    }

}