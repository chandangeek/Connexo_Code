/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.dataquality.kpi.console", service = ConsoleCommands.class,
        property = {
                "osgi.command.scope=dataquality",
                "osgi.command.function=triggerDataQualityKpiTask"
        },
        immediate = true
)
@SuppressWarnings("unused")
public class ConsoleCommands {

    private volatile TransactionService transactionService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile DataQualityKpiService dataQualityKpiService;
    private volatile ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setDataQualityKpiService(DataQualityKpiService dataQualityKpiService) {
        this.dataQualityKpiService = dataQualityKpiService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public void triggerDataQualityKpiTask() {
        System.out.println("Trigger data quality kpi task now");
        System.out.println("Usage: triggerDataQualityKpiTask <end device group name>");
        System.out.println("       triggerDataQualityKpiTask <usage point group name> <metrology purpose name>");
    }

    public void triggerDataQualityKpiTask(String endDeviceGroupName) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            EndDeviceGroup group = meteringGroupsService.findEndDeviceGroupByName(endDeviceGroupName)
                    .orElseThrow(() -> new IllegalArgumentException("No end device group with name: " + endDeviceGroupName));
            DeviceDataQualityKpi kpi = dataQualityKpiService.deviceDataQualityKpiFinder()
                    .forGroup(group)
                    .stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No data quality kpi configured for device group: " + endDeviceGroupName));
            ((DataQualityKpiImpl) kpi).triggerNow();
            context.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void triggerDataQualityKpiTask(String usagePointGroupName, String metrologyPurposeName) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            UsagePointGroup group = meteringGroupsService.findUsagePointGroupByName(usagePointGroupName)
                    .orElseThrow(() -> new IllegalArgumentException("No usage point group with name: " + usagePointGroupName));
            MetrologyPurpose metrologyPurpose = metrologyConfigurationService.getMetrologyPurposes().stream()
                    .filter(purpose -> purpose.getName().equalsIgnoreCase(metrologyPurposeName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No metrology purpose found with name: " + metrologyPurposeName));
            UsagePointDataQualityKpi kpi = dataQualityKpiService.usagePointDataQualityKpiFinder()
                    .forGroup(group)
                    .forPurpose(metrologyPurpose)
                    .stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No data quality kpi configured for usage point group :"
                            + usagePointGroupName + " and purpose: " + metrologyPurposeName));
            ((DataQualityKpiImpl) kpi).triggerNow();
            context.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
