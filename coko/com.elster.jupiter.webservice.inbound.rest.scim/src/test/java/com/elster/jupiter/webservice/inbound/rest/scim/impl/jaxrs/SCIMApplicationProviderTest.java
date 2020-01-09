package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Application;

import static org.assertj.core.api.Assertions.assertThat;

public class SCIMApplicationProviderTest {

    private static SCIMApplicationProvider scimApplicationProvider;

    @BeforeClass
    public static void setUp() {
        scimApplicationProvider = new SCIMApplicationProvider();
    }

    @Test
    public void shouldReturnSCIMApplication() {

        Exception expectedNullReference = null;
        Application SCIMApplication = null;

        try {
            SCIMApplication = scimApplicationProvider.get();
        } catch (Exception exception) {
            expectedNullReference = exception;
        }

        assertThat(expectedNullReference).isNull();
        assertThat(SCIMApplication).isNotNull();
    }
}