package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.dlms.DLMSCache;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class FrameCounterCacheBuilderTest  {

    @Mock
    private DLMSCache dlmsCache;

    @Mock
    private FrameCounterCache frameCounterCache;

    @Test(expected = FrameCounterException.class)
    public void useCacheAndNullDlmsSession(){
        new FrameCounterCacheBuilder(null, true).build();
    }

    @Test
    public void doNotUseCacheAndNullDlmsSession(){
        Optional<com.energyict.protocol.FrameCounterCache> frameCounterCache = new FrameCounterCacheBuilder(null, false).build();
        Assert.assertFalse(frameCounterCache.isPresent());
    }

    @Test(expected = FrameCounterException.class)
    public void useCacheAndNoFrameCacheInstanceDlmsSession(){
        Optional<com.energyict.protocol.FrameCounterCache> frameCounterCache = new FrameCounterCacheBuilder(dlmsCache, true).build();
    }

    @Test
    public void useCacheAndFrameCacheInstanceDlmsSession(){
        Optional<com.energyict.protocol.FrameCounterCache> frameCounterCache = new FrameCounterCacheBuilder(this.frameCounterCache, true).build();
        Assert.assertTrue(frameCounterCache.isPresent());
        Assert.assertEquals(this.frameCounterCache, frameCounterCache.get());
    }

}