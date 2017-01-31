/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;


import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedGroup;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;

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

@Component(name = "com.elster.jupiter.metering.groups.console",
        service = ConsoleCommands.class,
        property = {
                "osgi.command.scope=metering",
                "osgi.command.function=createEnumeratedEndDeviceGroup",
                "osgi.command.function=updateEnumeratedEndDeviceGroup",
                "osgi.command.function=createEnumeratedUsagePointGroup",
                "osgi.command.function=updateEnumeratedUsagePointGroup",
                "osgi.command.function=countGroupMembers",
                "osgi.command.function=endDeviceGroups"}, immediate = true)
public class ConsoleCommands {

    private volatile MeteringGroupsService meteringGroupsService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;

    public void createEnumeratedEndDeviceGroup(String name, long... ids) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> meteringGroupsService.createEnumeratedEndDeviceGroup()
                    .setName(name)
                    .at(clock.instant())
                    .containing(Arrays.stream(ids)
                            .mapToObj(meteringService::findEndDeviceById)
                            .flatMap(asStream())
                            .toArray(EndDevice[]::new))
                    .create()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void createEnumeratedUsagePointGroup(String name, long... ids) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> meteringGroupsService.createEnumeratedUsagePointGroup()
                                    .setName(name)
                                    .containing(Arrays.stream(ids)
                                            .mapToObj(meteringService::findUsagePointById)
                                            .flatMap(Functions.asStream())
                                            .toArray(UsagePoint[]::new))
                                    .create());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateEnumeratedEndDeviceGroup(String name, long... ids) {
        EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(name).orElseThrow(() -> new IllegalArgumentException("group not found"));
        final EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = (EnumeratedEndDeviceGroup) endDeviceGroup;
        final List<EnumeratedGroup.Entry<EndDevice>> entries = new ArrayList<>();
        LongStream deviceIds = Arrays.stream(ids);
        List<EndDevice> endDevices = deviceIds.mapToObj(meteringService::findEndDeviceById)
                .flatMap(asStream())
                .collect(Collectors.toList());

        Map<Long, EnumeratedGroup.Entry<EndDevice>> currentEntries = enumeratedEndDeviceGroup.getEntries().stream()
                .collect(endDeviceMapper());

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
            transactionService.execute(VoidTransaction.of(enumeratedEndDeviceGroup::update));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void updateEnumeratedUsagePointGroup(String name, long... ids) {
        UsagePointGroup usagePointGroup = meteringGroupsService.findUsagePointGroupByName(name).orElseThrow(() -> new IllegalArgumentException("group not found"));
        final EnumeratedUsagePointGroup enumeratedUsagePointGroup = (EnumeratedUsagePointGroup) usagePointGroup;
        LongStream usagePointIds = Arrays.stream(ids);
        List<UsagePoint> usagePoints = usagePointIds.mapToObj(meteringService::findUsagePointById)
                .flatMap(asStream())
                .collect(Collectors.toList());

        Map<Long, EnumeratedGroup.Entry<UsagePoint>> currentEntries = enumeratedUsagePointGroup.getEntries().stream()
                .collect(usagePointMapper());

        // remove those no longer mapped
        currentEntries.entrySet().stream()
                .filter(entry -> usagePoints.stream().mapToLong(UsagePoint::getId).noneMatch(id -> id == entry.getKey()))
                .forEach(entry -> enumeratedUsagePointGroup.remove(entry.getValue()));

        // add new ones
        usagePoints.stream()
                .filter(usagePoint -> !currentEntries.containsKey(usagePoint.getId()))
                .forEach(usagePoint -> enumeratedUsagePointGroup.add(usagePoint, Interval.sinceEpoch().toClosedRange()));
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(enumeratedUsagePointGroup::update));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    private Collector<EnumeratedGroup.Entry<EndDevice>, ?, Map<Long, EnumeratedGroup.Entry<EndDevice>>> endDeviceMapper() {
        return Collectors.toMap(entry -> entry.getMember().getId(), Function.identity());
    }

    private Collector<EnumeratedGroup.Entry<UsagePoint>, ?, Map<Long, EnumeratedGroup.Entry<UsagePoint>>> usagePointMapper() {
        return Collectors.toMap(entry -> entry.getMember().getId(), Function.identity());
    }

    public void endDeviceGroups() {
        meteringGroupsService.findEndDeviceGroups().stream()
                .peek(group -> System.out.println(group.getId() + ' ' + group.getName()))
                .flatMap(group -> group.getMembers(clock.instant()).stream())
                .map(device -> '\t' + device.getId() + ' ' + device.getName() + ' ' + device.getMRID())
                .forEach(System.out::println);
    }

    public void countGroupMembers(String mRID) {
        Optional<EndDeviceGroup> deviceGroup = meteringGroupsService.findEndDeviceGroup(mRID);
        if (deviceGroup.isPresent()) {
            this.countGroupMembers(deviceGroup.get());
        } else {
            System.out.println("No end device group with mRID " + mRID);
        }
    }

    private void countGroupMembers(EndDeviceGroup deviceGroup) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> this.doCountGroupMembers(deviceGroup)));
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
        } finally {
            threadPrincipalService.clear();
        }
    }

    private void doCountGroupMembers(EndDeviceGroup deviceGroup) {
        long memberCount = deviceGroup.getMemberCount(clock.instant());
        System.out.println("number of members in group: " + memberCount);
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
