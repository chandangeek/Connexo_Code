package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link DeviceProtocolTopologyAdapter}.
 *
 * @author gna
 * @since 5/04/12 - 12:12
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolTopologyAdapterTest {

    private static final long DEVICE_ID = 93;

    @Mock
    private IssueService issueService;
    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private CollectedTopology collectedTopology;
    @Mock
    private Device device;
    @Mock
    private DeviceIdentifier deviceIdentifier;

    @Before
    public void initializeCollecteData () {
        when(this.collectedDataFactory.createCollectedTopology(getDeviceIdentifier())).thenReturn(this.collectedTopology);
    }

    @Before
    public void initializeDevice () {
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
    }

    @Before
    public void initializeIssueService () {
        when(this.issueService.newProblem(anyString(), any(), anyVararg())).thenReturn(mock(Problem.class));
    }

    @Test
    public void getUnsupportedCollectedTopology(){
        DeviceProtocolTopologyAdapter deviceProtocolTopologyAdapter = new DeviceProtocolTopologyAdapter(issueService, collectedDataFactory);
        deviceProtocolTopologyAdapter.setDeviceIdentifier(getDeviceIdentifier());

        // Business method
        deviceProtocolTopologyAdapter.getDeviceTopology();

        // Asserts
        verify(this.collectedTopology).setFailureInformation(eq(ResultType.NotSupported), any(Issue.class));
        verify(this.issueService).newWarning(anyString(), any(), anyVararg());
    }

    private DeviceIdentifier getDeviceIdentifier(){
        return deviceIdentifier;
    }

}