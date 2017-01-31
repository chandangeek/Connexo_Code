/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.firmware.FirmwareVersion;

import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class FirmwareVersionEqualityTest extends EqualsContractTest {

    private FirmwareVersion firmwareInstance;
    private long fimwareVersionInstanceId = 132;

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private Thesaurus thesaurus;

    FirmwareVersionImpl setId(FirmwareVersionImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

    @Override
    protected Object getInstanceA() {
        if (firmwareInstance == null) {
            firmwareInstance = setId(new FirmwareVersionImpl(dataModel, eventService, thesaurus), fimwareVersionInstanceId);
        }
        return firmwareInstance;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return setId(new FirmwareVersionImpl(dataModel, eventService, thesaurus), fimwareVersionInstanceId);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Collections.singletonList(setId(new FirmwareVersionImpl(dataModel, eventService, thesaurus), fimwareVersionInstanceId+1));
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
