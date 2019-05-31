/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.AbstractEndPointInitializer;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

import aQute.bnd.annotation.ConsumerType;
import org.apache.cxf.interceptor.Fault;

@ConsumerType
public abstract class AbstractInboundEndPoint {
    private static final String BATCH_EXECUTOR_USER_NAME = "batch executor";

    /**
     * Attention: These fields are injectable by hardcoded names via {@link AbstractEndPointInitializer}.
     */
    protected TransactionService transactionService;
    protected ThreadPrincipalService threadPrincipalService;
    protected UserService userService;
    protected WebServicesService webServicesService;
    private InboundEndPointConfiguration endPointConfiguration;

    protected void setSecurityContext() {
        if (threadPrincipalService.getPrincipal() == null) {
            userService.findUser(BATCH_EXECUTOR_USER_NAME, userService.getRealm())
                    .ifPresent(threadPrincipalService::set);
        }
    }

    protected <RES, E extends Exception> RES runWithOccurrence(ExceptionThrowingSupplier<RES, E> supplier) throws E {
        try {
            // TODO: add method name & application
            webServicesService.startOccurrence(endPointConfiguration, null, null);
            RES result = supplier.get();
            webServicesService.passOccurrence();
            return result;
        } catch (Fault fault) {
            webServicesService.failOccurrence(new Exception(fault.getCause()));
            throw fault;
        } catch (Exception exception) {
            webServicesService.failOccurrence(exception);
            throw exception;
        }
    }

    protected <RES, E extends Exception> RES runInTransactionWithOccurrence(ExceptionThrowingSupplier<RES, E> supplier) throws E {
        setSecurityContext();
        return runWithOccurrence(() -> transactionService.execute(supplier));
    }

    protected InboundEndPointConfiguration getEndPointConfiguration() {
        return endPointConfiguration;
    }

    protected void log(LogLevel logLevel, String message) {
        webServicesService.getOccurrence().log(logLevel, message);
    }

    protected void log(String message, Exception exception) {
        webServicesService.getOccurrence().log(message, exception);
    }
}
