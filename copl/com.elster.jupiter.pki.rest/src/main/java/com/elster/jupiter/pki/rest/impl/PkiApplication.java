/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.pki.rest", service = Application.class, immediate = true, property = {"alias=/pir", "app=SYS", "name=" + PkiApplication.COMPONENT_NAME})
public class PkiApplication extends Application {

    public static final String COMPONENT_NAME = "PIR";

    private volatile PkiService pkiService;
    private volatile TransactionService transactionService;
    private volatile Thesaurus thesaurus;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
            TrustStoreResource.class,
            CertificateResource.class,
            MultiPartFeature.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(pkiService).to(PkiService.class);
                bind(transactionService).to(TransactionService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(TrustStoreInfoFactory.class).to(TrustStoreInfoFactory.class);
                bind(CertificateInfoFactory.class).to(CertificateInfoFactory.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
            }
        });
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setPkiService(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }
}
