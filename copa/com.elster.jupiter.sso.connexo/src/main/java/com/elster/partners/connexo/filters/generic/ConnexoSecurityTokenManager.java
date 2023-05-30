package com.elster.partners.connexo.filters.generic;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;


import javax.xml.bind.DatatypeConverter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConnexoSecurityTokenManager {

    private final long MAX_COUNT;
    private final int TIMEOUT;
    private final int TOKEN_EXPTIME;
    private final String PUBLIC_KEY;

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
        TOKEN_EXPTIME = (tokenExpTime != null) ? Integer.parseInt(tokenExpTime) : 1800;

        String publicKey = System.getProperty("com.elster.jupiter.sso.public.key");
        PUBLIC_KEY = (publicKey != null) ? publicKey : "";
    }

    public ConnexoPrincipal verifyToken(String token, boolean allowTokenRefresh) {
        if(token != null) {
            try {
                RSAKey rsaKey = getRSAKey(PUBLIC_KEY);
                if (rsaKey != null) {
                    SignedJWT signedJWT = SignedJWT.parse(token);
                    String issuer = signedJWT.getJWTClaimsSet().getIssuer();
                    Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
                    Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                    long count = (Long) signedJWT.getJWTClaimsSet().getClaim("cnt");
                    //long tokenNumericTermination = Long.parseLong(signedJWT.getJWTClaimsSet().getJWTID().split("[a-z]")[5]);

                    JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) rsaKey);


                    if (signedJWT.verify(verifier) && issuer.equals("Elster Connexo") &&
                            issueTime.before(expirationTime) && count < MAX_COUNT) {// && count == tokenNumericTermination
                        if (new Date().before(new Date(expirationTime.getTime()))) {
                            return constructPrincipal(signedJWT, token);
                        } else if (new Date().before(new Date(expirationTime.getTime() + TIMEOUT * 1000))) {
                            if(allowTokenRefresh) {
                                return constructPrincipal(signedJWT, updateToken(token));
                            }
                            else{
                                return constructPrincipal(signedJWT, token);
                            }
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
        }

        return null;
    }

    private String updateToken(String token) {
        ConnexoRestProxyManager restManager = ConnexoRestProxyManager.getInstance();
        String newToken = restManager.getConnexoAuthorizationToken("Bearer " + token);
        if(newToken != null && !newToken.isEmpty()) {
            return newToken;
        }

        return null;
    }

    public int getMaxAge(){
        return TOKEN_EXPTIME+TIMEOUT;
    }

    private ConnexoPrincipal constructPrincipal(SignedJWT signedJWT, String token) {
        ConnexoPrincipal principal = null;

        if(token != null) {
            try {
                long userId = Long.valueOf(signedJWT.getJWTClaimsSet().getSubject());
                String user = signedJWT.getJWTClaimsSet().getStringClaim("username");
                String email = signedJWT.getJWTClaimsSet().getStringClaim("email");
                List<String> groups = new ArrayList<>();
                ArrayList roles = (ArrayList) signedJWT.getJWTClaimsSet().getClaim("roles");
                if(null == email){
                    email = "NA";
                }
                if (roles != null) {
                    for (int i = 0; i < roles.size(); i++) {
                        AbstractMap role = (AbstractMap) roles.get(i);
                        if (role != null) {
                            groups.add((String) role.get("name"));
                        }
                    }
                }
                List<String> privileges =  new ArrayList<>();
                ArrayList privilegesObj = (ArrayList) signedJWT.getJWTClaimsSet().getClaim("privileges");
                if (privilegesObj != null) {
                    for (int i = 0; i < privilegesObj.size(); i++) {
                        String privilege =  privilegesObj.get(i).toString();
                        if (privilege != null) {
                            privileges.add(privilege);
                        }
                    }
                }

                principal = new ConnexoPrincipal(userId, user, groups, token, privileges, email);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return principal;
    }

    private RSAKey getRSAKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAKey rsaKey = null;
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        if(key != null && !key.isEmpty()){
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(key));
            rsaKey = (RSAPublicKey )keyFactory.generatePublic(keySpec);
        }
        return rsaKey;
    }
}
