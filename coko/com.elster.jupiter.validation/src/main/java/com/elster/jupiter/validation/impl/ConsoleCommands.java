package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.validation.console", service = ConsoleCommands.class, property = {"osgi.command.scope=validation", "osgi.command.function=printDdl"}, immediate = true)
public class ConsoleCommands {

    private volatile MeteringService meteringService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile ValidationService validationService;
    private volatile DataModel dataModel;

    public void printDdl() {
        try {
            for (Table<?> table : dataModel.getTables()) {
                for (String s : table.getDdl()) {
                    System.out.println(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
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
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
        this.dataModel = ((ValidationServiceImpl) validationService).getDataModel();
    }
}
