package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import org.fest.assertions.core.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 15/07/15
 * Time: 11:21
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigChangeEnginePartialConnectionTasksTest {

    private long incrementalConfigId = 1;
    private long incrementalConTaskId = 1;

    private DeviceType mockDeviceType() {
        return mock(DeviceType.class);
    }

    @Test
    public void deviceTypeHasNoConfigsTest() {
        DeviceType deviceType = mockDeviceType();
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).isEmpty();
    }

    @Test
    public void deviceTypeHasNoActiveDeviceConfigsTest() {
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mock(DeviceConfiguration.class);
        DeviceConfiguration deviceConfiguration2 = mock(DeviceConfiguration.class);
        DeviceConfiguration deviceConfiguration3 = mock(DeviceConfiguration.class);
        DeviceConfiguration deviceConfiguration4 = mock(DeviceConfiguration.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2, deviceConfiguration3, deviceConfiguration4));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).isEmpty();
    }

    @Test
    public void deviceTypeHasActiveConfigsWithNothingDefinedOnItTest() {
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        DeviceConfiguration deviceConfiguration3 = mockActiveDeviceConfiguration();
        DeviceConfiguration deviceConfiguration4 = mockActiveDeviceConfiguration();
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2, deviceConfiguration3, deviceConfiguration4));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).isEmpty();
    }

    @Test
    public void deviceTypeHasTwoConfigsWithExactlyOneConnectionTaskThatMatchesTest() {
        String name = "MyConnectionTaskName";
        DeviceType deviceType = mockDeviceType();
        ConnectionTypePluggableClass connectionTypePluggableClass = mockConnectionTypePluggableClass(1000L);
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask1 = mockPartialConnectionTask(name, connectionTypePluggableClass);
        when(deviceConfiguration1.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask1));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask2 = mockPartialConnectionTask(name, connectionTypePluggableClass);
        when(deviceConfiguration2.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask2));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(2);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask1, partialConnectionTask2, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask2, partialConnectionTask1, DeviceConfigChangeActionType.MATCH);
            }
        });
    }

    private ConnectionTypePluggableClass mockConnectionTypePluggableClass(long id) {
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(connectionTypePluggableClass.getId()).thenReturn(id);
        return connectionTypePluggableClass;
    }

    @Test
    public void deviceTypeHasTwoConfigsWithConnectionTaskThatDontMatchAtAllTest() {
        String name1 = "MyConnectionTaskName1";
        String name2 = "MyConnectionTaskName2";
        ConnectionTypePluggableClass connectionTypePluggableClass1 = mockConnectionTypePluggableClass(100L);
        ConnectionTypePluggableClass connectionTypePluggableClass2 = mockConnectionTypePluggableClass(200L);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask1 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        when(deviceConfiguration1.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask1));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask2 = mockPartialConnectionTask(name2, connectionTypePluggableClass2);
        when(deviceConfiguration2.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask2));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(4);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask1, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, null, partialConnectionTask2, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask2, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, null, partialConnectionTask1, DeviceConfigChangeActionType.ADD);
            }
        });
    }

    @Test
    public void deviceTypeHasTwoConfigsWithConnectionTaskThatMatchOnNameAndConflictOnConnectionTypeTest() {
        String name1 = "MyConnectionTaskName1";
        ConnectionTypePluggableClass connectionTypePluggableClass1 = mockConnectionTypePluggableClass(100L);
        ConnectionTypePluggableClass connectionTypePluggableClass2 = mockConnectionTypePluggableClass(200L);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask1 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        when(deviceConfiguration1.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask1));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask2 = mockPartialConnectionTask(name1, connectionTypePluggableClass2);
        when(deviceConfiguration2.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask2));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(4);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask1, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, null, partialConnectionTask2, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask2, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, null, partialConnectionTask1, DeviceConfigChangeActionType.ADD);
            }
        });
    }

    @Test
    public void deviceTypeHasTwoConfigsWithConnectionTaskThatMatchOnConnectionTypeAndConflictOnNameTest() {
        String name1 = "MyConnectionTaskName1";
        String name2 = "MyConnectionTaskName2";
        ConnectionTypePluggableClass connectionTypePluggableClass1 = mockConnectionTypePluggableClass(100L);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask1 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        when(deviceConfiguration1.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask1));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask2 = mockPartialConnectionTask(name2, connectionTypePluggableClass1);
        when(deviceConfiguration2.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask2));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(2);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask1, partialConnectionTask2, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask2, partialConnectionTask1, DeviceConfigChangeActionType.CONFLICT);
            }
        });
    }

    @Test
    public void unEvenConnectionTasksOneMatchesExactOneNotAtAllTest() {
        String name1 = "MyConnectionTaskName1";
        String name2 = "MyConnectionTaskName2";
        ConnectionTypePluggableClass connectionTypePluggableClass1 = mockConnectionTypePluggableClass(100L);
        ConnectionTypePluggableClass connectionTypePluggableClass2 = mockConnectionTypePluggableClass(200L);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask1 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        PartialConnectionTask partialConnectionTask2 = mockPartialConnectionTask(name2, connectionTypePluggableClass2);
        when(deviceConfiguration1.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask1, partialConnectionTask2));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask3 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        when(deviceConfiguration2.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask3));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(4);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask2, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask1, partialConnectionTask3, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask3, partialConnectionTask1, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, null, partialConnectionTask2, DeviceConfigChangeActionType.ADD);
            }
        });
    }

    @Test
    public void unEvenConnectionTasksOneMatchesExactOneMatchesOnTypeTest() {
        String name1 = "MyConnectionTaskName1";
        String name2 = "MyConnectionTaskName2";
        ConnectionTypePluggableClass connectionTypePluggableClass1 = mockConnectionTypePluggableClass(100L);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask1 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        PartialConnectionTask partialConnectionTask2 = mockPartialConnectionTask(name2, connectionTypePluggableClass1);
        when(deviceConfiguration1.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask1, partialConnectionTask2));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask3 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        when(deviceConfiguration2.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask3));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(4);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask2, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask1, partialConnectionTask3, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask3, partialConnectionTask1, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, null, partialConnectionTask2, DeviceConfigChangeActionType.ADD);
            }
        });
    }

    /**
     * Scenario:
     * <ul>
     * <li>DeviceConfig 1
     * <ul>
     * <li>ConnectionTask1
     * <ul>
     * <li>Name : GPRS</li>
     * <li>ConnectionType : ConnectionType1</li>
     * </ul>
     * </li>
     * <li>ConnectionTask2
     * <ul>
     * <li>Name : Optical</li>
     * <li>ConnectionType : ConnectionType2</li>
     * </ul>
     * </li>
     * </ul>
     * </li>
     * <li>DeviceConfig 2
     * <ul>
     * <li>ConnectionTask1
     * <ul>
     * <li>Name : IP</li>
     * <li>ConnectionType : ConnectionType1</li>
     * </ul>
     * </li>
     * <li>ConnectionTask2
     * <ul>
     * <li>Name : GPRS</li>
     * <li>ConnectionType : ConnectionType1</li>
     * </ul>
     * </li>
     * <li>ConnectionTask3
     * <ul>
     * <li>Name : Serial</li>
     * <li>ConnectionType : ConnectionType2</li>
     * </ul>
     * </li>
     * <li>ConnectionTask4
     * <ul>
     * <li>Name : Optical</li>
     * <li>ConnectionType : ConnectionType2</li>
     * </ul>
     * </li>
     * </ul>
     * </li>
     * </ul>
     * This should result in 4 MATCHES, 2 REMOVES and 2 ADDS
     */
    @Test
    public void complexScenario1Test() {
        String name1 = "GPRS";
        String name2 = "IP";
        String name3 = "Optical";
        String name4 = "Serial";
        ConnectionTypePluggableClass connectionTypePluggableClass1 = mockConnectionTypePluggableClass(100L);
        ConnectionTypePluggableClass connectionTypePluggableClass2 = mockConnectionTypePluggableClass(200L);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask1 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        PartialConnectionTask partialConnectionTask2 = mockPartialConnectionTask(name3, connectionTypePluggableClass2);
        when(deviceConfiguration1.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask1, partialConnectionTask2));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask3 = mockPartialConnectionTask(name2, connectionTypePluggableClass1);
        PartialConnectionTask partialConnectionTask4 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        PartialConnectionTask partialConnectionTask5 = mockPartialConnectionTask(name4, connectionTypePluggableClass2);
        PartialConnectionTask partialConnectionTask6 = mockPartialConnectionTask(name3, connectionTypePluggableClass2);
        when(deviceConfiguration2.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask3, partialConnectionTask4, partialConnectionTask5, partialConnectionTask6));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(8);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask1, partialConnectionTask4, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask2, partialConnectionTask6, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, null, partialConnectionTask3, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, null, partialConnectionTask5, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask4, partialConnectionTask1, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask6, partialConnectionTask2, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask5, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask5, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
    }

    /**
     * Scenario:
     * <ul>
     * <li>DeviceConfig 1
     * <ul>
     * <li>ConnectionTask1
     * <ul>
     * <li>Name : GPRS</li>
     * <li>ConnectionType : ConnectionType1</li>
     * </ul>
     * </li>
     * <li>ConnectionTask2
     * <ul>
     * <li>Name : Optical</li>
     * <li>ConnectionType : ConnectionType2</li>
     * </ul>
     * </li>
     * </ul>
     * </li>
     * <li>DeviceConfig 2
     * <ul>
     * <li>ConnectionTask1
     * <ul>
     * <li>Name : IP</li>
     * <li>ConnectionType : ConnectionType1</li>
     * </ul>
     * </li>
     * <li>ConnectionTask2
     * <ul>
     * <li>Name : GPRS</li>
     * <li>ConnectionType : ConnectionType1</li>
     * </ul>
     * </li>
     * <li>ConnectionTask3
     * <ul>
     * <li>Name : Serial</li>
     * <li>ConnectionType : ConnectionType2</li>
     * </ul>
     * </li>
     * <li>ConnectionTask4
     * <ul>
     * <li>Name : Optical</li>
     * <li>ConnectionType : ConnectionType2</li>
     * </ul>
     * </li>
     * </ul>
     * </li>
     * <li>DeviceConfig 3
     * <ul>
     * <li>ConnectionTask1
     * <ul>
     * <li>Name : RJ45</li>
     * <li>ConnectionType : ConnectionType1</li>
     * </ul>
     * </li>
     * <li>ConnectionTask2
     * <ul>
     * <li>Name : 4G</li>
     * <li>ConnectionType : ConnectionType1</li>
     * </ul>
     * </li>
     * <li>ConnectionTask3
     * <ul>
     * <li>Name : RJ11</li>
     * <li>ConnectionType : ConnectionType2</li>
     * </ul>
     * </li>
     * <li>ConnectionTask4
     * <ul>
     * <li>Name : InfraRed</li>
     * <li>ConnectionType : ConnectionType2</li>
     * </ul>
     * </li>
     * </ul>
     * <li>DeviceConfig4
     * <ul>
     * <li>ConnectionTask1
     * <ul>
     * <li>Name : MorseCode</li>
     * <li>ConnectionType : ConnectionType3</li>
     * </ul>
     * </li>
     * <li>ConnectionTask2
     * <ul>
     * <li>Name : Pidgins</li>
     * <li>ConnectionType : ConnectionType4</li>
     * </ul>
     * </li>
     * <li>ConnectionTask3
     * <ul>
     * <li>Name : IndianSmokeClouds</li>
     * <li>ConnectionType : ConnectionType5</li>
     * </ul>
     * </li>
     * <li>ConnectionTask4
     * <ul>
     * <li>Name : CheeseHoles</li>
     * <li>ConnectionType : ConnectionType6</li>
     * </ul>
     * </li>
     * </ul>
     * </li>
     * </ul>
     * This should result in 4 MATCHES, 24 CONFLICTS, 24 REMOVES and 24 ADDS
     */
    @Test
    public void complexScenario2Test() {
        String name1 = "GPRS";
        String name2 = "IP";
        String name3 = "Optical";
        String name4 = "Serial";
        String name5 = "RJ45";
        String name6 = "4G";
        String name7 = "RJ11";
        String name8 = "InfraRed";
        String name9 = "MorseCode";
        String name10 = "Pidgins";
        String name11 = "IndianSmokeClouds";
        String name12 = "CheeseHoles";
        ConnectionTypePluggableClass connectionTypePluggableClass1 = mockConnectionTypePluggableClass(100L);
        ConnectionTypePluggableClass connectionTypePluggableClass2 = mockConnectionTypePluggableClass(200L);
        ConnectionTypePluggableClass connectionTypePluggableClass3 = mockConnectionTypePluggableClass(300L);
        ConnectionTypePluggableClass connectionTypePluggableClass4 = mockConnectionTypePluggableClass(400L);
        ConnectionTypePluggableClass connectionTypePluggableClass5 = mockConnectionTypePluggableClass(500L);
        ConnectionTypePluggableClass connectionTypePluggableClass6 = mockConnectionTypePluggableClass(600L);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration1 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask1 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        PartialConnectionTask partialConnectionTask2 = mockPartialConnectionTask(name3, connectionTypePluggableClass2);
        when(deviceConfiguration1.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask1, partialConnectionTask2));
        DeviceConfiguration deviceConfiguration2 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask3 = mockPartialConnectionTask(name2, connectionTypePluggableClass1);
        PartialConnectionTask partialConnectionTask4 = mockPartialConnectionTask(name1, connectionTypePluggableClass1);
        PartialConnectionTask partialConnectionTask5 = mockPartialConnectionTask(name4, connectionTypePluggableClass2);
        PartialConnectionTask partialConnectionTask6 = mockPartialConnectionTask(name3, connectionTypePluggableClass2);
        when(deviceConfiguration2.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask3, partialConnectionTask4, partialConnectionTask5, partialConnectionTask6));
        DeviceConfiguration deviceConfiguration3 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask7 = mockPartialConnectionTask(name5, connectionTypePluggableClass1);
        PartialConnectionTask partialConnectionTask8 = mockPartialConnectionTask(name6, connectionTypePluggableClass1);
        PartialConnectionTask partialConnectionTask9 = mockPartialConnectionTask(name7, connectionTypePluggableClass2);
        PartialConnectionTask partialConnectionTask10 = mockPartialConnectionTask(name8, connectionTypePluggableClass2);
        when(deviceConfiguration3.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask7, partialConnectionTask8, partialConnectionTask9, partialConnectionTask10));
        DeviceConfiguration deviceConfiguration4 = mockActiveDeviceConfiguration();
        PartialConnectionTask partialConnectionTask11 = mockPartialConnectionTask(name9, connectionTypePluggableClass3);
        PartialConnectionTask partialConnectionTask12 = mockPartialConnectionTask(name10, connectionTypePluggableClass4);
        PartialConnectionTask partialConnectionTask13 = mockPartialConnectionTask(name11, connectionTypePluggableClass5);
        PartialConnectionTask partialConnectionTask14 = mockPartialConnectionTask(name12, connectionTypePluggableClass6);
        when(deviceConfiguration4.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask11, partialConnectionTask12, partialConnectionTask13, partialConnectionTask14));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration1, deviceConfiguration2, deviceConfiguration3, deviceConfiguration4));
        DeviceConfigChangeEngine deviceConfigChangeEngine = new DeviceConfigChangeEngine(deviceType);
        deviceConfigChangeEngine.calculateConfigChangeActions();
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).hasSize(76);
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask1, partialConnectionTask4, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, partialConnectionTask2, partialConnectionTask6, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, null, partialConnectionTask3, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration2, null, partialConnectionTask5, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask4, partialConnectionTask1, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask6, partialConnectionTask2, DeviceConfigChangeActionType.MATCH);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask5, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration1, partialConnectionTask5, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration3, partialConnectionTask1, partialConnectionTask7, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration3, partialConnectionTask1, partialConnectionTask8, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration3, partialConnectionTask2, partialConnectionTask9, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration3, partialConnectionTask2, partialConnectionTask10, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration4, partialConnectionTask1, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration4, partialConnectionTask1, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration4, null, partialConnectionTask11, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration4, null, partialConnectionTask12, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration4, null, partialConnectionTask13, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration1, deviceConfiguration4, null, partialConnectionTask14, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration3, partialConnectionTask3, partialConnectionTask7, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration3, partialConnectionTask3, partialConnectionTask8, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration3, partialConnectionTask4, partialConnectionTask7, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration3, partialConnectionTask4, partialConnectionTask8, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration3, partialConnectionTask5, partialConnectionTask9, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration3, partialConnectionTask5, partialConnectionTask10, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration3, partialConnectionTask6, partialConnectionTask9, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration3, partialConnectionTask6, partialConnectionTask10, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration4, partialConnectionTask3, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration4, partialConnectionTask4, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration4, partialConnectionTask5, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration4, partialConnectionTask6, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration4, null, partialConnectionTask11, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration4, null, partialConnectionTask12, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration4, null, partialConnectionTask13, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration2, deviceConfiguration4, null, partialConnectionTask14, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration1, partialConnectionTask7, partialConnectionTask1, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration1, partialConnectionTask8, partialConnectionTask1, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration1, partialConnectionTask9, partialConnectionTask2, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration1, partialConnectionTask10, partialConnectionTask2, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration2, partialConnectionTask7, partialConnectionTask3, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration2, partialConnectionTask7, partialConnectionTask4, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration2, partialConnectionTask8, partialConnectionTask3, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration2, partialConnectionTask8, partialConnectionTask4, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration2, partialConnectionTask9, partialConnectionTask5, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration2, partialConnectionTask9, partialConnectionTask6, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration2, partialConnectionTask10, partialConnectionTask5, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration2, partialConnectionTask10, partialConnectionTask6, DeviceConfigChangeActionType.CONFLICT);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration4, partialConnectionTask7, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration4, partialConnectionTask8, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration4, partialConnectionTask9, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration4, partialConnectionTask10, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration4, null, partialConnectionTask11, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration4, null, partialConnectionTask12, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration4, null, partialConnectionTask13, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration3, deviceConfiguration4, null, partialConnectionTask14, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration1, null, partialConnectionTask1, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration1, null, partialConnectionTask2, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration1, partialConnectionTask11, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration1, partialConnectionTask12, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration1, partialConnectionTask13, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration1, partialConnectionTask14, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration2, partialConnectionTask11, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration2, partialConnectionTask12, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration2, partialConnectionTask13, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration2, partialConnectionTask14, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration2, null, partialConnectionTask3, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration2, null, partialConnectionTask4, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration2, null, partialConnectionTask5, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration2, null, partialConnectionTask6, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration3, partialConnectionTask11, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration3, partialConnectionTask12, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration3, partialConnectionTask13, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration3, partialConnectionTask14, null, DeviceConfigChangeActionType.REMOVE);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration3, null, partialConnectionTask7, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration3, null, partialConnectionTask8, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration3, null, partialConnectionTask9, DeviceConfigChangeActionType.ADD);
            }
        });
        assertThat(deviceConfigChangeEngine.getDeviceConfigChangeActions()).haveExactly(1, new Condition<DeviceConfigChangeAction>() {
            @Override
            public boolean matches(DeviceConfigChangeAction deviceConfigChangeAction) {
                return matchConnectionTask(deviceConfigChangeAction, deviceConfiguration4, deviceConfiguration3, null, partialConnectionTask10, DeviceConfigChangeActionType.ADD);
            }
        });
    }

    private boolean matchConnectionTask(DeviceConfigChangeAction deviceConfigChangeAction, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration, PartialConnectionTask origin, PartialConnectionTask destination, DeviceConfigChangeActionType actionType) {
        return deviceConfigChangeAction.getOriginDeviceConfiguration().equals(originDeviceConfiguration)
                && deviceConfigChangeAction.getDestinationDeviceConfiguration().equals(destinationDeviceConfiguration)
                && (deviceConfigChangeAction.getOrigin() == null || deviceConfigChangeAction.getOrigin().equals(origin))
                && (deviceConfigChangeAction.getDestination() == null || deviceConfigChangeAction.getDestination().equals(destination))
                && deviceConfigChangeAction.getActionType().equals(actionType);
    }

    private PartialConnectionTask mockPartialConnectionTask(String name, ConnectionTypePluggableClass connectionTypePluggableClass) {
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getName()).thenReturn(name);
        when(partialConnectionTask.getPluggableClass()).thenReturn(connectionTypePluggableClass);
        when(partialConnectionTask.toString()).thenReturn("CT - " + incrementalConTaskId + " - " + name);
        when(partialConnectionTask.getId()).thenReturn(incrementalConTaskId++);
        return partialConnectionTask;
    }

    private DeviceConfiguration mockActiveDeviceConfiguration() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.isActive()).thenReturn(true);
        when(deviceConfiguration.toString()).thenReturn("DC - " + incrementalConfigId);
        when(deviceConfiguration.getId()).thenReturn(incrementalConfigId++);
        return deviceConfiguration;
    }
}