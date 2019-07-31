package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.issue.share.IssueDeviceFilter;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;

import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.device.topology.TopologyTimeslice;
import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
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

                TopologyTimeline timeline = topologyService.getPhysicalTopologyTimeline(device);
                relatedDevices.addAll(timeline.getAllDevices().stream()
                        .filter(d -> hasNotEnded(timeline, d))
                        .sorted(new DeviceRecentlyAddedComporator(timeline))
                        .collect(Collectors.toList()));
            }
        }

        return relatedDevices.stream().map(this::findEndDeviceByMdcDevice).flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty()).collect(Collectors.toList());
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

    private static class DeviceRecentlyAddedComporator implements Comparator<Device> {

        private TopologyTimeline timeline;
        DeviceRecentlyAddedComporator(TopologyTimeline timeline) {
            this.timeline = timeline;
        }

        @Override
        public int compare(Device d1, Device d2) {
            Optional<Instant> d1AddTime = this.timeline.mostRecentlyAddedOn(d1);
            Optional<Instant> d2AddTime = this.timeline.mostRecentlyAddedOn(d2);
            if (!d1AddTime.isPresent() && !d2AddTime.isPresent()) {
                return 0;
            } else if (!d1AddTime.isPresent() && d2AddTime.isPresent()) {
                return 1;
            } else if (!d2AddTime.isPresent() && d1AddTime.isPresent()) {
                return -1;
            }
            return -1 * d1AddTime.get().compareTo(d2AddTime.get());
        }

    }

    private static boolean hasNotEnded(TopologyTimeline timeline, Device device) {
        List<TopologyTimeslice> x1 = timeline.getSlices()
                .stream()
                .filter(s -> contains(s, device)).collect(Collectors.toList());

        List<TopologyTimeslice> x2 = timeline.getSlices()
                .stream()
                .filter(s -> contains(s, device))
                .sorted((s1, s2) -> s2.getPeriod().lowerEndpoint().compareTo(s1.getPeriod().lowerEndpoint()))
                .collect(Collectors.toList());

        Optional<TopologyTimeslice> first = timeline.getSlices()
                .stream()
                .filter(s -> contains(s, device))
                .sorted((s1, s2) -> s2.getPeriod().lowerEndpoint().compareTo(s1.getPeriod().lowerEndpoint()))
                .findFirst();
        return first.filter(topologyTimeslice -> !topologyTimeslice.getPeriod().hasUpperBound()).isPresent();
    }

    private static boolean contains(TopologyTimeslice timeslice, Device device) {
        return timeslice.getDevices()
                .stream()
                .anyMatch(d -> d.getId() == device.getId());
    }
}
