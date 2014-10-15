package com.elster.jupiter.systemadmin.rest;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.systemadmin.rest.resource.LicenseResource;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableSet;

@Component(name = "com.elster.jupiter.systemadmin.rest", service = Application.class, immediate = true, property = {"alias=/lic"})
public class LicensingApplication extends Application implements BinderProvider {
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile LicenseService licenseService;
    private volatile NlsService nlsService;

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(licenseService).to(LicenseService.class);
                bind(userService).to(UserService.class);
                bind(transactionService).to(TransactionService.class);
                bind(restQueryService).to(RestQueryService.class);
                bind(nlsService).to(NlsService.class);
            }
        };
    }
    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(LicenseResource.class,
                MultiPartFeature.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedExceptionMapper.class);
    }

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }
    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }
}
