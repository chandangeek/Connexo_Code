/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.security.Principal;
import java.util.List;

public abstract class CommandWithTransaction {

    private TransactionService transactionService;
    private ThreadPrincipalService threadPrincipalService;
    private UserService userService;
    private User user;

    @Inject
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Inject
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void runInTransaction() {
        checkServices();
        if (!transactionService.isInTransaction()) {
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
        if (!transactionService.isInTransaction()) {
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
                throw ex;
            } finally {
                clearPrincipal();
            }
        } else {
            toRunInsideTransaction.run();
        }
    }

    private void checkServices() {
        if (transactionService == null || threadPrincipalService == null) {
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

    protected Principal getPrincipal() {
        if (user == null) {
            user = createSuperUser();
        }
        return user;
    }

    protected User createSuperUser() {
        return userService.findUser("root").orElseGet(() ->
        {
            threadPrincipalService.set(() -> "console");
            try (TransactionContext context = transactionService.getContext()) {
                User user = userService.createUser("root", "root");
                userService.getGroups()
                        .stream()
                        .filter(
                                group -> group.getPrivileges().values()
                                        .stream()
                                        .flatMap(List::stream)
                                        .noneMatch(privilege -> privilege.getCategory()
                                                .getName()
                                                .equals(DualControlService.DUAL_CONTROL_GRANT_CATEGORY))

                        )
                        .forEach(user::join);
                context.commit();
                return user;
            } finally {
                threadPrincipalService.clear();
            }
        });
    }
}
