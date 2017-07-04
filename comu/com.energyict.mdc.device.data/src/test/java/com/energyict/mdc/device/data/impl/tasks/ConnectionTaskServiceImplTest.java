/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Stijn Vanhoorelbeke
 * @since 29.06.17 - 09:59
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTaskServiceImplTest {

    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private EventService eventService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;

    private ConnectionTaskService connectionTaskService;

    @Before
    public void setUp() throws Exception {
        connectionTaskService =  new ConnectionTaskServiceImpl(deviceDataModelService, eventService, protocolPluggableService);
    }

    @Test
    public void findConnectionTaskByDeviceAndConnectionFunctionTest() {
        Device device = mock(Device.class);
        ConnectionFunction connectionFunction_1 = mock(ConnectionFunction.class);
        ConnectionFunction connectionFunction_2 = mock(ConnectionFunction.class);

        PartialConnectionTask partialWithoutConnectionFunction = mock(PartialConnectionTask.class);
        when(partialWithoutConnectionFunction.getConnectionFunction()).thenReturn(Optional.empty());
        PartialConnectionTask partialWithFunction1 = mock(PartialConnectionTask.class);
        when(partialWithFunction1.getConnectionFunction()).thenReturn(Optional.of(connectionFunction_1));
        PartialConnectionTask partialWithFunction2 = mock(PartialConnectionTask.class);
        when(partialWithFunction1.getConnectionFunction()).thenReturn(Optional.of(connectionFunction_2));

        ConnectionTask withoutConnectionFunction = mock(ConnectionTask.class);
        when(withoutConnectionFunction.getPartialConnectionTask()).thenReturn(partialWithoutConnectionFunction);
        ConnectionTask withFunction1 = mock(ConnectionTask.class);
        when(withFunction1.getPartialConnectionTask()).thenReturn(partialWithFunction1);
        ConnectionTask withFunction2 = mock(ConnectionTask.class);
        when(withFunction2.getPartialConnectionTask()).thenReturn(partialWithFunction2);

        DataMapper dataMapper = mock(DataMapper.class);
        when(dataMapper.find(ConnectionTaskFields.DEVICE.fieldName(), device)).thenReturn(Arrays.asList(withoutConnectionFunction, withFunction1, withFunction2));
        DataModel dataModel = mock(DataModel.class);
        when(dataModel.mapper(ConnectionTask.class)).thenReturn(dataMapper);
        when(deviceDataModelService.dataModel()).thenReturn(dataModel);

        // Business method
        Optional<ConnectionTask> connectionTask = connectionTaskService.findConnectionTaskByDeviceAndConnectionFunction(device, connectionFunction_2);

        // Asserts
        assertTrue(connectionTask.isPresent());
        assertTrue(connectionTask.get().getPartialConnectionTask().getConnectionFunction().isPresent());
        assertEquals(connectionTask.get().getPartialConnectionTask().getConnectionFunction().get(), connectionFunction_2);
    }
}