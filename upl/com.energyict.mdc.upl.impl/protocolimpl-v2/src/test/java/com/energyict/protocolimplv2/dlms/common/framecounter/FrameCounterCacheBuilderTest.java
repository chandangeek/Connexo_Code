package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.dlms.DLMSCache;
import com.energyict.protocol.FrameCounterCache;
import com.energyict.protocolimplv2.dlms.as3000.dlms.AS3000Cache;
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
    private AS3000Cache as3000Cache;

    @Test(expected = FrameCounterException.class)
    public void useCacheAndNullDlmsSession(){
        new FrameCounterCacheBuilder(null, true).build();
    }

    @Test
    public void doNotUseCacheAndNullDlmsSession(){
        Optional<FrameCounterCache> frameCounterCache = new FrameCounterCacheBuilder(null, false).build();
        Assert.assertFalse(frameCounterCache.isPresent());
    }

    @Test(expected = FrameCounterException.class)
    public void useCacheAndNoFrameCacheInstanceDlmsSession(){
        Optional<FrameCounterCache> frameCounterCache = new FrameCounterCacheBuilder(dlmsCache, true).build();
    }

    @Test
    public void useCacheAndFrameCacheInstanceDlmsSession(){
        Optional<FrameCounterCache> frameCounterCache = new FrameCounterCacheBuilder(as3000Cache, true).build();
        Assert.assertTrue(frameCounterCache.isPresent());
        Assert.assertEquals(as3000Cache, frameCounterCache.get());
    }

}