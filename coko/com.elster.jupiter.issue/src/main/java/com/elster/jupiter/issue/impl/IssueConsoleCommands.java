package com.elster.jupiter.issue.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.event.EventConst;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;

@Component(name = "com.elster.jupiter.issue.console", service = IssueConsoleCommands.class, property = {"osgi.command.scope=issue", "osgi.command.function=setAppServer"}, immediate = true)
public class IssueConsoleCommands {
    private volatile MessageService messageService;
    private volatile AppService appService;
    private volatile CronExpressionParser cronExpressionParser;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    public void setAppServer(String name, String cronString, int numberOfThreads) {
        threadPrincipalService.set(new Principal() {
            @Override
            public String getName() {
                return "console";
            }
        });
        try (TransactionContext context = transactionService.getContext()) {
            AppServer appServer = appService.createAppServer(name, cronExpressionParser.parse(cronString));

            Optional<SubscriberSpec> subscriberSpec = messageService.getSubscriberSpec(EventService.JUPITER_EVENTS, EventConst.AQ_SUBSCRIBER_NAME);
            if (!subscriberSpec.isPresent()) {
                System.out.println("Subscriber " + EventConst.AQ_SUBSCRIBER_NAME +" not found.");
            }
            SubscriberExecutionSpec subscriberExecutionSpec = appServer.createSubscriberExecutionSpec(subscriberSpec.get(), numberOfThreads);
            context.commit();
        }
        finally {
            threadPrincipalService.clear();
        }
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        this.cronExpressionParser = cronExpressionParser;
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
