package com.elster.jupiter.http.whiteboard.impl.token;

import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.http.whiteboard.TokenValidation;
import com.elster.jupiter.http.whiteboard.UserJWT;
import com.elster.jupiter.http.whiteboard.impl.RoleClaimInfo;
import com.elster.jupiter.http.whiteboard.impl.TableSpecs;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.blacklist.BlackListTokenService;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Where;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
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

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
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
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


@Component(
        name = "com.elster.jupiter.http.whiteboard.token.InMemoryCacheBasedTokenService",
        property = {
                "name=" + DatabaseBasedTokenService.SERVICE_NAME
        },
        immediate = true,
        service = {
                TokenService.class
        }
)
public class DatabaseBasedTokenService implements TokenService<UserJWT> {

    public static final String SERVICE_NAME = "TNS";

    private static final String ISSUER_NAME = "Elster Connexo";

    private volatile UserService userService;

    private volatile BlackListTokenService blackListTokenService;

    private volatile TransactionService transactionService;

    private volatile UpgradeService upgradeService;

    private volatile Thesaurus thesaurus;

    private volatile NlsService nlsService;

    private volatile DataModel dataModel;

    private Timer expiredTokensCleaningTimer = new Timer("JWT Token Expiry");

    private KeyFactory keyFactory;

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    private long TOKEN_REFRESH_MAX_COUNT;

    // All the variables below are representing milliseconds
    private long TIMEOUT_FRAME_TO_REFRESH_TOKEN;
    private long TOKEN_EXPIRATION_TIME;
    private long EXPIRED_TOKEN_TASK_EXECUTION_PERIOD = 30 * 60 * 1000; // 30 minutes

    public DatabaseBasedTokenService() {
    }

    @Inject
    public DatabaseBasedTokenService(UserService userService,
                                     BlackListTokenService blackListTokenService,
                                     TransactionService transactionService,
                                     UpgradeService upgradeService,
                                     OrmService ormService,
                                     NlsService nlsService) {
        setUserService(userService);
        setBlackListTokenService(blackListTokenService);
        setTransactionService(transactionService);
        setUpgradeService(upgradeService);
        setOrmService(ormService);
        setNlsService(nlsService);
        activate();
    }

    @Activate
    public void activate() {
        TableSpecs.HTW_JWTSTORE.addTo(dataModel);

        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(NlsService.class).toInstance(nlsService);
                bind(UserService.class).toInstance(userService);
                bind(BlackListTokenService.class).toInstance(blackListTokenService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(TransactionService.class).toInstance(transactionService);
                bind(UpgradeService.class).toInstance(upgradeService);
                bind(TokenService.class).to(DatabaseBasedTokenService.class).in(Scopes.SINGLETON);
            }
        });

        this.upgradeService.register(
                InstallIdentifier.identifier("Pulse", DatabaseBasedTokenService.SERVICE_NAME),
                dataModel,
                Installer.class,
                ImmutableMap.of()
        );

        try {
            this.keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        this.expiredTokensCleaningTimer.scheduleAtFixedRate(new ExpiredTokensCleaningTaks(), 0, EXPIRED_TOKEN_TASK_EXECUTION_PERIOD);
    }

    @Override
    public void initialize(byte[] publicKey, byte[] privateKey, long tokenExpirationTime, long tokenRefreshThershold, long timeoutFrameToRefreshToken) {
        this.publicKey = extractPublicKey(publicKey);
        this.privateKey = extractPrivateKey(privateKey);
        this.TOKEN_EXPIRATION_TIME = tokenExpirationTime * 1000; // Converting to millis
        this.TOKEN_REFRESH_MAX_COUNT = tokenRefreshThershold;
        this.TIMEOUT_FRAME_TO_REFRESH_TOKEN = timeoutFrameToRefreshToken * 1000; // Converting to millis
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
        final long tokenExpirationTime = System.currentTimeMillis() + TOKEN_EXPIRATION_TIME;

        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setJWTID(jwtId.toString());
        claimsSet.setSubject(Long.toString(user.getId()));
        claimsSet.setIssuer(ISSUER_NAME);
        claimsSet.setIssueTime(new Date());
        claimsSet.setExpirationTime(new Date(tokenExpirationTime));
        claimsSet.setCustomClaims(customClaims);

        final SignedJWT signedJWT = createAndSignSignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

        final UserJWT userJWT = dataModel.getInstance(UserJWT.class)
                .init(jwtId.toString(), BigDecimal.valueOf(user.getId()), signedJWT.serialize(), Instant.ofEpochMilli(tokenExpirationTime));

        userJWT.save();

        return userJWT;
    }

    @Override
    public SignedJWT createServiceSignedJWT(User user, long expiresIn, String subject, String issuer, Map<String, Object> customClaims) throws JOSEException, ParseException {
        final UUID jwtId = UUID.randomUUID();
        final Map<String, Object> userSpecificClaims = createUserSpecificClaims(user, 0);
        userSpecificClaims.putAll(customClaims);

        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setJWTID(jwtId.toString());
        claimsSet.setSubject(String.valueOf(subject.hashCode()));
        claimsSet.setIssuer(issuer);
        claimsSet.setIssueTime(new Date());
        claimsSet.setExpirationTime(new Date(expiresIn));
        claimsSet.setCustomClaims(userSpecificClaims);

        final SignedJWT signedJWT = createAndSignSignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

        final UserJWT userJWT = dataModel.getInstance(UserJWT.class)
                .init(jwtId.toString(), BigDecimal.valueOf(user.getId()), signedJWT.serialize(), Instant.ofEpochMilli(expiresIn));

        userJWT.save();

        return SignedJWT.parse(userJWT.getToken());
    }

    @Override
    public SignedJWT createPermamentSignedJWT(User user) throws JOSEException {
        final UUID jwtId = UUID.randomUUID();

        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setJWTID(jwtId.toString());
        claimsSet.setSubject(Long.toString(user.getId()));
        claimsSet.setIssuer(ISSUER_NAME);
        claimsSet.setIssueTime(new Date());
        claimsSet.setExpirationTime(new Date(Long.MAX_VALUE));
        claimsSet.setCustomClaims(createUserSpecificClaims(user, 0));

        final SignedJWT signedJWT = createAndSignSignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

        final UserJWT userJWT = dataModel.getInstance(UserJWT.class)
                .init(jwtId.toString(), BigDecimal.valueOf(user.getId()), signedJWT.serialize(), Instant.ofEpochMilli(Long.MAX_VALUE));

        userJWT.save();

        return signedJWT;
    }

    private SignedJWT createAndSignSignedJWT(final JWSHeader jwsHeader, final JWTClaimsSet jwtClaimsSet) throws JOSEException {
        final SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
        JWSSigner jwsSigner = new RSASSASigner(privateKey);
        signedJWT.sign(jwsSigner);
        return signedJWT;
    }


    @Override
    public Optional<UserJWT> getUserJWT(final UUID jwtId) {
        return dataModel.query(UserJWT.class)
                .select(Operator.EQUAL.compare("jwtId", jwtId.toString()))
                .stream()
                .findFirst();
    }

    @Override
    public TokenValidation validateSignedJWT(final SignedJWT signedJWT) throws JOSEException, ParseException {
        final ReadOnlyJWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();

        final User user = getAssociatedUser(Long.parseLong(jwtClaimsSet.getSubject()));

        final boolean result = Objects.nonNull(user)
                && !user.isRoleModified()
                && verifyIfUserHasUserJWTInStorage(user, UUID.fromString(jwtClaimsSet.getJWTID()))
                && verifyJWTSignature(signedJWT)
                && verifyJWTIssuer(jwtClaimsSet.getIssuer())
                && verifyBlackList(jwtClaimsSet.getSubject(), signedJWT.serialize());

        /*
         *  Additional logic to refresh access:
         *
         *  When token is located inside a time frame which is
         *  starts when token epxiraion time is passed and ends
         *  when expiration time + TIMEOUT_FRAME_TO_REFRESH_TOKEN is reached.
         */
        final boolean isTokenExpired = !verifyJWTExpirationTime(jwtClaimsSet.getExpirationTime());

        if (result && isTokenExpired && isTokenValidForRefresh(signedJWT)) {
            // Increasing refresh counter by one
            final long refreshCounter = (Long) jwtClaimsSet.getCustomClaim("cnt") + 1L;

            // Invalidate previously issued token
            invalidateUserJWT(UUID.fromString(jwtClaimsSet.getJWTID()));

            // Create new token with increased counter
            final UserJWT userJWT = createUserJWT(user, createUserSpecificClaims(user, refreshCounter));
            return new TokenValidation(true, user, userJWT.getToken());
        } else {
            userService.removeLoggedUser(user);
        }
        return new TokenValidation(result && !isTokenExpired, user, signedJWT.serialize());
    }

    private boolean isTokenValidForRefresh(SignedJWT signedJWT) throws ParseException {
        final ReadOnlyJWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        final Long refreshCounter = (Long) jwtClaimsSet.getCustomClaim("cnt");
        final Date expirationTime = jwtClaimsSet.getExpirationTime();
        final Date expirationOfTokenRenewalTimeFrame = new Date(expirationTime.getTime() + TIMEOUT_FRAME_TO_REFRESH_TOKEN);
        return new Date().after(expirationTime) && new Date().before(expirationOfTokenRenewalTimeFrame) && refreshCounter < TOKEN_REFRESH_MAX_COUNT;
    }

    @Override
    public TokenValidation validateServiceSignedJWT(SignedJWT signedJWT) throws JOSEException, ParseException {
        final ReadOnlyJWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();

        final boolean result = verifyJWTSignature(signedJWT)
                && verifyJWTExpirationTime(jwtClaimsSet.getExpirationTime());

        return new TokenValidation(result, null, signedJWT.serialize());
    }

    private boolean verifyBlackList(final String userId, final String token) {
        return !blackListTokenService.findToken(Long.parseLong(userId), token).isPresent();
    }

    private boolean verifyIfUserHasUserJWTInStorage(final User user, final UUID jwtId) {
        final Optional<UserJWT> userJWT = getUserJWT(jwtId);
        return userJWT.filter(jwt -> user.getId() == jwt.getUserId().longValue()).isPresent();
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
        dataModel.query(UserJWT.class)
                .select(Operator.EQUAL.compare("jwtId", jwtId.toString()))
                .forEach(UserJWT::delete);
    }

    @Override
    public void invalidateAllUserJWTsForUser(final User user) {
        dataModel.query(UserJWT.class)
                .select(Operator.EQUAL.compare("userId", user.getId()))
                .forEach(UserJWT::delete);
    }

    private void deleteExpiredUserJWTs() {
        dataModel.query(UserJWT.class)
                .select(Where.where("expirationDate").isLessThanOrEqual(Instant.now()))
                .forEach(UserJWT::delete);
    }

    public Map<String, Object> createUserSpecificClaims(final User user, long count) {
        List<Group> userGroups = user.getGroups();
        List<RoleClaimInfo> roles = new ArrayList<>();
        List<String> privileges = new ArrayList<>();

        if (!userGroups.isEmpty()) {
            privileges.add("privilege.public.api.rest");
            privileges.add("privilege.pulse.public.api.rest");
            privileges.add("privilege.view.userAndRole");
        }

        userGroups.forEach(group -> {
            group.getPrivileges().forEach((key, value) -> {
                if (key.equals("BPM") || key.equals("YFN"))
                    value.forEach(p -> privileges.add(p.getName()));
            });
            roles.add(new RoleClaimInfo(group.getId(), group.getName()));
        });

        final HashMap<String, Object> result = new HashMap<>();
        result.put("username", user.getName());
        result.put("roles", roles);
        result.put("privileges", privileges);
        result.put("cnt", count);
        return result;
    }

    private class ExpiredTokensCleaningTaks extends TimerTask {
        @Override
        public void run() {
            try (TransactionContext context = transactionService.getContext()) {
                deleteExpiredUserJWTs();
                context.commit();
            }
        }
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setBlackListTokenService(BlackListTokenService blackListTokenService) {
        this.blackListTokenService = blackListTokenService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(DatabaseBasedTokenService.SERVICE_NAME, "Token Service responsible for token managment operations");
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(DatabaseBasedTokenService.SERVICE_NAME, Layer.SERVICE);
    }
}
