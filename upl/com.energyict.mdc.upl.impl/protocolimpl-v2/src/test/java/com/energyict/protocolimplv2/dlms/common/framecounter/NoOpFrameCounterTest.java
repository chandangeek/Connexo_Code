package com.energyict.protocolimplv2.dlms.common.framecounter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NoOpFrameCounterTest  {

    @Test
    public void nullAll() {
        Assert.assertFalse(new NoOpFrameCounter().get().isPresent());
    }

    @Test
    public void allOk() {
        Assert.assertFalse(new NoOpFrameCounter().get().isPresent());
    }

}