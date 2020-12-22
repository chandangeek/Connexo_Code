package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.protocol.FrameCounterCache;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CachedFrameCounterTest {

    @Mock
    private PublicClientFrameCounter publicClientFrameCounter;
    @Mock
    private FrameCounterCache frameCounterCache;

    @Mock
    private DlmsSessionProperties dlmsSessionProperties;

    @Test
    public void validCachedValueFound() {
        int clientId = 1;
        long cachedfc = 100L;
        Mockito.when(frameCounterCache.getTXFrameCounter(clientId)).thenReturn(cachedfc);
        Mockito.when(dlmsSessionProperties.getClientMacAddress()).thenReturn(clientId);

        CachedFrameCounter cachedFrameCounter = new CachedFrameCounter(dlmsSessionProperties, publicClientFrameCounter, frameCounterCache);
        Optional<Long> fc = cachedFrameCounter.get();
        Assert.assertTrue(fc.isPresent());
        Assert.assertEquals(cachedfc, fc.get().longValue());
    }

    @Test
    public void noValidCachedValueFound() {
        int clientId = 1;
        long cachedfc = 99L;
        Mockito.when(frameCounterCache.getTXFrameCounter(clientId)).thenReturn(FrameCounterCache.DEFAULT_FC);
        Mockito.when(dlmsSessionProperties.getClientMacAddress()).thenReturn(clientId);
        Mockito.when(publicClientFrameCounter.get()).thenReturn(Optional.of(cachedfc));

        CachedFrameCounter cachedFrameCounter = new CachedFrameCounter(dlmsSessionProperties, publicClientFrameCounter, frameCounterCache);
        Optional<Long> fc = cachedFrameCounter.get();
        Assert.assertTrue(fc.isPresent());
        Assert.assertEquals(cachedfc, fc.get().longValue());
    }

}