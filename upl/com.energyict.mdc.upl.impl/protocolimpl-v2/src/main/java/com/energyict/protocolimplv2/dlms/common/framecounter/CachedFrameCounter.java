package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.protocol.FrameCounterCache;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;

import java.util.Optional;

public class CachedFrameCounter  implements FrameCounter {

    private final DlmsSessionProperties dlmsSessionProperties;
    private final PublicClientFrameCounter publicClientFrameCounter;
    private final FrameCounterCache frameCounterCache;

    public CachedFrameCounter(DlmsSessionProperties dlmsSessionProperties, PublicClientFrameCounter publicClientFrameCounter, FrameCounterCache frameCounterCache) {
        this.dlmsSessionProperties = dlmsSessionProperties;
        this.publicClientFrameCounter = publicClientFrameCounter;
        this.frameCounterCache = frameCounterCache;
    }

    @Override
    public Optional<Long> get() {
        long cachedFrameCounter = frameCounterCache.getTXFrameCounter(dlmsSessionProperties.getClientMacAddress());
        if (cachedFrameCounter == FrameCounterCache.DEFAULT_FC) {
            Optional<Long> aLong = publicClientFrameCounter.get();
            if (!aLong.isPresent()) {
                throw new FrameCounterException(ProtocolExceptionMessageSeeds.FRAME_COUNTER_NOT_AVAILABLE);
            }
            frameCounterCache.setTXFrameCounter(dlmsSessionProperties.getClientMacAddress(), aLong.get());
            return aLong;
        }
        else {
            return Optional.of(cachedFrameCounter);
        }
    }
}
