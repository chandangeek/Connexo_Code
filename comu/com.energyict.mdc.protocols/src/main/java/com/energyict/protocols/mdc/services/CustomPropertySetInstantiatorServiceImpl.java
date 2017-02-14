package com.energyict.protocols.mdc.services;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 13/02/2017 - 11:18
 */
@Component(name = "com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService", service = CustomPropertySetInstantiatorService.class, immediate = true)
public class CustomPropertySetInstantiatorServiceImpl implements CustomPropertySetInstantiatorService {

    private Injector injector;

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    // For OSGi purposes
    public CustomPropertySetInstantiatorServiceImpl() {
        super();
    }

    @Inject
    public CustomPropertySetInstantiatorServiceImpl(PropertySpecService propertySpecService, NlsService nlsService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setNlsService(nlsService);
        this.activate();
    }

    @Activate
    public void activate() {
        this.injector = Guice.createInjector(this.getModule());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                this.bind(PropertySpecService.class).toInstance(propertySpecService);
                this.bind(Thesaurus.class).toInstance(thesaurus);
                this.bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        };
    }

    @Override
    public CustomPropertySet createCustomPropertySet(String javaClassName) throws ClassNotFoundException {
        Class<?> cpsClass = this.getClass().getClassLoader().loadClass(javaClassName);
        return (CustomPropertySet) injector.getInstance(cpsClass);
    }

    @Override
    public Class forName(String javaClassName) throws ClassNotFoundException {
        return Class.forName(javaClassName);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    private void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN);
    }
}