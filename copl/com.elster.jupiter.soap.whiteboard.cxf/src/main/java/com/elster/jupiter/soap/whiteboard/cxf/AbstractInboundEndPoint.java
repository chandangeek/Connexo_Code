/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.AbstractEndPointInitializer;
import com.elster.jupiter.soap.whiteboard.cxf.impl.MessageUtils;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.SetMultimap;
import org.apache.cxf.interceptor.Fault;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Basic abstract class for implementation of inbound endpoints that should be provided with {@link InboundSoapEndPointProvider} and (in the future) {@link InboundRestEndPointProvider}.
 * Contains methods to wrap web service methods logic with or without DB transaction and track with web service call occurrences.
 * Creation of related web service call occurrences and (if needed) web service issues is implemented in message interceptors for SOAP web services,
 * but their states are tracked in this abstract class.
 * <b>NB:</b> During the implementation please don't forget to introduce explicit dependency on {@link WebServicesService} in the provider of the subclass,
 * otherwise the provider may not register on whiteboard and thus the inbound endpoint may work incorrectly (e.g. some fields below won't be injected).
 */
@ConsumerType
public abstract class AbstractInboundEndPoint {
    private static final String BATCH_EXECUTOR_USER_NAME = "batch executor";
    private final Set<String> webServiceMethodNames;
    @Resource
    protected WebServiceContext webServiceContext;

    /**
     * Attention: These fields are injectable by hardcoded names via {@link AbstractEndPointInitializer}.
     */
    protected TransactionService transactionService;
    protected ThreadPrincipalService threadPrincipalService;
    protected UserService userService;
    protected WebServicesService webServicesService;
    protected WebServiceCallOccurrenceService webServiceCallOccurrenceService;
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

    /**
     * Sets the "batch executor" principal for further operations. Principal can be required for DB transaction support, checking privileges etc.
     */
    protected void setSecurityContext() {
        if (threadPrincipalService.getPrincipal() == null) {
            userService.findUser(BATCH_EXECUTOR_USER_NAME, userService.getRealm())
                    .ifPresent(threadPrincipalService::set);
        }
    }

    /**
     * Wraps the {@code supplier} into a try/catch block and creates a {@link WebServiceCallOccurrence} for this operation.
     * At the end of execution the occurrence is passed unless any exception is caught; otherwise the occurrence is failed, and all exceptions are rethrown.
     *
     * @param supplier The operation to execute in scope of the web service call occurrence.
     * @param <RES>    The type of result returned by the {@code supplier}.
     * @param <E>      The type of {@link Fault} exception thrown from the {@code supplier}.
     * @return The result (response) object returned by the {@code supplier}.
     * @throws E The exception thrown from the {@code supplier}.
     */
    protected <RES, E extends Exception> RES runWithOccurrence(ExceptionThrowingSupplier<RES, E> supplier) throws E {
        long id = MessageUtils.getOccurrenceId(webServiceContext);
        try {
            saveRequestNameAndApplicationIfNeeded(id);
            RES result = supplier.get();
            webServiceCallOccurrenceService.passOccurrence(id);
            return result;
        } catch (Fault fault) {
            webServiceCallOccurrenceService.failOccurrence(id, new Exception(fault.getCause()));
            throw fault;
        } catch (Exception exception) {
            webServiceCallOccurrenceService.failOccurrence(id, exception);
            throw exception;
        }
    }

    private void saveRequestNameAndApplicationIfNeeded(long id) {
        WebServiceCallOccurrence occurrence = webServiceCallOccurrenceService.getOngoingOccurrence(id);
        boolean needToSave = false;
        if (!occurrence.getApplicationName().isPresent()) {
            occurrence.setApplicationName(getApplicationName());
            needToSave = true;
        }
        if (!occurrence.getRequest().isPresent()) {
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

    /**
     * Wraps the {@code supplier} into a transaction performed under user "batch executor" and creates a {@link WebServiceCallOccurrence} for this operation.
     * At the end of execution the occurrence is passed unless any exception is caught; otherwise the occurrence is failed, and all exceptions are rethrown.
     * The same as {@link #setSecurityContext()} and {@link #runWithOccurrence(ExceptionThrowingSupplier)} with supplier wrapped into a transaction.
     *
     * @param supplier The operation to execute in scope of the web service call occurrence.
     * @param <RES>    The type of result returned by the {@code supplier}.
     * @param <E>      The type of {@link Fault} exception thrown from the {@code supplier}.
     * @return The result (response) object returned by the {@code supplier}.
     * @throws E The exception thrown from the {@code supplier}.
     */
    protected <RES, E extends Exception> RES runInTransactionWithOccurrence(ExceptionThrowingSupplier<RES, E> supplier) throws E {
        setSecurityContext();
        return runWithOccurrence(() -> transactionService.execute(supplier));
    }

    protected InboundEndPointConfiguration getEndPointConfiguration() {
        return endPointConfiguration;
    }

    protected void log(LogLevel logLevel, String message) {
        webServiceCallOccurrenceService.getOngoingOccurrence(MessageUtils.getOccurrenceId(webServiceContext)).log(logLevel, message);
    }

    protected void log(String message, Exception exception) {
        webServiceCallOccurrenceService.getOngoingOccurrence(MessageUtils.getOccurrenceId(webServiceContext)).log(message, exception);
    }

    protected void saveRelatedAttribute(String type, String value) {
        if (value != null && !value.isEmpty()) {
            webServiceCallOccurrenceService.getOngoingOccurrence(MessageUtils.getOccurrenceId(webServiceContext)).saveRelatedAttribute(type, value);
        }
    }

    protected void saveRelatedAttributes(SetMultimap<String, String> values) {
        webServiceCallOccurrenceService.getOngoingOccurrence(MessageUtils.getOccurrenceId(webServiceContext)).saveRelatedAttributes(values);
    }

    private String getApplicationName() {
        return this instanceof ApplicationSpecific ?
                ((ApplicationSpecific) this).getApplication() :
                ApplicationSpecific.WebServiceApplicationName.MULTISENSE_INSIGHT.getName();
    }
}
