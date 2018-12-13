/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Usage:
 * mdckpis:create 3006 (Y (MIN | MAX) <connection setup target> | N) (Y (MIN | MAX) <communication task target> | N)
 * Examples:
 * <ul>
 * <li>mdckpis:create ElsterAS3000 Y MIN "50" N:
 *     creates a new DataCollectionKpi that calculates the connection setup kpi every hour (hard coded)
 *     with a minimum target of 50 for the {@link com.energyict.mdc.device.data.Device}s
 *     in the {@link com.elster.jupiter.metering.groups.QueryEndDeviceGroup} with the id 3006</li>
 * </ul>.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-15 (14:01)
 */
@Component(name = "com.energyict.mdc.device.data.mdckpis", service = MdcKPIs.class,
        property = {
                "osgi.command.scope=mdckpis",
                "osgi.command.function=create"},
        immediate = true)
@SuppressWarnings("unused")
public class MdcKPIs {

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile DataCollectionKpiService dataCollectionKpiService;

    @SuppressWarnings("unused")
    public void create(String... options) {
        this.transactionService.execute(() -> this.doCreate(options));
    }

    private DataCollectionKpi doCreate(String... options) {
        Iterator<String> optionsIterator = Arrays.asList(options).iterator();
        String deviceGroupMRID = optionsIterator.next();
        Optional<EndDeviceGroup> deviceGroup = this.meteringGroupsService.findEndDeviceGroup(deviceGroupMRID);
        if (deviceGroup.isPresent()) {
            if (deviceGroup.get() instanceof QueryEndDeviceGroup) {
                QueryEndDeviceGroup queryDeviceGroup = (QueryEndDeviceGroup) deviceGroup.get();
                this.setPrincipal();
                return this.buildFromOptions(this.dataCollectionKpiService.newDataCollectionKpi(queryDeviceGroup), optionsIterator);
            }
            else {
                System.out.println("Currently only support for " + QueryEndDeviceGroup.class.getName() + " but got " + deviceGroup.getClass().getName());
            }
        }
        else {
            System.out.println("Device group with mRID '" + deviceGroupMRID + "' does not exist");
        }
        return null;
    }

    private DataCollectionKpi buildFromOptions(DataCollectionKpiService.DataCollectionKpiBuilder builder, Iterator<String> optionsIterator) {
        Stream.of(BuilderFromOptions.values())
            .forEach(b -> b.build(builder, optionsIterator));
        return builder.save();
    }

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private Principal getPrincipal() {
        return () -> "gogo-console";
    }

    @Reference
    @SuppressWarnings("unused")
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setDataCollectionKpiService(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    private static void showUsage() {
        System.out.println("Usage: mdckpis:create 3006 (Y (MIN | MAX) <connection setup target> | N) (Y (MIN | MAX) <communication task target> | N)");
    }

    private enum BuilderFromOptions {
        CONNECTIONS {
            @Override
            protected String description() {
                return "connection setup";
            }

            @Override
            protected DataCollectionKpiService.KpiTargetBuilder targetBuilder(DataCollectionKpiService.DataCollectionKpiBuilder builder) {
                builder.frequency(Duration.ofHours(1));
                return builder.calculateConnectionSetupKpi();
            }
        },

        COMMUNICATIONS {
            @Override
            protected String description() {
                return "communication task";
            }

            @Override
            protected DataCollectionKpiService.KpiTargetBuilder targetBuilder(DataCollectionKpiService.DataCollectionKpiBuilder builder) {
                builder.frequency(Duration.ofHours(1));
                return builder.calculateComTaskExecutionKpi();
            }
        };

        protected abstract DataCollectionKpiService.KpiTargetBuilder targetBuilder(DataCollectionKpiService.DataCollectionKpiBuilder builder);

        protected abstract String description();

        private void build(DataCollectionKpiService.DataCollectionKpiBuilder builder, Iterator<String> options) {
            if ("Y".equals(options.next())) {
                DataCollectionKpiService.KpiTargetBuilder targetBuilder = this.targetBuilder(builder);
                String targetType = options.next();
                switch (targetType) {
                    case "MAX": {
                        targetBuilder.expectingAsMaximum(new BigDecimal(options.next()));
                        break;
                    }
                    case "MIN": {
                        targetBuilder.expectingAsMaximum(new BigDecimal(options.next()));
                        break;
                    }
                    default: {
                        showUsage();
                        throw new IllegalArgumentException("Unexpected token: " + targetType + ", expecting either MIN or MAX");
                    }
                }
            }
            else {
                System.out.println("No " + this.description() + " kpi information requested");
            }
        }

    }
}