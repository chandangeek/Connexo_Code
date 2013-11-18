package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.validation.console", service = ConsoleCommands.class, property = {"osgi.command.scope=validation", "osgi.command.function=printDdl"}, immediate = true)
public class ConsoleCommands {

    private volatile MeteringService meteringService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    public void printDdl() {
        try {
            for (Table table : Bus.getOrmClient().getDataModel().getTables()) {
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
}
