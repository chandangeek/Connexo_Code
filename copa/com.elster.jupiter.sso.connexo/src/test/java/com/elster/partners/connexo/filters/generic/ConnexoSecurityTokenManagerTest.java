package com.elster.partners.connexo.filters.generic;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dragos on 2/2/2016.
 */
public class ConnexoSecurityTokenManagerTest {
    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;

    @BeforeClass
    public static void generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        keyGenerator.initialize(1024,random);
        KeyPair keyPair = keyGenerator.genKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field tokenInstance = ConnexoSecurityTokenManager.class.getDeclaredField("instance");
        tokenInstance.setAccessible(true);
        tokenInstance.set(null, null);
    }

    @Test
    public void testVerifyInvalidToken(){
        // Given
        ConnexoSecurityTokenManager manager = ConnexoSecurityTokenManager.getInstance();
        System.setProperty("com.elster.jupiter.token.refresh.maxcount", "100");
        System.setProperty("com.elster.jupiter.timeout", "300");
        System.setProperty("com.elster.jupiter.token.expirationtime", "300");
        System.setProperty("com.elster.jupiter.sso.public.key", "test-key");

        // When
        // Then
        assertNull(manager.verifyToken("invalid-token", true));
        assertNull(manager.verifyToken("invalid-token", false));
    }

    @Test
    public void testVerifyValidToken() throws JOSEException, NoSuchFieldException, IllegalAccessException {
        // Given
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 60);
        Map<String, Object> role = new HashMap<>();
        role.put("id", 1);
        role.put("name", "Role1");
        String token = createToken(1, "TestUser", Arrays.asList(new JSONObject(role)), "Elster Connexo", 1, new Date(), calendar.getTime());

        System.setProperty("com.elster.jupiter.sso.public.key", new String(DatatypeConverter.printBase64Binary(publicKey
                .getEncoded())));
        ConnexoSecurityTokenManager manager = ConnexoSecurityTokenManager.getInstance();

        // When
        ConnexoPrincipal principal = manager.verifyToken(token, true);

        // Then
        assertNotNull(principal);
        assertTrue(principal.isValid());
        assertEquals(token, principal.getToken());
        assertEquals(1, principal.getUserId());
        assertEquals("TestUser", principal.getName());
        assertEquals(Arrays.asList("Role1"), principal.getRoles());
    }

    @Test
    public void testVerifyExpiredToken() throws JOSEException {
        // Given
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -700);
        Date issueDate = calendar.getTime();
        calendar.add(Calendar.SECOND, 100);
        Map<String, Object> role = new HashMap<>();
        role.put("id", 1);
        role.put("name", "Role1");
        String token = createToken(1, "TestUser", Arrays.asList(new JSONObject(role)), "Elster Connexo", 1, issueDate, calendar.getTime());

        System.setProperty("com.elster.jupiter.sso.public.key", new String(DatatypeConverter.printBase64Binary(publicKey
                .getEncoded())));
        ConnexoSecurityTokenManager manager = ConnexoSecurityTokenManager.getInstance();

        // When
        ConnexoPrincipal principal = manager.verifyToken(token, true);

        // Then
        assertNull(principal);
    }

    @Test
    public void testVerifyInvalidIssuerToken() throws JOSEException {
        // Given
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 60);
        Map<String, Object> role = new HashMap<>();
        role.put("id", 1);
        role.put("name", "Role1");
        String token = createToken(1, "TestUser", Arrays.asList(new JSONObject(role)), "Another issuer", 1, new Date(), calendar.getTime());

        System.setProperty("com.elster.jupiter.sso.public.key", new String(DatatypeConverter.printBase64Binary(publicKey
                .getEncoded())));
        ConnexoSecurityTokenManager manager = ConnexoSecurityTokenManager.getInstance();

        // When
        ConnexoPrincipal principal = manager.verifyToken(token, true);

        // Then
        assertNull(principal);
    }

    @Test
    public void testVerifyMaxCountExceededToken() throws JOSEException {
        // Given
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 60);
        Map<String, Object> role = new HashMap<>();
        role.put("id", 1);
        role.put("name", "Role1");
        String token = createToken(1, "TestUser", Arrays.asList(new JSONObject(role)), "Elster Connexo", 100, new Date(), calendar.getTime());

        System.setProperty("com.elster.jupiter.sso.public.key", new String(DatatypeConverter.printBase64Binary(publicKey
                .getEncoded())));
        ConnexoSecurityTokenManager manager = ConnexoSecurityTokenManager.getInstance();

        // When
        ConnexoPrincipal principal = manager.verifyToken(token, true);

        // Then
        assertNull(principal);
    }

    @Test
    public void testTokenRefresh() throws JOSEException, NoSuchFieldException, IllegalAccessException {
        // Given
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -400);
        Date issueDate = calendar.getTime();
        calendar.add(Calendar.SECOND, 200);
        Map<String, Object> role = new HashMap<>();
        role.put("id", 1);
        role.put("name", "Role1");
        String token = createToken(1, "TestUser", Arrays.asList(new JSONObject(role)), "Elster Connexo", 1, issueDate, calendar.getTime());

        System.setProperty("com.elster.jupiter.sso.public.key", new String(DatatypeConverter.printBase64Binary(publicKey
                .getEncoded())));
        ConnexoSecurityTokenManager manager = ConnexoSecurityTokenManager.getInstance();

        ConnexoRestProxyManager restManager = mock(ConnexoRestProxyManager.class);
        Field restInstance = ConnexoRestProxyManager.class.getDeclaredField("instance");
        restInstance.setAccessible(true);
        restInstance.set(null, restManager);

        when(restManager.getConnexoAuthorizationToken("Bearer " + token)).thenReturn("New token");

        // When
        ConnexoPrincipal principal = manager.verifyToken(token, true);

        // Then
        assertNotNull(principal);
        assertTrue(principal.isValid());
        assertEquals("New token", principal.getToken());
        assertEquals(1, principal.getUserId());
        assertEquals("TestUser", principal.getName());
        assertEquals(Arrays.asList("Role1"), principal.getRoles());
        verify(restManager).getConnexoAuthorizationToken("Bearer " + token);
    }

    @Test
    public void testTokenNotRefreshedWhenNotAllowed() throws JOSEException, NoSuchFieldException, IllegalAccessException {
        // Given
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -400);
        Date issueDate = calendar.getTime();
        calendar.add(Calendar.SECOND, 200);
        Map<String, Object> role = new HashMap<>();
        role.put("id", 1);
        role.put("name", "Role1");
        String token = createToken(1, "TestUser", Arrays.asList(new JSONObject(role)), "Elster Connexo", 1, issueDate, calendar.getTime());

        System.setProperty("com.elster.jupiter.sso.public.key", new String(DatatypeConverter.printBase64Binary(publicKey
                .getEncoded())));
        ConnexoSecurityTokenManager manager = ConnexoSecurityTokenManager.getInstance();

        ConnexoRestProxyManager restManager = mock(ConnexoRestProxyManager.class);
        Field restInstance = ConnexoRestProxyManager.class.getDeclaredField("instance");
        restInstance.setAccessible(true);
        restInstance.set(null, restManager);

        // When
        ConnexoPrincipal principal = manager.verifyToken(token, false);

        // Then
        assertNotNull(principal);
        assertTrue(principal.isValid());
        assertEquals(token, principal.getToken());
        assertEquals(1, principal.getUserId());
        assertEquals("TestUser", principal.getName());
        assertEquals(Arrays.asList("Role1"), principal.getRoles());
        verify(restManager, never()).getConnexoAuthorizationToken(anyString());
    }

    private String createToken(long userId, String user, List<JSONObject> roles, String issuer, long count, Date issueTime, Date expirationTime) throws JOSEException {
        JWSSigner signer = new RSASSASigner(privateKey);

        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setCustomClaim("username", user);
        claimsSet.setSubject(Long.toString(userId));
        claimsSet.setCustomClaim("roles", roles);
        claimsSet.setIssuer(issuer);
        claimsSet.setCustomClaim("cnt", count);
        claimsSet.setJWTID("token" + count);
        claimsSet.setIssueTime(issueTime);
        claimsSet.setExpirationTime(expirationTime);

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}
