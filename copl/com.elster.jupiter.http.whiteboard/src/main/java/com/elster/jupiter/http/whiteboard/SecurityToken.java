package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.http.whiteboard.impl.WhiteBoard;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.*;

public class SecurityToken {

    private static final String SHARED_SECRET = "{my greatest secret tokenaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa}";
    private final  static long MAX_COUNT = WhiteBoard.getTokenRefreshMaxCnt();
    private final static int TIMEOUT = WhiteBoard.getTimeout();
    private final static int TOKEN_EXPTIME = WhiteBoard.getTokenExpTime();
    private static long COUNT = 0;

    public SecurityToken() {
    }

    public static String createToken(User user) {

        // Create HMAC signer
        JWSSigner signer = new MACSigner(SHARED_SECRET.getBytes());

        List<Group> userGroups = user.getGroups();
        List<RoleClaimInfo> roles = new ArrayList<>();

        for (Group group : userGroups){
            roles.add(new RoleClaimInfo(group.getId(),group.getName()));
        }
        
        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setCustomClaim("username",user.getName());
        claimsSet.setSubject(Long.toString(user.getId()));
        claimsSet.setCustomClaim("roles",roles);
        claimsSet.setIssuer("Elster Connexo");
        claimsSet.setJWTID("token" + (++COUNT));
        claimsSet.setIssueTime(new Date());
        claimsSet.setExpirationTime(new Date(System.currentTimeMillis() + TOKEN_EXPTIME*1000));
        claimsSet.setCustomClaim("cnt", COUNT);

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        // Apply the HMAC protection
        try {
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            e.printStackTrace();
            return null;
        }
        // Serialize to compact form, produces something like
        return signedJWT.serialize();
    }


    public static Optional<User> verifyToken(String token, HttpServletRequest request, HttpServletResponse response, UserService userService) {
        try {
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
