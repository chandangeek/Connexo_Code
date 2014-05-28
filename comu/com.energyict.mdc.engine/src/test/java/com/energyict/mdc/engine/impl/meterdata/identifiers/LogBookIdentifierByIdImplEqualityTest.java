package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.energyict.mdc.device.data.DeviceDataService;
import org.junit.BeforeClass;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

/**
 * Tests the equals contract of the {@link LogBookIdentifierByIdImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-06 (12:03)
 */
public class LogBookIdentifierByIdImplEqualityTest extends EqualsContractTest {

    private static final long LOGBOOK_ID_A = 1;
    private static final long LOGBOOK_ID_B = 2;

    private static DeviceDataService deviceDataService;
    private static LogBookIdentifierByIdImpl instanceA;

    @BeforeClass
    public static void setup () {
        deviceDataService = mock(DeviceDataService.class);
        instanceA = new LogBookIdentifierByIdImpl(LOGBOOK_ID_A, deviceDataService);
    }

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new LogBookIdentifierByIdImpl(LOGBOOK_ID_A, deviceDataService);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(new LogBookIdentifierByIdImpl(LOGBOOK_ID_B, deviceDataService));
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