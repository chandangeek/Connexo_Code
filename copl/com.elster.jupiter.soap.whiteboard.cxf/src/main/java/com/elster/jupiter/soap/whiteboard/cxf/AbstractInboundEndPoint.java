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

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@ConsumerType
public abstract class AbstractInboundEndPoint {
    private static final String BATCH_EXECUTOR_USER_NAME = "batch executor";
    private final Set<String> webServiceMethodNames;

    /**
     * Attention: These fields are injectable by hardcoded names via {@link AbstractEndPointInitializer}.
     */
    protected TransactionService transactionService;
    protected ThreadPrincipalService threadPrincipalService;
    protected UserService userService;
    protected WebServicesService webServicesService;
    private InboundEndPointConfiguration endPointConfiguration;

    public AbstractInboundEndPoint() {
        webServiceMethodNames = Arrays.stream(getClass().getInterfaces())
                .filter(face -> face.getAnnotation(WebService.class) != null)
                .map(Class::getMethods)
                .flatMap(Arrays::stream)
                .filter(meth -> meth.getAnnotation(WebMethod.class) != null)
                .map(Method::getName)
                .collect(Collectors.toSet());
    }

    protected void setSecurityContext() {
        if (threadPrincipalService.getPrincipal() == null) {
            userService.findUser(BATCH_EXECUTOR_USER_NAME, userService.getRealm())
                    .ifPresent(threadPrincipalService::set);
        }
    }

    protected <RES, E extends Exception> RES runWithOccurrence(ExceptionThrowingSupplier<RES, E> supplier) throws E {
        try {
            saveRequestNameAndApplicationIfNeeded();
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

    private void saveRequestNameAndApplicationIfNeeded() {
        WebServiceCallOccurrence occurrence = webServicesService.getOccurrence();
        boolean needToSave = false;
        if (occurrence.getApplicationName() == null) {
            occurrence.setApplicationName(getApplicationName());
            needToSave = true;
        }
        if (occurrence.getRequest() == null) {
            StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            String methodName = Arrays.stream(stackTrace)
                    .skip(2) // skip method names until the calling ones
                    .map(StackTraceElement::getMethodName)
                    .filter(webServiceMethodNames::contains)
                    .findFirst()
                    .orElse(stackTrace[2].getMethodName());
            occurrence.setRequest(methodName);
            needToSave = true;
        }
        if (needToSave) {
            transactionService.runInIndependentTransaction(occurrence::save);
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

    private String getApplicationName() {
        return this instanceof ApplicationSpecific ?
                ((ApplicationSpecific) this).getApplication() :
                ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
    }
}
