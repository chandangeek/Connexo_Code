package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.jupiter.yellowfin.rest" , service=Application.class , immediate = true , property = {"alias=/yfn", "app=YFN", "name=" + YellowfinApplication.COMPONENT_NAME} )
public class YellowfinApplication extends Application implements BinderProvider{
    public static final String APP_KEY = "YFN";
    public static final String COMPONENT_NAME = "YFN";

    private volatile TransactionService transactionService;
    private volatile YellowfinService yellowfinService;
    private volatile YellowfinGroupsService yellowfinGroupsService;
    private volatile License license;

    public YellowfinApplication() {
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(YellowfinResource.class, YellowfinDeviceGroupsResource.class);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setYellowfinGroupsService(YellowfinGroupsService yellowfinGroupsService) {
        this.yellowfinGroupsService = yellowfinGroupsService;
    }

    @Reference
    public void setYellowfinService(YellowfinService yellowfinService) {
        this.yellowfinService = yellowfinService;
    }

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Activate
    public void activate(ComponentContext context) {
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(transactionService).to(TransactionService.class);
                bind(yellowfinService).to(YellowfinService.class);
                bind(yellowfinGroupsService).to(YellowfinGroupsService.class);
            }
        };
    }
}

