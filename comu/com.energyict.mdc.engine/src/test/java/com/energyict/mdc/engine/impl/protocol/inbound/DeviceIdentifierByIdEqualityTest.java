package com.energyict.mdc.engine.impl.protocol.inbound;

import com.energyict.mdc.device.data.DeviceDataService;

import com.elster.jupiter.devtools.tests.EqualsContractTest;

import java.util.Arrays;

import com.energyict.mdc.engine.impl.DeviceIdentifierById;
import org.junit.*;

import static org.mockito.Mockito.mock;

/**
 * Tests the equals contract of the {@link com.energyict.mdc.engine.impl.DeviceIdentifierById} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-06 (11:53)
 */
public class DeviceIdentifierByIdEqualityTest extends EqualsContractTest {

    private static final long DEVICE_ID_A = 97;
    private static final long DEVICE_ID_B = 1579;

    private static DeviceDataService deviceDataService;
    private static DeviceIdentifierById instanceA;

    @BeforeClass
    public static void setup () {
        deviceDataService = mock(DeviceDataService.class);
        instanceA = new DeviceIdentifierById(DEVICE_ID_A, deviceDataService);
    }

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new DeviceIdentifierById(DEVICE_ID_A, deviceDataService);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(new DeviceIdentifierById(DEVICE_ID_B, deviceDataService));
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

}