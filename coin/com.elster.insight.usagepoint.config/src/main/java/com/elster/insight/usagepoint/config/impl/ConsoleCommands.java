package com.elster.insight.usagepoint.config.impl;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;

@Component(name = "com.elster.insight.usagepoint.config.console", service = ConsoleCommands.class, property = {"osgi.command.scope=usagepoint", "osgi.command.function=createMetrologyConfiguration", "osgi.command.function=renameMetrologyConfiguration", "osgi.command.function=deleteMetrologyConfiguration", "osgi.command.function=metrologyConfigurations"}, immediate = true)
public class ConsoleCommands {

    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    public void createMetrologyConfiguration(String name) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                usagePointConfigurationService.newMetrologyConfiguration(name);
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void renameMetrologyConfiguration(long id, String name) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).get();
                metrologyConfiguration.setName(name);
                metrologyConfiguration.save();
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }
    
    public void deleteMetrologyConfiguration(long id) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).get();
                metrologyConfiguration.delete();
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void metrologyConfigurations() {
       usagePointConfigurationService.findAllMetrologyConfigurations().stream().forEach(System.out::println);
    }
    
    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

}
