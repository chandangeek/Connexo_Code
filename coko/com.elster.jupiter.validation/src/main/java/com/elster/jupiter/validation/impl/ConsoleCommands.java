package com.elster.jupiter.validation.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.validation.console", service = ConsoleCommands.class, property = {"osgi.command.scope=validation", "osgi.command.function=createValidationRuleSet"}, immediate = true)
public class ConsoleCommands {

    private volatile ValidationService validationService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile UserService userService;

    public void createValidationRuleSet() {
        Optional<User> found = userService.findUser("batch executor");
        if(!found.isPresent()) {
            System.out.println("User not found");
        }
        threadPrincipalService.set(found.get());
        try {
            transactionService.execute(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    ValidationRuleSet validationRuleSet = new ValidationRuleSetImpl("myfirst validation ruleset");
                    validationRuleSet.save();
                }
            });
        } finally {
            threadPrincipalService.clear();
        }
    }

    @Reference
    public void setMeteringService(ValidationService validationService1) {
        this.validationService = validationService1;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
