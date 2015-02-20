package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore("Base functionality for rest tests")
@RunWith(MockitoJUnitRunner.class)
public class BaseValidationRestTest extends JerseyTest {

    @Mock
    protected NlsService nlsService;
    @Mock
    protected Thesaurus thesaurus;
    @Mock
    protected TransactionService transactionService;
    @Mock
    protected ValidationService validationService;
    @Mock
    protected MeteringService meteringService;
    
    protected RestQueryService restQueryService;
    protected PropertyUtils propertyUtils;
    protected ValidationApplication serviceLocator;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        serviceLocator = new ValidationApplication();
        serviceLocator.setValidationService(validationService);
        serviceLocator.setMeteringService(meteringService);
        serviceLocator.setRestQueryService(restQueryService);
        serviceLocator.setTransactionService(transactionService);
        serviceLocator.setNlsService(nlsService);
        
        when(transactionService.execute(Matchers.any())).thenAnswer(new Answer() {
            
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ((Transaction<?>)invocation.getArguments()[0]).perform();
            }
        });
    }
    
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    private void init() {
        restQueryService = mock(RestQueryService.class);
        propertyUtils = new PropertyUtils();
    }

    @Override
    protected Application configure() {
        init();
        
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        ResourceConfig resourceConfig = new ResourceConfig(
                ValidationResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(restQueryService).to(RestQueryService.class);
                bind(propertyUtils).to(PropertyUtils.class);
                bind(validationService).to(ValidationService.class);
                bind(transactionService).to(TransactionService.class);
//              bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class);
        super.configureClient(config);
    }
}
