package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs;

import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthExceptionMapper;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter.BasicAuthorizationFilter;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter.BearerAuthorizationFilter;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter.TokenEndPointResponseFilter;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.TokenService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.impl.TokenServiceImpl;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource.TokenResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.SCIMService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.impl.SCIMServiceImpl;
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

    private volatile UserService userService;

    public SCIMApplication(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                // Resources
                ServiceProviderConfigResource.class,
                ResourceTypeResource.class,
                SchemaResource.class,
                UserResource.class,
                GroupResource.class,
                TokenResource.class,

                // Filters
                BasicAuthorizationFilter.class,
                BearerAuthorizationFilter.class,
                TokenEndPointResponseFilter.class,

                // Exception mappers
                OAuthExceptionMapper.class
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
                bind(SCIMServiceImpl.class).to(SCIMService.class).in(Singleton.class);
                bind(userService).to(UserService.class);
            }
        };
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.SCIM_PROVISIONING_TOOL.getName();
    }

}
