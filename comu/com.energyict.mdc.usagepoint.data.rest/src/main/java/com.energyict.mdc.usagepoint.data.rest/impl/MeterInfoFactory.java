package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;

public class MeterInfoFactory {
    private final DeviceService deviceService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Inject
    public MeterInfoFactory(DeviceService deviceService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceService = deviceService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    private static class MACollector {
        List<MeterInfo> meterInfos = new ArrayList<>();

        void add(MeterInfo meterInfo) {
            if (!meterInfos.isEmpty()) {
                MeterInfo previous = meterInfos.get(meterInfos.size() - 1);
                if (previous.mRID.equals(meterInfo.mRID)) {
                    previous.end = meterInfo.end;
                    previous.active = meterInfo.active;
                } else {
                    meterInfos.add(meterInfo);
                }
            } else {
                meterInfos.add(meterInfo);
            }
        }

        List<MeterInfo> getMeterInfos() {
            return this.meterInfos;
        }
    }

    List<MeterInfo> getDevicesHistory(UsagePoint usagePoint) {
        MACollector maCollector = usagePoint.getMeterActivations()
                .stream()
                .filter(ma -> {
                    State state = ma.getMeter().get().getState().get();
                    return !state.getName().equals(DefaultState.REMOVED.getKey());
                })
                .map(this::asInfo)
                .collect(Collector.of(MACollector::new, MACollector::add, (c1, c2) -> c1));
        Collections.reverse(maCollector.getMeterInfos());
        return maCollector.getMeterInfos();
    }

    private MeterInfo asInfo(MeterActivation meterActivation) {
        MeterInfo meterInfo = new MeterInfo();
        Meter meter = meterActivation.getMeter().get();
        Device device = deviceService.findByUniqueMrid(meterActivation.getMeter().get().getMRID()).get();
        meterInfo.mRID = meter.getMRID();
        meterInfo.serialNumber = device.getSerialNumber();
        meterInfo.deviceType = new IdWithNameInfo(device.getDeviceType().getId(), device.getDeviceType().getName());
        meterInfo.state = DefaultState.from(device.getState()).map(deviceLifeCycleConfigurationService::getDisplayName).orElseGet(device.getState()::getName);
        meterInfo.start = meterActivation.getStart().toEpochMilli();
        if (meterActivation.getEnd() != null) {
            meterInfo.end = meterActivation.getEnd().toEpochMilli();
        } else {
            meterInfo.active = true;
        }
        return meterInfo;
    }
}
