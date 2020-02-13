package com.elster.jupiter.http.whiteboard.impl.token;

import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.http.whiteboard.impl.RoleClaimInfo;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.xml.bind.DatatypeConverter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component(
        name = "com.elster.jupiter.http.whiteboard.token.InMemoryCacheBasedTokenService",
        property = {
                "name=" + InMemoryCacheBasedTokenService.SERVICE_NAME
        },
        immediate = true,
        service = {
                TokenService.class
        }
)
public class InMemoryCacheBasedTokenService implements TokenService<UserJWT> {

    public static final String SERVICE_NAME = "TNS";

    private static final String ISSUER_NAME = "Elster Connexo";

    private volatile UserService userService;

    private volatile Cache<UUID, UserJWT> CASHE;

    private KeyFactory keyFactory;

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    private long TIMEOUT;
    private long TOKEN_REFRESH_MAX_COUNT;
    private long TOKEN_EXPIRATION_TIME;

    @Activate
    public void activate() throws NoSuchAlgorithmException {
        keyFactory = KeyFactory.getInstance("RSA");
    }

    @Override
    public void initialize(byte[] publicKey, byte[] privateKey, long tokenExpirationTime, long tokenRefreshThershold, long timeout) {
        this.publicKey = extractPublicKey(publicKey);
        this.privateKey = extractPrivateKey(privateKey);
        this.TOKEN_EXPIRATION_TIME = tokenExpirationTime;
        this.TOKEN_REFRESH_MAX_COUNT = tokenRefreshThershold;
        this.TIMEOUT = timeout;

        CASHE = CacheBuilder.newBuilder()
                .expireAfterWrite(TOKEN_EXPIRATION_TIME * 1000, TimeUnit.MILLISECONDS)
                .maximumSize(100000)
                .build();
    }

    private RSAPublicKey extractPublicKey(final byte[] publicKey) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(new String(publicKey)));
        try {
            return (RSAPublicKey) keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    private RSAPrivateKey extractPrivateKey(final byte[] privateKey) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(new String(privateKey)));
        try {
            return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserJWT createUserJWT(User user, Map<String, Object> customClaims) throws JOSEException {
        final UUID jwtId = UUID.randomUUID();
        final long tokenExpirationTime = System.currentTimeMillis() + TOKEN_EXPIRATION_TIME * 1000;

        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setJWTID(jwtId.toString());
        claimsSet.setSubject(Long.toString(user.getId()));
        claimsSet.setIssuer(ISSUER_NAME);
        claimsSet.setIssueTime(new Date());
        claimsSet.setExpirationTime(new Date(tokenExpirationTime));
        claimsSet.setCustomClaims(customClaims);

        final UserJWT userJWT = new UserJWT(
                user,
                createAndSignSignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet),
                Instant.ofEpochMilli(tokenExpirationTime)
        );

        CASHE.put(jwtId, userJWT);

        return userJWT;
    }

    private SignedJWT createAndSignSignedJWT(final JWSHeader jwsHeader, final JWTClaimsSet jwtClaimsSet) throws JOSEException {
        final SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
        JWSSigner jwsSigner = new RSASSASigner(privateKey);
        signedJWT.sign(jwsSigner);
        return signedJWT;
    }


    @Override
    public UserJWT getUserJWT(final UUID jwtId) {
        return CASHE.getIfPresent(jwtId);
    }

    @Override
    public TokenValidation validateSignedJWT(final SignedJWT signedJWT) throws JOSEException, ParseException {
        final ReadOnlyJWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();

        final User user = getAssociatedUser(Long.parseLong(jwtClaimsSet.getSubject()));

        final boolean result = Objects.nonNull(user)
                && verifyIfUserHasUsetJWTInCache(user, UUID.fromString(jwtClaimsSet.getJWTID()))
                && verifyJWTSignature(signedJWT)
                && verifyJWTIssuer(jwtClaimsSet.getIssuer())
                && verifyJWTExpirationTime(jwtClaimsSet.getExpirationTime());

        return new TokenValidation(result, user, signedJWT.serialize());
    }

    private boolean verifyIfUserHasUsetJWTInCache(final User user, final UUID jwtId) {
        final UserJWT userJWT = getUserJWT(jwtId);

        if (userJWT == null) {
            return false;
        }

        final User userJWTUser = userJWT.getUser();

        return user.equals(userJWTUser);
    }

    private User getAssociatedUser(final long userId) {
        return userService.getLoggedInUser(userId).orElse(null);
    }

    private boolean verifyJWTIssuer(final String issuer) {
        return ISSUER_NAME.equals(issuer);
    }

    private boolean verifyJWTExpirationTime(final Date expirationTime) {
        return new Date().before(expirationTime);
    }

    private boolean verifyJWTSignature(final SignedJWT signedJWT) throws JOSEException {
        JWSVerifier jwsVerifier = new RSASSAVerifier(publicKey);
        return signedJWT.verify(jwsVerifier);
    }

    @Override
    public void invalidateUserJWT(final UUID jwtId) {
        CASHE.invalidate(jwtId);
    }

    @Override
    public void invalidateAllUserJWTsForUser(final User user) {
        final List<UUID> userJWTS = CASHE.asMap().entrySet().stream()
                .filter(uuidUserJWTEntry -> filterUserJWTEntryByUser(uuidUserJWTEntry, user))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        CASHE.invalidateAll(userJWTS);
    }

    private boolean filterUserJWTEntryByUser(final Map.Entry<UUID, UserJWT> userJWTEntry, final User user) {
        return userJWTEntry.getValue().getUser().equals(user);
    }

    public Map<String, Object> createCustomClaimsForUser(final User user, long count) {
        List<Group> userGroups = user.getGroups();
        List<RoleClaimInfo> roles = new ArrayList<>();
        List<String> privileges = new ArrayList<>();

        userGroups.forEach(group -> {
            group.getPrivileges().forEach((key, value) -> {
                if (key.equals("BPM") || key.equals("YFN"))
                    value.forEach(p -> privileges.add(p.getName()));
            });

            privileges.add("privilege.public.api.rest");
            privileges.add("privilege.pulse.public.api.rest");
            privileges.add("privilege.view.userAndRole");

            roles.add(new RoleClaimInfo(group.getId(), group.getName()));
        });

        final HashMap<String, Object> result = new HashMap<>();
        result.put("username", user.getName());
        result.put("roles", roles);
        result.put("privileges", privileges);
        result.put("cnt", count);
        return result;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
