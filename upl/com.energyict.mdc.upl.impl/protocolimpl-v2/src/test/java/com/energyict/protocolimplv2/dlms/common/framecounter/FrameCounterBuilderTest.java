package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.FrameCounterCache;
import com.energyict.protocolimplv2.dlms.common.dlms.PublicClientDlmsSessionProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class FrameCounterBuilderTest {

    @Mock
    private PublicClientDlmsSessionProvider publicClientSpecs;
    @Mock
    private FrameCounterCache frameCounterCache;
    @Mock
    private FrameCounterCacheBuilder frameCounterCacheBuilder;
    @Mock
    private ObisCode obisCode;
    @Mock
    private DlmsSessionProperties dlmsSessionProperties;

    @Test
    public void noOpFCBuild() {
        Mockito.when(frameCounterCacheBuilder.build()).thenReturn(Optional.empty());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(0, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(1, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(2, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(3, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(4, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());

        Mockito.when(frameCounterCacheBuilder.build()).thenReturn(Optional.of(frameCounterCache));
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(0, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(1, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(2, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(3, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(4, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
    }

    @Test
    public void deviceFCBuild() {
        Mockito.when(frameCounterCacheBuilder.build()).thenReturn(Optional.empty());
        Assert.assertEquals(PublicClientFrameCounter.class, FrameCounterBuilder.build(5, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
    }

    @Test
    public void cacheFCBuild() {
        Mockito.when(frameCounterCacheBuilder.build()).thenReturn(Optional.of(frameCounterCache));
        Assert.assertEquals(CachedFrameCounter.class, FrameCounterBuilder.build(5, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
    }

    @Test
    public void noOpFCBuildForWrongSecurityLevel() {
        Mockito.when(frameCounterCacheBuilder.build()).thenReturn(Optional.empty());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(-1, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(-1, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(6, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());
        Assert.assertEquals(NoOpFrameCounter.class, FrameCounterBuilder.build(6, frameCounterCacheBuilder, publicClientSpecs, obisCode, dlmsSessionProperties).getClass());

    }
}