/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.gogo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;

@Component(service = DataLifeCycleCommands.class,property = {"osgi.command.scope=metering", "osgi.command.function=purgeData", "osgi.command.function=retention"} , immediate = true)
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
 
}