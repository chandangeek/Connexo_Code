package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.SuccessIndicator;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import java.util.Map;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceDataServiceImpl#getConnectionTaskStatusCount(ConnectionTaskFilterSpecification)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-31 (17:24)
 */
public class DeviceDataServiceConnectionTaskStatusCountTest extends PersistenceIntegrationTest {

    private static final long CONNECTION_TYPE_ID = 97L;

    /**
     * Tests the target method with all the default options, i.e.
     * <<ul>
     * <li>empty set of {@link ConnectionTypePluggableClass}es so that the method will query all from database</li>
     * <li>empty set of {@link ComPortPool}s so that the method will query all from database</li>
     * <li>empty set of {@link DeviceType}s so that the method will query all from database</li>
     * <li>all {@link TaskStatus}es</li>
     * <li>empty set of {@link SuccessIndicator}s</li>
     * </ul>
     */
    @Test
    @Transactional
    public void testAllDefaultOptions() {
        // Business method
        Map<TaskStatus, Long> counters = inMemoryPersistence.getDeviceDataService().getConnectionTaskStatusCount();

        // Asserts: with no connection tasks in the system, all counters should be there with zero value
        assertThat(counters).hasSize(TaskStatus.values().length);
        for (Long statusCount : counters.values()) {
            assertThat(statusCount).isZero();
        }
    }

    /**
     * Tests the target method with the following options:
     * <<ul>
     * <li>empty set of {@link ConnectionTypePluggableClass}es so that the method will query all from database</li>
     * <li>empty set of {@link DeviceType}s so that the method will query all from database</li>
     * <li>a single {@link ComPortPool}</li>
     * <li>all {@link TaskStatus}es</li>
     * <li>empty set of {@link SuccessIndicator}s</li>
     * </ul>
     */
    @Test
    @Transactional
    public void testWithSingleComPortPool() {
        ComPortPool comPortPool = this.createComPortPool();
        ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
        filter.comPortPools.add(comPortPool);

        // Business method
        Map<TaskStatus, Long> counters = inMemoryPersistence.getDeviceDataService().getConnectionTaskStatusCount(filter);

        // Asserts: with no connection tasks in the system, all counters should be there with zero value
        assertThat(counters).hasSize(TaskStatus.values().length);
        for (Long statusCount : counters.values()) {
            assertThat(statusCount).isZero();
        }
    }

    /**
     * Tests the target method with the following options:
     * <<ul>
     * <li>a single {@link ConnectionTypePluggableClass}es so that the method will query all from database</li>
     * <li>empty set of {@link ComPortPool}s so that the method will query all from database</li>
     * <li>empty set of {@link DeviceType}s</li>
     * <li>all {@link TaskStatus}es</li>
     * <li>empty set of {@link SuccessIndicator}s</li>
     * </ul>
     */
    @Test
    @Transactional
    public void testWithSingleConnectionType() {
        ConnectionTypePluggableClass connectionType = mock(ConnectionTypePluggableClass.class);
        when(connectionType.getId()).thenReturn(CONNECTION_TYPE_ID);
        ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
        filter.connectionTypes.add(connectionType);

        // Business method
        Map<TaskStatus, Long> counters = inMemoryPersistence.getDeviceDataService().getConnectionTaskStatusCount(filter);

        // Asserts: with no connection tasks in the system, all counters should be there with zero value
        assertThat(counters).hasSize(TaskStatus.values().length);
        for (Long statusCount : counters.values()) {
            assertThat(statusCount).isZero();
        }
    }

    /**
     * Tests the target method with the following options:
     * <<ul>
     * <li>empty set of {@link ConnectionTypePluggableClass}es so that the method will query all from database</li>
     * <li>empty set of {@link ComPortPool}s so that the method will query all from database</li>
     * <li>a single {@link DeviceType}</li>
     * <li>all {@link TaskStatus}es</li>
     * <li>empty set of {@link SuccessIndicator}s</li>
     * </ul>
     */
    @Test
    @Transactional
    public void testWithSingleDeviceType() {
        ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
        filter.deviceTypes.add(this.deviceType);

        // Business method
        Map<TaskStatus, Long> counters = inMemoryPersistence.getDeviceDataService().getConnectionTaskStatusCount(filter);

        // Asserts: with no connection tasks in the system, all counters should be there with zero value
        assertThat(counters).hasSize(TaskStatus.values().length);
        for (Long statusCount : counters.values()) {
            assertThat(statusCount).isZero();
        }
    }

    private ComPortPool createComPortPool() {
        OutboundComPortPool comPortPool = inMemoryPersistence.getEngineModelService().newOutboundComPortPool();
        comPortPool.setName("DeviceDataServiceConnectionTaskStatusCountTest");
        comPortPool.setTaskExecutionTimeout(TimeDuration.minutes(1));
        comPortPool.setComPortType(ComPortType.TCP);
        comPortPool.save();
        return comPortPool;
    }

}