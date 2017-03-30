/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.validation.console",
        service = ValidationConsoleCommands.class,
        property = {"osgi.command.scope=validation",
                "osgi.command.function=availableValidators",
                "osgi.command.function=createRuleSet",
                "osgi.command.function=printDdl"},
        immediate = true)
public class ValidationConsoleCommands {

    private volatile ValidationService validationService;
    private volatile TransactionService transactionService;

    public void printDdl() {
        try {
            ((ValidationServiceImpl) validationService).getDataModel().getTables().forEach(
                    table -> table.getDdl().forEach(System.out::println));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void availableValidators() {
        System.out.println("Usage: availableValidators <qualityCodeSystem: MDM, MDC, OTHER>");
    }

    public void availableValidators(String qualityCodeSystem) {
        validationService.getAvailableValidators(QualityCodeSystem.of(qualityCodeSystem)).stream()
                .peek(est -> System.out.println(est.getDefaultFormat()))
                .flatMap(est -> est.getPropertySpecs().stream())
                .map(spec -> spec.getName() + ' ' + spec.getValueFactory().getValueType().toString())
                .forEach(System.out::println);
    }

    public void createRuleSet() {
        System.out.println("Usage: createRuleSet <ruleSetName> <qualityCodeSystem: MDM, MDC, OTHER>");
    }

    public void createRuleSet(String name, String qualityCodeSystem) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        validationService.createValidationRuleSet(name, QualityCodeSystem.of(qualityCodeSystem));
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }
}
