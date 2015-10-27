package com.elster.insight.usagepoint.config.impl;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

@Component(name = "com.elster.insight.usagepoint.config.console", 
    service = ConsoleCommands.class, 
    property = {"osgi.command.scope=usagepoint", 
                "osgi.command.function=createMetrologyConfiguration", 
                "osgi.command.function=renameMetrologyConfiguration", 
                "osgi.command.function=deleteMetrologyConfiguration", 
                "osgi.command.function=metrologyConfigurations",
                "osgi.command.function=linkUsagePointToMetrologyConfiguration",
                "osgi.command.function=createValidationRuleSet",
                "osgi.command.function=assignValRuleSetToMetrologyConfig"}, immediate = true)
public class ConsoleCommands {

    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringService meteringService;
    private volatile ValidationService validationService;

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
                metrologyConfiguration.update();
                
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
    
    public void linkUsagePointToMetrologyConfiguration(String usagePointMRID, String metrologyConfigName) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                UsagePoint up = meteringService.findUsagePoint(usagePointMRID).orElseThrow(() -> new IllegalArgumentException("Usage Point " + usagePointMRID + " not found."));
                MetrologyConfiguration mc = usagePointConfigurationService.findMetrologyConfiguration(metrologyConfigName).orElseThrow(() -> new IllegalArgumentException("Metrology configuration " + metrologyConfigName + " not found."));
                usagePointConfigurationService.link(up, mc);
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }

    }
    
    public void createValidationRuleSet(String name) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                validationService.createValidationRuleSet(name);
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }
    
    public void assignValRuleSetToMetrologyConfig(String metrologyConfigName, String ruleSetName) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                MetrologyConfiguration mc = usagePointConfigurationService.findMetrologyConfiguration(metrologyConfigName).orElseThrow(() -> new IllegalArgumentException("Metrology configuration " + metrologyConfigName + " not found."));
                ValidationRuleSet vrs = validationService.getValidationRuleSet(ruleSetName).orElseThrow(() -> new IllegalArgumentException("Rule set " + ruleSetName + " not found."));
                mc.addValidationRuleSet(vrs);
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
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

    @Reference
    public void setMetringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }
}