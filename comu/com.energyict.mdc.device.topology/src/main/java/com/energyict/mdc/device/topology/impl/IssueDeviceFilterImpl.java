package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.issue.share.IssueDeviceFilter;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.device.topology.impl.IssueDeviceFilter", service = IssueDeviceFilter.class, immediate = true)
public class IssueDeviceFilterImpl implements IssueDeviceFilter {

    private volatile DeviceService deviceService;
    private volatile MeteringService meteringService;
    private volatile TopologyService topologyService;


    public IssueDeviceFilterImpl() {
    }

    @Inject
    public IssueDeviceFilterImpl(DeviceService deviceService, MeteringService meteringService, TopologyService topologyService) {
        setDeviceService(deviceService);
        setMeteringService(meteringService);
        setTopologyService(topologyService);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Override
    public List<EndDevice> getShowTopologyCondition(List<EndDevice> endDeviceList) {

        List<Device> relatedDevices = new ArrayList<>();
        for (EndDevice ed : endDeviceList) {
            Optional<Device> deviceCandidate = deviceService.findDeviceById(Long.parseLong(ed.getAmrId()));
            if (deviceCandidate.isPresent()) {
                Device device = deviceCandidate.get();

                Optional<Device> masterDevice = topologyService.getPhysicalGateway(device);
                if (masterDevice.isPresent()) {
                    relatedDevices.add(masterDevice.get());
                    continue;
                }

                relatedDevices.addAll(topologyService.getSlaveDevices(device));
            }
        }

        return relatedDevices.stream().map(this::findEndDeviceByMdcDevice)
                .flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty())
                .collect(Collectors.toList());
    }

    private Optional<EndDevice> findEndDeviceByMdcDevice(Device ed) {

        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
        if (amrSystemRef.isPresent()) {
            Optional<Meter> meterRef = amrSystemRef.get().findMeter(String.valueOf(ed.getId()));
            if (meterRef.isPresent()) {
                return Optional.of(meterRef.get());
            }
        }
        return Optional.empty();
    }
}
