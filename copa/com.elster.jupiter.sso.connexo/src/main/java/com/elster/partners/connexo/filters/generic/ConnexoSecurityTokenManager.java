package com.elster.partners.connexo.filters.generic;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class ConnexoSecurityTokenManager {

    private static final String SHARED_SECRET = "{secret}";
    private final long MAX_COUNT;
    private final int TIMEOUT;
    private final int TOKEN_EXPTIME;
    private long COUNT = 0;

    private String token = null;
    private ConnexoPrincipal principal = null;
    private boolean tokenUpdated = false;

    private static ConnexoSecurityTokenManager instance = null;

    public static synchronized ConnexoSecurityTokenManager getInstance() {
        if(instance == null) {
            instance = new ConnexoSecurityTokenManager();
        }

        return instance;
    }

    private ConnexoSecurityTokenManager() {
        String maxCount = System.getProperty("com.elster.jupiter.token.refresh.maxcount");
        MAX_COUNT = (maxCount != null)? Long.parseLong(maxCount) : 100;

        String timeout = System.getProperty("com.elster.jupiter.timeout");
        TIMEOUT = (timeout != null) ? Integer.parseInt(timeout) : 300;

        String tokenExpTime = System.getProperty("com.elster.jupiter.token.expirationtime");
        TOKEN_EXPTIME = (tokenExpTime != null) ? Integer.parseInt(tokenExpTime) : 300;
    }

    public boolean verifyToken(String token) {
        try {
            this.tokenUpdated = false;
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SHARED_SECRET);
            String issuer = signedJWT.getJWTClaimsSet().getIssuer();
            Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            long count = (Long)signedJWT.getJWTClaimsSet().getCustomClaim("cnt");
            long tokenNumericTermination = Long.parseLong(signedJWT.getJWTClaimsSet().getJWTID().split("[a-z]")[5]);


            if (signedJWT.verify(verifier) && issuer.equals("Elster Connexo") &&
                    issueTime.before(expirationTime) && count == tokenNumericTermination) {
                if (count < MAX_COUNT) {
                    constructPrincipal(signedJWT);
                    if (new Date().before(new Date(expirationTime.getTime())) && this.principal != null) {
                        return true;
                    } else if (new Date().before(new Date(expirationTime.getTime() + TIMEOUT*1000))) {
                        ++COUNT;
                        return createToken();
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
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
            String user = signedJWT.getJWTClaimsSet().getSubject();
            List<String> groups = signedJWT.getJWTClaimsSet().getStringListClaim("Roles");
            this.principal = new ConnexoPrincipal(userId, user, groups);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean createToken() {
        if(this.principal != null) {
            JWSSigner signer = new MACSigner(SHARED_SECRET.getBytes());

            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setSubject(Long.toString(this.principal.getUserId()));
            claimsSet.setIssuer("Elster Connexo");
            claimsSet.setJWTID("token" + COUNT);
            claimsSet.setIssueTime(new Date());
            claimsSet.setExpirationTime(new Date(System.currentTimeMillis() + TOKEN_EXPTIME * 1000));
            claimsSet.setCustomClaim("cnt", COUNT);

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

            try {
                signedJWT.sign(signer);
            } catch (JOSEException e) {
                e.printStackTrace();
                return false;
            }

            this.tokenUpdated = true;
            this.token = signedJWT.serialize();
            return true;
        }

        return false;
    }
}
