/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.MessageSeeds;
import com.elster.jupiter.http.whiteboard.UnderlyingNetworkException;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(
        name = "com.elster.jupiter.http.whiteboard",
        service = {Application.class, TranslationKeyProvider.class},
        property = {"alias=/apps", "name=HTW"},
        immediate = true)
public final class WhiteBoardImpl extends Application implements BinderProvider, TranslationKeyProvider {
    static String COMPONENTNAME = "HTW";

    private volatile HttpService httpService;
    private volatile UserService userService;
    private volatile JsonService jsonService;
    private volatile LicenseService licenseService;
    private volatile TransactionService transactionService;
    private volatile QueryService queryService;
    private volatile HttpAuthenticationService httpAuthenticationService;

    private final Object registrationLock = new Object();

    private AtomicReference<EventAdmin> eventAdminHolder = new AtomicReference<>();

    private List<HttpResource> resources = new CopyOnWriteArrayList<>();
    private List<App> apps = new CopyOnWriteArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(WhiteBoardImpl.class.getName());

    public WhiteBoardImpl() {
        super();
    }

    @Inject
    WhiteBoardImpl(TransactionService transactionService, QueryService queryService, HttpAuthenticationService httpAuthenticationService) {
        this();
        setTransactionService(transactionService);
        setQueryService(queryService);
        setHttpAuthenticationService(httpAuthenticationService);
        activate(null, null);
    }


    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Reference
    public void setEventAdminService(EventAdmin eventAdminService) {
        this.eventAdminHolder.set(eventAdminService);
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setHttpAuthenticationService(HttpAuthenticationService httpAuthenticationService) {
        this.httpAuthenticationService = httpAuthenticationService;
    }

    @Reference(name = "ZResource", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(HttpResource resource) {
        String alias = getAlias(resource.getAlias());
        HttpContext httpContext = new HttpContextImpl(resource.getResolver(), eventAdminHolder, httpAuthenticationService);
        synchronized (registrationLock) {
            try {
                httpService.registerResources(alias, resource.getLocalName(), httpContext);
                resources.add(resource);
            } catch (NamespaceException e) {
                LOGGER.log(Level.SEVERE, "Error while registering " + alias + ": " + e.getMessage(), e);
                throw new UnderlyingNetworkException(e);
            }
        }
    }

    public void removeResource(HttpResource resource) {
        synchronized (registrationLock) {
            httpService.unregister(getAlias(resource.getAlias()));
            resources.remove(resource);
        }
    }

    @Reference(name = "ZApplication", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addApplication(App resource) {
        if (resource.isInternalApp()) {
            addResource(resource.getMainResource());
        }
        apps.add(resource);
    }

    public void removeApplication(App app) {
        if (app.getMainResource() != null) {
            removeResource(app.getMainResource());
        }
        apps.remove(app);
    }

    @Activate
    public void activate(BundleContext context, Map<String, Object> props) {
        boolean generateEvents = props != null && Boolean.TRUE.equals(props.get("event"));
        if (!generateEvents) {
            eventAdminHolder.set(null);
        }
    }


    String getAlias(String name) {
        return "/apps" + (name.startsWith("/") ? name : "/" + name);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(PageResource.class, AppResource.class);
    }

    List<HttpResource> getResources() {
        return new ArrayList<>(resources);
    }

    LicenseService getLicenseService() {
        return licenseService;
    }

    List<App> getApps() {
        return apps;
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                this.bind(jsonService).to(JsonService.class);
                this.bind(userService).to(UserService.class);
                this.bind(queryService).to(QueryService.class);
                this.bind(httpAuthenticationService).to(HttpAuthenticationService.class);
                this.bind(WhiteBoardImpl.this).to(WhiteBoardImpl.class);
            }
        };
    }


    @Override
    public String getComponentName() {
        return "HTW";
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

}


