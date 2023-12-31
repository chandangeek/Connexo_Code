/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.identifiers.LogBookIdentifierById;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.obis.ObisCode;
import org.junit.BeforeClass;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

/**
 * Tests the equals contract of the {@link LogBookIdentifierById} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-06 (12:03)
 */
public class LogBookIdentifierByIdImplEqualityTest extends EqualsContractTest {

    private static final long LOGBOOK_ID_A = 1;
    private static final long LOGBOOK_ID_B = 2;

    private static LogBookService logBookService;
    private static LogBookIdentifierById instanceA;

    @BeforeClass
    public static void setup() {
        logBookService = mock(LogBookService.class);
        instanceA = new LogBookIdentifierById(LOGBOOK_ID_A, ObisCode.fromString("1.1.1.1.1.1"), mock(DeviceIdentifier.class));
    }

    @Override
    protected Object getInstanceA() {
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new LogBookIdentifierById(LOGBOOK_ID_A, ObisCode.fromString("1.1.1.1.1.1"), mock(DeviceIdentifier.class));
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(new LogBookIdentifierById(LOGBOOK_ID_B, ObisCode.fromString("1.1.1.1.1.1"), mock(DeviceIdentifier.class)));
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