package com.elster.jupiter.http.whiteboard.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.Test;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by dragos on 7/16/2015.
 */
public class JOSETest {
    @Test
    public void testEncryptJWT() {
        String payload = "Hello world!";
        System.out.println("Payload: " + payload);

        // Create an HMAC-protected JWS object with some payload
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256),
                new Payload(payload));

        System.out.println("JWS object generated");

        // We need a 256-bit key for HS256 which must be pre-shared
        byte[] sharedKey = new byte[32];
        new SecureRandom().nextBytes(sharedKey);

        // Apply the HMAC to the JWS object
        try {
            jwsObject.sign(new MACSigner(sharedKey));
        } catch (JOSEException e) {
            e.printStackTrace();
        }

        System.out.println("JWS object signed");

        // Serialise to URL-safe format
        String ticket = jwsObject.serialize();

        System.out.println("JWS: " + ticket);

        JWSObject jwsReceivedObject;
        try {
            jwsReceivedObject = JWSObject.parse(ticket);

            System.out.println("JWS object processed");

            // continue with signature verification...
            if(jwsReceivedObject.verify(new MACVerifier(sharedKey))){
                System.out.println("JWS object verified");
                assertThat(jwsReceivedObject.getPayload().toString()).isEqualTo(payload);
            }
        } catch (java.text.ParseException e) {
            // Invalid JWS object encoding
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }


    }
}
