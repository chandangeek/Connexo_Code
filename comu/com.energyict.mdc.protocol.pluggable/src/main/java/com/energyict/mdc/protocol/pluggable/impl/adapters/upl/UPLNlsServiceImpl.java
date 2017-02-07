package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.nls.Layer;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.google.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides an implementation for the {@link PropertySpecService universal protocol nls service}
 * delegating as much as possible to the {@link NlsService Connexo service}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-28 (13:06)
 */
@SuppressWarnings("unused")
@Component(name = "com.energyict.mdc.protocol.pluggable.upl.nlsservice", service = {NlsService.class}, immediate = true)
public class UPLNlsServiceImpl implements NlsService {
    private volatile com.elster.jupiter.nls.NlsService actual;

    // For OSGi framework
    public UPLNlsServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public UPLNlsServiceImpl(com.elster.jupiter.nls.NlsService nlsService) {
        super();
        this.setActualNlsService(nlsService);
    }

    @Reference
    public void setActualNlsService(com.elster.jupiter.nls.NlsService actual) {
        this.actual = actual;
    }

    @Activate
    public void activate() {
        Services.nlsService(this);
    }

    @Deactivate
    public void deactivate() {
        Services.nlsService(null);
    }

    @Override
    public Thesaurus getThesaurus(String id) {
        return new UPLThesaurusAdapter(this.actual.getThesaurus(id, Layer.DOMAIN));
    }

}