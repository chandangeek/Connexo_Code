package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.UnderlyingNetworkException;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.ws.rs.core.Application;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.http.whiteboard", service = Application.class, property = {"alias=/apps"}, immediate = true)
public class WhiteBoard extends Application implements BinderProvider {
    private volatile HttpService httpService;
    private volatile UserService userService;
    private volatile LicenseService licenseService;
    private volatile TransactionService transactionService;
    private volatile YellowfinService yellowfinService;
    private AtomicReference<EventAdmin> eventAdminHolder = new AtomicReference<>();
    private boolean generateEvents;

    private final static String SESSION_TIMEOUT = "com.elster.jupiter.session.timeout";
    private int sessionTimeout = 600; // default value 10 min
    private List<HttpResource> resources = new CopyOnWriteArrayList<>();
    private List<App> apps = new CopyOnWriteArrayList<>();

    private BundleContext context;

    private static final Logger LOGGER = Logger.getLogger(WhiteBoard.class.getName());

    public WhiteBoard() {
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
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Reference
    public void setYellowfinService(YellowfinService yellowfinService) {
        this.yellowfinService = yellowfinService;
    }

    @Reference
    public void setEventAdminService(EventAdmin eventAdminService) {
        this.eventAdminHolder.set(eventAdminService);
    }

    @Reference(name = "ZResource", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(HttpResource resource) {
        String alias = getAlias(resource.getAlias());
        HttpContext httpContext = new HttpContextImpl(this, resource.getResolver(), userService, transactionService, yellowfinService, eventAdminHolder);
        try {
            httpService.registerResources(alias, resource.getLocalName(), httpContext);
            resources.add(resource);
        } catch (NamespaceException e) {
            LOGGER.log(Level.SEVERE, "Error while registering " + alias + ": " + e.getMessage(), e);
            throw new UnderlyingNetworkException(e);
        }
    }


    @Reference(name = "ZApplication", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addApplication(App resource) {
        List<String> applications = licenseService.getLicensedApplicationKeys();
        if(resource.getKey().equals("SYS") ||
                applications.stream().filter(application -> application.equals(resource.getKey())).findFirst().isPresent()){
            if (resource.isInternalApp()) {
                addResource(resource.getMainResource());
            }
            apps.add(resource);
        }
    }

    @Activate
    public void activate(BundleContext context, Map<String, Object> props) {
        boolean generateEvents = props != null && Boolean.TRUE.equals(props.get("event"));
        if (!generateEvents) {
            eventAdminHolder.set(null);
        }
        if (context != null) {
            int timeout = 0;
            String sessionTimeoutParam = context.getProperty(SESSION_TIMEOUT);
            if (sessionTimeoutParam != null) {
                try {
                    timeout = Integer.parseInt(sessionTimeoutParam);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse '" + sessionTimeoutParam + "' as a timeout value.", e);
                }
            }

            if (timeout > 0) {
                sessionTimeout = timeout;
            }

            this.context = context;
        }
    }

    int getSessionTimeout() {
        return sessionTimeout;
    }

    public void removeResource(HttpResource resource) {
        httpService.unregister(getAlias(resource.getAlias()));
        resources.remove(resource);
    }

    public void removeApplication(App app) {
        removeResource(app.getMainResource());
        apps.remove(app);
    }

    void checkLicense(){
        Optional<License> license;
        List<String> applications = licenseService.getLicensedApplicationKeys();

        for(App application : apps){
            if(!application.getKey().equals("SYS")){
                license = licenseService.getLicenseForApplication(application.getKey());
                if( !license.isPresent() ||
                    (license.isPresent() && license.get().getGracePeriodInDays() == 0)){
                    unregisterRestApplication(application.getKey());
                }
            }
        }
    }

    private void unregisterRestApplication(String application){
        try {
            ServiceReference<?>[] restApps = context.getAllServiceReferences(Application.class.getName(), "(app=" + application + ")");
            for(ServiceReference<?> restApp : restApps){
                if(restApp.getProperty("alias") != null){
                    httpService.unregister("/api" + restApp.getProperty("alias").toString());
                }
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
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

    List<App> getApps() {
        return apps;
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                this.bind(WhiteBoard.this).to(WhiteBoard.class);
                this.bind(yellowfinService).to(YellowfinService.class);
            }
        };
    }
}

	
