/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.cps;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.app.MdcAppService;
import com.energyict.mdc.device.data.Device;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

import javax.validation.MessageInterpolator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDeviceTypePersistenceSupport<T extends PersistentDomainExtension<Device>> implements PersistenceSupport<Device, T> {

    protected final Thesaurus thesaurus;

    public AbstractDeviceTypePersistenceSupport(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<Module> module() {
        return Optional.of(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        });
    }

    @Override
    public String application() {
        return MdcAppService.APPLICATION_NAME;
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(@SuppressWarnings("rawtypes") Table table) {
        return Collections.emptyList();
    }
}