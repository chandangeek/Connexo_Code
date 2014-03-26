package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.rest.util.BinderProvider;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(name = "com.elster.jupiter.http.whiteboard", service = Application.class, property = {"alias=/apps"}, immediate = true)
public class WhiteBoard extends Application implements BinderProvider {
    private volatile HttpService httpService;
    private List<HttpResource> resources = new CopyOnWriteArrayList<>();

    public WhiteBoard() {
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Reference(name = "ZResource", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(HttpResource resource) {
        HttpContext httpContext = new HttpContextImpl(resource.getResolver());
        try {
            httpService.registerResources(getAlias(resource.getAlias()), resource.getLocalName(), httpContext);
            resources.add(resource);
        } catch (NamespaceException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
        return ImmutableSet.<Class<?>>of(PageResource.class);
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

	
