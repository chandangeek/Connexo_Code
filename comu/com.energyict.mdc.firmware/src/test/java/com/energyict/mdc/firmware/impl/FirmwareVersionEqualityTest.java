package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.firmware.FirmwareVersion;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.fest.reflect.core.Reflection.field;

/**
 * Copyrights EnergyICT
 * Date: 16.10.15
 * Time: 13:28
 */
@RunWith(MockitoJUnitRunner.class)
public class FirmwareVersionEqualityTest extends EqualsContractTest {

    private FirmwareVersion firmwareInstance;
    private long fimwareVersionInstanceId = 132;

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;

    FirmwareVersionImpl setId(FirmwareVersionImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

    @Override
    protected Object getInstanceA() {
        if (firmwareInstance == null) {
            firmwareInstance = setId(new FirmwareVersionImpl(dataModel, eventService), fimwareVersionInstanceId);
        }
        return firmwareInstance;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return setId(new FirmwareVersionImpl(dataModel, eventService), fimwareVersionInstanceId);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Collections.singletonList(setId(new FirmwareVersionImpl(dataModel, eventService), fimwareVersionInstanceId+1));
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
