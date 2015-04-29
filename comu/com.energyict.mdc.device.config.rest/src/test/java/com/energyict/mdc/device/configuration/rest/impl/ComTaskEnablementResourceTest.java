package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareUpgradeOptions;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.FirmwareUpgradeTask;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComTaskEnablementResourceTest extends DeviceConfigurationApplicationJerseyTest {

    private final long firmwareComTaskId = 103;
    private final long loadProfilesComTaskId = 16;
    private final long registersComTaskId = 645;
    private final long topologyComTaskId = 46;
    private final String topologyComTaskName = "TopologyComTask";
    private final String registersComTaskName = "RegistersComTask";
    private final String loadProfilesComTaskName = "LoadProfilesComTask";
    private final String firmwareComTaskName = "Firmware management";

    @Before
    public void initBefore() {
        when(firmwareService.isFirmwareUpgradeAllowedFor(any(DeviceType.class))).thenReturn(false);
    }

    @Test
    public void getAllowedComTasksWhichAreNotDefinedYetNoComTasksWithFirmwareTest() {
        mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade();
        ComTask firmwareComTask = mockFirmwareComTask();
        when(taskService.findAllComTasks()).thenReturn(Arrays.asList(firmwareComTask));
        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List) map.get("data")).hasSize(1);
    }

    @Test
    public void getAllowedComTasksWhichAreNotDefinedYetNoComTasksWithFirmwareButDisallowTest() {
        mockSimpleDeviceTypeAndConfig();
        ComTask firmwareComTask = mockFirmwareComTask();
        when(taskService.findAllComTasks()).thenReturn(Arrays.asList(firmwareComTask));
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
        when(taskService.findAllComTasks()).thenReturn(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
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
        when(taskService.findAllComTasks()).thenReturn(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
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
        when(taskService.findAllComTasks()).thenReturn(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
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
        when(taskService.findAllComTasks()).thenReturn(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
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
        when(taskService.findAllComTasks()).thenReturn(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
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
        FirmwareUpgradeOptions firmwareUpgradeOption = mock(FirmwareUpgradeOptions.class);
        when(firmwareService.isFirmwareUpgradeAllowedFor(deviceType)).thenReturn(true);
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
        FirmwareUpgradeTask firmwareUpgradeTask = mock(FirmwareUpgradeTask.class);
        when(firmwareComTask.getProtocolTasks()).thenReturn(Arrays.asList(firmwareUpgradeTask));
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
}