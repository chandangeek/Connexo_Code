package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs;

import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthExceptionMapper;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.SCIMExceptionMapper;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource.TokenResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.SCIMService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.impl.SCIMServiceImpl;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.GroupResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.ResourceTypeResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.ServiceProviderConfigResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.UserResource;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SCIMApplication extends Application implements ApplicationSpecific {

    private final UserService userService;

    private final TokenService tokenService;

    @Inject
    public SCIMApplication(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                // Resources
                ServiceProviderConfigResource.class,
                ResourceTypeResource.class,
                UserResource.class,
                GroupResource.class,
                TokenResource.class,

                // Exception mappers
                OAuthExceptionMapper.class,
                SCIMExceptionMapper.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(SCIMServiceImpl.class).to(SCIMService.class);
            bind(userService).to(UserService.class);
            bind(tokenService).to(TokenService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
        }
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.SYSTEM.getName();
    }

}
