package com.energyict.mdc.common.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.services.ObisCodeDescriptor;

import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link ObisCodeDescriptor} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-27 (15:00)
 */
@Component(name = "com.energyict.mdc.obis.descriptor", service = ObisCodeDescriptor.class)
public class ObisCodeDescriptorImpl implements ObisCodeDescriptor {
    private volatile Thesaurus thesaurus;

    // For OSGi purposes
    public ObisCodeDescriptorImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public ObisCodeDescriptorImpl(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String describe(ObisCode obisCode) {
        return new ObisCodeAnalyzer(obisCode, this.thesaurus).getDescription();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN);
    }
}