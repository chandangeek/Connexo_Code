package com.elster.jupiter.devtools.rest;

import aQute.lib.strings.Strings;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionMapper;
import com.elster.jupiter.rest.util.ConcurrentModificationInfo;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.rest.util.TransactionWrapper;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * This can serve as base test class for all our REST applications.
 * It makes setup of the test framework very easy
 * <p>
 * ONLY FOR TESTING PURPOSES -- NO PRODUCTION USAGE ALLOWED
 *
 * <pre>

                              @@@@@@@@@;
                            @@@@@@@@@@@@@:
                           @@@@@@@@@@@@@@@#
                          @@@@@@@@@@@@@@@@@
                          @@@@@@@@@@@@@@@@@#
                         .@@@@@@@@@@@@@@@@@@
                         :@@@@@@@@@@@@@@@@@@
                         `@@@@@@@@@@@@@@@@@@
                          @@@   '@@@@   @@@,
                          @@'    @@@     @@
                          @@+    @@@     @@
                           @@   @@@@@   @@
                           @@@@@@@ @@@@@@@
                           :@@@@@   @@@@@@
                            @@@@@`, @@@@@
                               @@@@@@@`
                             .@ @@@@@#'+
                              @+      @
                              @@@#''@@@
                                @@@@@;
                                 @@@`        #
                       @@#                  @@@
                      ,@@@@                @@@@
                      @@@@@@@#          @@@@@@@`
                      @@@;@@@@@@,    #@@@@@:@@@@
                             @@@@@@@@@@@
                               ,@@@@@;
                               @@@@@@@;
                             @@@@.  @@@@`
                           @@@@'      @@@@
                      ,@@@@@@@         +@@@@@@
                      @@@@@@+            @@@@@@@
                      @@@@@@             @@@@@@`
                        +@@;              @@@
                          ;                @`
 </pre>
 */
public abstract class FelixRestApplicationJerseyTest extends JerseyTest {

    @Mock
    public TransactionService transactionService;
    @Mock
    public Thesaurus thesaurus;
    @Mock
    public NlsService nlsService;
    @Mock
    public TransactionContext transactionContext;

    public void setupMocks() {
        when(nlsService.getThesaurus(anyString(), anyObject())).thenReturn(thesaurus);
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            if (TranslationKeyProvider.class.isAssignableFrom(getApplication().getClass())) {
                return ((TranslationKeyProvider) getApplication()).
                        getKeys().stream().
                        filter(key -> key.getKey().equals(invocationOnMock.getArguments()[0])).
                        map(TranslationKey::getDefaultFormat).
                        findFirst().
                        orElse((String) invocationOnMock.getArguments()[1]);
            }
            if (MessageSeedProvider.class.isAssignableFrom(getApplication().getClass())) {
                return ((MessageSeedProvider) getApplication()).
                        getSeeds().stream().
                        filter(messageSeed -> messageSeed.getKey().equals(invocationOnMock.getArguments()[0])).
                        map(MessageSeed::getDefaultFormat).
                        findFirst().
                        orElse((String) invocationOnMock.getArguments()[1]);
            }
            return invocationOnMock.getArguments()[1];
        });
        when(thesaurus.getFormat(any(TranslationKey.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((TranslationKey) invocation.getArguments()[0]));
        when(thesaurus.getFormat(any(MessageSeed.class)))
                .thenAnswer(invocation -> new SimpleNlsMessageFormat((MessageSeed) invocation.getArguments()[0]));
        when(transactionService.getContext()).thenReturn(transactionContext);

    }

    @Override
    protected final Application configure() {
        MockitoAnnotations.initMocks(this);
        setupMocks();
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        Application application = getApplication();
        ResourceConfig resourceConfig = new ResourceConfig(application.getClasses());
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(ObjectMapperProvider.class);
        resourceConfig.register(LocalizedFieldValidationExceptionMapper.class);
        resourceConfig.register(LocalizedExceptionMapper.class);
        resourceConfig.register(ConstraintViolationExceptionMapper.class);
        resourceConfig.register(JsonMappingExceptionMapper.class);
        resourceConfig.register(RestValidationExceptionMapper.class);
        resourceConfig.register(ConcurrentModificationExceptionMapper.class);
        resourceConfig.register(TransactionWrapper.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(ConcurrentModificationInfo.class).to(ConcurrentModificationInfo.class);
                bind(ConcurrentModificationExceptionFactory.class).to(ConcurrentModificationExceptionFactory.class);
                bind(transactionService).to(TransactionService.class);
            }
        });
        application.getSingletons().stream().filter(s -> s instanceof AbstractBinder).forEach(resourceConfig::register);
        return resourceConfig;
    }

    /**
     * Set up your bundle's application, set all required services (usually with @Reference annotation on the setter) and return
     *
     * @return
     */
    protected abstract Application getApplication();

    @Override
    protected final void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing
        config.register(ObjectMapperProvider.class);
        config.register(MultiPartFeature.class);
        config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);  // Makes DELETE accept an entity
        super.configureClient(config);
    }

    @Test
    public void checkRestMethodsHaveProducesAnnotation() {
        List<String> errors = getApplication()
                .getClasses().stream() // also contains non-resources, but that doesn't matter
                .flatMap(clazz -> Stream.of(clazz.getMethods()))
                .filter(method -> method.isAnnotationPresent(GET.class) || method.isAnnotationPresent(POST.class) || method.isAnnotationPresent(PUT.class) || method.isAnnotationPresent(DELETE.class) || method.isAnnotationPresent(PROPFIND.class))
                .filter(method -> !method.isAnnotationPresent(Produces.class))
                .map(method -> method.getDeclaringClass().getSimpleName() + ":" + method.getName())
                .collect(toList());

        if (!errors.isEmpty()) {
            fail("@Produces is missing on " + Strings.join(", ", errors));
        }
    }
}
