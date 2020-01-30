package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SLOBaseTest extends JerseyTest {

    protected final static String SLO_ENDPOINT_PATH = "saml/v2/logout";

    // The name of query parameter which holds the value
    protected final static String SLO_REQUEST_QUERY_PARAM_NAME = "SAMLRequest";

    // Deflated and encoded SAML Single Logout Request
    protected final static String SLO_REQUEST_QUERY_PARAM_VALUE = "lVbZkqJIFH2fiPkHw340LEjANaqqI1kEVFAQcHmZSCFZlE0WUb9+UKsdq6enu+fBCPNyz8lzD3lv8vr1FIWNI87yIInfmuCFbH59//OP1xxFYTqcJl5SFjo+lDgvGnVmnA9vT96aZRYPE5QH+TBGEc6HhT1cQGU6pF7IYZolRWInYfMJ8nMEynOcFbWEZkPm35qpe6IZtLU7PacNer1uu+e6nTZjA7eN3S61pVCXHCC72bC+Ca9Jamiel1iO8wLFRR0iAdMme23QN0gwBPSQ7G6aDb6uJIhRcUP5RZEOCSJw0hd8QlEa4hc7iYhFEHshvhe/wNkxsPFL6qfN2phG42bN8LZV9v5BkH/GOzhKABHhAjmoQFfoK/GMenXy4SLwahVlhj88cvKHnKqqXir6Jck8giJJkiAHRJ3j5IH35UPCBx47cuwmNzoOxUkc2CgMLrfaFFz4idOAoZdkQeFH/0EOCEBeydv4ZLdtwMRfmsRti382uYn8bTqS+aa1HSUZ/pLlqJ37qAOoD+IrrY5dnOHYxg1Tl9+aX373dd8qNTIU526SRfnn5S/FfTISx0ccJil22vm3GmuB/4/wh+a9Ev/WyAdefep+08NPMmvrwIeuO4mFwhK/q4DhggunJkJHpftl7ASlZdqTxZmFbzcBz8m3wMPy+/K70/N4z3fE1l63zPE8BiG1NHwOzPa+H9n62pOgR6HLXLhslrKMBqFdSKdMiPpUKyxmVU0iueczNLJQG2tF55xpIr3b2ZjdBCpaUvKRDJ1wclj7VprZy9l6rc1wy16VG8syuyo2OZ6eqFN4Bow5zsyVs7nw+aQnkfNe5ZHyNg9zblZZnUg7VYNLTtNB1n97lPOkv55fdXCCz48CVx1ywNe9+Fhw13Hj1g1T4HdFljl9x3EQtTxYySz06h8PVdbbH/x9IA4qkoUaOYI8CxUtrzhtzVuaJgrVuNjwgq7AvgiBKXA1WKP81KbCcr0yT4IB56ynWixMFG7EhttY97fiIH7i4Hk4juwdxKOKPCs8PCm7/WlmKLRysdAtZlxj3iOmCPDEXeD4zrs2YAT2ii5UfHXjk3no80gML2jZ8R3jSZtXyaZolY44yreU/cShKDDanBVZdBVIitziIC7kLc1rQl2zCSEjqpDn2ECbsJ7G4bMyMec5v/UFoCQLGRmao1tTKu9P/H2RLd39ftQSGJVOT2MJCPoOnJPpUtQLKI0zcjUoj1owV8ZrXlqJl5V1zlpmOtPtoNM92cuCW3Dp2FsxjOZvrCNFGBvPOq5ctAbe1pJYmB1PR9Aa+SuPlABI2S0O5w4DS3Y1laaHDmtOZ8m8LKZgCdSqtkKDbAJgZeygc61V0hhh5Gmmqm5tHVMbbsHqWbE21NZpJI17TLgjK+nqo07uWNarRgn8aS4Pb7kGC80KVgJLXH5wZuCs9g16RbggD/C8pXp93lyZaC6up+tBquqBu9sXx6k/Eg4WL7jEgmPF5FiutH7EavSmY6vHLB6vLsvDrrXRzaMHWEc+ZX7t50jo7el+Z+SGkmTODXuxGUx1deV6Y4abrv2EkLcHTlbwnlBjo8NKJJvmJifNYZfHMWdtAWVrqXmmZ7uCPtPrCqUjF2pv95b6vkkewXsbEc8N9qkFny5Jtb7nZb6xmF//aGV9ObkBzh5z75e3ZrMxqmcoKn7+7XCNBE7bvaUOi+voDXBcNN9nqjCdibL6lzugbBv0aQa7Nun2aDywyYHL9CnXxn2SpHuoS3UA7n3c0nfZ1ylC/OAz6P1v";

    @Mock
    protected HttpAuthenticationService httpAuthenticationService;

    @Mock
    protected SamlResponseService samlResponseService;

    @Override
    protected Application configure() {
        MockitoAnnotations.initMocks(this);

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new SLOTestApplication(httpAuthenticationService, samlResponseService);
    }

    private class SLOTestApplication extends Application {

        private volatile HttpAuthenticationService httpAuthenticationService;

        private volatile SamlResponseService samlResponseService;

        public SLOTestApplication(HttpAuthenticationService httpAuthenticationService, SamlResponseService samlResponseService) {
            this.httpAuthenticationService = httpAuthenticationService;
            this.samlResponseService = samlResponseService;
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
                    bind(httpAuthenticationService).to(HttpAuthenticationService.class);
                    bind(samlResponseService).to(SamlResponseService.class);
                }
            };
        }
    }
}
