package com.elster.jupiter.gogo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.transaction.TransactionService;

/**
 * Copyrights EnergyICT
 * Date: 17/06/2014
 * Time: 18:00
 */
@Component(service = DataLifeCycleCommands.class,property = {"osgi.command.scope=metering", "osgi.command.function=purgeData"} , immediate = true)                
public class DataLifeCycleCommands {

    private volatile LifeCycleService lifeCycleService;
    private volatile TransactionService transactionService;
    
    @Reference
    public void setDataLifceCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }
    
    @Reference
    public void setTransactionService(TransactionService transactionService) {
    	this.transactionService = transactionService;
    }

    public void purgeData() {
    	try {
    		transactionService.execute(() -> {lifeCycleService.runNow(); return null;});    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
 
}