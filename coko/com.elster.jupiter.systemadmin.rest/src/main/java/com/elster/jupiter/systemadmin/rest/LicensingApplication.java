package com.elster.jupiter.systemadmin.rest;

import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.systemadmin.LicensingService;
import com.elster.jupiter.systemadmin.rest.resource.LicensingResource;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.jupiter.systemadmin.rest", service = Application.class, immediate = true, property = {"alias=/sam"})
public class LicensingApplication  extends Application implements BinderProvider {
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile LicensingService licensingService;

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(licensingService).to(LicensingService.class);
                bind(userService).to(UserService.class);
                bind(transactionService).to(TransactionService.class);
                bind(restQueryService).to(RestQueryService.class);
            }
        };
    }
    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(LicensingResource.class, MultiPartFeature.class);
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
    public void setLicensingService(LicensingService licensingService) {
        this.licensingService = licensingService;
    }
    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }
}
