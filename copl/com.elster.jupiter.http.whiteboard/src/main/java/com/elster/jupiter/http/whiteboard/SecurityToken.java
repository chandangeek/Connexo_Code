package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.http.whiteboard.impl.WhiteBoardImpl;
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
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.security.NoSuchAlgorithmException;
import java.util.Date;



public class SecurityToken {

    private final long MAX_COUNT = WhiteBoardImpl.getTokenRefreshMaxCount();
    private final int TIMEOUT = WhiteBoardImpl.getTimeout();
    private final int TOKEN_EXPTIME = WhiteBoardImpl.getTokenExpTime();
    private final String PUBLIC_KEY = WhiteBoardImpl.getPublicKey();
    private final String PRIVATE_KEY = WhiteBoardImpl.getPrivateKey();

    private static SecurityToken instance = new SecurityToken();


    public static synchronized SecurityToken getInstance(){
        return instance;
    }

    private SecurityToken() {
    }

    public Optional<String> createToken(HttpServletRequest request, HttpServletResponse response, User user, long count) {
        Optional<String> token = Optional.empty();
        try {
            Optional<RSAKey> rsaKey = getRSAKey(PRIVATE_KEY, "PRV");
            if(rsaKey.isPresent()){
                RSAPrivateKey privateKey = (RSAPrivateKey)rsaKey.get();
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
                claimsSet.setExpirationTime(new Date(System.currentTimeMillis() + TOKEN_EXPTIME * 1000));

                SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

                signedJWT.sign(signer);
                token = Optional.of(signedJWT.serialize());

                response.setHeader("X-AUTH-TOKEN", token.get());
                response.setHeader("Authorization", "Bearer " + token.get());
                createCookie("X-CONNEXO-TOKEN",token.get(),"/",response);
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return token;

    }

    public Optional<User> verifyToken(String token, HttpServletRequest request, HttpServletResponse response, UserService userService) {
        try {
            Optional<SignedJWT> signedJWT = checkTokenIntegrity(token);
                if (signedJWT.isPresent() && (Long)signedJWT.get().getJWTClaimsSet().getCustomClaim("cnt") < MAX_COUNT) {
                    long userId = Long.valueOf(signedJWT.get().getJWTClaimsSet().getSubject());
                    Optional<User> user = userService.getLoggedInUser(userId);
                    if (new Date().before(new Date(signedJWT.get().getJWTClaimsSet().getExpirationTime().getTime()))) {
                        response.setHeader("X-AUTH-TOKEN", token);
                        response.setHeader("Authorization", "Bearer " + token);
                        return user;
                    } else if (new Date().before(new Date(signedJWT.get().getJWTClaimsSet().getExpirationTime().getTime() + TIMEOUT*1000))) {
                        if(!userService.findUser(user.get().getName(), user.get().getDomain()).isPresent()) return Optional.empty();
                        long count = (Long) signedJWT.get().getJWTClaimsSet().getCustomClaim("cnt");
                        Optional<String> newToken = createToken(request, response, userService.getLoggedInUser(userId).get(),++count);
                        if(newToken.isPresent()){
                            response.setHeader("X-AUTH-TOKEN", newToken.get());
                            response.setHeader("Authorization", "Bearer " + newToken.get());
                            createCookie("X-CONNEXO-TOKEN",newToken.get(),"/",response);
                            return user;
                        }
                    }
                }
            removeCookie(request, response);
            invalidateSession(request);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public boolean doComparison(Cookie cookie, String token){
        Optional<SignedJWT> jwtCookie = checkTokenIntegrity(cookie.getValue());
        Optional<SignedJWT> jwtToken = checkTokenIntegrity(token);

        try {
            if(jwtCookie.isPresent() && jwtToken.isPresent()){
                if(new String(jwtCookie.get().getSigningInput(), StandardCharsets.UTF_8).equals(new String(jwtToken.get().getSigningInput(),StandardCharsets.UTF_8))) return true;
                else if(jwtCookie.get().getJWTClaimsSet().getSubject().equals(jwtToken.get().getJWTClaimsSet().getSubject())
                        && (jwtCookie.get().getJWTClaimsSet().getIssueTime().after(jwtToken.get().getJWTClaimsSet().getIssueTime()) || jwtCookie.get().getJWTClaimsSet().getIssueTime().equals(jwtToken.get().getJWTClaimsSet().getIssueTime()))
                        && (jwtCookie.get().getJWTClaimsSet().getExpirationTime().after(jwtToken.get().getJWTClaimsSet().getExpirationTime()) || jwtCookie.get().getJWTClaimsSet().getExpirationTime().equals(jwtToken.get().getJWTClaimsSet().getExpirationTime()))
                        && (Long)jwtCookie.get().getJWTClaimsSet().getCustomClaim("cnt") >= (Long)jwtToken.get().getJWTClaimsSet().getCustomClaim("cnt")) return true;
            }else{
                return false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    private Optional<SignedJWT> checkTokenIntegrity(String token) {
        try {
            Optional<RSAKey> rsaKey = getRSAKey(PUBLIC_KEY, "PUB");
            if (token != null && !(token.equals("null") || token.equals("undefined"))
                    && rsaKey.isPresent()) {

                SignedJWT signedJWT = SignedJWT.parse(token);
                String issuer = signedJWT.getJWTClaimsSet().getIssuer();
                Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
                Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                long count = (Long) signedJWT.getJWTClaimsSet().getCustomClaim("cnt");
                long tokenNumericTermination = Long.parseLong(signedJWT.getJWTClaimsSet().getJWTID().split("[a-z]")[5]);
                JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) rsaKey.get());

                if (signedJWT.verify(verifier) && issuer.equals("Elster Connexo") &&
                        issueTime.before(expirationTime) && count == tokenNumericTermination) {
                    return Optional.of(signedJWT);
                }
            }
        }
        catch (ParseException e) {
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

    public void removeCookie(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> tokenCookie = Arrays.asList(request.getCookies()).stream().filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (tokenCookie.isPresent()) {
            StringBuilder cookie = new StringBuilder(tokenCookie.get().getName() + "=" + null + "; ");
            cookie.append("Path=/; ");
            cookie.append("Expires=Thu, 01 Jan 1970 00:00:01 GMT; ");
            cookie.append("Max-Age=" + 0 + "; ");
            cookie.append("HttpOnly");
            response.setHeader("Set-Cookie", cookie.toString());
        }
    }
    public void invalidateSession(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    public void createCookie(String cookieName, String cookieValue, String cookiePath, HttpServletResponse response ){
        DateFormat dateFormatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss 'GMT'", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, TIMEOUT+TOKEN_EXPTIME);
        StringBuilder cookie = new StringBuilder(cookieName + "=" + cookieValue + "; ");
        cookie.append("Path="+cookiePath + "; ");
        cookie.append("Expires=" + dateFormatter.format(calendar.getTime()) + "; ");
        cookie.append("Max-Age=" + (TIMEOUT+TOKEN_EXPTIME) + "; ");
        cookie.append("HttpOnly");
        response.setHeader("Set-Cookie", cookie.toString());
    }

    private Optional<RSAKey> getRSAKey(String key, String keyType) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Optional<RSAKey> rsaKey = Optional.empty();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        if(key!=null && !key.isEmpty() && keyType.equals("PRV")) {
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(key));
            rsaKey = Optional.of((RSAPrivateKey)keyFactory.generatePrivate(encodedKeySpec));

        }else if(key!=null && !key.isEmpty() && keyType.equals("PUB")){
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(key));
            rsaKey = Optional.of((RSAPublicKey )keyFactory.generatePublic(keySpec));
        }
        return rsaKey;

    }
}
