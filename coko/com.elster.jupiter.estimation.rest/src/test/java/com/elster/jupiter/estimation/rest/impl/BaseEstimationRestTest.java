package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.rest.PropertyUtils;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
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

import javax.ws.rs.core.Application;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@Ignore("Base functionality for rest tests")
@RunWith(MockitoJUnitRunner.class)
public class BaseEstimationRestTest extends JerseyTest {

    @Mock
    protected NlsService nlsService;
    @Mock
    protected Thesaurus thesaurus;
    @Mock
    protected TransactionService transactionService;
    @Mock
    protected EstimationService estimationService;
    @Mock
    protected MeteringService meteringService;
    @Mock
    protected TimeService timeService;
    @Mock
    protected MeteringGroupsService meteringGroupsService;
    @Mock
    protected PropertyUtils propertyUtils;
    @Mock
    protected RestQueryService restQueryService;

    protected EstimationApplication estimationApplication;
    @Mock
    private com.elster.jupiter.transaction.TransactionContext transactionContext;

    @Before
    public void setUp() throws Exception {
        when(nlsService.getThesaurus(anyString(), any())).thenReturn(thesaurus);
        propertyUtils = new PropertyUtils(nlsService);

        super.setUp();
        estimationApplication = new EstimationApplication();
        estimationApplication.setEstimationService(estimationService);
        estimationApplication.setMeteringService(meteringService);
        estimationApplication.setRestQueryService(restQueryService);
        estimationApplication.setTransactionService(transactionService);
        estimationApplication.setNlsService(nlsService);
        estimationApplication.setTimeService(timeService);
        estimationApplication.setMeteringGroupsService(meteringGroupsService);

        when(transactionService.execute(Matchers.any())).thenAnswer(new Answer<Object>() {
            
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ((Transaction<?>)invocation.getArguments()[0]).perform();
            }
        });
        when(transactionService.getContext()).thenReturn(transactionContext);
    }
    
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected Application configure() {

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        ResourceConfig resourceConfig = new ResourceConfig(
                EstimationResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(restQueryService).to(RestQueryService.class);
                bind(propertyUtils).to(PropertyUtils.class);
                bind(nlsService).to(NlsService.class);
                bind(estimationService).to(EstimationService.class);
                bind(transactionService).to(TransactionService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(timeService).to(TimeService.class);
                bind(meteringGroupsService).to(MeteringGroupsService.class);
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
