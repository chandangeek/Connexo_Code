package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.resource;

import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.SCIMApplication;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class OAuthBaseTest extends JerseyTest {
    protected static final String TOKEN_RESOURCE_PATH = "/token";
    protected static final String CLIENT_CREDENTIALS = Base64.getEncoder().encodeToString("enexis.password".getBytes());
    protected static final Form TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS = new Form();
    protected static final Form TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_UNKNOWN = new Form();
    protected static final Form TOKEN_REQUEST_FORM_WITHOUT_GRANT_TYPE = new Form();
    protected static final RSAPrivateKey privateKey;

    static {
        TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_CLIENT_CREDENTIALS.param("grant_type", "client_credentials");
        TOKEN_REQUEST_FORM_WITH_GRANT_TYPE_UNKNOWN.param("grant_type", "unknown");

        KeyPairGenerator keyGenerator = null;
        try {
            keyGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        SecureRandom random = new SecureRandom();
        Objects.requireNonNull(keyGenerator).initialize(1024, random);
        KeyPair keyPair = keyGenerator.genKeyPair();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    @Mock
    protected UserService userService;
    @Mock
    protected TokenService tokenService;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new SCIMApplication(userService, tokenService);
    }

    protected SignedJWT parseJws(final String jws) throws ParseException {
        String[] splitedToken = jws.split("\\.");
        String jwt = splitedToken[0] + "." + splitedToken[1] + ".";

        return SignedJWT.parse(jwt);
    }

    public SignedJWT createServiceSignedJWT(long expiresIn, String subject, String issuer, Map<String, Object> customClaims) throws JOSEException {
        final UUID jwtId = UUID.randomUUID();

        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setJWTID(jwtId.toString());
        claimsSet.setSubject(String.valueOf(subject.hashCode()));
        claimsSet.setIssuer(issuer);
        claimsSet.setIssueTime(new Date());
        claimsSet.setExpirationTime(new Date(expiresIn));
        claimsSet.setCustomClaims(customClaims);

        final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
        JWSSigner jwsSigner = new RSASSASigner(privateKey);
        signedJWT.sign(jwsSigner);
        return signedJWT;
    }
}
