package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.UnderlyingNetworkException;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.http.whiteboard", service = Application.class, property = {"alias=/apps"}, immediate = true)
public class WhiteBoard extends Application implements BinderProvider {
    private volatile HttpService httpService;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private AtomicReference<EventAdmin> eventAdminHolder = new AtomicReference<>();
    private boolean generateEvents;

    private final static String SESSION_TIMEOUT = "com.elster.jupiter.session.timeout";

    private int sessionTimeout = 600; // default value 10 min
    private List<HttpResource> resources = new CopyOnWriteArrayList<>();

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
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
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
            LOGGER.log(Level.SEVERE, "Error while registering " + alias + ": " + e.getMessage() , e);
            throw new UnderlyingNetworkException(e);
        }
    }

    @Activate
    public void activate(BundleContext context, Map<String,Object> props) {
    	boolean generateEvents = props != null && Boolean.TRUE.equals(props.get("event"));
    	if (!generateEvents) {
    		eventAdminHolder.set(null);
    	}
        if(context != null){
            int timeout = 0;
            String sessionTimeoutParam = context.getProperty(SESSION_TIMEOUT);
            if(sessionTimeoutParam != null){
                try{
                    timeout = Integer.parseInt(sessionTimeoutParam);
                } catch(NumberFormatException e){
                    throw new IllegalArgumentException("Cannot parse '" + sessionTimeoutParam + "' as a timeout value.", e);
                }
            }

            if(timeout > 0){
                sessionTimeout = timeout;
            }
        }
    }

    int getSessionTimeout(){
        return sessionTimeout;
    }
    
    public void removeResource(HttpResource resource) {
        httpService.unregister(getAlias(resource.getAlias()));
        resources.remove(resource);
    }
    
    String getAlias(String name) {
        return "/apps" + name;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(PageResource.class, AppResource.class);
    }

    List<HttpResource> getResources() {
        return new ArrayList<>(resources);
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                this.bind(WhiteBoard.this).to(WhiteBoard.class);
            }
        };
    }
}

	
