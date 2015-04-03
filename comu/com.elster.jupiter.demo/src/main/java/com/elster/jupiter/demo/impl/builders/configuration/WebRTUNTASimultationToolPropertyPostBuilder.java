package com.elster.jupiter.demo.impl.builders.configuration;

import com.energyict.mdc.device.data.Device;

import java.time.ZoneId;
import java.util.TimeZone;
import java.util.function.Consumer;

public class WebRTUNTASimultationToolPropertyPostBuilder implements Consumer<Device> {
    @Override
    public void accept(Device device) {
        device.setProtocolProperty("NTASimulationTool", true);
        device.setProtocolProperty("TimeZone", TimeZone.getTimeZone("Europe/Brussels"));
        device.save();
    }

}
