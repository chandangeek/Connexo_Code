package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.elster.jupiter.util.streams.Functions.asStream;

@Component(name = "com.elster.jupiter.metering.groups.console", service = ConsoleCommands.class, property = {"osgi.command.scope=metering", "osgi.command.function=createEnumeratedEndDeviceGroup", "osgi.command.function=updateEnumeratedEndDeviceGroup", "osgi.command.function=endDeviceGroups"}, immediate = true)
public class ConsoleCommands {

    private volatile MeteringGroupsService meteringGroupsService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;

    public void createEnumeratedEndDeviceGroup(String name, long... ids) {
        final EnumeratedEndDeviceGroup group = meteringGroupsService.createEnumeratedEndDeviceGroup(name);
        final List<EnumeratedEndDeviceGroup.Entry> entries = new ArrayList<>();
        for (long id : ids) {
            Optional<EndDevice> endDevice = meteringService.findEndDevice(id);
            if (endDevice.isPresent()) {
                EnumeratedEndDeviceGroup.Entry entry = group.add(endDevice.get(), Range.atLeast(clock.instant()));
                entries.add(entry);
            }
        }
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(group::save));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void updateEnumeratedEndDeviceGroup(String name, long... ids) {
        EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(name).orElseThrow(() -> new IllegalArgumentException("group not found"));
        final EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = (EnumeratedEndDeviceGroup) endDeviceGroup;
        final List<EnumeratedEndDeviceGroup.Entry> entries = new ArrayList<>();
        LongStream deviceIds = Arrays.stream(ids);
        List<EndDevice> endDevices = deviceIds.mapToObj(meteringService::findEndDevice)
                .flatMap(asStream())
                .collect(Collectors.toList());

        Map<Long, EnumeratedEndDeviceGroup.Entry> currentEntries = enumeratedEndDeviceGroup.getEntries().stream()
                .collect(mapper());

        // remove those no longer mapped
        currentEntries.entrySet().stream()
                .filter(entry -> endDevices.stream().mapToLong(EndDevice::getId).noneMatch(id -> id == entry.getKey()))
                .forEach(entry -> enumeratedEndDeviceGroup.remove(entry.getValue()));

        // add new ones
        endDevices.stream()
                .filter(device -> !currentEntries.containsKey(device.getId()))
                .forEach(device -> enumeratedEndDeviceGroup.add(device, Interval.sinceEpoch().toClosedRange()));
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(enumeratedEndDeviceGroup::save));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    private Collector<EnumeratedEndDeviceGroup.Entry, ?, Map<Long, EnumeratedEndDeviceGroup.Entry>> mapper() {
        return Collectors.toMap(entry -> entry.getEndDevice().getId(), Function.identity());
    }

    public void endDeviceGroups() {
        meteringGroupsService.findEndDeviceGroups().stream()
                .peek(group -> System.out.println(group.getId() + " " + group.getName()))
                .flatMap(group -> group.getMembers(clock.instant()).stream())
                .map(device -> "\t" + device.getId() + " " + device.getMRID())
                .forEach(System.out::println);
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
