package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.ConsoleUser;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;


import javax.inject.Inject;
import java.security.Principal;

public abstract class CommandWithTransaction {

    private TransactionService transactionService;
    private ThreadPrincipalService threadPrincipalService;

    @Inject
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Inject
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public void runInTransaction() {
        checkServices();
        if(!transactionService.isInTransaction()) {
            setPrincipal();
            try {
                System.out.println("Starting execution");
                transactionService.execute(() -> {
                    run();
                    return null;
                });
                System.out.println("Transaction completed successfully");
            } catch (Exception ex) {
                System.out.println("Transaction failed!");
                ex.printStackTrace();
                throw ex;
            } finally {
                clearPrincipal();
            }
        } else {
            this.run();
        }
    }

    protected void executeTransaction(Runnable toRunInsideTransaction) {
        checkServices();
        if(!transactionService.isInTransaction()) {
            setPrincipal();
            try {
                System.out.println("Starting execution");
                transactionService.execute(() -> {
                    toRunInsideTransaction.run();
                    return null;
                });
                System.out.println("Transaction completed successfully");
            } catch (Exception ex) {
                System.out.println("Transaction failed!");
                ex.printStackTrace();
            } finally {
                clearPrincipal();
            }
        } else {
            toRunInsideTransaction.run();
        }
    }

    private void checkServices(){
        if(transactionService==null || threadPrincipalService==null){
            throw new IllegalStateException();
        }
    }

    public abstract void run();

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private Principal getPrincipal() {
        return new ConsoleUser();
    }
}
