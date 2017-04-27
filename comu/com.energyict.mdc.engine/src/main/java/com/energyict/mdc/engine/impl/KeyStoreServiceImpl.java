package com.energyict.mdc.engine.impl;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.crypto.KeyStoreService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.security.KeyStore;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link KeyStoreService} interface
 * that delegates to the {@link com.elster.jupiter.datavault.KeyStoreService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (08:51)
 */
@Component(name = "com.energyict.mdc.upl.crypto.keystore", service = {KeyStoreService.class}, immediate = true)
@SuppressWarnings("unused")
public class KeyStoreServiceImpl implements KeyStoreService {

    private volatile com.elster.jupiter.datavault.KeyStoreService actual;

    // For OSGi purposes
    public KeyStoreServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public KeyStoreServiceImpl(com.elster.jupiter.datavault.KeyStoreService actual) {
        this();
        this.setActualKeyStoreService(actual);
    }

    @Activate
    public void activate() {
        Services.keyStoreService(this);
    }

    @Deactivate
    public void deactivate() {
        Services.keyStoreService(null);
    }

    @Reference
    public void setActualKeyStoreService(com.elster.jupiter.datavault.KeyStoreService actual) {
        this.actual = actual;
    }

    @Override
    public KeyStore findOrCreate(StoreType type, String name) {
        if ("DLMS".equals(name)) {
            DLMSKeyStoreUserFile keyStoreUserFile = new DLMSKeyStoreUserFile(this.actual);
            switch (type) {
                case KEY: {
                    return keyStoreUserFile.findOrCreateDLMSKeyStore();
                }
                case TRUST: {
                    return keyStoreUserFile.findOrCreateDLMSTrustStore();
                }
                default: {
                    throw new IllegalArgumentException("Unsupported key store type " + name + ". Must be one of: " + Stream.of(StoreType.values())
                            .map(StoreType::getStoreTypeValue)
                            .collect(Collectors.joining(", ")));
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported key store name " + name + ". The only currently supported one is DLMS");
        }
    }

}