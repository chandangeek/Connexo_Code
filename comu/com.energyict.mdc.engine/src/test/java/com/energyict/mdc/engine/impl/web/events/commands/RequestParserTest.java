/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundCapableComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RequestParser} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (10:18)
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestParserTest {

    private static final long DEVICE1_ID = 1;
    private static final long DEVICE2_ID = DEVICE1_ID + 1;
    private static final String DEVICE1_MRID = "MRID_"+DEVICE1_ID;
    private static final String DEVICE2_MRID = "MRID_"+DEVICE2_ID;
    private static final long CONNECTION_TASK1_ID = DEVICE2_ID + 1;
    private static final long CONNECTION_TASK2_ID = CONNECTION_TASK1_ID + 1;
    private static final long COM_TASK_EXECUTION1_ID = CONNECTION_TASK2_ID + 1;
    private static final long COM_TASK_EXECUTION2_ID = COM_TASK_EXECUTION1_ID + 1;
    private static final long COM_PORT1_ID = COM_TASK_EXECUTION2_ID + 1;
    private static final long COM_PORT2_ID = COM_PORT1_ID + 1;
    private static final String COM_PORT1_NAME = "Comport_" + COM_PORT1_ID;
    private static final String COM_PORT2_NAME = "Comport_" + COM_PORT2_ID;
    private static final long COM_PORT_POOL1_ID = COM_PORT2_ID + 1;
    private static final long COM_PORT_POOL2_ID = COM_PORT_POOL1_ID + 1;
    private static final long NON_EXISTING_DEVICE_ID = 999;
    private static final String NON_EXISTING_DEVICE_MRID = "999";
    private static final long NON_EXISTING_CONNECTION_TASK_ID = NON_EXISTING_DEVICE_ID - 1;
    private static final long NON_EXISTING_COMTASK_TASK_ID = NON_EXISTING_CONNECTION_TASK_ID - 1;
    private static final long NON_EXISTING_COMPORT_ID = NON_EXISTING_COMTASK_TASK_ID - 1;
    private static final long NON_EXISTING_COMPORT_POOL_ID = NON_EXISTING_COMPORT_ID - 1;

    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private RequestParser.ServiceProvider serviceProvider;
    @Mock
    private RunningComServer runningComServer;
    @Mock
    private OutboundCapableComServer comServer;

    @Before
    public void initializeServiceProvider() {
        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.serviceProvider.communicationTaskService()).thenReturn(this.communicationTaskService);
        when(this.serviceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.serviceProvider.engineConfigurationService()).thenReturn(this.engineConfigurationService);
        when(this.serviceProvider.identificationService()).thenReturn(this.identificationService);
        when(this.runningComServer.getComServer()).thenReturn(this.comServer);
    }

    @Test(expected = UnexpectedRequestFormatException.class)
    public void testWrongFormat() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Anything as long as it does not conform to the expected parse format");

        // Expected UnexpectedRequestFormatException because the request does not match with the expected request pattern
    }

    @Test(expected = RequestTypeParseException.class)
    public void testUnknownRequestType() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for something: Unknown");

        // Expected an RequestTypeParseException because "something" is not a known request type
    }

    @Test(expected = UnknownCategoryParseException.class)
    public void testNonExistingErrorCategory() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for errors: Unknown");

        // Expected an UnknownCategoryParseException because "Unknown" is not a known Category
    }

    @Test(expected = UnknownCategoryParseException.class)
    public void testNonExistingWarningCategory() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for warnings: Unknown");

        // Expected an UnknownCategoryParseException because "Unknown" is not a known Category
    }

    @Test(expected = UnknownCategoryParseException.class)
    public void testNonExistingInfoCategory() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for info: Unknown");

        // Expected an UnknownCategoryParseException because "Unknown" is not a known Category
    }

    @Test(expected = UnknownCategoryParseException.class)
    public void testNonExistingDebugCategory() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for debugging: Unknown");

        // Expected an UnknownCategoryParseException because "Unknown" is not a known Category
    }

    @Test(expected = UnknownCategoryParseException.class)
    public void testNonExistingTraceCategory() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for tracing: Unknown");

        // Expected an UnknownCategoryParseException because "Unknown" is not a known Category
    }

    @Test
    public void testErrorCategoriesSeparatedBySpaces() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        String categoryNames = "CONNECTION COMTASK COLLECTED_DATA_PROCESSING LOGGING";
        try {
            //Business method
            parser.parse("Register request for errors: " + categoryNames);
        } catch (UnknownCategoryParseException e) {
            assertThat(e.getMessage()).contains(categoryNames);
        }
    }

    @Test
    public void testAllCategoriesInUpperCase() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        String categoryNames = "CONNECTION,COMTASK,COLLECTED_DATA_PROCESSING,LOGGING";

        //Business method
        Request request = parser.parse("Register request for warnings: " + categoryNames);

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
    }

    @Test
    public void testAllCategoriesInLowerCase() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        String categoryNames = "connection,comtask,collected_data_processing,logging";

        //Business method
        Request request = parser.parse("Register request for info: " + categoryNames);

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
    }

    @Test
    public void testAllCategoriesInMixedCase() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        String categoryNames = "connECtion,comTask,colleCTed_Data_Processing,loGGinG";

        //Business method
        Request request = parser.parse("Register request for debugging: " + categoryNames);

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
    }

    @Test
    public void testNoCategories() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for tracing:");

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
    }

    @Test
    public void testErrorCategory() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for errors: CONNECTION");

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION);
        assertThat(loggingRequest.getLevel()).isEqualTo(LogLevel.ERROR);
    }

    @Test
    public void testNoDebuggingCategories() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for events for debugging:");

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
        assertThat(loggingRequest.getLevel()).isEqualTo(LogLevel.DEBUG);
    }

    @Test
    public void testNoDevices() throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for device:");

        // Asserts
        assertThat(request).isInstanceOf(AllDevicesRequest.class);
    }

    @Test
    public void testOneDevice() throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for device: " + DEVICE1_MRID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID);
    }

    @Test
    public void testOneDeviceWithMRID() throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for device: " + DEVICE1_MRID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID);
    }

    @Test(expected = CanNotFindForIdentifier.class)
    public void testOneDeviceWithMRIDAndUnknown() throws RequestParseException {
        doThrow(CanNotFindForIdentifier.class).when(this.identificationService).createDeviceIdentifierByMRID(anyString());
        this.mockDevices();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for device: unknown");

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).isEmpty();
    }

    public void testMultipleDeviceWithMRID() throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for device: " + DEVICE1_MRID + ","+DEVICE2_MRID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID, DEVICE2_ID);
    }

    @Test
    public void testMultipleDeviceWithMRIDAndSpaces() throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for device: " + DEVICE1_MRID + ", "+DEVICE2_MRID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID, DEVICE2_ID);
    }

    @Test(expected = BusinessObjectParseException.class)
    public void testMultipleDeviceWithMRIDAndUnknown() throws RequestParseException {
        doThrow(CanNotFindForIdentifier.device(mock(DeviceIdentifier.class), null)).when(this.identificationService).createDeviceIdentifierByMRID("unknown");
        this.mockDevices();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for device: " + DEVICE1_MRID + ", "+DEVICE2_MRID + ", unknown");

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID, DEVICE2_ID);
    }


    @Test(expected = BusinessObjectParseException.class)
    public void testNonExistingDevice() throws RequestParseException {
        doThrow(CanNotFindForIdentifier.device(mock(DeviceIdentifier.class), null)).when(this.identificationService).createDeviceIdentifierByMRID(NON_EXISTING_DEVICE_MRID);
        this.mockDevices();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for device: " + NON_EXISTING_DEVICE_MRID);

        // Expected BusinessObjectIdParseException because the device request does not exist
    }

    @Test
    public void testDeviceMixedCase() throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for Device: " + DEVICE1_MRID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID);
    }

    @Test
    public void testNoConnectionTask() throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for connectionTask:");

        // Asserts
        assertThat(request).isInstanceOf(AllConnectionTasksRequest.class);
    }

    @Test
    public void testOneConnectionTask() throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for connectionTask: " + CONNECTION_TASK1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ConnectionTaskRequest.class);
        ConnectionTaskRequest connectionTaskRequest = (ConnectionTaskRequest) request;
        assertThat(connectionTaskRequest.getBusinessObjectIds()).containsOnly(CONNECTION_TASK1_ID);
    }

    @Test
    public void testMultipleConnectionTaskIds() throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for connectionTask: " + CONNECTION_TASK1_ID + "," + CONNECTION_TASK2_ID);

        // Asserts
        assertThat(request).isInstanceOf(ConnectionTaskRequest.class);
        ConnectionTaskRequest connectionTaskRequest = (ConnectionTaskRequest) request;
        assertThat(connectionTaskRequest.getBusinessObjectIds()).containsOnly(CONNECTION_TASK1_ID, CONNECTION_TASK2_ID);
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonExistingConnectionTask() throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);
        when(this.connectionTaskService.findConnectionTask(NON_EXISTING_CONNECTION_TASK_ID)).thenReturn(Optional.empty());

        //Business method
        parser.parse("Register request for connectionTask: " + NON_EXISTING_CONNECTION_TASK_ID);

        // Expected BusinessObjectIdParseException because the connectionTask does not exist
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonNumericalConnectionTaskId() throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for connectionTask: notAnumber");

        // Expected BusinessObjectIdParseException because the connectionTask id is not a number
    }

    @Test
    public void testConnectionTaskMixedCase() throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("register Request for Connectiontask: " + CONNECTION_TASK1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ConnectionTaskRequest.class);
        ConnectionTaskRequest connectionTaskRequest = (ConnectionTaskRequest) request;
        assertThat(connectionTaskRequest.getBusinessObjectIds()).containsOnly(CONNECTION_TASK1_ID);
    }

    @Test
    public void testNoComTask() throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comTaskExecution:");

        // Asserts
        assertThat(request).isInstanceOf(AllComTaskExecutionsRequest.class);
    }

    @Test
    public void testOneComTask() throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comTaskExecution: " + COM_TASK_EXECUTION1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComTaskExecutionRequest.class);
        ComTaskExecutionRequest comTaskExecutionRequest = (ComTaskExecutionRequest) request;
        assertThat(comTaskExecutionRequest.getBusinessObjectIds()).containsOnly(COM_TASK_EXECUTION1_ID);
    }

    @Test
    public void testMultipleComTaskIds() throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comTaskExecution: " + COM_TASK_EXECUTION1_ID + "," + COM_TASK_EXECUTION2_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComTaskExecutionRequest.class);
        ComTaskExecutionRequest comTaskExecutionRequest = (ComTaskExecutionRequest) request;
        assertThat(comTaskExecutionRequest.getBusinessObjectIds()).containsOnly(COM_TASK_EXECUTION1_ID, COM_TASK_EXECUTION2_ID);
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonExistingComTaskExecution() throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);
        when(this.communicationTaskService.findComTaskExecution(NON_EXISTING_COMTASK_TASK_ID)).thenReturn(Optional.empty());

        //Business method
        parser.parse("Register request for comTaskExecution: " + NON_EXISTING_COMTASK_TASK_ID);

        // Expected BusinessObjectIdParseException because the comTask does not exist
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonNumericalComTaskId() throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for comTaskExecution: notAnumber");

        // Expected BusinessObjectIdParseException because the comTask id is not a number
    }

    @Test
    public void testComTaskMixedCase() throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("register request for ComtaskExecution: " + COM_TASK_EXECUTION1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComTaskExecutionRequest.class);
        ComTaskExecutionRequest comTaskExecutionRequest = (ComTaskExecutionRequest) request;
        assertThat(comTaskExecutionRequest.getBusinessObjectIds()).containsOnly(COM_TASK_EXECUTION1_ID);
    }

    @Test
    public void testNoComPort() throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPort:");

        // Asserts
        assertThat(request).isInstanceOf(AllComPortsRequest.class);
    }

    @Test
    public void testOneComPort() throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPort: " + COM_PORT1_NAME);

        // Asserts
        assertThat(request).isInstanceOf(ComPortRequest.class);
        ComPortRequest comPortRequest = (ComPortRequest) request;
        assertThat(comPortRequest.getBusinessObjectIds()).containsOnly(COM_PORT1_ID);
    }

    @Test
    public void testOneComPortWithName() throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPort: " + COM_PORT1_NAME);

        // Asserts
        assertThat(request).isInstanceOf(ComPortRequest.class);
        ComPortRequest comPortRequest = (ComPortRequest) request;
        assertThat(comPortRequest.getBusinessObjectIds()).containsOnly(COM_PORT1_ID);
    }

    @Test
    public void testMultipleComPortWithName() throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPort: " + COM_PORT1_NAME + "," + COM_PORT2_NAME);

        // Asserts
        assertThat(request).isInstanceOf(ComPortRequest.class);
        ComPortRequest comPortRequest = (ComPortRequest) request;
        assertThat(comPortRequest.getBusinessObjectIds()).containsOnly(COM_PORT1_ID, COM_PORT2_ID);
    }

    @Test
    public void testMultipleComPortsWithTheSameName() throws RequestParseException {
        this.mockComPorts();
        this.mockSameComPortsOnAnotherServer();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPort: " + COM_PORT1_NAME + "," + COM_PORT2_NAME);

        // Asserts
        assertThat(request).isInstanceOf(ComPortRequest.class);
        ComPortRequest comPortRequest = (ComPortRequest) request;
        assertThat(comPortRequest.getBusinessObjectIds()).containsOnly(COM_PORT1_ID, COM_PORT2_ID);
    }

    @Test
    public void testMultipleComPortWithNameAndSpaces() throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPort: " + COM_PORT1_NAME + " , " + COM_PORT2_NAME + ", ,,");

        // Asserts
        assertThat(request).isInstanceOf(ComPortRequest.class);
        ComPortRequest comPortRequest = (ComPortRequest) request;
        assertThat(comPortRequest.getBusinessObjectIds()).containsOnly(COM_PORT1_ID, COM_PORT2_ID);
    }

    @Test(expected = BusinessObjectParseException.class)
    public void testNonExistingComPort() throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for comPort: " + NON_EXISTING_COMPORT_ID);

        // Expected BusinessObjectIdParseException because the comPort request accepts only 1 comPort id
    }

    @Test
    public void testComPortMixedCase() throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for coMPorT: " + COM_PORT1_NAME);

        // Asserts
        assertThat(request).isInstanceOf(ComPortRequest.class);
        ComPortRequest comPortRequest = (ComPortRequest) request;
        assertThat(comPortRequest.getBusinessObjectIds()).containsOnly(COM_PORT1_ID);
    }

    @Test
    public void testNoComPortPool() throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPortPool: ");

        // Asserts
        assertThat(request).isInstanceOf(AllComPortPoolsRequest.class);
    }

    @Test
    public void testOneComPortPool() throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPortPool: " + COM_PORT_POOL1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComPortPoolRequest.class);
        ComPortPoolRequest comPortPoolRequest = (ComPortPoolRequest) request;
        assertThat(comPortPoolRequest.getBusinessObjectIds()).containsOnly(COM_PORT_POOL1_ID);
    }

    @Test
    public void testMultipleComPortPoolIds() throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPortPool: " + COM_PORT_POOL1_ID + "," + COM_PORT_POOL2_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComPortPoolRequest.class);
        ComPortPoolRequest comPortPoolRequest = (ComPortPoolRequest) request;
        assertThat(comPortPoolRequest.getBusinessObjectIds()).containsOnly(COM_PORT_POOL1_ID, COM_PORT_POOL2_ID);
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonExistingComPortPool() throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for comPortPool: " + NON_EXISTING_COMPORT_POOL_ID);

        // Expected BusinessObjectIdParseException because the ComPortPool does not exist
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonNumericalComPortPoolId() throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        parser.parse("Register request for comPortPool: notAnumber");

        // Expected BusinessObjectIdParseException because the comPortPool id is not a number
    }

    @Test
    public void testComPortPoolMixedCase() throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse("Register request for coMporTpooL: " + COM_PORT_POOL1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComPortPoolRequest.class);
        ComPortPoolRequest comPortPoolRequest = (ComPortPoolRequest) request;
        assertThat(comPortPoolRequest.getBusinessObjectIds()).containsOnly(COM_PORT_POOL1_ID);
    }

    @Test
    public void testPing() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse(RequestParser.PING_MESSAGE);

        //Asserts
        assertThat(request).isNotNull();
    }

    @Test
    public void testPong() throws RequestParseException {
        RequestParser parser = new RequestParser(this.runningComServer, this.serviceProvider);

        //Business method
        Request request = parser.parse(RequestParser.PONG_MESSAGE);

        //Asserts
        assertThat(request).isNotNull();
    }

    private void mockDevices() {
        this.mockDevice(DEVICE1_ID);
        this.mockDevice(DEVICE2_ID);
    }

    private void mockDevice(long deviceId) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(deviceId);
        when(this.deviceService.findDeviceById(deviceId)).thenReturn(Optional.of(device));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        when(deviceIdentifier.getDeviceIdentifierType()).thenReturn(DeviceIdentifierType.ActualDevice);
        when(deviceIdentifier.getIdentifier()).thenReturn(String.valueOf(deviceId));
        when(this.identificationService.createDeviceIdentifierByDatabaseId(deviceId)).thenReturn(deviceIdentifier);
        when(this.identificationService.createDeviceIdentifierByMRID("MRID_"+deviceId)).thenReturn(deviceIdentifier);
    }

    private void mockConnectionTasks() {
        ConnectionTask connectionTask1 = mock(ConnectionTask.class);
        when(connectionTask1.getId()).thenReturn(CONNECTION_TASK1_ID);
        ConnectionTask connectionTask2 = mock(ConnectionTask.class);
        when(connectionTask2.getId()).thenReturn(CONNECTION_TASK2_ID);
        when(this.connectionTaskService.findConnectionTask(CONNECTION_TASK1_ID)).thenReturn(Optional.of(connectionTask1));
        when(this.connectionTaskService.findConnectionTask(CONNECTION_TASK2_ID)).thenReturn(Optional.of(connectionTask2));
    }

    private void mockComTaskExecutions() {
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        when(comTaskExecution1.getId()).thenReturn(COM_TASK_EXECUTION1_ID);
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        when(comTaskExecution2.getId()).thenReturn(COM_TASK_EXECUTION2_ID);
        when(this.communicationTaskService.findComTaskExecution(COM_TASK_EXECUTION1_ID)).thenReturn(Optional.of(comTaskExecution1));
        when(this.communicationTaskService.findComTaskExecution(COM_TASK_EXECUTION2_ID)).thenReturn(Optional.of(comTaskExecution2));
    }

    private void mockComPorts() {
        OutboundComPort comPort1 = mock(OutboundComPort.class);
        when(comPort1.getId()).thenReturn(COM_PORT1_ID);
        when(comPort1.getName()).thenReturn(COM_PORT1_NAME);
        OutboundComPort comPort2 = mock(OutboundComPort.class);
        when(comPort2.getId()).thenReturn(COM_PORT2_ID);
        when(comPort2.getName()).thenReturn(COM_PORT2_NAME);
        doReturn(Optional.of(comPort1)).when(this.engineConfigurationService).findComPort(COM_PORT1_ID);
        doReturn(Optional.of(comPort2)).when(this.engineConfigurationService).findComPort(COM_PORT2_ID);
        when(this.comServer.getComPorts()).thenReturn(Arrays.asList(comPort1, comPort2));
        when(this.comServer.getOutboundComPorts()).thenReturn(Arrays.asList(comPort1, comPort2));
        doReturn(Optional.empty()).when(this.engineConfigurationService).findComPort(NON_EXISTING_COMPORT_ID);
    }

    private void mockSameComPortsOnAnotherServer() {
        OutboundCapableComServer otherComServer = mock(OutboundCapableComServer.class);
        OutboundComPort comPort1 = mock(OutboundComPort.class);
        when(comPort1.getId()).thenReturn(COM_PORT1_ID * 2);
        when(comPort1.getName()).thenReturn(COM_PORT1_NAME);
        OutboundComPort comPort2 = mock(OutboundComPort.class);
        when(comPort2.getId()).thenReturn(COM_PORT2_ID * 2);
        when(comPort2.getName()).thenReturn(COM_PORT2_NAME);
        doReturn(Optional.of(comPort1)).when(this.engineConfigurationService).findComPort(COM_PORT1_ID * 2);
        doReturn(Optional.of(comPort2)).when(this.engineConfigurationService).findComPort(COM_PORT2_ID * 2);
        when(otherComServer.getComPorts()).thenReturn(Arrays.asList(comPort1, comPort2));
        when(otherComServer.getOutboundComPorts()).thenReturn(Arrays.asList(comPort1, comPort2));
        doReturn(Optional.empty()).when(this.engineConfigurationService).findComPort(NON_EXISTING_COMPORT_ID);
    }

    private void mockComPortPools() {
        ComPortPool comPortPool1 = mock(ComPortPool.class);
        when(comPortPool1.getId()).thenReturn(COM_PORT_POOL1_ID);
        ComPortPool comPortPool2 = mock(ComPortPool.class);
        when(comPortPool2.getId()).thenReturn(COM_PORT_POOL2_ID);
        doReturn(Optional.of(comPortPool1)).when(this.engineConfigurationService).findComPortPool(COM_PORT_POOL1_ID);
        doReturn(Optional.of(comPortPool2)).when(this.engineConfigurationService).findComPortPool(COM_PORT_POOL2_ID);
        doReturn(Optional.empty()).when(this.engineConfigurationService).findComPortPool(NON_EXISTING_COMPORT_POOL_ID);
    }

}