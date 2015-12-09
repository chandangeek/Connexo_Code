package com.elster.partners.connexo.filters.generic;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class ConnexoSecurityTokenManager {

    private final long MAX_COUNT;
    private final int TIMEOUT;
    private final int TOKEN_EXPTIME;

    private String token = null;
    private ConnexoPrincipal principal = null;
    private boolean tokenUpdated = false;

    private static ConnexoSecurityTokenManager instance = null;

    public static synchronized ConnexoSecurityTokenManager getInstance(Properties properties) {
        if(instance == null) {
            instance = new ConnexoSecurityTokenManager(properties);
        }

        return instance;
    }

    private ConnexoSecurityTokenManager(Properties properties) {
        String maxCount = properties.getProperty("com.elster.jupiter.token.refresh.maxcount");
        MAX_COUNT = (maxCount != null)? Long.parseLong(maxCount) : 100;

        String timeout = properties.getProperty("com.elster.jupiter.timeout");
        TIMEOUT = (timeout != null) ? Integer.parseInt(timeout) : 300;

        String tokenExpTime = properties.getProperty("com.elster.jupiter.token.expirationtime");
        TOKEN_EXPTIME = (tokenExpTime != null) ? Integer.parseInt(tokenExpTime) : 300;
    }

    public boolean verifyToken(String token) {
        try {
            this.tokenUpdated = false;
            SignedJWT signedJWT = SignedJWT.parse(token);
            String publicKey = String.valueOf(signedJWT.getJWTClaimsSet().getCustomClaim("publicKey"));
            String issuer = signedJWT.getJWTClaimsSet().getIssuer();
            Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            long count = (Long)signedJWT.getJWTClaimsSet().getCustomClaim("cnt");
            long tokenNumericTermination = Long.parseLong(signedJWT.getJWTClaimsSet().getJWTID().split("[a-z]")[5]);
            BigInteger modulus = new BigInteger(publicKey.split(" ")[0]);
            BigInteger exp = new BigInteger(publicKey.split(" ")[1]);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus,exp);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

            JWSVerifier verifier = new RSASSAVerifier(pubKey);


            if (signedJWT.verify(verifier) && issuer.equals("Elster Connexo") &&
                    issueTime.before(expirationTime) && count == tokenNumericTermination) {
                if (count < MAX_COUNT) {
                    constructPrincipal(signedJWT);
                    if (new Date().before(new Date(expirationTime.getTime())) && this.principal != null) {
                        return true;
                    } else if (new Date().before(new Date(expirationTime.getTime() + TIMEOUT*1000))) {
                        return createToken(signedJWT.getJWTClaimsSet());
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean needToUpdateToken(){
        return this.tokenUpdated;
    }

    public String getUpdatedToken(){
        return this.token;
    }

    public ConnexoPrincipal getPrincipal(){
        return this.principal;
    }

    public int getMaxAge(){
        return TOKEN_EXPTIME+TIMEOUT;
    }

    private void constructPrincipal(SignedJWT signedJWT) {
        try {
            this.principal = null;

            long userId = Long.valueOf(signedJWT.getJWTClaimsSet().getSubject());
            String user = signedJWT.getJWTClaimsSet().getStringClaim("username");

            List<String> groups = new ArrayList<>();
            JSONArray roles = (JSONArray) signedJWT.getJWTClaimsSet().getClaim("roles");
            if(roles != null) {
                for (int i = 0; i < roles.size(); i++) {
                    JSONObject role = (JSONObject) roles.get(i);
                    if(role != null) {
                        groups.add((String) role.get("name"));
                    }
                }
            }

            this.principal = new ConnexoPrincipal(userId, user, groups);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean createToken(ReadOnlyJWTClaimsSet claimsSet) {
        if(this.principal != null) {
            long count = (Long) claimsSet.getCustomClaim("cnt");

            try {
                KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
                SecureRandom random = new SecureRandom();
                keyGenerator.initialize(1024, random);
                KeyPair keyPair = keyGenerator.genKeyPair();
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                JWSSigner signer = new RSASSASigner(privateKey);

                String pk =  publicKey.getModulus().toString() + " " +
                        publicKey.getPublicExponent().toString();

                JWTClaimsSet newClaimsSet = new JWTClaimsSet(claimsSet);
                newClaimsSet.setIssueTime(new Date());
                newClaimsSet.setExpirationTime(new Date(System.currentTimeMillis() + TOKEN_EXPTIME * 1000));
                newClaimsSet.setCustomClaim("cnt", count + 1);
                newClaimsSet.setCustomClaim("publicKey", pk);

                SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), newClaimsSet);

                signedJWT.sign(signer);
                this.tokenUpdated = true;
                this.token = signedJWT.serialize();
                return true;

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (JOSEException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
