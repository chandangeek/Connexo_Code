package com.elster.jupiter.users.rest.impl;

import javax.ws.rs.core.Response;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CSRFTokenServiceTest extends UsersRestApplicationJerseyTest {
    @Test
    public void csrfTokenServiceTest(){
       Response response = target("csrf/token").request().get();
       assertEquals("should return status 200", 200, response.getStatus());
       assertNotNull("Should return value", response.getEntity());
    }
}
