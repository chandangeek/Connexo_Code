/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;

import oracle.jdbc.aq.AQMessage;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.io.PrintStream;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MSG console commands
 */
@Component(name = "com.elster.jupiter.messaging.commands", service = ConsoleCommandsImpl.class,
        property = {"name=" + MessageService.COMPONENTNAME + "2", "osgi.command.scope=messagingoracle",
                "osgi.command.function=aqcreatetable",
                "osgi.command.function=aqdroptable",
                "osgi.command.function=drain",
                "osgi.command.function=subscribe",
                "osgi.command.function=createQueue",
                "osgi.command.function=destinations",
                "osgi.command.function=activate",
                "osgi.command.function=resubscribe",
                "osgi.command.function=drainToException",
                "osgi.command.function=enqueue",
                "osgi.command.function=enqueueMultipleMessages",
                "osgi.command.function=numberOfMessages",
                "osgi.command.function=numberOfErrors",
                "osgi.command.function=retries",
                "osgi.command.function=retryDelay",
                "osgi.command.function=updateRetrying",
                "osgi.command.function=purgeErrors"
        })
public class ConsoleCommandsImpl {

    private static final Logger LOGGER = Logger.getLogger(ConsoleCommandsImpl.class.getName());

    private PrintStream output = System.out;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile MessageService messageService;

    @Activate
    public void activate(BundleContext context) {
    }

    @Deactivate
    public void deactivate() {
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
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void aqcreatetable(String in) {
        output.println("About to create Queue table " + in);
        try {
            messageService.createQueueTableSpec(in, "RAW", false);
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void aqdroptable(String in) {
        try {
            Optional<QueueTableSpec> queueTableSpec = messageService.getQueueTableSpec(in);
            if (queueTableSpec.isPresent()) {
                queueTableSpec.get().deactivate();
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    public void drain(String subscriberName, String destinationName) {
        Optional<SubscriberSpec> spec = messageService.getSubscriberSpec(destinationName, subscriberName);
        try {
            AQMessage message = ((SubscriberSpecImpl) spec.get()).receiveNow();
            while (message != null) {
                output.println(new String(message.getPayload()));
                message = ((SubscriberSpecImpl) spec.get()).receiveNow();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void drainToException(String subscriberName, String destinationName) {
        SubscriberSpecImpl spec = (SubscriberSpecImpl) messageService.getSubscriberSpec(destinationName, subscriberName).get();
        AQMessage message = null;
        boolean empty = false;
        while (!empty) {
            try (TransactionContext context = transactionService.getContext()) {
                message = spec.receiveNow();
                if (message != null) {
                    throw new RuntimeException();
                }
                empty = true;
            } catch (RuntimeException e) {
                // just generating retries
            }
        }
    }

    public void createQueue(final String queueName, final int retryDelay, final int retries) {
        try {
            transactionService.builder()
                    .principal(() -> "Command line")
                    .run(() -> {
                        Optional<QueueTableSpec> defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE");
                        if (defaultQueueTableSpec.isPresent()) {
                            DestinationSpec destinationSpec = defaultQueueTableSpec.get().createDestinationSpec(queueName, retryDelay, retries);
                            destinationSpec.activate();
                        } else {
                            System.err.println("RAWQUEUETABLE not present! Please create first");
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void subscribe(final String subscriberName, final String component, final String layer, final String destinationName) {
        try {
            transactionService.builder()
                    .principal(() -> "Command line")
                    .run(() -> {
                        Optional<DestinationSpec> destination = messageService.getDestinationSpec(destinationName);
                        if (!destination.isPresent()) {
                            System.err.println("No such destination " + destinationName);
                        }
                        destination.get().subscribe(new SimpleTranslationKey(subscriberName, subscriberName), component, Layer.valueOf(layer));
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void resubscribe(String destinationName, String subsriberName) {
        try {
            transactionService.builder()
                    .principal(() -> "Command line")
                    .run(() -> {
                        Optional<DestinationSpec> destination = messageService.getDestinationSpec(destinationName);
                        if (!destination.isPresent()) {
                            System.err.println("No such destination " + destinationName);
                        }
                        Optional<SubscriberSpec> subscriber = destination.get().getSubscribers().stream().filter(s -> s.getName().equalsIgnoreCase(subsriberName)).findFirst();
                        if (subscriber.isPresent()) {
                            SubscriberSpecImpl subscriberImpl = (SubscriberSpecImpl) subscriber.get();
                            subscriberImpl.unSubscribe();
                            subscriberImpl.subscribe();
                        }

                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void activate(String destinationName) {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destinationName).orElseThrow(IllegalArgumentException::new);
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(destinationSpec::activate));
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void destinations() {
        ((MessageServiceImpl) messageService).getDataModel().mapper(DestinationSpec.class).find().stream()
                .peek(dest -> System.out.println(dest.getName()))
                .flatMap(dest -> dest.getSubscribers().stream())
                .map(s -> "\t" + s.getName())
                .forEach(System.out::println);
    }

    public void enqueue(String destination, String message) {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destination).orElseThrow(IllegalArgumentException::new);
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                destinationSpec.message(message).send();
            }));
        } finally {
            threadPrincipalService.clear();
        }

    }

    public void enqueueMultipleMessages(String destination, String message, int numberOfmessages) {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destination).orElseThrow(IllegalArgumentException::new);
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                for (int i = 0; i < numberOfmessages; i++) {
                    destinationSpec.message(message).send();
                }
            }));
        } finally {
            threadPrincipalService.clear();
        }


    }

    public void numberOfMessages(String destination) {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destination).orElseThrow(IllegalArgumentException::new);
        System.out.println(destinationSpec.numberOfMessages());
    }

    public void numberOfErrors(String destination) {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destination).orElseThrow(IllegalArgumentException::new);
        System.out.println(destinationSpec.errorCount());
    }

    public void retries(String destination) {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destination).orElseThrow(IllegalArgumentException::new);
        System.out.println(destinationSpec.numberOfRetries());
    }

    public void retryDelay(String destination) {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destination).orElseThrow(IllegalArgumentException::new);
        System.out.println(destinationSpec.retryDelay().getSeconds());
    }

    public void updateRetrying(String destination, int retries, long delay) {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destination).orElseThrow(IllegalArgumentException::new);

        transactionService.builder().principal(() -> "Console").run(() -> {
            destinationSpec.updateRetryBehavior(retries, Duration.ofSeconds(delay));
        });
    }

    public void purgeErrors(String destination) {
        DestinationSpec destinationSpec = messageService.getDestinationSpec(destination).orElseThrow(IllegalArgumentException::new);
        destinationSpec.purgeErrors();
    }

}
