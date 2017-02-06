package com.energyict.mdc.io.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialPort;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.upl.io.LibraryType;
import com.energyict.mdc.upl.io.ModemType;
import com.energyict.mdc.upl.io.SerialComponentService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the rxtx library and creates {@link ServerSerialPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:10)
 */
@Component(name = "com.energyict.mdc.io.rxtx.at", service = SerialComponentService.class, property = {"library=" + LibraryType.Target.RXTX, "modem-type=" + ModemType.Target.AT})
@SuppressWarnings("unused")
public class RxTxAtModemComponentServiceImpl extends AbstractSerialComponentServiceImpl {

    // For OSGi framework only
    public RxTxAtModemComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    @Inject
    public RxTxAtModemComponentServiceImpl(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public ServerSerialPort newSerialPort(SerialPortConfiguration configuration) {
        return new RxTxSerialPort(configuration);
    }

    @Reference
    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.doSetPropertySpecService(propertySpecService);
    }

    @Reference
    @Override
    public void setNlsService(NlsService nlsService) {
        this.setThesaurusWith(nlsService);
    }
}