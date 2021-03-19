package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.FrameCounterCache;
import com.energyict.protocolimplv2.dlms.common.dlms.PublicClientDlmsSessionProvider;

import java.util.Optional;

public class FrameCounterBuilder {

    public static FrameCounter build(int securityLevel, FrameCounterCacheBuilder frameCounterCacheBuilder, PublicClientDlmsSessionProvider publicClientDlmsSessionProvider, ObisCode obisCode, DlmsSessionProperties dlmsSessionProperties) {
        if (securityLevel == 5) {
            PublicClientFrameCounter publicClientFrameCounter = new PublicClientFrameCounter(obisCode, publicClientDlmsSessionProvider);
            Optional<FrameCounterCache> frameCounterCache = frameCounterCacheBuilder.build();
            if (frameCounterCache.isPresent()) {
                return new CachedFrameCounter(dlmsSessionProperties, publicClientFrameCounter, frameCounterCache.get());
            }
            return publicClientFrameCounter;
        }
        return new NoOpFrameCounter();
    }

}
