package com.elster.jupiter.validation.rest;


//import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
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
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.Matchers.matches;
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
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected ValidationService validationService;
    @Mock
    protected MeteringService meteringService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    protected EndDeviceGroup endDeviceGroup;
    @Mock
    protected TimeService timeService;


    protected DataValidationTaskBuilder builder = initBuilderStub();

    @Mock
    protected DataValidationTask dataValidationTask;

    protected RestQueryService restQueryService;
    protected PropertyUtils propertyUtils;
    protected ValidationApplication serviceLocator;

    private DataValidationTaskBuilder initBuilderStub() {
        final Object proxyInstance = Proxy.newProxyInstance(DataValidationTaskBuilder.class.getClassLoader(), new Class<?>[]{DataValidationTaskBuilder.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (DataValidationTaskBuilder.class.isAssignableFrom(method.getReturnType())) {
                    return builderGetter.get();
                }

                return taskGetter.get();
            }

            private Supplier<DataValidationTask> taskGetter = () -> dataValidationTask;
            private Supplier<DataValidationTaskBuilder> builderGetter = () -> builder;
        });
        return (DataValidationTaskBuilder) proxyInstance;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        serviceLocator = new ValidationApplication();
        serviceLocator.setValidationService(validationService);
        serviceLocator.setMeteringService(meteringService);
        serviceLocator.setRestQueryService(restQueryService);
        serviceLocator.setTransactionService(transactionService);
        serviceLocator.setNlsService(nlsService);
        serviceLocator.setTimeService(timeService);

        when(validationService.newTaskBuilder()).thenReturn(builder);

        when(meteringGroupsService.findEndDeviceGroup(1)).thenReturn(Optional.of(endDeviceGroup));

        when(transactionService.getContext()).thenReturn(mock(TransactionContext.class));

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
        thesaurus = mock(Thesaurus.class);
    }

    @Override
    protected Application configure() {
        init();
        
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        ResourceConfig resourceConfig = new ResourceConfig(
                ValidationResource.class,
                DataValidationTaskResource.class,
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
                bind(meteringGroupsService).to(MeteringGroupsService.class);
                bind(timeService).to(TimeService.class);
                bind(thesaurus).to(Thesaurus.class);
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
