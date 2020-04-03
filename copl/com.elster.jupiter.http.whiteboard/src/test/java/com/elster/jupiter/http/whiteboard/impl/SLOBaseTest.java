package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.SAMLSingleLogoutService;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.http.whiteboard.impl.saml.SAMLUtilities;
import com.elster.jupiter.http.whiteboard.impl.saml.slo.SAMLSingleLogoutServiceImpl;
import com.elster.jupiter.http.whiteboard.impl.saml.slo.SLOResource;
import com.elster.jupiter.http.whiteboard.UserJWT;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;

public class SLOBaseTest extends JerseyTest {

    protected static final SAMLUtilities samlUtilities = SAMLUtilities.getInstance();

    protected final static String SLO_ENDPOINT_PATH = "saml/v2/logout";

    protected final static String SLO_NAME_RELAY_STATE = "RelayState";
    protected final static String SLO_VALUE_RELAY_STATE = "example.com";

    // The name of query parameter which holds the value
    protected final static String SLO_NAME_LOGOUT_REQUEST = "SAMLRequest";

    /**
     * In order to create Single Logout Request you may use following resources:
     * <p>
     * https://www.samltool.com/self_signed_certs.php - obtain self-signed certs
     * https://www.samltool.com/generic_slo_req.php - example of SAML Logout Request (we are using with embedded signature)
     * https://www.samltool.com/encode.php - deflate + base64 encoding tool
     * <p>
     * This is an example of SAML Logout Request:
     * <p>
     * <samlp:LogoutRequest xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="pfxd4d369e8-9ea1-780c-aff8-a1d11a9862a1" Version="2.0" IssueInstant="2014-07-18T01:13:06Z" Destination="http://localhost:9000/saml/v2/logout">
     * <saml:Issuer>http://sp.example.com/demo1/metadata.php</saml:Issuer>
     * <ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
     * <ds:SignedInfo>
     * <ds:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
     * <ds:SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
     * <ds:Reference URI="#pfxd4d369e8-9ea1-780c-aff8-a1d11a9862a1">
     * <ds:Transforms>
     * <ds:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
     * <ds:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
     * </ds:Transforms>
     * <ds:DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
     * <ds:DigestValue>Q9PRlugQZKSBt+Ed9i6bKUGWND0=</ds:DigestValue>
     * </ds:Reference>
     * </ds:SignedInfo>
     * <ds:SignatureValue>e861LsuFQi4dmtZanZlFjCtHym5SLhjwRZMxW2DSMhPwWxg7tD2vOH7mgqqFd3Syt9Q6VYSiWyIdYkpf4jsVTGZDXKk2zQbUFG/avRC9EsgMIw7UfeMwFw0D/XGDqihV9YoQEc85wGdbafQOGhMXBxkt+1Ba37ok8mCZAEFlZpw=</ds:SignatureValue>
     * <ds:KeyInfo>
     * <ds:X509Data>
     * <ds:X509Certificate>MIICajCCAdOgAwIBAgIBADANBgkqhkiG9w0BAQ0FADBSMQswCQYDVQQGEwJ1czETMBEGA1UECAwKQ2FsaWZvcm5pYTEVMBMGA1UECgwMT25lbG9naW4gSW5jMRcwFQYDVQQDDA5zcC5leGFtcGxlLmNvbTAeFw0xNDA3MTcxNDEyNTZaFw0xNTA3MTcxNDEyNTZaMFIxCzAJBgNVBAYTAnVzMRMwEQYDVQQIDApDYWxpZm9ybmlhMRUwEwYDVQQKDAxPbmVsb2dpbiBJbmMxFzAVBgNVBAMMDnNwLmV4YW1wbGUuY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDZx+ON4IUoIWxgukTb1tOiX3bMYzYQiwWPUNMp+Fq82xoNogso2bykZG0yiJm5o8zv/sd6pGouayMgkx/2FSOdc36T0jGbCHuRSbtia0PEzNIRtmViMrt3AeoWBidRXmZsxCNLwgIV6dn2WpuE5Az0bHgpZnQxTKFek0BMKU/d8wIDAQABo1AwTjAdBgNVHQ4EFgQUGHxYqZYyX7cTxKVODVgZwSTdCnwwHwYDVR0jBBgwFoAUGHxYqZYyX7cTxKVODVgZwSTdCnwwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQ0FAAOBgQByFOl+hMFICbd3DJfnp2Rgd/dqttsZG/tyhILWvErbio/DEe98mXpowhTkC04ENprOyXi7ZbUqiicF89uAGyt1oqgTUCD1VsLahqIcmrzgumNyTwLGWo17WDAa1/usDhetWAMhgzF/Cnf5ek0nK00m0YZGyc4LzgD0CROMASTWNg==</ds:X509Certificate>
     * </ds:X509Data>
     * </ds:KeyInfo>
     * </ds:Signature>
     * <saml:NameID SPNameQualifier="http://sp.example.com/demo1/metadata.php" Format="urn:oasis:names:tc:SAML:2.0:nameid-format:transient">uuid-test-user</saml:NameID>
     * </samlp:LogoutRequest>
     * <p>
     * NameID = uudi-test-user | use it for tests.
     * <p>
     * Below you can find this example to be encoded and deflated.
     */
    protected final static String SLO_VALUE_LOGOUT_REQUEST = "lVbZkqJIFH2fiPkHw340LEjANaqqI1kEVFAQcHmZSCFZlE0WUb9+UKsdq6enu+fBCPNyz8lzD3lv8vr1FIWNI87yIInfmuCFbH59//OP1xxFYTqcJl5SFjo+lDgvGnVmnA9vT96aZRYPE5QH+TBGEc6HhT1cQGU6pF7IYZolRWInYfMJ8nMEynOcFbWEZkPm35qpe6IZtLU7PacNer1uu+e6nTZjA7eN3S61pVCXHCC72bC+Ca9Jamiel1iO8wLFRR0iAdMme23QN0gwBPSQ7G6aDb6uJIhRcUP5RZEOCSJw0hd8QlEa4hc7iYhFEHshvhe/wNkxsPFL6qfN2phG42bN8LZV9v5BkH/GOzhKABHhAjmoQFfoK/GMenXy4SLwahVlhj88cvKHnKqqXir6Jck8giJJkiAHRJ3j5IH35UPCBx47cuwmNzoOxUkc2CgMLrfaFFz4idOAoZdkQeFH/0EOCEBeydv4ZLdtwMRfmsRti382uYn8bTqS+aa1HSUZ/pLlqJ37qAOoD+IrrY5dnOHYxg1Tl9+aX373dd8qNTIU526SRfnn5S/FfTISx0ccJil22vm3GmuB/4/wh+a9Ev/WyAdefep+08NPMmvrwIeuO4mFwhK/q4DhggunJkJHpftl7ASlZdqTxZmFbzcBz8m3wMPy+/K70/N4z3fE1l63zPE8BiG1NHwOzPa+H9n62pOgR6HLXLhslrKMBqFdSKdMiPpUKyxmVU0iueczNLJQG2tF55xpIr3b2ZjdBCpaUvKRDJ1wclj7VprZy9l6rc1wy16VG8syuyo2OZ6eqFN4Bow5zsyVs7nw+aQnkfNe5ZHyNg9zblZZnUg7VYNLTtNB1n97lPOkv55fdXCCz48CVx1ywNe9+Fhw13Hj1g1T4HdFljl9x3EQtTxYySz06h8PVdbbH/x9IA4qkoUaOYI8CxUtrzhtzVuaJgrVuNjwgq7AvgiBKXA1WKP81KbCcr0yT4IB56ynWixMFG7EhttY97fiIH7i4Hk4juwdxKOKPCs8PCm7/WlmKLRysdAtZlxj3iOmCPDEXeD4zrs2YAT2ii5UfHXjk3no80gML2jZ8R3jSZtXyaZolY44yreU/cShKDDanBVZdBVIitziIC7kLc1rQl2zCSEjqpDn2ECbsJ7G4bMyMec5v/UFoCQLGRmao1tTKu9P/H2RLd39ftQSGJVOT2MJCPoOnJPpUtQLKI0zcjUoj1owV8ZrXlqJl5V1zlpmOtPtoNM92cuCW3Dp2FsxjOZvrCNFGBvPOq5ctAbe1pJYmB1PR9Aa+SuPlABI2S0O5w4DS3Y1laaHDmtOZ8m8LKZgCdSqtkKDbAJgZeygc61V0hhh5Gmmqm5tHVMbbsHqWbE21NZpJI17TLgjK+nqo07uWNarRgn8aS4Pb7kGC80KVgJLXH5wZuCs9g16RbggD/C8pXp93lyZaC6up+tBquqBu9sXx6k/Eg4WL7jEgmPF5FiutH7EavSmY6vHLB6vLsvDrrXRzaMHWEc+ZX7t50jo7el+Z+SGkmTODXuxGUx1deV6Y4abrv2EkLcHTlbwnlBjo8NKJJvmJifNYZfHMWdtAWVrqXmmZ7uCPtPrCqUjF2pv95b6vkkewXsbEc8N9qkFny5Jtb7nZb6xmF//aGV9ObkBzh5z75e3ZrMxqmcoKn7+7XCNBE7bvaUOi+voDXBcNN9nqjCdibL6lzugbBv0aQa7Nun2aDywyYHL9CnXxn2SpHuoS3UA7n3c0nfZ1ylC/OAz6P1v";

    protected SAMLSingleLogoutService samlSingleLogoutService;

    @Mock
    protected HttpServletResponse httpServletResponse;

    @Mock
    protected TokenService<UserJWT> tokenService;

    @Mock
    protected UserService userService;

    @Mock
    protected User user;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        samlSingleLogoutService = new SAMLSingleLogoutServiceImpl(tokenService, userService);

        return new SLOTestApplication(samlSingleLogoutService, tokenService, userService, httpServletResponse);
    }

    private class SLOTestApplication extends Application {

        private volatile SAMLSingleLogoutService samlSingleLogoutService;
        private volatile TokenService tokenService;
        private volatile UserService userService;
        private volatile HttpServletResponse httpServletResponse;

        public SLOTestApplication(SAMLSingleLogoutService samlSingleLogoutService, TokenService tokenService, UserService userService, HttpServletResponse httpServletResponse) {
            this.samlSingleLogoutService = samlSingleLogoutService;
            this.tokenService = tokenService;
            this.userService = userService;
            this.httpServletResponse = httpServletResponse;
        }

        @Override
        public Set<Class<?>> getClasses() {
            return ImmutableSet.of(
                    SLOResource.class
            );
        }

        @Override
        public Set<Object> getSingletons() {
            Set<Object> hashSet = new HashSet<>(super.getSingletons());
            hashSet.add(getBinder());
            return Collections.unmodifiableSet(hashSet);
        }

        private Binder getBinder() {
            return new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(samlSingleLogoutService).to(SAMLSingleLogoutService.class);
                    bind(tokenService).to(TokenService.class);
                    bind(userService).to(UserService.class);
                    bind(httpServletResponse).to(HttpServletResponse.class);
                }
            };
        }
    }
}
