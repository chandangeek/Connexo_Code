package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource.TokenResource;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.resource.*;
import com.google.common.collect.ImmutableSet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SCIMApplicationTest {

    private static SCIMApplication scimApplication;

    @BeforeClass
    public static void setUp() {
        scimApplication = new SCIMApplication(null);
    }

    @Test
    public void shouldReturnResources() {
        final ImmutableSet<Class<?>> expected = ImmutableSet.of(
                ServiceProviderConfigResource.class,
                ResourceTypeResource.class,
                UserResource.class,
                GroupResource.class,
                TokenResource.class
        );

        final Set<Class<?>> actual = scimApplication.getClasses();

        assertThat(actual).containsAll(expected);
    }


}