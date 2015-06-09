package com.elster.jupiter.devtools.rest;

import com.elster.jupiter.nls.*;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.JsonMappingExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
            for (MessageSeed messageSeeds : getMessageSeeds()) {
                if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return messageSeeds.getDefaultFormat();
                }
                if (TranslationKeyProvider.class.isAssignableFrom(getApplication().getClass())) {
                    return ((TranslationKeyProvider)getApplication()).
                            getKeys().stream().
                            filter(key->key.getKey().equals(invocationOnMock.getArguments()[0])).
                            map(TranslationKey::getDefaultFormat).
                            findFirst().
                            orElse((String) invocationOnMock.getArguments()[1]);
                }

            }
            return invocationOnMock.getArguments()[1];
        });
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenAnswer(invocation -> new SimpleNlsMessageFormat((MessageSeed) invocation.getArguments()[0]));
        when(transactionService.getContext()).thenReturn(transactionContext);
    }

    /**
     * Just return all values for the bundles messageSeeds here, or empty array if there are none
     */
    protected abstract MessageSeed[] getMessageSeeds();

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
    public void checkRestMethodsHaveProducesAnnotation() throws IOException {
        ClassPath classPath = ClassPath.from(this.getClass().getClassLoader());
        String packageName = this.getClass().getPackage().getName();
        if (packageName.endsWith(".impl")) {
            packageName = packageName.substring(0, packageName.lastIndexOf(".impl"));
        }
        StringBuilder builder = new StringBuilder();
        for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
            Class<?> loadedClass = classInfo.load();
            for (Method method : loadedClass.getMethods()) {
                if (isRestMethod(method)) {
                    if (!method.isAnnotationPresent(Produces.class)) {
                        builder.append("REST method \'").append(method.getName()).append("\' of class \'").append(classInfo.getName()).append("\' does not have the @Produce annotation");
                        builder.append("\n");
                    }
                }
            }
        }
        if (builder.length() > 0) {
            Assert.fail(builder.toString());
        }
    }

    private boolean isRestMethod(Method method) {
        return method.isAnnotationPresent(GET.class) ||
                method.isAnnotationPresent(PUT.class) ||
                method.isAnnotationPresent(POST.class) ||
                method.isAnnotationPresent(DELETE.class) ||
                method.isAnnotationPresent(HEAD.class) ||
                method.isAnnotationPresent(OPTIONS.class);
    }


}
