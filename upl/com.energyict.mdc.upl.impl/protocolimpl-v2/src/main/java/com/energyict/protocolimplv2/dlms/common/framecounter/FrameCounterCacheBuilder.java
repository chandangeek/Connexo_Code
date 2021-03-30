package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.dlms.DLMSCache;
import com.energyict.protocol.FrameCounterCache;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;

import java.util.Optional;

public class FrameCounterCacheBuilder {

    private final DLMSCache dlmsCache;
    private final boolean useCache;


    public FrameCounterCacheBuilder(DLMSCache dlmsCache, boolean useCache) {
        this.dlmsCache = dlmsCache;
        this.useCache = useCache;
    }

    public Optional<FrameCounterCache> build() {
        if (useCache) {
            if (dlmsCache != null && dlmsCache instanceof FrameCounterCache) {
                return Optional.of((FrameCounterCache) dlmsCache);
            }
            else {
                // this happens only if there is a development issue and we have useCache property yet we have not implemented it (and injected it) for this protocol
                throw new FrameCounterException(ProtocolExceptionMessageSeeds.FRAME_COUNTER_CACHE_NOT_SUPPORTED);
            }
        }
        return Optional.empty();
    }

}
