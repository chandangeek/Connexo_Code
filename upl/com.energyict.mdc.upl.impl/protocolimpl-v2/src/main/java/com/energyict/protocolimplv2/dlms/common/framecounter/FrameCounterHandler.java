package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;

import java.util.Optional;

public class FrameCounterHandler {

    private final Optional<Long> frameCounter;

    public FrameCounterHandler(Optional<Long> frameCounter) {
        this.frameCounter = frameCounter;
    }

    public void handle(DlmsSessionProperties dlmsSessionProperties) {

        if (frameCounter.isPresent()) {
            dlmsSessionProperties.getSecurityProvider().setInitialFrameCounter(frameCounter.get());
        }
    }
}
