/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.dataqualitykpi.console", service = ConsoleCommands.class,
        property = {
                "osgi.command.scope=dataqualitykpi",
                "osgi.command.function=triggerDataQualityKpiTask"
        },
        immediate = true
)
public class ConsoleCommands {

    private volatile TaskService taskService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
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

    public void triggerDataQualityKpiTask(String endDeviceGroupName) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            EndDeviceGroup group = meteringGroupsService.findEndDeviceGroupByName(endDeviceGroupName)
                    .orElseThrow(() -> new IllegalArgumentException("No end device group with name: " + endDeviceGroupName));
            String taskName = KpiType.VALIDATION.recurrentTaskName(group.getId());
            RecurrentTask recurrentTask = taskService.getRecurrentTask(taskName)
                    .orElseThrow(() -> new RuntimeException("No recurrent task found for name: " + taskName));
            recurrentTask.triggerNow();
            context.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
