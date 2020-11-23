/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.pki.impl.tasks;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.SecurityManagementServiceImpl;
import com.elster.jupiter.pki.impl.TranslationKeys;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component(name = "com.elster.jupiter.pki.impl.tasks.AddCertificateRequestDataHandler",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + AddCertificateRequestDataHandlerFactory.ADD_CERTIFICATE_REQUEST_DATA_TASK_SUBSCRIBER,
                "destination=" + AddCertificateRequestDataHandlerFactory.ADD_CERTIFICATE_REQUEST_DATA_TASK_DESTINATION},
        immediate = true)
public class AddCertificateRequestDataHandlerFactory implements MessageHandlerFactory {
    public static final String APPLICATION_NAME = "MultiSense";
    public static final String ADD_CERTIFICATE_REQUEST_DATA_TASK_PERIOD_PROPERTY = "com.elster.jupiter.pki.addcertificaterequestdata.task.period";
    public static final String ADD_CERTIFICATE_REQUEST_DATA_TASK_RUNTIME_PROPERTY = "com.elster.jupiter.pki.addcertificaterequestdata.task.start";
    public static final String ADD_CERTIFICATE_REQUEST_DATA_TASK_FILE_PATH_PROPERTY = "com.elster.jupiter.pki.addcertificaterequestdata.task.parameters.file";

    public static final String ADD_CERTIFICATE_REQUEST_DATA_TASK_DESTINATION = "AddCertReqDataTopic";
    public static final String ADD_CERTIFICATE_REQUEST_DATA_TASK_SUBSCRIBER = "AddCertReqDataSubscriber";
    public static final String ADD_CERTIFICATE_REQUEST_DATA_TASK_SUBSCRIBER_FORMAT = "Adds certificate request data";

    public static final String ADD_CERTIFICATE_REQUEST_DATA_TASK_NAME = "Add Certificate Request Data Task";
    private static final String QUEUE_TABLE_SPEC_NAME = "MSG_RAWTOPICTABLE";
    private static final int ADD_CERTIFICATE_REQUEST_DATA_TASK_RETRY_DELAY = 60;
    private static final Logger LOGGER = Logger.getLogger(AddCertificateRequestDataHandlerFactory.class.getName());

    private volatile SecurityManagementServiceImpl securityManagementServiceImpl;
    private volatile MessageService messageService;
    private volatile NlsService nlsService;
    private volatile TaskService taskService;
    private volatile Thesaurus thesaurus;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    private volatile String path;
    private volatile String period;
    private volatile int count;
    private volatile String runtimeValue;
    private volatile LocalTime runtime;

    @Activate
    public void activate(BundleContext context) {
        Optional<PeriodicalScheduleExpression> scheduleOpt = Optional.empty();
        this.thesaurus = nlsService.getThesaurus(SecurityManagementService.COMPONENTNAME, Layer.DOMAIN);
        threadPrincipalService.set(() -> "Installer");

        path = context.getProperty(ADD_CERTIFICATE_REQUEST_DATA_TASK_FILE_PATH_PROPERTY);
        if (Checks.is(path).emptyOrOnlyWhiteSpace()) {
            LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_IS_NOT_SET.getDefaultFormat(), ADD_CERTIFICATE_REQUEST_DATA_TASK_FILE_PATH_PROPERTY);
        }

        runtimeValue = context.getProperty(ADD_CERTIFICATE_REQUEST_DATA_TASK_RUNTIME_PROPERTY);
        if (!Checks.is(runtimeValue).emptyOrOnlyWhiteSpace()) {
            try {
                runtime = LocalTime.parse(runtimeValue, DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH));
            } catch (DateTimeParseException ex) {
                LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_FORMAT_ERROR.getDefaultFormat(), ADD_CERTIFICATE_REQUEST_DATA_TASK_RUNTIME_PROPERTY);
            }
        } else {
            runtime = LocalTime.MIN;
        }

        period = context.getProperty(ADD_CERTIFICATE_REQUEST_DATA_TASK_PERIOD_PROPERTY);
        if (!Checks.is(period).emptyOrOnlyWhiteSpace()) {
            String[] periodParams = period.trim().split(" ");
            if (periodParams.length != 2) {
                LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_FORMAT_ERROR.getDefaultFormat(), ADD_CERTIFICATE_REQUEST_DATA_TASK_PERIOD_PROPERTY);
            } else {
                try {
                    count = Integer.valueOf(periodParams[0]);
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_FORMAT_ERROR.getDefaultFormat(), ADD_CERTIFICATE_REQUEST_DATA_TASK_PERIOD_PROPERTY);
                }
                scheduleOpt = Optional.ofNullable(getPeriodicalScheduleExpression(count, periodParams[1]));
            }
        }

        try (TransactionContext cont = transactionService.getContext()) {
            Optional<RecurrentTask> recurrentTaskOpt = taskService.getRecurrentTask(ADD_CERTIFICATE_REQUEST_DATA_TASK_NAME);
            if (!scheduleOpt.isPresent()) {
                recurrentTaskOpt.ifPresent(task -> task.delete());
            } else {
                PeriodicalScheduleExpression schedule = scheduleOpt.get();
                RecurrentTask recurrentTask = recurrentTaskOpt.orElseGet(() -> createAddCertificateRequestDataTask(schedule));
                recurrentTask.setScheduleExpression(schedule);
                recurrentTask.updateNextExecution();
                recurrentTask.save();
            }
            cont.commit();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    private PeriodicalScheduleExpression getPeriodicalScheduleExpression(Integer count, String periodType) {
        String checkPeriod = periodType.toLowerCase();
        Calendar c = Calendar.getInstance();
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        switch (checkPeriod) {
            case "minute":
            case "minutes":
                return PeriodicalScheduleExpression
                        .every(count).minutes().at(0).build();
            case "hour":
            case "hours":
                return PeriodicalScheduleExpression
                        .every(count).hours().at(0, 0).build();
            case "day":
            case "days":
                return PeriodicalScheduleExpression
                        .every(count).days().at(runtime.getHour(), runtime.getMinute(), 0).build();
            case "week":
            case "weeks":
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
                return PeriodicalScheduleExpression
                        .every(count).weeks().at(DayOfWeek.of(dayOfWeek), runtime.getHour(), runtime.getMinute(), 0).build();
            case "month":
            case "months":
                return PeriodicalScheduleExpression
                        .every(count).months().at(dayOfMonth, runtime.getHour(), runtime.getMinute(), 0).build();
            case "year":
            case "years":
                int monthOfYear = c.get(Calendar.MONTH) + 1;
                return PeriodicalScheduleExpression
                        .every(count).years().at(monthOfYear, dayOfMonth, runtime.getHour(), runtime.getMinute(), 0).build();
            default:
                return null;
        }
    }

    private RecurrentTask createAddCertificateRequestDataTask(PeriodicalScheduleExpression schedule) {
        DestinationSpec destination = messageService.getDestinationSpec(ADD_CERTIFICATE_REQUEST_DATA_TASK_DESTINATION).orElseGet(() -> {
            DestinationSpec newDestination = messageService.getQueueTableSpec(QUEUE_TABLE_SPEC_NAME).orElseThrow(
                    () -> new IllegalStateException("'" + QUEUE_TABLE_SPEC_NAME + "' QueueTableSpec should be defined."))
                    .createDestinationSpec(ADD_CERTIFICATE_REQUEST_DATA_TASK_DESTINATION, ADD_CERTIFICATE_REQUEST_DATA_TASK_RETRY_DELAY);
            newDestination.activate();
            newDestination.subscribe(TranslationKeys.ADD_CERTIFICATE_REQUEST_DATA_TASK_SUBSCRIBER_NAME, SecurityManagementService.COMPONENTNAME, new SecurityManagementServiceImpl().getLayer());
            return newDestination;
        });
        return taskService.newBuilder().setApplication(APPLICATION_NAME).setName(ADD_CERTIFICATE_REQUEST_DATA_TASK_NAME)
                .setScheduleExpression(schedule).setDestination(destination)
                .setPayLoad("payload").build();
    }

    @Override
    public MessageHandler newMessageHandler() {
        return taskService.createMessageHandler(new AddCertificateRequestDataHandler(path, securityManagementServiceImpl, transactionService, threadPrincipalService, thesaurus, LOGGER));
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Reference
    public final void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public final void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setSecurityManagementServiceImpl(SecurityManagementServiceImpl securityManagementServiceImpl) {
        this.securityManagementServiceImpl = securityManagementServiceImpl;
    }
}
