package com.elster.jupiter.validation.rest;

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
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.impl.DataValidationTaskResource;
import com.elster.jupiter.validation.rest.impl.ValidationApplication;
import com.elster.jupiter.validation.rest.impl.ValidationResource;
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

import javax.ws.rs.core.Application;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
    private MeteringGroupsService meteringGroupsService;
    @Mock
    protected EndDeviceGroup endDeviceGroup;
    @Mock
    protected TimeService timeService;
    @Mock
    protected RestQueryService restQueryService;
    @Mock
    protected DataValidationTask dataValidationTask;

    protected DataValidationTaskBuilder builder = initBuilderStub();
    protected PropertyUtils propertyUtils;
    protected ValidationApplication serviceLocator;
    private ValidationRuleInfoFactory validationRuleInfoFactory;

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
        when(nlsService.getThesaurus(anyString(), any())).thenReturn(thesaurus);
        propertyUtils = new PropertyUtils(nlsService);
        validationRuleInfoFactory = new ValidationRuleInfoFactory(propertyUtils);
        super.setUp();
        serviceLocator = new ValidationApplication();
        serviceLocator.setValidationService(validationService);
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

    @Override
    protected Application configure() {
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
                bind(nlsService).to(NlsService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(validationRuleInfoFactory).to(ValidationRuleInfoFactory.class);
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
