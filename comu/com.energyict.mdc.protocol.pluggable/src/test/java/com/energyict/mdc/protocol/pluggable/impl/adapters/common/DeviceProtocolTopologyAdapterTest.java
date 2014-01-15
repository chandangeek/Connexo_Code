package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.issues.Bus;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
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

    private static final int DEVICE_ID = 93;

    @Mock
    private static UserEnvironment userEnvironment = mock(UserEnvironment.class);
    @Mock
    private Environment environment;
    @Mock
    private IssueService issueService;

    @BeforeClass
    public static void initializeUserEnvironment () {
        UserEnvironment.setDefault(userEnvironment);
        when(userEnvironment.getErrorMsg(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }
        });
    }

    @AfterClass
    public static void cleanupUserEnvironment () {
        UserEnvironment.setDefault(null);
    }

    @Before
    public void initializeEnvironment () {
        Environment.DEFAULT.set(this.environment);
        when(this.environment.getTranslation("devicetopologynotsupported")).thenReturn("devicetopologynotsupported");
    }

    @After
    public void cleanupEnvironment () {
        Environment.DEFAULT.set(null);
    }

    @Before
    public void initializeIssueService () {
        Bus.setIssueService(this.issueService);
        when(this.issueService.newProblem(anyString(), anyString(), anyVararg())).thenReturn(mock(Problem.class));
    }

    @After
    public void cleanupIssueService () {
        Bus.clearIssueService(this.issueService);
    }

    @Test
    public void getUnsupportedCollectedTopology(){
        DeviceProtocolTopologyAdapter deviceProtocolTopologyAdapter = new DeviceProtocolTopologyAdapter();
        deviceProtocolTopologyAdapter.setDeviceIdentifier(getDeviceIdentifier());
        CollectedTopology deviceTopology = deviceProtocolTopologyAdapter.getDeviceTopology();
        assertEquals("Device topology should be unsupported", ResultType.NotSupported, deviceTopology.getResultType());
        verify(this.issueService).newProblem(anyString(), anyString(), anyVararg());
        DeviceIdentifier deviceIdentifier = deviceTopology.getDeviceIdentifier();
        assertEquals("TopologyId should be equal to " + DEVICE_ID, DEVICE_ID, deviceIdentifier.findDevice().getId());
    }

    private DeviceIdentifier getDeviceIdentifier(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        return deviceIdentifier;
    }

}