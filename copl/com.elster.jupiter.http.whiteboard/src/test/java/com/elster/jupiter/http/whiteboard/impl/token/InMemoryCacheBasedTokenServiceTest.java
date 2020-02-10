package com.elster.jupiter.http.whiteboard.impl.token;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.nimbusds.jose.JOSEException;
import org.junit.Before;
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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryCacheBasedTokenServiceTest {

    private static final Logger LOG = Logger.getLogger(InMemoryCacheBasedTokenServiceTest.class.getName());

    @Mock
    private User userMock;

    @Mock
    private UserService userService;

    private static volatile InMemoryCacheBasedTokenService inMemoryCacheBasedTokenService;

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
        inMemoryCacheBasedTokenService = new InMemoryCacheBasedTokenService();
        inMemoryCacheBasedTokenService.activate();
        inMemoryCacheBasedTokenService.setUserService(userService);
        inMemoryCacheBasedTokenService.initialize(PUBLIC_KEY, PRIVATE_KEY, TOKEN_EXPIRATION_TIME, TOKEN_REFRESH_MAX_COUNT, TIMEOUT);
    }

    private static void generetaSelfSignedCert() throws NoSuchAlgorithmException {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        final KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PUBLIC_KEY = DatatypeConverter.printBase64Binary(keyPair.getPublic().getEncoded()).getBytes();
        PRIVATE_KEY = DatatypeConverter.printBase64Binary(keyPair.getPrivate().getEncoded()).getBytes();
    }

    @Test
    public void shouldCreateUserJWT() throws JOSEException {
        when(userMock.getId()).thenReturn(new SecureRandom().nextLong());
        when(userMock.getGroups()).thenReturn(new ArrayList<>());
        when(userMock.getName()).thenReturn("TEST_USER_NAME");

        final UserJWT userJWT = inMemoryCacheBasedTokenService.createUserJWT(userMock, inMemoryCacheBasedTokenService.createCustomClaimsForUser(userMock, 0));

        assertThat(userJWT).isNotNull();
        assertThat(userJWT.getUser()).isEqualToComparingFieldByField(userMock);

        // Truncate to seconds, 'cause there will be the difference between expiration dates due to expenses during token calculation
        assertThat(userJWT.getExpirationDate().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(Instant.ofEpochMilli(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME * 1000).truncatedTo(ChronoUnit.SECONDS));

        verify(userMock).getId();
        verify(userMock).getGroups();
        verify(userMock).getName();
    }

    @Test
    public void shouldCreateValidateInvalidateUserJWT() throws JOSEException, ParseException {
        when(userMock.getId()).thenReturn(new SecureRandom().nextLong());
        when(userMock.getGroups()).thenReturn(new ArrayList<>());
        when(userMock.getName()).thenReturn("TEST_USER_NAME");

        final UserJWT userJWT = inMemoryCacheBasedTokenService.createUserJWT(userMock, inMemoryCacheBasedTokenService.createCustomClaimsForUser(userMock, 0));
        assertThat(userJWT).isNotNull();

        final UserJWT validUserJWTshouldNotBeNull = inMemoryCacheBasedTokenService.getUserJWT(extractJwtIdfromUserJWT(userJWT));
        assertThat(validUserJWTshouldNotBeNull).isNotNull();

        when(userService.getLoggedInUser(anyLong())).thenAnswer(invocationOnMock -> Optional.of(userMock));
        final TokenValidation tokenValidation = inMemoryCacheBasedTokenService.validateSignedJWT(userJWT.getSignedJWT());
        assertThat(tokenValidation.isValid()).isTrue();

        inMemoryCacheBasedTokenService.invalidateUserJWT(extractJwtIdfromUserJWT(userJWT));

        final UserJWT invalidatedUserJWTshouldBeNull = inMemoryCacheBasedTokenService.getUserJWT(extractJwtIdfromUserJWT(userJWT));
        assertThat(invalidatedUserJWTshouldBeNull).isNull();

        verify(userMock).getId();
        verify(userMock).getGroups();
        verify(userMock).getName();
    }

    @Test
    public void shouldCreate100_000UserJWTs() {
        final long executionTime = createTokensForUsers(createListOfUserMocks(100000));
        LOG.info(String.format("TOKEN_SERVICE_TEST: 100 000 user tokens were generated in %f seconds.", executionTime / 1000D));
    }

    @Test
    public void shouldInvalidate100_000UserJWTs() {
        final List<User> listOfUserMocks = createListOfUserMocks(100000);
        createTokensForUsers(listOfUserMocks);
        final long executionTime = invalidateTokensForUsers(listOfUserMocks);
        LOG.info(String.format("TOKEN_SERVICE_TEST: 100 000 user tokens were invalidated in %f seconds", executionTime / 1000D));
    }

    @Test
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
        userList.parallelStream()
                .forEach(user -> {
                    try {
                        output.add(inMemoryCacheBasedTokenService.createUserJWT(user, inMemoryCacheBasedTokenService.createCustomClaimsForUser(user, 0)));
                    } catch (JOSEException e) {
                        e.printStackTrace();
                    }
                });
        return System.currentTimeMillis() - startTime;
    }

    private long invalidateTokensForUsers(final List<User> userList) {
        long startTime = System.currentTimeMillis();
        userList.parallelStream().forEach(inMemoryCacheBasedTokenService::invalidateAllUserJWTsForUser);
        return System.currentTimeMillis() - startTime;
    }

    private long validateTokensForUserJWTs(final List<UserJWT> userJWTS) {
        long startTime = System.currentTimeMillis();
        userJWTS.parallelStream().forEach(userJWT -> {
            try {
                inMemoryCacheBasedTokenService.validateSignedJWT(userJWT.getSignedJWT());
            } catch (JOSEException | ParseException e) {
                e.printStackTrace();
            }
        });
        return System.currentTimeMillis() - startTime;
    }

    private UUID extractJwtIdfromUserJWT(final UserJWT userJWT) throws ParseException {
        return UUID.fromString(userJWT.getSignedJWT().getJWTClaimsSet().getJWTID());
    }

}