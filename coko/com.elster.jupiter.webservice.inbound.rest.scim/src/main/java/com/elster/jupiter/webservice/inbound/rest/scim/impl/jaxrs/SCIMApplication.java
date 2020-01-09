package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.TokenService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenRequest;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.impl.TokenServiceImpl;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource.TokenResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.*;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SCIMApplication extends Application implements ApplicationSpecific {

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                ServiceProviderConfigResource.class,
                ResourceTypeResource.class,
                SchemaResource.class,
                UserResource.class,
                GroupResource.class,
                TokenResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>(super.getSingletons());
        hashSet.add(getBinder());
        return Collections.unmodifiableSet(hashSet);
    }

    private Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(TokenServiceImpl.class).to(TokenService.class).in(Singleton.class);
                bind(ExceptionFactory.class).to(ExceptionFactory.class);
                bind(TokenRequest.class).to(TokenRequest.class);
                bind(TokenResponse.class).to(TokenResponse.class);
            }
        };
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.SCIM_PROVISIONING_TOOL.getName();
    }

}
