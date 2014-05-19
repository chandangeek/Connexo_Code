package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import com.google.common.base.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
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
    private static final long CONNECTION_TASK1_ID = DEVICE2_ID + 1;
    private static final long CONNECTION_TASK2_ID = CONNECTION_TASK1_ID + 1;
    private static final long COM_TASK_EXECUTION1_ID = CONNECTION_TASK2_ID + 1;
    private static final long COM_TASK_EXECUTION2_ID = COM_TASK_EXECUTION1_ID + 1;
    private static final long COM_PORT1_ID = COM_TASK_EXECUTION2_ID + 1;
    private static final long COM_PORT2_ID = COM_PORT1_ID + 1;
    private static final long COM_PORT_POOL1_ID = COM_PORT2_ID+ 1;
    private static final long COM_PORT_POOL2_ID = COM_PORT_POOL1_ID + 1;
    private static final long NON_EXISTING_DEVICE_ID = 999;
    private static final long NON_EXISTING_CONNECTION_TASK_ID = NON_EXISTING_DEVICE_ID - 1;
    private static final long NON_EXISTING_COMTASK_TASK_ID = NON_EXISTING_CONNECTION_TASK_ID - 1;
    private static final long NON_EXISTING_COMPORT_ID = NON_EXISTING_COMTASK_TASK_ID - 1;
    private static final long NON_EXISTING_COMPORT_POOL_ID = NON_EXISTING_COMPORT_ID - 1;

    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private EngineModelService engineModelService;

    @Test(expected = UnexpectedRequestFormatException.class)
    public void testWrongFormat () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Anything as long as it does not conform to the expected parse format");

        // Expected UnexpectedRequestFormatException because the request does not match with the expected request pattern
    }

    @Test(expected = RequestTypeParseException.class)
    public void testUnknownRequestType () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for something: Unknown");

        // Expected an RequestTypeParseException because "something" is not a known request type
    }

    @Test(expected = UnknownCategoryParseException.class)
    public void testNonExistingErrorCategory () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for errors: Unknown");

        // Expected an UnknownCategoryParseException because "Unknown" is not a known Category
    }

    @Test(expected = UnknownCategoryParseException.class)
    public void testNonExistingWarningCategory () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for warnings: Unknown");

        // Expected an UnknownCategoryParseException because "Unknown" is not a known Category
    }

    @Test(expected = UnknownCategoryParseException.class)
    public void testNonExistingInfoCategory () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for info: Unknown");

        // Expected an UnknownCategoryParseException because "Unknown" is not a known Category
    }

    @Test(expected = UnknownCategoryParseException.class)
    public void testNonExistingDebugCategory () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for debugging: Unknown");

        // Expected an UnknownCategoryParseException because "Unknown" is not a known Category
    }

    @Test(expected = UnknownCategoryParseException.class)
    public void testNonExistingTraceCategory () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for tracing: Unknown");

        // Expected an UnknownCategoryParseException because "Unknown" is not a known Category
    }

    @Test
    public void testErrorCategoriesSeparatedBySpaces () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        String categoryNames = "CONNECTION COMTASK COLLECTED_DATA_PROCESSING LOGGING";
        try {
            //Business method
            parser.parse("Register request for errors: " + categoryNames);
        }
        catch (UnknownCategoryParseException e) {
            assertThat(e.getMessage()).contains(categoryNames);
        }
    }

    @Test
    public void testAllCategoriesInUpperCase () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        String categoryNames = "CONNECTION,COMTASK,COLLECTED_DATA_PROCESSING,LOGGING";

        //Business method
        Request request = parser.parse("Register request for warnings: " + categoryNames);

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
    }

    @Test
    public void testAllCategoriesInLowerCase () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        String categoryNames = "connection,comtask,collected_data_processing,logging";

        //Business method
        Request request = parser.parse("Register request for info: " + categoryNames);

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
    }

    @Test
    public void testAllCategoriesInMixedCase () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        String categoryNames = "connECtion,comTask,colleCTed_Data_Processing,loGGinG";

        //Business method
        Request request = parser.parse("Register request for debugging: " + categoryNames);

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
    }

    @Test
    public void testNoCategories () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for tracing:");

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
    }

    @Test
    public void testErrorCategoryWithDefaultMarker () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for errors: CONNECTION");

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION);
        assertThat(loggingRequest.getLevel()).isEqualTo(LogLevel.ERROR);
    }

    @Test
    public void testErrorCategoryWithBinaryMarker () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for binary events for errors: CONNECTION");

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        assertThat(request.useBinaryEvents()).isTrue();
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION);
        assertThat(loggingRequest.getLevel()).isEqualTo(LogLevel.ERROR);
    }

    @Test
    public void testTracingCategoryWithTextMarker () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for text events for tracing: CONNECTION");

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION);
        assertThat(loggingRequest.getLevel()).isEqualTo(LogLevel.TRACE);
    }

    @Test
    public void testNoDebuggingCategoriesWithBinaryMarker () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for binary events for debugging:");

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        assertThat(request.useBinaryEvents()).isTrue();
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
        assertThat(loggingRequest.getLevel()).isEqualTo(LogLevel.DEBUG);
    }

    @Test
    public void testNoInfoCategoriesWithTextMarker () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for text events for info:");

        // Asserts
        assertThat(request).isInstanceOf(LoggingRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
        LoggingRequest loggingRequest = (LoggingRequest) request;
        assertThat(loggingRequest.getCategories()).containsOnly(Category.CONNECTION, Category.COMTASK, Category.COLLECTED_DATA_PROCESSING, Category.LOGGING);
        assertThat(loggingRequest.getLevel()).isEqualTo(LogLevel.INFO);
    }

    @Test(expected = RequestTypeParseException.class)
    public void testNoCategoriesWithUnknownMarker () throws RequestParseException {
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for encrypted events for warning:");

        // Expected RequestTypeParseException because the request does not match with the expected request pattern
    }

    @Test
    public void testNoDevicesWihtoutMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for device:");

        // Asserts
        assertThat(request).isInstanceOf(AllDevicesRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
    }

    @Test
    public void testNoDevicesWithBinaryMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for binary events for device:");

        // Asserts
        assertThat(request).isInstanceOf(AllDevicesRequest.class);
        assertThat(request.useBinaryEvents()).isTrue();
    }

    @Test
    public void testNoDevicesWithTextMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for text events for device:");

        // Asserts
        assertThat(request).isInstanceOf(AllDevicesRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
    }

    @Test
    public void testOneDeviceWithoutMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for device: " + DEVICE1_ID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID);
    }

    @Test
    public void testOneDeviceWithBinaryMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for binary events for device: " + DEVICE1_ID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        assertThat(request.useBinaryEvents()).isTrue();
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID);
    }

    @Test
    public void testOneDeviceWithTextMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for text events for device: " + DEVICE1_ID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID);
    }

    @Test
    public void testMultipleDeviceIdsWithoutMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for device: " + DEVICE1_ID + "," + DEVICE2_ID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID, DEVICE2_ID);
    }

    @Test
    public void testMultipleDeviceIdsWithBinaryMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for binary events for device: " + DEVICE1_ID + "," + DEVICE2_ID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        assertThat(request.useBinaryEvents()).isTrue();
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID, DEVICE2_ID);
    }

    @Test
    public void testMultipleDeviceIdsWithTextMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for text events for device: " + DEVICE1_ID + "," + DEVICE2_ID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID, DEVICE2_ID);
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonExistingDeviceWithoutMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for device: " + NON_EXISTING_DEVICE_ID);

        // Expected BusinessObjectIdParseException because the device request does not exist
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonExistingDeviceWithBinaryMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for binary events for device: " + NON_EXISTING_DEVICE_ID);

        // Expected BusinessObjectIdParseException because the device request does not exist
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonExistingDeviceWithTextMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for text events for device: " + NON_EXISTING_DEVICE_ID);

        // Expected BusinessObjectIdParseException because the device request does not exist
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonNumericalDeviceIdWithoutMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for device: notAnumber");

        // Expected BusinessObjectIdParseException because the device id is not a number
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonNumericalDeviceIdWithBinaryMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for binary events for device: notAnumber");

        // Expected BusinessObjectIdParseException because the device id is not a number
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonNumericalDeviceIdWithTextMarker () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for text events for device: notAnumber");

        // Expected BusinessObjectIdParseException because the device id is not a number
    }

    @Test
    public void testDeviceMixedCase () throws RequestParseException {
        this.mockDevices();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for Device: " + DEVICE1_ID);

        // Asserts
        assertThat(request).isInstanceOf(DeviceRequest.class);
        DeviceRequest deviceRequest = (DeviceRequest) request;
        assertThat(deviceRequest.getBusinessObjectIds()).containsOnly(DEVICE1_ID);
    }

    @Test
    public void testNoConnectionTask () throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for connectionTask:");

        // Asserts
        assertThat(request).isInstanceOf(AllConnectionTasksRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
    }

    @Test
    public void testOneConnectionTask () throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for connectionTask: " + CONNECTION_TASK1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ConnectionTaskRequest.class);
        ConnectionTaskRequest connectionTaskRequest = (ConnectionTaskRequest) request;
        assertThat(connectionTaskRequest.getBusinessObjectIds()).containsOnly(CONNECTION_TASK1_ID);
    }

    @Test
    public void testMultipleConnectionTaskIds () throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for connectionTask: " + CONNECTION_TASK1_ID + "," + CONNECTION_TASK2_ID);

        // Asserts
        assertThat(request).isInstanceOf(ConnectionTaskRequest.class);
        ConnectionTaskRequest connectionTaskRequest = (ConnectionTaskRequest) request;
        assertThat(connectionTaskRequest.getBusinessObjectIds()).containsOnly(CONNECTION_TASK1_ID, CONNECTION_TASK2_ID);
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonExistingConnectionTask () throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for connectionTask: " + NON_EXISTING_CONNECTION_TASK_ID);

        // Expected BusinessObjectIdParseException because the connectionTask does not exist
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonNumericalConnectionTaskId () throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for connectionTask: notAnumber");

        // Expected BusinessObjectIdParseException because the connectionTask id is not a number
    }

    @Test
    public void testConnectionTaskMixedCase () throws RequestParseException {
        this.mockConnectionTasks();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("register Request for Connectiontask: " + CONNECTION_TASK1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ConnectionTaskRequest.class);
        ConnectionTaskRequest connectionTaskRequest = (ConnectionTaskRequest) request;
        assertThat(connectionTaskRequest.getBusinessObjectIds()).containsOnly(CONNECTION_TASK1_ID);
    }

    @Test
    public void testNoComTask () throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comTaskExecution:");

        // Asserts
        assertThat(request).isInstanceOf(AllComTaskExecutionsRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
    }

    @Test
    public void testOneComTask () throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comTaskExecution: " + COM_TASK_EXECUTION1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComTaskExecutionRequest.class);
        ComTaskExecutionRequest comTaskExecutionRequest = (ComTaskExecutionRequest) request;
        assertThat(comTaskExecutionRequest.getBusinessObjectIds()).containsOnly(COM_TASK_EXECUTION1_ID);
    }

    @Test
    public void testMultipleComTaskIds () throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comTaskExecution: " + COM_TASK_EXECUTION1_ID + "," + COM_TASK_EXECUTION2_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComTaskExecutionRequest.class);
        ComTaskExecutionRequest comTaskExecutionRequest = (ComTaskExecutionRequest) request;
        assertThat(comTaskExecutionRequest.getBusinessObjectIds()).containsOnly(COM_TASK_EXECUTION1_ID, COM_TASK_EXECUTION2_ID);
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonExistingComTaskExecution () throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for comTaskExecution: " + NON_EXISTING_COMTASK_TASK_ID);

        // Expected BusinessObjectIdParseException because the comTask does not exist
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonNumericalComTaskId () throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for comTaskExecution: notAnumber");

        // Expected BusinessObjectIdParseException because the comTask id is not a number
    }

    @Test
    public void testComTaskMixedCase () throws RequestParseException {
        this.mockComTaskExecutions();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("register request for ComtaskExecution: " + COM_TASK_EXECUTION1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComTaskExecutionRequest.class);
        ComTaskExecutionRequest comTaskExecutionRequest = (ComTaskExecutionRequest) request;
        assertThat(comTaskExecutionRequest.getBusinessObjectIds()).containsOnly(COM_TASK_EXECUTION1_ID);
    }

    @Test
    public void testNoComPort () throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPort:");

        // Asserts
        assertThat(request).isInstanceOf(AllComPortsRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
    }

    @Test
    public void testOneComPort () throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPort: " + COM_PORT1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComPortRequest.class);
        ComPortRequest comPortRequest = (ComPortRequest) request;
        assertThat(comPortRequest.getBusinessObjectIds()).containsOnly(COM_PORT1_ID);
    }

    @Test
    public void testMultipleComPortIds () throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPort: " + COM_PORT1_ID + "," + COM_PORT2_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComPortRequest.class);
        ComPortRequest comPortRequest = (ComPortRequest) request;
        assertThat(comPortRequest.getBusinessObjectIds()).containsOnly(COM_PORT1_ID, COM_PORT2_ID);
    }

    @Test(expected = RequestParseException.class)
    public void testNonExistingComPort () throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for comPort: " + NON_EXISTING_COMPORT_ID);

        // Expected RequestParseException because the comPort request accepts only 1 comPort id
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonNumericalComPortId () throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for comPort: notAnumber");

        // Expected BusinessObjectIdParseException because the comPort id is not a number
    }

    @Test
    public void testComPortMixedCase () throws RequestParseException {
        this.mockComPorts();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for coMPorT: " + COM_PORT1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComPortRequest.class);
        ComPortRequest comPortRequest = (ComPortRequest) request;
        assertThat(comPortRequest.getBusinessObjectIds()).containsOnly(COM_PORT1_ID);
    }

    @Test
    public void testNoComPortPool () throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPortPool: ");

        // Asserts
        assertThat(request).isInstanceOf(AllComPortPoolsRequest.class);
        assertThat(request.useBinaryEvents()).isFalse();
    }

    @Test
    public void testOneComPortPool () throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPortPool: " + COM_PORT_POOL1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComPortPoolRequest.class);
        ComPortPoolRequest comPortPoolRequest = (ComPortPoolRequest) request;
        assertThat(comPortPoolRequest.getBusinessObjectIds()).containsOnly(COM_PORT_POOL1_ID);
    }

    @Test
    public void testMultipleComPortPoolIds () throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for comPortPool: " + COM_PORT_POOL1_ID + "," + COM_PORT_POOL2_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComPortPoolRequest.class);
        ComPortPoolRequest comPortPoolRequest = (ComPortPoolRequest) request;
        assertThat(comPortPoolRequest.getBusinessObjectIds()).containsOnly(COM_PORT_POOL1_ID, COM_PORT_POOL2_ID);
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonExistingComPortPool () throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for comPortPool: " + NON_EXISTING_COMPORT_POOL_ID);

        // Expected BusinessObjectIdParseException because the ComPortPool does not exist
    }

    @Test(expected = BusinessObjectIdParseException.class)
    public void testNonNumericalComPortPoolId () throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        parser.parse("Register request for comPortPool: notAnumber");

        // Expected BusinessObjectIdParseException because the comPortPool id is not a number
    }

    @Test
    public void testComPortPoolMixedCase () throws RequestParseException {
        this.mockComPortPools();
        RequestParser parser = new RequestParser(serviceProvider);

        //Business method
        Request request = parser.parse("Register request for coMporTpooL: " + COM_PORT_POOL1_ID);

        // Asserts
        assertThat(request).isInstanceOf(ComPortPoolRequest.class);
        ComPortPoolRequest comPortPoolRequest = (ComPortPoolRequest) request;
        assertThat(comPortPoolRequest.getBusinessObjectIds()).containsOnly(COM_PORT_POOL1_ID);
    }

    private void mockDevices () {
        Device device1 = mock(Device.class);
        when(device1.getId()).thenReturn(DEVICE1_ID);
        BaseDevice device2 = mock(BaseDevice.class);
        when(device2.getId()).thenReturn(DEVICE2_ID);
        when(this.deviceDataService.findDeviceById(DEVICE1_ID)).thenReturn(device1);
    }

    private void mockConnectionTasks () {
        ConnectionTask connectionTask1 = mock(ConnectionTask.class);
        when(connectionTask1.getId()).thenReturn(CONNECTION_TASK1_ID);
        ConnectionTask connectionTask2 = mock(ConnectionTask.class);
        when(connectionTask2.getId()).thenReturn(CONNECTION_TASK2_ID);
        when(this.deviceDataService.findConnectionTask(CONNECTION_TASK1_ID)).thenReturn(Optional.of(connectionTask1));
        when(this.deviceDataService.findConnectionTask(CONNECTION_TASK2_ID)).thenReturn(Optional.of(connectionTask2));
    }

    private void mockComTaskExecutions () {
        ComTaskExecution comTaskExecution1 = mock(ComTaskExecution.class);
        when(comTaskExecution1.getId()).thenReturn(COM_TASK_EXECUTION1_ID);
        ComTaskExecution comTaskExecution2 = mock(ComTaskExecution.class);
        when(comTaskExecution2.getId()).thenReturn(COM_TASK_EXECUTION2_ID);
        when(this.deviceDataService.findComTaskExecution(COM_TASK_EXECUTION1_ID)).thenReturn(comTaskExecution1);
        when(this.deviceDataService.findComTaskExecution(COM_TASK_EXECUTION2_ID)).thenReturn(comTaskExecution2);
    }

    private void mockComPorts () {
        ComPort comPort1 = mock(ComPort.class);
        when(comPort1.getId()).thenReturn(COM_PORT1_ID);
        ComPort comPort2 = mock(ComPort.class);
        when(comPort2.getId()).thenReturn(COM_PORT2_ID);
        when(this.engineModelService.findComPort(COM_PORT1_ID)).thenReturn(comPort1);
        when(this.engineModelService.findComPort(COM_PORT2_ID)).thenReturn(comPort2);
    }

    private void mockComPortPools () {
        ComPortPool comPortPool1 = mock(ComPortPool.class);
        when(comPortPool1.getId()).thenReturn(Long.valueOf(COM_PORT_POOL1_ID));
        ComPortPool comPortPool2 = mock(ComPortPool.class);
        when(comPortPool2.getId()).thenReturn(Long.valueOf(COM_PORT_POOL2_ID));
        when(this.engineModelService.findComPortPool(COM_PORT_POOL1_ID)).thenReturn(comPortPool1);
        when(this.engineModelService.findComPortPool(COM_PORT_POOL2_ID)).thenReturn(comPortPool2);
    }

}