package com.energyict.protocolimplv2.dlms.common.framecounter;

import java.util.Optional;

public class NoOpFrameCounter implements FrameCounter {

    @Override
    public Optional<Long> get() {
        return Optional.empty();
    }

}
