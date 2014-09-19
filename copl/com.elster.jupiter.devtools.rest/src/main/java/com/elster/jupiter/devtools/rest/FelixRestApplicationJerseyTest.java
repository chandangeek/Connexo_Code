package com.elster.jupiter.devtools.rest;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This can serve as base test class for all our REST applications.
 * It makes setup of the test framework very easy
 *
 * ONLY FOR TESTING PURPOSES -- NO PRODUCTION USAGE ALLOWED
 *
 *



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

    public void setupMocks () {
        when(nlsService.getThesaurus(anyString(), anyObject())).thenReturn(thesaurus);
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            for (MessageSeed messageSeeds : getMessageSeeds()) {
                if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return messageSeeds.getDefaultFormat();
                }
            }
            return (String) invocationOnMock.getArguments()[1];
        });
        NlsMessageFormat mft = mock(NlsMessageFormat.class);
        when(mft.format(any(Object[].class))).thenReturn("format");
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(mft);
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
        application.getSingletons().stream().filter(s -> s instanceof AbstractBinder).forEach(resourceConfig::register);
        return resourceConfig;
    }

    /**
     * Set up your bundle's application, set all required services (usually with @Reference annotation on the setter) and return
     * @return
     */
    protected abstract Application getApplication();

    @Override
    protected final void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing
        super.configureClient(config);
    }

}
