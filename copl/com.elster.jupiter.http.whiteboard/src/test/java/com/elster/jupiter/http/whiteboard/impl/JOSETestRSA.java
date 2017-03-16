/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.*;
import java.text.ParseException;
import java.util.Date;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.junit.Test;
import static org.junit.Assert.*;

public class JOSETestRSA {

    @Test

    public void testEncryptJWTRSA() {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

            SecureRandom random = new SecureRandom();
            keyGenerator.initialize(1024,random);
            KeyPair keyPair = keyGenerator.genKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            JWSSigner signer = new RSASSASigner(privateKey);
            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setSubject("admin");
            claimsSet.setIssuer("Connexo");
            claimsSet.setIssueTime(new Date());
            claimsSet.setExpirationTime(new Date(System.currentTimeMillis()*1000 + 60000));

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256),claimsSet);

            //RSA signature
            signedJWT.sign(signer);

            // serialize
            String s = signedJWT.serialize();

            //parse the JWS and verify its RSA signature
            signedJWT = SignedJWT.parse(s);

            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            assertTrue(signedJWT.verify(verifier));

            // Verify claims
            assertEquals("admin", signedJWT.getJWTClaimsSet().getSubject());
            assertEquals("Connexo", signedJWT.getJWTClaimsSet().getIssuer());
            assertTrue(new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime()));
            assertTrue(signedJWT.getJWTClaimsSet().getIssueTime().before(signedJWT.getJWTClaimsSet().getExpirationTime()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }
    }

}
