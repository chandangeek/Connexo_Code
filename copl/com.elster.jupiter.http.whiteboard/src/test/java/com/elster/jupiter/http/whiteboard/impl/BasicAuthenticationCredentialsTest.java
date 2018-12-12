/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import java.util.Base64;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class BasicAuthenticationCredentialsTest {


    @Test(expected = IllegalArgumentException.class)
    public void nullAuthentication(){
        new BasicAuthenticationCredentials(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyAuthentication(){
        new BasicAuthenticationCredentials("");

    }


    @Test(expected = IllegalArgumentException.class)
    public void noColon() {
        byte[] bytes = "user".getBytes();
        String authentication = Base64.getEncoder().encodeToString(bytes);
        new BasicAuthenticationCredentials(authentication);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidBase64() {
        new BasicAuthenticationCredentials("<>!");
    }

    @Test
    public void noName(){
        byte[] bytes = ":".getBytes();
        String authentication = Base64.getEncoder().encodeToString(bytes);
        BasicAuthenticationCredentials credentials = new BasicAuthenticationCredentials(authentication);

        assertTrue(credentials.getUserName().isEmpty());
    }

    @Test
    public void validName(){
        byte[] bytes = "user:".getBytes();
        String authentication = Base64.getEncoder().encodeToString(bytes);
        BasicAuthenticationCredentials credentials = new BasicAuthenticationCredentials(authentication);

        assertEquals("user", credentials.getUserName());
    }

    @Test
    public void validNameAndPassword() {
        byte[] bytes = "user:password".getBytes();
        String authentication = Base64.getEncoder().encodeToString(bytes);
        BasicAuthenticationCredentials credentials = new BasicAuthenticationCredentials(authentication);

        assertEquals("user", credentials.getUserName());
    }

}
