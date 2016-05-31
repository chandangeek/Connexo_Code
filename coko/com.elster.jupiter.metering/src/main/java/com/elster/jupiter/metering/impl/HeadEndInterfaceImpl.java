package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceCapabilities;
import com.elster.jupiter.metering.HeadEndInterface;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;

import java.util.ArrayList;
import java.util.List;


public class HeadEndInterfaceImpl implements HeadEndInterface {

    private final MeteringService meteringService;

    HeadEndInterfaceImpl(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public EndDeviceCapabilities getCapabilites(EndDevice endDevice) {
        return new EndDeviceCapabilitiesImpl();
    }

    //todo: mocked data, should be removed
    private class EndDeviceCapabilitiesImpl implements EndDeviceCapabilities {

        @Override
        public List<ReadingType> getConfiguredReadingTypes() {
            List<ReadingType> rts = new ArrayList<>();
            rts.add(meteringService.getReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0").get());
            return rts;
        }
    }
}
