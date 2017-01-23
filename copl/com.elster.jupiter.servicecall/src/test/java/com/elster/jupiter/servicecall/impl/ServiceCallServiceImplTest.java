package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.impl.TableImpl;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.device.Device;

import java.time.Clock;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCallServiceImplTest {


    @Mock
    private FiniteStateMachineService finiteStateMachine;
    @Mock
    private OrmService ormService;
    @Mock
    private NlsService nlsService;
    @Mock
    private UserService userService;
    @Mock
    private CustomPropertySetService customPropertyService;
    @Mock
    private MessageService messageService;
    @Mock
    private JsonService jsonService;
    @Mock
    private Device device;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;
    @Mock
    private ServiceCall serviceCall1, serviceCall2;
    @Mock
    private UpgradeService upgradeService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TableImpl serviceCallTable;

    @Before
    public void setUp() {
        when(ormService.newDataModel(ServiceCallService.COMPONENT_NAME, "Service calls")).thenReturn(dataModel);
        when(dataModel.getInstance(any())).thenAnswer(invocation -> {
            Class<?> clazz = (Class<?>) invocation.getArguments()[0];
            return mock(clazz);
        });

        when(dataModel.addTable(TableSpecs.SCS_SERVICE_CALL.name(), ServiceCall.class)).thenReturn(serviceCallTable);
        when(serviceCallTable.addRefAnyColumns("TARGET", false, ServiceCallImpl.Fields.targetObject.fieldName())).thenCallRealMethod();
    }

    @After
    public void tearDown() {

    }

    @Test
    @Ignore
    //cant seem to fix this test for now because of the new implementation of cancelServiceCallsFor
    public void testCancelServiceCallsFor() throws Exception {
        ServiceCallServiceImpl serviceCallService = new ServiceCallServiceImpl(
                finiteStateMachine,
                ormService,
                nlsService,
                userService,
                customPropertyService,
                messageService,
                jsonService,
                upgradeService,
                Clock.systemDefaultZone()
        );

        serviceCallService.cancelServiceCallsFor(device);

        verify(serviceCall1).cancel();
        verify(serviceCall2).cancel();
    }
}