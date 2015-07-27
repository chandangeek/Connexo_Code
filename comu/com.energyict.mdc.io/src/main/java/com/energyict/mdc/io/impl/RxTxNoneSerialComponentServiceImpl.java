package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.LibraryType;
import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.ModemType;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.ServerSerialPort;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the RxTx library.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:15)
 */
@Component(name = "com.energyict.mdc.io.rxtx.none", service = SerialComponentService.class, property = {"library=" + LibraryType.Target.RXTX, "modem-type=" + ModemType.Target.NONE})
@SuppressWarnings("unused")
public class RxTxNoneSerialComponentServiceImpl extends RxTxSerialComponentServiceImpl {

    // For OSGi framework only
    public RxTxNoneSerialComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    @Inject
    public RxTxNoneSerialComponentServiceImpl(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Reference
    @Override
    @SuppressWarnings("unused")
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Override
    public ServerSerialPort newSerialPort(SerialPortConfiguration configuration) {
        return new RxTxSerialPort(configuration);
    }

    @Override
    public ModemComponent newModemComponent(TypedProperties properties) {
        // What part of ModemType.Target.NONE did the client code NOT understand?
        throw new UnsupportedOperationException(RxTxNoneSerialComponentServiceImpl.class.getName() + " does not support modem operations");
    }

    @Override
    public ModemComponent newModemComponent(String phoneNumber, String commandPrefix, TimeDuration connectTimeout, TimeDuration delayAfterConnect, TimeDuration delayBeforeSend, TimeDuration commandTimeout, BigDecimal commandTry, List<String> modemInitStrings, List<String> globalModemInitStrings, String addressSelector, TimeDuration lineToggleDelay, String postDialCommands) {
        // What part of ModemType.Target.NONE did the client code NOT understand?
        throw new UnsupportedOperationException(RxTxNoneSerialComponentServiceImpl.class.getName() + " does not support modem operations");
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.baudRatePropertySpec(true),
                this.parityPropertySpec(true),
                this.nrOfStopBitsPropertySpec(true),
                this.nrOfDataBitsPropertySpec(true),
                this.flowControlPropertySpec());
    }

}