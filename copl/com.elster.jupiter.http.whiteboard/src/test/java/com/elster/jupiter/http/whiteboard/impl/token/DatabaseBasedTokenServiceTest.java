package com.elster.jupiter.http.whiteboard.impl.token;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.http.whiteboard.TokenValidation;
import com.elster.jupiter.http.whiteboard.UserJWT;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.DatatypeConverter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseBasedTokenServiceTest extends TokenServicePersistence {

    private static final Logger LOG = Logger.getLogger(DatabaseBasedTokenServiceTest.class.getName());

    @Mock
    private User userMock;

    private final UserService userService = inMemoryPersistence.getUserService();

    private static volatile DatabaseBasedTokenService databaseBasedTokenService;

    private static String KEY_ALGORITHM = "RSA";
    private static int KEY_SIZE = 1024;
    private static byte[] PUBLIC_KEY;
    private static byte[] PRIVATE_KEY;

    private static long TOKEN_EXPIRATION_TIME = 60000;
    private static long TOKEN_REFRESH_MAX_COUNT = 10;
    private static long TIMEOUT = 300;

    @Before
    public void setUp() throws NoSuchAlgorithmException {
        generetaSelfSignedCert();
        databaseBasedTokenService = (DatabaseBasedTokenService) inMemoryPersistence.getTokenService();
        databaseBasedTokenService.initialize(PUBLIC_KEY, PRIVATE_KEY, TOKEN_EXPIRATION_TIME, TOKEN_REFRESH_MAX_COUNT, TIMEOUT);
    }

    private static void generetaSelfSignedCert() throws NoSuchAlgorithmException {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        final KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PUBLIC_KEY = DatatypeConverter.printBase64Binary(keyPair.getPublic().getEncoded()).getBytes();
        PRIVATE_KEY = DatatypeConverter.printBase64Binary(keyPair.getPrivate().getEncoded()).getBytes();
    }

    @Test
    @Transactional
    public void shouldCreateUserJWT() throws JOSEException {
        when(userMock.getId()).thenReturn(new SecureRandom().nextLong());
        when(userMock.getGroups()).thenReturn(new ArrayList<>());
        when(userMock.getName()).thenReturn("TEST_USER_NAME");

        final UserJWT userJWT = databaseBasedTokenService.createUserJWT(userMock, databaseBasedTokenService.createUserSpecificClaims(userMock, 0));

        assertThat(userJWT).isNotNull();
        assertThat(userJWT.getUserId().longValue()).isEqualTo(userMock.getId());

        // Truncate to seconds, 'cause there will be the difference between expiration dates due to expenses during token calculation
        assertThat(userJWT.getExpirationDate().truncatedTo(ChronoUnit.MINUTES))
                .isEqualTo(Instant.ofEpochMilli(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME * 1000).truncatedTo(ChronoUnit.MINUTES));

        verify(userMock).getGroups();
        verify(userMock).getName();
    }

    @Test
    @Transactional
    public void shouldCreateValidateInvalidateUserJWT() throws JOSEException, ParseException {
        final User scimUser = userService.createSCIMUser("TEST_USER_NAME", "TEST_USER", UUID.randomUUID().toString());

        final UserJWT userJWT = databaseBasedTokenService.createUserJWT(scimUser, databaseBasedTokenService.createUserSpecificClaims(userMock, 0));
        assertThat(userJWT).isNotNull();

        final Optional<UserJWT> validUserJWTshouldNotBeNull = databaseBasedTokenService.getUserJWT(extractJwtIdfromUserJWT(userJWT));
        assertThat(validUserJWTshouldNotBeNull).isPresent();

        final TokenValidation tokenValidation = databaseBasedTokenService.validateSignedJWT(SignedJWT.parse(userJWT.getToken()));
        assertThat(tokenValidation.isValid()).isTrue();

        databaseBasedTokenService.invalidateUserJWT(extractJwtIdfromUserJWT(userJWT));

        final Optional<UserJWT> invalidatedUserJWTshouldBeNull = databaseBasedTokenService.getUserJWT(extractJwtIdfromUserJWT(userJWT));
        assertThat(invalidatedUserJWTshouldBeNull).isEmpty();
    }

    @Test
    @Transactional
    @Ignore
    public void shouldCreate100_000UserJWTs() {
        final long executionTime = createTokensForUsers(createListOfUserMocks(100000));
        LOG.info(String.format("TOKEN_SERVICE_TEST: 100 000 user tokens were generated in %f seconds.", executionTime / 1000D));
    }

    @Test
    @Ignore
    public void shouldInvalidate100_000UserJWTs() {
        final List<User> listOfUserMocks = createListOfUserMocks(100000);
        createTokensForUsers(listOfUserMocks);
        final long executionTime = invalidateTokensForUsers(listOfUserMocks);
        LOG.info(String.format("TOKEN_SERVICE_TEST: 100 000 user tokens were invalidated in %f seconds", executionTime / 1000D));
    }

    @Test
    @Transactional
    @Ignore
    public void shouldValidate100UserJWTs() {
        final List<User> listOfUserMocks = createListOfUserMocks(100);
        final CopyOnWriteArrayList<UserJWT> userJWTS = new CopyOnWriteArrayList<>();
        createTokensForUsers(listOfUserMocks, userJWTS);

        when(userService.getLoggedInUser(Matchers.anyLong())).thenAnswer(invocationOnMock -> {
            final Long userId = invocationOnMock.getArgumentAt(0, Long.class);
            return listOfUserMocks.stream().filter(user -> user.getId() == userId).findFirst();
        });

        final long executionTime = validateTokensForUserJWTs(userJWTS);
        LOG.info(String.format("TOKEN_SERVICE_TEST: 100 000 user tokens were validated in %f seconds", executionTime / 1000D));
    }

    @Test
    @Transactional
    public void shouldCreatePermanentToken() throws JOSEException, ParseException {
        final SignedJWT permamentSignedJWT = databaseBasedTokenService.createPermamentSignedJWT(userMock);

        final Optional<UserJWT> userJWT = databaseBasedTokenService.getUserJWT(UUID.fromString(permamentSignedJWT.getJWTClaimsSet().getJWTID()));

        assertThat(userJWT.isPresent()).isTrue();

        final UserJWT actualUserJWT = userJWT.get();
        assertThat(actualUserJWT.getExpirationDate()).isEqualTo(Instant.ofEpochMilli(Long.MAX_VALUE));
    }

    private List<User> createListOfUserMocks(final long count) {
        when(userMock.getId()).thenAnswer(invocationOnMock -> new SecureRandom().nextLong());
        when(userMock.getGroups()).thenReturn(new ArrayList<>());
        when(userMock.getName()).thenReturn("TEST_USER_NAME");

        final List<User> userMocks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            userMocks.add(userMock);
        }
        return userMocks;
    }

    private long createTokensForUsers(final List<User> userList) {
        return createTokensForUsers(userList, new CopyOnWriteArrayList<>());
    }

    private long createTokensForUsers(final List<User> userList, final List<UserJWT> output) {
        long startTime = System.currentTimeMillis();
        try (TransactionContext context = inMemoryPersistence.getTransactionService().getContext()) {
            userList.parallelStream()
                    .forEach(user -> {
                        try {
                            output.add(databaseBasedTokenService.createUserJWT(user, databaseBasedTokenService.createUserSpecificClaims(user, 0)));
                        } catch (JOSEException e) {
                            e.printStackTrace();
                        }
                    });
            context.commit();
        }
        return System.currentTimeMillis() - startTime;
    }

    private long invalidateTokensForUsers(final List<User> userList) {
        long startTime = System.currentTimeMillis();
        userList.parallelStream().forEach(databaseBasedTokenService::invalidateAllUserJWTsForUser);
        return System.currentTimeMillis() - startTime;
    }

    private long validateTokensForUserJWTs(final List<UserJWT> userJWTS) {
        long startTime = System.currentTimeMillis();
        userJWTS.parallelStream().forEach(userJWT -> {
            try {
                databaseBasedTokenService.validateSignedJWT(SignedJWT.parse(userJWT.getToken()));
            } catch (JOSEException | ParseException e) {
                e.printStackTrace();
            }
        });
        return System.currentTimeMillis() - startTime;
    }

    private UUID extractJwtIdfromUserJWT(final UserJWT userJWT) throws ParseException {
        return UUID.fromString(SignedJWT.parse(userJWT.getToken()).getJWTClaimsSet().getJWTID());
    }
}