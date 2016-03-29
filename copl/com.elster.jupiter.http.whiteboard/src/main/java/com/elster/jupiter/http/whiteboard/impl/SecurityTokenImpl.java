package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;
import com.nimbusds.jose.*;
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
import java.util.*;


public class SecurityTokenImpl {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final int tokenExpiration;
    private final int maxTokenCount;
    private final int timeOut;
    private final String USR_QUEUE_DEST = "UsrQueueDest";
    private volatile MessageService messageService;
    private volatile JsonService jsonService;


    public SecurityTokenImpl(byte[] publicKey, byte[] privateKey, int tokenExpiration, int maxTokenCount, int timeOut, MessageService messageService, JsonService jsonService) throws
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
        this.messageService = messageService;
        this.jsonService = jsonService;
    }

    public int getCookieMaxAge() {
        return tokenExpiration + timeOut;
    }

    public String createToken(User user, long count) {
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
            logMessage("Token renewal for user ", "["+user.getName()+"]");
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

    public TokenValidation verifyToken(String token, UserService userService) {
        Optional<User> user = Optional.empty();
        try {
            Optional<SignedJWT> signedJWT = checkTokenIntegrity(token);
            if (signedJWT.isPresent() && (Long) signedJWT.get()
                    .getJWTClaimsSet()
                    .getCustomClaim("cnt") < maxTokenCount) {
                long userId = Long.valueOf(signedJWT.get().getJWTClaimsSet().getSubject());
                user = userService.getLoggedInUser(userId);
                if (new Date().before(new Date(signedJWT.get().getJWTClaimsSet().getExpirationTime().getTime()))) {
                    return new TokenValidation(user.isPresent(), user.orElse(null), token);
                } else if (new Date().before(new Date(signedJWT.get()
                        .getJWTClaimsSet()
                        .getExpirationTime()
                        .getTime() + timeOut * 1000))) {
                    if (userService.getUser(userId).isPresent()) {
                        long count = (Long) signedJWT.get().getJWTClaimsSet().getCustomClaim("cnt");
                        String newToken = createToken(userService.getLoggedInUser(userId).get(), ++count);
                        return new TokenValidation(user.isPresent(), user.orElse(null), newToken);
                    } else {
                        if(user.isPresent()) {
                            logMessage("Token expired for user ", user.get().getName());
                        }
                        return new TokenValidation(false, null, null);
                    }
                }
            } else {
                if(user.isPresent()) {
                    logMessage("Token expired for user ", user.get().getName());
                }
                return new TokenValidation(false, null, null);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(user.isPresent()) {
            logMessage("Token expired for user ", user.get().getName());
        }
        return new TokenValidation(false, null, null);
    }

    public boolean compareTokens(String cookie, String token) {
        Optional<SignedJWT> jwtCookie = checkTokenIntegrity(cookie);
        Optional<SignedJWT> jwtToken = checkTokenIntegrity(token);

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
            e.printStackTrace();
        }

        return false;
    }

    private Optional<SignedJWT> checkTokenIntegrity(String token) {
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
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private void logMessage(String message, String userName){
        Map<String, String> messageProperties = new HashMap<>();
        messageProperties.put(message, userName);
        Optional<DestinationSpec> found = messageService.getDestinationSpec(USR_QUEUE_DEST);
        if(found.isPresent()){
            String json = jsonService.serialize(messageProperties);
            found.get().message(json).send();
        }
    }


}
