package com.elster.jupiter.validation.rest;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.impl.ValidationApplication;

import javax.ws.rs.core.Application;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.function.Supplier;

import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.when;

public class BaseValidationRestTest extends FelixRestApplicationJerseyTest {

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


    @Override
    public void setupMocks() {
        super.setupMocks();
        propertyUtils = new PropertyUtils(nlsService);
        validationRuleInfoFactory = new ValidationRuleInfoFactory(propertyUtils);
        when(validationService.newTaskBuilder()).thenReturn(builder);
        when(meteringGroupsService.findEndDeviceGroup(1)).thenReturn(Optional.of(endDeviceGroup));
        when(transactionService.execute(Matchers.any())).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return ((Transaction<?>) invocation.getArguments()[0]).perform();
            }
        });
    }

    @Override
    protected Application getApplication() {
        ValidationApplication app = new ValidationApplication();
        app.setValidationService(validationService);
        app.setRestQueryService(restQueryService);
        app.setTransactionService(transactionService);
        app.setMeteringGroupsService(meteringGroupsService);
        app.setNlsService(nlsService);
        app.setTimeService(timeService);
        return app;
    }

}
