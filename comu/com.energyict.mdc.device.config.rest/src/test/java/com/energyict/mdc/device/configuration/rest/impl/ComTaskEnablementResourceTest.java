package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComTaskEnablementResourceTest extends DeviceConfigurationApplicationJerseyTest {

    private final static long firmwareComTaskId = 103;
    private final static long loadProfilesComTaskId = 16;
    private final static long registersComTaskId = 645;
    private final static long topologyComTaskId = 46;
    private final static String topologyComTaskName = "TopologyComTask";
    private final static String registersComTaskName = "RegistersComTask";
    private final static String loadProfilesComTaskName = "LoadProfilesComTask";
    private final static String firmwareComTaskName = "Firmware management";

    @Before
    public void initBefore() {
        when(firmwareService.findFirmwareManagementOptionsByDeviceType(any(DeviceType.class))).thenReturn(Optional.<FirmwareManagementOptions>empty());
    }

    @Test
    public void getAllowedComTasksWhichAreNotDefinedYetNoComTasksWithFirmwareTest() {
        mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade();
        ComTask firmwareComTask = mockFirmwareComTask();
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List) map.get("data")).hasSize(1);
    }

    @Test
    public void getAllowedComTasksWhichAreNotDefinedYetNoComTasksWithFirmwareButDisallowTest() {
        mockSimpleDeviceTypeAndConfig();
        ComTask firmwareComTask = mockFirmwareComTask();
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).hasSize(0);
    }

    @Test
    public void getAllowedComTaskWhichAreNotDefinedYetWithSomeDefinedNoFirmwareDeviceTypeAllowsFirmwareTest() {
        DeviceType deviceType = mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade();
        DeviceConfiguration deviceConfiguration = deviceType.getConfigurations().get(0);
        ComTask firmwareComTask = mockFirmwareComTask();
        ComTask loadProfilesComTask = mockLoadProfilesComTask();
        ComTaskEnablement loadProfilesComTaskEnablement = mockLoadProfilesComTaskEnablement(loadProfilesComTask);
        ComTask registersComTask = mockRegistersComTask();
        ComTaskEnablement registersComTaskEnablement = mockRegistersComTaskEnablement(registersComTask);
        ComTask topologyComTask = mockTopologyComTask();
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(loadProfilesComTaskEnablement, registersComTaskEnablement));

        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(2);
        assertThat((List) map.get("data")).hasSize(2);
        assertThat(((Map) ((List) map.get("data")).get(0)).get("name")).isEqualTo(firmwareComTaskName);
        assertThat(((Map) ((List) map.get("data")).get(1)).get("name")).isEqualTo(topologyComTaskName);
    }

    @Test
    public void getAllowedComTaskWhichAreNotDefinedYetWithSomeDefinedNoFirmwareDeviceTypeDoesntAllowFirmwareTest() {
        DeviceType deviceType = mockSimpleDeviceTypeAndConfig();
        DeviceConfiguration deviceConfiguration = deviceType.getConfigurations().get(0);
        ComTask firmwareComTask = mockFirmwareComTask();
        ComTask loadProfilesComTask = mockLoadProfilesComTask();
        ComTaskEnablement loadProfilesComTaskEnablement = mockLoadProfilesComTaskEnablement(loadProfilesComTask);
        ComTask registersComTask = mockRegistersComTask();
        ComTaskEnablement registersComTaskEnablement = mockRegistersComTaskEnablement(registersComTask);
        ComTask topologyComTask = mockTopologyComTask();
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(loadProfilesComTaskEnablement, registersComTaskEnablement));

        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List) map.get("data")).hasSize(1);
        assertThat(((Map) ((List) map.get("data")).get(0)).get("name")).isEqualTo(topologyComTaskName);
    }

    @Test
    public void getAllowedComTaskWhichAreNotDefinedYetWithSomeDefinedWithFirmwareDeviceTypeAllowFirmwareTest() {
        DeviceType deviceType = mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade();
        DeviceConfiguration deviceConfiguration = deviceType.getConfigurations().get(0);
        ComTask firmwareComTask = mockFirmwareComTask();
        ComTask loadProfilesComTask = mockLoadProfilesComTask();
        ComTaskEnablement loadProfilesComTaskEnablement = mockLoadProfilesComTaskEnablement(loadProfilesComTask);
        ComTask registersComTask = mockRegistersComTask();
        ComTaskEnablement registersComTaskEnablement = mockRegistersComTaskEnablement(registersComTask);
        ComTask topologyComTask = mockTopologyComTask();
        ComTaskEnablement firmwareComTaskEnablement = mockFirmwareComTaskEnablement(firmwareComTask);
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(loadProfilesComTaskEnablement, registersComTaskEnablement, firmwareComTaskEnablement));

        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List) map.get("data")).hasSize(1);
        assertThat(((Map) ((List) map.get("data")).get(0)).get("name")).isEqualTo(topologyComTaskName);
    }

    @Test
    public void getAllowedComTaskWhichAreNotDefinedYetWithAllDefinedWithFirmwareDeviceTypeAllowFirmwareTest() {
        DeviceType deviceType = mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade();
        DeviceConfiguration deviceConfiguration = deviceType.getConfigurations().get(0);
        ComTask firmwareComTask = mockFirmwareComTask();
        ComTask loadProfilesComTask = mockLoadProfilesComTask();
        ComTaskEnablement loadProfilesComTaskEnablement = mockLoadProfilesComTaskEnablement(loadProfilesComTask);
        ComTask registersComTask = mockRegistersComTask();
        ComTaskEnablement registersComTaskEnablement = mockRegistersComTaskEnablement(registersComTask);
        ComTask topologyComTask = mockTopologyComTask();
        ComTaskEnablement topologyComTaskEnablement = mockTopologyComTaskEnablement(topologyComTask);
        ComTaskEnablement firmwareComTaskEnablement = mockFirmwareComTaskEnablement(firmwareComTask);
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(loadProfilesComTaskEnablement, topologyComTaskEnablement, registersComTaskEnablement, firmwareComTaskEnablement));

        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).hasSize(0);
    }

    @Test
    public void getAllowedComTaskWhichAreNotDefinedYetWithAllDefinedWithFirmwareDeviceTypeDoesntAllowFirmwareTest() {
        DeviceType deviceType = mockSimpleDeviceTypeAndConfig();
        DeviceConfiguration deviceConfiguration = deviceType.getConfigurations().get(0);
        ComTask firmwareComTask = mockFirmwareComTask();
        ComTask loadProfilesComTask = mockLoadProfilesComTask();
        ComTaskEnablement loadProfilesComTaskEnablement = mockLoadProfilesComTaskEnablement(loadProfilesComTask);
        ComTask registersComTask = mockRegistersComTask();
        ComTaskEnablement registersComTaskEnablement = mockRegistersComTaskEnablement(registersComTask);
        ComTask topologyComTask = mockTopologyComTask();
        ComTaskEnablement topologyComTaskEnablement = mockTopologyComTaskEnablement(topologyComTask);
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(loadProfilesComTaskEnablement, topologyComTaskEnablement, registersComTaskEnablement));

        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).hasSize(0);
    }

    private ComTaskEnablement mockTopologyComTaskEnablement(ComTask topologyComTask) {
        ComTaskEnablement topologyComTaskEnablement = mock(ComTaskEnablement.class);
        when(topologyComTaskEnablement.getComTask()).thenReturn(topologyComTask);
        return topologyComTaskEnablement;
    }

    private ComTaskEnablement mockFirmwareComTaskEnablement(ComTask firmwareComTask) {
        ComTaskEnablement firmwareComTaskEnablement = mock(ComTaskEnablement.class);
        when(firmwareComTaskEnablement.getComTask()).thenReturn(firmwareComTask);
        return firmwareComTaskEnablement;
    }

    private ComTask mockTopologyComTask() {
        ComTask topologyComTask = mock(ComTask.class);
        when(topologyComTask.getId()).thenReturn(topologyComTaskId);
        when(topologyComTask.getName()).thenReturn(topologyComTaskName);
        return topologyComTask;
    }

    private ComTaskEnablement mockRegistersComTaskEnablement(ComTask registersComTask) {
        ComTaskEnablement registersComTaskEnablement = mock(ComTaskEnablement.class);
        when(registersComTaskEnablement.getComTask()).thenReturn(registersComTask);
        return registersComTaskEnablement;
    }

    private ComTask mockRegistersComTask() {
        ComTask registersComTask = mock(ComTask.class);
        when(registersComTask.getId()).thenReturn(registersComTaskId);
        when(registersComTask.getName()).thenReturn(registersComTaskName);
        return registersComTask;
    }

    private ComTaskEnablement mockLoadProfilesComTaskEnablement(ComTask loadProfilesComTask) {
        ComTaskEnablement loadProfilesComTaskEnablement = mock(ComTaskEnablement.class);
        when(loadProfilesComTaskEnablement.getComTask()).thenReturn(loadProfilesComTask);
        return loadProfilesComTaskEnablement;
    }

    private ComTask mockLoadProfilesComTask() {
        ComTask loadProfilesComTask = mock(ComTask.class);
        when(loadProfilesComTask.getId()).thenReturn(loadProfilesComTaskId);
        when(loadProfilesComTask.getName()).thenReturn(loadProfilesComTaskName);
        return loadProfilesComTask;
    }

    private DeviceType mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade() {
        DeviceType deviceType = mockSimpleDeviceTypeAndConfig();
        FirmwareManagementOptions firmwareUpgradeOption = mock(FirmwareManagementOptions.class);
        when(firmwareService.findFirmwareManagementOptionsByDeviceType(deviceType)).thenReturn(Optional.of(firmwareUpgradeOption));
        return deviceType;
    }

    private DeviceType mockSimpleDeviceTypeAndConfig() {
        DeviceType deviceType = mockDeviceType("device", 1);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("config", 1, deviceType);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));
        return deviceType;
    }

    private ComTask mockFirmwareComTask() {
        ComTask firmwareComTask = mock(ComTask.class);
        when(firmwareComTask.getId()).thenReturn(firmwareComTaskId);
        when(firmwareComTask.getName()).thenReturn(firmwareComTaskName);
        FirmwareManagementTask firmwareManagementTask = mock(FirmwareManagementTask.class);
        when(firmwareComTask.getProtocolTasks()).thenReturn(Arrays.asList(firmwareManagementTask));
        when(taskService.findFirmwareComTask()).thenReturn(Optional.of(firmwareComTask));
        return firmwareComTask;
    }

    protected DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        return deviceType;
    }

    protected DeviceConfiguration mockDeviceConfiguration(String name, long id, DeviceType deviceType) {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name);
        when(deviceConfiguration.getId()).thenReturn(id);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        return deviceConfiguration;
    }

    <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }


}