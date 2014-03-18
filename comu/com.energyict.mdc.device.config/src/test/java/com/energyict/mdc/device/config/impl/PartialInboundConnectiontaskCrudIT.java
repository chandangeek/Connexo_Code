package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PartialInboundConnectiontaskCrudIT extends PersistenceTest {

    private InboundComPortPool inboundComPortPool;

    @Before
    public void setUp() {
        inboundComPortPool = getInjector().getInstance(InboundComPortPool.class);
        inboundComPortPool.save();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreate() {

        DataModel dataModel = getInjector().getInstance(DataModel.class);

        PartialInboundConnectionTask inboundConnectionTask = getInjector().getInstance(PartialInboundConnectionTask.class);

        inboundConnectionTask.setComportPool(inboundComPortPool);
    }

    private Injector getInjector() {
        return inMemoryPersistence.getInjector();
    }


}
