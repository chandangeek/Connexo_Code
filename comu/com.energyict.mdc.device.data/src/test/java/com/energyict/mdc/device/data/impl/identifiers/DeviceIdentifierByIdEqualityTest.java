package com.energyict.mdc.device.data.impl.identifiers;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.energyict.mdc.device.data.DeviceService;
import org.junit.BeforeClass;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

/**
 * Tests the equals contract of the {@link DeviceIdentifierById} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-06 (11:53)
 */
public class DeviceIdentifierByIdEqualityTest extends EqualsContractTest {

    private static final long DEVICE_ID_A = 97;
    private static final long DEVICE_ID_B = 1579;

    private static DeviceService deviceService;
    private static DeviceIdentifierById instanceA;

    @BeforeClass
    public static void setup() {
        deviceService = mock(DeviceService.class);
        instanceA = new DeviceIdentifierById(DEVICE_ID_A);
    }

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new DeviceIdentifierById(DEVICE_ID_A);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(new DeviceIdentifierById(DEVICE_ID_B));
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