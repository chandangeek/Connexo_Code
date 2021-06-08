/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.gogo;

import com.elster.jupiter.util.ResultWrapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component(service = DataLifeCycleCommands.class,property = {"osgi.command.scope=metering", "osgi.command.function=purgeData", "osgi.command.function=retention", "osgi.command.function=createPartitions"} , immediate = true)
public class DataLifeCycleCommands {

    private volatile LifeCycleService lifeCycleService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService principalService;
    
    @Reference
    public void setDataLifceCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }
    
    @Reference
    public void setTransactionService(TransactionService transactionService) {
    	this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService principalService) {
    	this.principalService = principalService;
    }
    
    public void purgeData() {
    	try {
    		principalService.set(() -> "Gogo");
    		transactionService.execute(() -> {lifeCycleService.runNow(); return null;});    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
    		principalService.clear();
    	}
    }
    
    public void retention(int days) {
    	try {
    		principalService.set(() -> "Gogo");
    		transactionService.execute(() -> {
    			lifeCycleService.getCategories().forEach(category -> category.setRetentionDays(days));
    			return null;
    		});    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
    		principalService.clear();
    	}
    }

	public void createPartitions() {
		System.out.println("Usage: createPartitions <dry-run flag>");
		System.out.println("e.g.   createPartitions false");
		System.out.println("In case of dry-run flag is 'true' partitions will not be actually created, it's just dry-run to see SQL commands for creation of partitions in logs.");
	}

	public void createPartitions(boolean dryRun) {
		try {
			principalService.set(() -> "Gogo");
			transactionService.run(() -> {
				ResultWrapper<String> result = lifeCycleService.createPartitions(Logger.getLogger(DataLifeCycleCommands.class.getName()), dryRun);
				if (!result.getFailedObjects().isEmpty()) {
					System.out.println("Partitions haven't been created for the following tables. Please check logs. [" + String.join(",", result.getFailedObjects()) + "]");
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			principalService.clear();
		}
	}
}