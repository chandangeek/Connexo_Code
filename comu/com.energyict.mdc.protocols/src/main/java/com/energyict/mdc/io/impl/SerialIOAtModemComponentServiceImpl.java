package com.energyict.mdc.io.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.channel.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.upl.io.LibraryType;
import com.energyict.mdc.upl.io.ModemType;
import com.energyict.mdc.upl.io.SerialComponentService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the serialio library and creates {@link ServerSerialPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:10)
 */
@Component(name = "com.energyict.mdc.io.serialio.at", service = SerialComponentService.class, property = {"library=" + LibraryType.Target.SERIALIO, "modem-type=" + ModemType.Target.AT}, immediate = true)
@SuppressWarnings("unused")
public class SerialIOAtModemComponentServiceImpl extends AbstractSerialComponentServiceImpl {

    // For OSGi framework only
    public SerialIOAtModemComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    @Inject
    public SerialIOAtModemComponentServiceImpl(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public ServerSerialPort newSerialPort(SerialPortConfiguration configuration) {
        return new SioSerialPort(configuration);
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