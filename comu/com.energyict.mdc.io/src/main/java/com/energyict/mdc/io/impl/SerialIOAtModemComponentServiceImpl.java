/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
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
@Component(name = "com.energyict.mdc.io.serialio.at", service = SerialComponentService.class, property = {"library=" + LibraryType.Target.SERIALIO, "modem-type=" + ModemType.Target.AT})
@SuppressWarnings("unused")
public class SerialIOAtModemComponentServiceImpl extends SerialIOComponentServiceImpl {

    // For OSGi framework only
    public SerialIOAtModemComponentServiceImpl() {
        super();
    }

    // For guice injection purposes
    @Inject
    public SerialIOAtModemComponentServiceImpl(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Reference
    @Override
    @SuppressWarnings("unused")
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.doSetPropertySpecService(propertySpecService);
    }

    @Reference
    @Override
    @SuppressWarnings("unused")
    public void setNlsService(NlsService nlsService) {
        this.setThesaurusWith(nlsService);
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
    protected void addModemComponentProperties(List<PropertySpec> propertySpecs) {
        propertySpecs.addAll(new TypedAtModemProperties(this.getPropertySpecService(), this.getThesaurus()).getPropertySpecs());
    }

}