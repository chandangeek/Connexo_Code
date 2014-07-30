package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComTaskCompletionOverview;
import com.energyict.mdc.dashboard.ConnectionStatusOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.Arrays;
import java.util.Collections;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DashboardServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (14:08)
 */
@RunWith(MockitoJUnitRunner.class)
public class DashboardServiceImplTest {

    @Mock
    private EngineModelService engineModelService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;

    private DashboardServiceImpl dashboardService;

    @Before
    public void setupService () {
        this.dashboardService = new DashboardServiceImpl(this.engineModelService, this.deviceConfigurationService, this.deviceDataService, this.protocolPluggableService);
    }

    @Test
    public void testConnectionOverview () {
        // Business methods
        ConnectionStatusOverview overview = this.dashboardService.getConnectionStatusOverview();

        // Asserts
        assertThat(overview).isNotNull();
    }

    @Test
    public void testComTaskCompletionOverview () {
        // Business methods
        ComTaskCompletionOverview overview = this.dashboardService.getComTaskCompletionOverview();

        // Asserts
        assertThat(overview).isNotNull();
    }

    @Test
    public void testComPortPoolBreakdownWithoutComPortPools () {
        when(this.engineModelService.findAllComPortPools()).thenReturn(Collections.<ComPortPool>emptyList());

        // Business methods
        ComPortPoolBreakdown breakdown = this.dashboardService.getComPortPoolBreakdown();

        // Asserts
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testComPortPoolBreakdownWithComPortPoolsButNoConnections () {
        ComPortPool comPortPool = mock(ComPortPool.class);
        when(this.engineModelService.findAllComPortPools()).thenReturn(Arrays.asList(comPortPool));

        // Business methods
        ComPortPoolBreakdown breakdown = this.dashboardService.getComPortPoolBreakdown();

        // Asserts
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testConnectionTypeBreakdownWithoutConnectionTypes () {
        when(this.protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Collections.<ConnectionTypePluggableClass>emptyList());

        // Business methods
        ConnectionTypeBreakdown breakdown = this.dashboardService.getConnectionTypeBreakdown();

        // Asserts
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testConnectionTypeBreakdownWithConnectionTypesButNoConnections () {
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(this.protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Arrays.asList(connectionTypePluggableClass));

        // Business methods
        ConnectionTypeBreakdown breakdown = this.dashboardService.getConnectionTypeBreakdown();

        // Asserts
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.iterator().next()).isNotNull();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testDeviceTypeBreakdownWithoutDeviceTypes () {
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Collections.<DeviceType>emptyList());
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        // Business methods
        DeviceTypeBreakdown breakdown = this.dashboardService.getDeviceTypeBreakdown();

        // Asserts
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isFalse();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

    @Test
    public void testDeviceTypeBreakdownWithDeviceTypesButNoConnections () {
        DeviceType deviceType = mock(DeviceType.class);
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Arrays.asList(deviceType));
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        // Business methods
        DeviceTypeBreakdown breakdown = this.dashboardService.getDeviceTypeBreakdown();

        // Asserts
        assertThat(breakdown).isNotNull();
        assertThat(breakdown.iterator().hasNext()).isTrue();
        assertThat(breakdown.iterator().next()).isNotNull();
        assertThat(breakdown.getTotalCount()).isZero();
        assertThat(breakdown.getTotalSuccessCount()).isZero();
        assertThat(breakdown.getTotalFailedCount()).isZero();
        assertThat(breakdown.getTotalPendingCount()).isZero();
    }

}