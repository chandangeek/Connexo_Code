/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.security.auth.Subject;
import javax.ws.rs.client.Client;
import java.security.Principal;
import java.util.Base64;

/**
 * Holder for username and password, delivers HttpAuthenticationFeature so actual credentials are not exposed
 */
public class SecurityEnvelope implements Principal {
    private final String encoded;

    public SecurityEnvelope(String credentials) {
        encoded=credentials;
    }

    @Override
    public String getName() {
        String decode = new String(Base64.getDecoder().decode(encoded));
        return decode.substring(0,decode.indexOf(":"));
    }

    private String getPassword() {
        String decode = new String(Base64.getDecoder().decode(encoded));
        return decode.substring(decode.indexOf(":")+1);
    }

    public void authenticate(Client client) {
        if (encoded!=null) {
            client.register(HttpAuthenticationFeature.basic(getName(), getPassword()));
        }
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}
