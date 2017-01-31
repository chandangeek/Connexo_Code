/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SecurityTokenImpl {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final int tokenExpiration;
    private final int maxTokenCount;
    private final int timeOut;
    private static final String TOKEN_INVALID = "Invalid token ";
    private static final String TOKEN_EXPIRED = "Token expired for user ";
    private static final String TOKEN_RENEWAL = "Token renewal for user ";
    private static final String TOKEN_GENERATED = "Token generated for user ";
    private static final String USER_NOT_FOUND = "User not found ";
    private static final String USER_DISABLED = "User account disabled ";
    private Logger tokenRenewal = Logger.getLogger("tokenRenewal");


    public SecurityTokenImpl(byte[] publicKey, byte[] privateKey, int tokenExpiration, int maxTokenCount, int timeOut) throws
            NoSuchAlgorithmException,
            InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(new String(privateKey)));
        this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(encodedKeySpec);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(new String(publicKey)));
        this.publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        this.tokenExpiration = tokenExpiration;
        this.maxTokenCount = maxTokenCount;
        this.timeOut = timeOut;
    }

    public int getCookieMaxAge() {
        return tokenExpiration + timeOut;
    }

    public String createToken(User user, long count, String ipAddr) {
        try {
            JWSSigner signer = new RSASSASigner(privateKey);

            List<Group> userGroups = user.getGroups();
            List<RoleClaimInfo> roles = new ArrayList<>();
            for (Group group : userGroups) {
                roles.add(new RoleClaimInfo(group.getId(), group.getName()));
            }

            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setCustomClaim("username", user.getName());
            claimsSet.setSubject(Long.toString(user.getId()));
            claimsSet.setCustomClaim("roles", roles);
            claimsSet.setIssuer("Elster Connexo");
            claimsSet.setCustomClaim("cnt", count);
            claimsSet.setJWTID("token" + count);
            claimsSet.setIssueTime(new Date());
            claimsSet.setExpirationTime(new Date(System.currentTimeMillis() + tokenExpiration * 1000));

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

            signedJWT.sign(signer);
            if(count > 0){
                logMessage(TOKEN_RENEWAL, "["+ user.getDomain() + "/" + user.getName()+"]", ipAddr);
            }else{
                logMessage(TOKEN_GENERATED, "["+ user.getDomain() + "/" + user.getName()+"]", ipAddr);
            }
            return signedJWT.serialize();


        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    class TokenValidation {
        private final boolean valid;
        private final User user;
        private final String token;

        TokenValidation(boolean valid, User user, String token) {
            this.valid = valid;
            this.user = user;
            this.token = token;
        }


        public boolean isValid() {
            return valid;
        }

        public Optional<User> getUser() {
            if (isValid()) {
                return Optional.ofNullable(user);
            } else {
                return Optional.empty();
            }
        }

        public String getToken() {
            if (valid) {
                return token;
            } else {
                return null;
            }
        }
    }

    public TokenValidation verifyToken(String token, UserService userService, String ipAddr) {
        Optional<User> user = Optional.empty();
        try {
            Optional<SignedJWT> signedJWT = checkTokenIntegrity(token, ipAddr);
            if (signedJWT.isPresent() && (Long) signedJWT.get()
                    .getJWTClaimsSet()
                    .getCustomClaim("cnt") < maxTokenCount) {
                long userId = Long.valueOf(signedJWT.get().getJWTClaimsSet().getSubject());
                user = userService.getLoggedInUser(userId);
                if (new Date().before(new Date(signedJWT.get().getJWTClaimsSet().getExpirationTime().getTime()))) {
                    if(!user.isPresent()) {
                        logMessage(USER_NOT_FOUND, "[id: " + userId + "]", ipAddr);
                    }
                    return new TokenValidation(user.isPresent(), user.orElse(null), token);
                } else if (new Date().before(new Date(signedJWT.get()
                        .getJWTClaimsSet()
                        .getExpirationTime()
                        .getTime() + timeOut * 1000))) {
                    if (userService.getUser(userId).isPresent()) {
                        if(userService.getUser(userId).get().getStatus()) {
                            long count = (Long) signedJWT.get().getJWTClaimsSet().getCustomClaim("cnt");
                            String newToken = createToken(userService.getLoggedInUser(userId).get(), ++count, ipAddr);
                            return new TokenValidation(user.isPresent(), user.orElse(null), newToken);
                        }else {
                            logMessage(USER_DISABLED, "[" + userService.getUser(userId).get().getDomain() + "/" + userService.getUser(userId).get().getName() + "]", ipAddr);
                        }
                    } else {
                        if(user.isPresent()) {
                            logMessage(USER_NOT_FOUND, "[name: " + user.get().getDomain() + "/" + user.get().getName() + "]", ipAddr);
                        } else {
                            logMessage(USER_NOT_FOUND, "[id: " + userId + "]", ipAddr);
                        }
                    }
                } else {
                    logMessage(TOKEN_EXPIRED, "[" + user.get().getDomain() + "/" + user.get().getName() + "]", ipAddr);
                }
            } else {
                logMessage(TOKEN_INVALID, "[" + token + "]", ipAddr);
            }

        } catch (ParseException e) {
            logMessage(TOKEN_INVALID, "[" + token + "]", ipAddr);
        }
        return new TokenValidation(false, null, null);
    }

    public boolean compareTokens(String cookie, String token, String ipAddr) {
        Optional<SignedJWT> jwtCookie = checkTokenIntegrity(cookie, ipAddr);
        Optional<SignedJWT> jwtToken = checkTokenIntegrity(token, ipAddr);

        try {
            if (jwtCookie.isPresent() && jwtToken.isPresent()) {
                if (new String(jwtCookie.get().getSigningInput(), StandardCharsets.UTF_8)
                        .equals(new String(jwtToken.get().getSigningInput(), StandardCharsets.UTF_8))) {
                    return true;
                } else if (jwtCookie.get().getJWTClaimsSet().getSubject()
                        .equals(jwtToken.get().getJWTClaimsSet().getSubject())
                        && (Long) jwtCookie.get().getJWTClaimsSet().getCustomClaim("cnt") >= (Long) jwtToken.get()
                        .getJWTClaimsSet().getCustomClaim("cnt")) {
                    return true;
                }
            } else {
                return false;
            }

        } catch (ParseException e) {
            logMessage(TOKEN_INVALID, "[" + token + "]", ipAddr);
        }
        logMessage(TOKEN_INVALID, "[" + token + "]", ipAddr);
        return false;
    }

    private Optional<SignedJWT> checkTokenIntegrity(String token, String ipAddr) {
        try {
            if (token != null && !("null".equals(token) || "undefined".equals(token)) && publicKey != null) {
                SignedJWT signedJWT = SignedJWT.parse(token);
                String issuer = signedJWT.getJWTClaimsSet().getIssuer();
                Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
                Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                long count = (Long) signedJWT.getJWTClaimsSet().getCustomClaim("cnt");
                long tokenNumericTermination = Long.parseLong(signedJWT.getJWTClaimsSet().getJWTID().split("[a-z]")[5]);
                JWSVerifier verifier = new RSASSAVerifier(publicKey);

                if (signedJWT.verify(verifier) && issuer.equals("Elster Connexo") &&
                        issueTime.before(expirationTime) && count == tokenNumericTermination) {
                    return Optional.of(signedJWT);
                }
            }
        } catch (ParseException | JOSEException e) {
            logMessage(TOKEN_INVALID, "[" + token + "]", ipAddr);
        }
        return Optional.empty();
    }

    private void logMessage(String message, String userName, String ipAddr){
        ipAddr = ipAddr.equals("0:0:0:0:0:0:0:1") ? "localhost" : ipAddr;
        if(message.equals(TOKEN_INVALID)){
            tokenRenewal.log(Level.WARNING, message + userName + " " , ipAddr);
        } else if(message.equals(TOKEN_GENERATED) || message.equals(TOKEN_EXPIRED) || message.equals(TOKEN_RENEWAL) || message.equals(USER_DISABLED) || message.equals(USER_NOT_FOUND)){
            tokenRenewal.log(Level.INFO, message + userName + " " , ipAddr);
        }
    }

}
