package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.http.whiteboard.impl.WhiteBoard;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.util.*;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.security.KeyPair;
import java.security.KeyPairGenerator;


public class SecurityToken {

    private final  static long MAX_COUNT = WhiteBoard.getTokenRefreshMaxCnt();
    private final static int TIMEOUT = WhiteBoard.getTimeout();
    private final static int TOKEN_EXPTIME = WhiteBoard.getTokenExpTime();
    private static long COUNT = 0;

    public SecurityToken() {
    }

    public static String createToken(User user) {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(1024);
            KeyPair keyPair = keyGenerator.genKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            JWSSigner signer = new RSASSASigner(privateKey);

            List<Group> userGroups = user.getGroups();
            List<RoleClaimInfo> roles = new ArrayList<>();

            for (Group group : userGroups) {
                roles.add(new RoleClaimInfo(group.getId(), group.getName()));
            }

           String pk =  publicKey.getModulus().toString() + " " +
                    publicKey.getPublicExponent().toString();
            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setCustomClaim("username", user.getName());
            claimsSet.setSubject(Long.toString(user.getId()));
            claimsSet.setCustomClaim("roles", roles);
            claimsSet.setIssuer("Elster Connexo");
            claimsSet.setJWTID("token" + (++COUNT));
            claimsSet.setIssueTime(new Date());
            claimsSet.setExpirationTime(new Date(System.currentTimeMillis() + TOKEN_EXPTIME * 1000));
            claimsSet.setCustomClaim("cnt", COUNT);
            claimsSet.setCustomClaim("publicKey", pk);

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (JOSEException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Optional<User> verifyToken(String token, HttpServletRequest request, HttpServletResponse response, UserService userService) {
        try {

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
                    long userId = Long.valueOf(signedJWT.getJWTClaimsSet().getSubject());
                    if (new Date().before(new Date(expirationTime.getTime()))) {
                        return userService.getLoggedInUser(userId);
                    } else if (new Date().before(new Date(expirationTime.getTime() + TIMEOUT*1000))) {
                        String newToken = createToken(userService.getLoggedInUser(userId).get());
                        response.setHeader("X-AUTH-TOKEN", newToken);
                        response.setHeader("Authorization", "Bearer " + newToken);
                        createCookie("X-CONNEXO-TOKEN",newToken,"/",TOKEN_EXPTIME+TIMEOUT,true,response);
                        return userService.getLoggedInUser(userId);
                    }
                }
            }
            removeCookie(request, response);
            invalidateSession(request);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }


    public static void removeCookie(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> tokenCookie = Arrays.asList(request.getCookies()).stream().filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (tokenCookie.isPresent()) {
            response.setHeader("Authorization", null);
            response.setHeader("X-AUTH-TOKEN", null);
            createCookie("X-CONNEXO-TOKEN", null, "/", 0, true, response);
        }
    }
    public static void invalidateSession(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        resetCount();
    }

    public static void createCookie(String cookieName, String cookieValue, String cookiePath, int maxAge, boolean isHTTPOnly, HttpServletResponse response ){
        Cookie tokenCookie = new Cookie(cookieName, cookieValue);
        tokenCookie.setPath(cookiePath);
        tokenCookie.setMaxAge(maxAge); //seconds
        tokenCookie.setHttpOnly(isHTTPOnly);
        response.addCookie(tokenCookie);
    }

    private static void resetCount(){
        COUNT=0;
    }
}
