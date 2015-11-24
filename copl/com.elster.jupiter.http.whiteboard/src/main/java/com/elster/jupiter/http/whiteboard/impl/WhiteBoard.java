package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.MessageSeeds;
import com.elster.jupiter.http.whiteboard.UnderlyingNetworkException;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.callback.InstallService;
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
        service = {Application.class, InstallService.class, TranslationKeyProvider.class},
        property = {"alias=/apps", "name=WEB"},
        immediate = true)
public class WhiteBoard extends Application implements BinderProvider, InstallService, TranslationKeyProvider{
    private volatile HttpService httpService;
    private volatile UserService userService;
    private volatile JsonService jsonService;
    private volatile LicenseService licenseService;
    private volatile EventService eventService;
    private volatile TransactionService transactionService;

    private AtomicReference<EventAdmin> eventAdminHolder = new AtomicReference<>();

    private final static String TIMEOUT = "com.elster.jupiter.timeout";
    private final static String TOKEN_REFRESH_MAX_COUNT = "com.elster.jupiter.token.refresh.maxcount";
    private final static String TOKEN_EXPIRATION_TIME = "com.elster.jupiter.token.expirationtime";
    private  static int timeout = 300; // default value 5 min
    private  static long tokenRefreshMaxCnt = 100;
    private static int tokenExpTime = 300;
    private List<HttpResource> resources = new CopyOnWriteArrayList<>();
    private List<App> apps = new CopyOnWriteArrayList<>();

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
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setEventAdminService(EventAdmin eventAdminService) {
        this.eventAdminHolder.set(eventAdminService);
    }

    @Reference(name = "ZResource", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(HttpResource resource) {

        String alias = getAlias(resource.getAlias());
        HttpContext httpContext = new HttpContextImpl(this, resource.getResolver(), userService, transactionService, eventAdminHolder);
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
        if (resource.isInternalApp()) {
            addResource(resource.getMainResource());
        }
        apps.add(resource);
    }

    public void activate(BundleContext context, Map<String, Object> props) {
        boolean generateEvents = props != null && Boolean.TRUE.equals(props.get("event"));
        if (!generateEvents) {
            eventAdminHolder.set(null);
        }
        if (context != null) {
            List<String> initList = new ArrayList<>();
            initList.add(TIMEOUT);
            initList.add(TOKEN_REFRESH_MAX_COUNT);
            initList.add(TOKEN_EXPIRATION_TIME);
            for(String initElement:initList){
                int temp=0;
                String tempParam = context.getProperty(initElement);
                if(tempParam!=null) {
                    try {
                        temp = Integer.parseInt(tempParam);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Cannot parse '" + tempParam + "'", e);
                    }
                }
                if(temp>0) {
                    if(initElement.equals(TIMEOUT)) timeout = temp;// *1000 since the check will be against millis
                    else if(initElement.equals(TOKEN_REFRESH_MAX_COUNT)) tokenRefreshMaxCnt = temp;
                    else tokenExpTime = temp;
                }
            }

        /*    if (tempTimeOut > 0) {
                timeout = tempTimeOut;
            }

            }
            int tempTimeOut = 0;
            String timeoutParam = context.getProperty(TIMEOUT);
            if (timeoutParam != null) {
                try {
                    tempTimeOut = Integer.parseInt(timeoutParam);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse '" + timeoutParam + "' as a timeout value.", e);
                }
            }

            if (tempTimeOut > 0) {
                timeout = tempTimeOut;
            }

            int tempTokeRefreshMaxCnt = 0;
            String tokenRefreshMaxCntParam = context.getProperty(TOKEN_REFRESH_MAX_COUNT);
            if (tokenRefreshMaxCntParam != null) {
                try {
                    tempTokeRefreshMaxCnt = Integer.parseInt(tokenRefreshMaxCntParam);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse '" + tokenRefreshMaxCntParam + "' as a numeric value.", e);
                }
            }

            if (tempTokeRefreshMaxCnt > 0) {
                tokenRefreshMaxCnt = tempTokeRefreshMaxCnt;
            }

            if (tempTimeOut > 0) {
                timeout = tempTimeOut;
            }

            int tempTokeExpirationTime = 0;
            String tempTokeExpirationTimeParam = context.getProperty(TOKEN_EXPIRATION_TIME);
            if (tempTokeExpirationTimeParam != null) {
                try {
                    tempTokeExpirationTime = Integer.parseInt(tempTokeExpirationTimeParam);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse '" + tokenRefreshMaxCntParam + "' as a numeric value.", e);
                }
            }

            if (tempTokeExpirationTime > 0) {
                tokenExpTime = tempTokeExpirationTime;
            } */

        }
    }

    public static int getTimeout() {
        return timeout;
    }

    public static long getTokenRefreshMaxCnt() {
        return tokenRefreshMaxCnt;
    }

    public static int getTokenExpTime() {
        return tokenExpTime;
    }

    public void removeResource(HttpResource resource) {
        httpService.unregister(getAlias(resource.getAlias()));
        resources.remove(resource);
    }

    public void removeApplication(App app) {
        if (app.getMainResource() != null) {
            removeResource(app.getMainResource());
        }
        apps.remove(app);
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
                this.bind(eventService).to(EventService.class);
                this.bind(jsonService).to(JsonService.class);
                this.bind(userService).to(UserService.class);
                this.bind(WhiteBoard.this).to(WhiteBoard.class);
            }
        };
    }

    @Override
    public void install() {
        createEventTypes();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT");
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Could not install eventType '" + eventType.name() + "': " + ex.getMessage(), ex);
            }
        }
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

	
