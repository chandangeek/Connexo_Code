package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.OAuthExceptionMapper;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.error.SCIMExceptionMapper;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter.BasicAuthorizationFilter;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter.BearerAuthorizationFilter;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter.TokenEndPointResponseFilter;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.TokenService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.impl.TokenServiceImpl;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource.TokenResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.SCIMService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.impl.SCIMServiceImpl;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.GroupResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.ResourceTypeResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.SchemaResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.ServiceProviderConfigResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.UserResource;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.webservice.inbound.rest.scim.SCIMApplication",
        service = {
                Application.class,
                TranslationKeyProvider.class,
                MessageSeedProvider.class
        },
        immediate = true,
        property = {
                "name=C99",
                "app=ENE",
                "alias=/scim",
                "version=v1.0"
        }
)
public class SCIMApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {

    public static final String COMPONENT_NAME = "C99";

    private volatile UserService userService;

    public SCIMApplication() {
        // NOOP, so OSGi can manage this bean
    }

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
                OAuthExceptionMapper.class,
                SCIMExceptionMapper.class
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

    @Reference
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Collections.emptyList();
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Collections.emptyList();
    }
}
