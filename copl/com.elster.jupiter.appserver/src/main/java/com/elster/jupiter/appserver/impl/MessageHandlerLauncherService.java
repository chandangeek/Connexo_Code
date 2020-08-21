/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Registration;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

@Component(name = "com.elster.jupiter.appserver.messagehandlerlauncher", service = MessageHandlerLauncherService.class, immediate = true)
public class MessageHandlerLauncherService implements IAppService.CommandListener {
    private static final Logger LOGGER = Logger.getLogger(MessageHandlerLauncherService.class.getName());
    private final Object configureLock = new Object();
    @GuardedBy("configureLock")
    private final Map<SubscriberKey, MessageHandlerLauncherPojo> executors = new HashMap<>();
    @GuardedBy("configureLock")
    private final Map<CancellableTaskExecutorService, List<Future<?>>> futures = new HashMap<>();
    private final Queue<SubscriberKey> toBeLaunched = new LinkedList<>();
    private final Map<SubscriberKey, MessageHandlerFactory> handlerFactories = new ConcurrentHashMap<>();
    private volatile IAppService appService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile boolean active = false;
    private volatile boolean reconfigureNeeded = false;

    private ThreadGroup threadGroup;
    private Principal batchPrincipal;
    private Registration commandRegistration;
    private MessageService messageService;

    public MessageHandlerLauncherService() {
        // for OSGi
    }

    @Inject
    MessageHandlerLauncherService(IAppService appService, ThreadPrincipalService threadPrincipalService, UserService userService, TransactionService transactionService, MessageService messageService) {
        this.appService = appService;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.messageService = messageService;
    }

    public AppService getAppService() {
        return appService;
    }

    @Reference
    public void setAppService(IAppService appService) {
        this.appService = appService;
        commandRegistration = this.appService.addCommandListener(this);
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Activate
    public void activate() {
        threadGroup = new ThreadGroup(MessageHandlerLauncherService.class.getSimpleName());
        synchronized (configureLock) {
            toBeLaunched.forEach(launch());
        }
        while (reconfigureNeeded) {
            reconfigureNeeded = false;
            reconfigure();
        }
        active = true;
    }

    private Consumer<SubscriberKey> launch() {
        return key -> addMessageHandlerFactory(key, handlerFactories.get(key));
    }

    void appServerStarted() {
        synchronized (configureLock) {
            if (!executors.isEmpty()) {
                throw new IllegalStateException();
            }
            Thread.currentThread().setName("handlers size : " + handlerFactories.keySet().size());
            handlerFactories.keySet().forEach(launch());
        }
    }

    void appServerStopped() {
        synchronized (configureLock) {
            stopLaunched();
        }
    }

    private Thesaurus getThesaurus() {
        return appService.getThesaurus();
    }

    @Deactivate
    public void deactivate() {
        commandRegistration.unregister();
        synchronized (configureLock) {
            stopLaunched();
        }
        final ThreadGroup toClean = threadGroup;
        threadGroup = null;
        Thread groupCleaner = new Thread(() -> destroyThreadGroup(toClean));
        groupCleaner.setDaemon(true);
        groupCleaner.start();
        active = false;
    }

    private void stopLaunched() {
        int nbOfExecutors = executors.size();
        LOGGER.info("#message services: " + nbOfExecutors + " #message service threads: " + futures.size());
        AtomicInteger i = new AtomicInteger(0);
        executors.values().forEach(messageHandlerLauncherPojo -> {
            LOGGER.info("stopping message service " + i.incrementAndGet() + "/" + nbOfExecutors);
            shutDownServiceWithCancelling(messageHandlerLauncherPojo);
        });
        executors.clear();
        futures.clear();
        LOGGER.info("message services stopped");
    }

    private void destroyThreadGroup(ThreadGroup toClean) {
        try {
            Thread[] threads = new Thread[toClean.activeCount()];
            for (int count = toClean.enumerate(threads); count > 0; count = toClean.enumerate(threads)) {
                for (int i = 0; i < count; i++) {
                    threads[i].join();
                }
            }
            toClean.destroy();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(MessageHandlerFactory factory, Map<String, Object> map) {
        SubscriberKey subscriberKey = getSubscriberKey(map);

        addNewMessageHandlerFactory(subscriberKey, factory);
        addExtraMessageHandlerFactories(subscriberKey, factory);
    }

    private void addNewMessageHandlerFactory(SubscriberKey key, MessageHandlerFactory factory) {
        handlerFactories.put(key, factory);
        if (transactionService == null || threadPrincipalService == null) {
            toBeLaunched.add(key);
            return;
        }

        addMessageHandlerFactory(key, factory);
    }

    private void addExtraMessageHandlerFactories(SubscriberKey key, MessageHandlerFactory factory) {
        List<DestinationSpec> destinationSpecTypeNames = messageService.findDestinationSpecs().stream()
                .filter(d -> !d.isDefault())
                .filter(d -> d.getQueueTypeName().equals(key.getDestination()))
                .distinct().collect(Collectors.toList());

        if (!destinationSpecTypeNames.isEmpty()) {
            destinationSpecTypeNames.forEach(spec ->
                    addNewMessageHandlerFactory(SubscriberKey.of(spec.getName(), spec.getName()), factory));
        }
    }

    private SubscriberKey getSubscriberKey(Map<String, Object> map) {
        String destinationName = (String) map.get("destination");
        String subscriberName = (String) map.get("subscriber");
        return SubscriberKey.of(destinationName, subscriberName);
    }

    public void removeResource(MessageHandlerFactory factory, Map<String, Object> map) {
        SubscriberKey key = getSubscriberKey(map);
        handlerFactories.remove(key);
        synchronized (configureLock) {
            stopServing(key);
        }
    }

    private void stopServing(SubscriberKey key) {
        MessageHandlerLauncherPojo handlerPojo = executors.get(key);
        if (handlerPojo != null) {
            LOGGER.info("HandlerPojo found " + "   Subscriber: " + key.getSubscriber() + "Destination:   " + key.getDestination());
            CancellableTaskExecutorService executorService = handlerPojo.getCancellableTaskExecutorService();
            if (executorService != null) {
                shutDownServiceWithCancelling(handlerPojo);
                futures.remove(executorService);
            }
            executors.remove(key);
        } else {
            LOGGER.info("Avoid null pointer exception " + "   Subscriber: " + key.getSubscriber() + "Destination:   " + key.getDestination());
        }
    }

    Map<SubscriberKey, Integer> futureReport() {
        Map<SubscriberKey, MessageHandlerLauncherPojo> executorsSnapshot;
        Map<CancellableTaskExecutorService, List<Future<?>>> futuresSnapshot;
        synchronized (configureLock) {
            executorsSnapshot = ImmutableMap.copyOf(this.executors);
            futuresSnapshot = ImmutableMap.copyOf(this.futures);
        }
        Map<SubscriberKey, MessageHandlerLauncherPojo> executorsCopy = executorsSnapshot;
        Map<CancellableTaskExecutorService, List<Future<?>>> futuresCopy = futuresSnapshot;
        return handlerFactories.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(),
                        executorsCopy.containsKey(entry.getKey()) ? executorsCopy.get(entry.getKey()).getCancellableTaskExecutorService() : null))
                .filter(pair -> pair.getLast() != null)
                .map(pair -> Pair.of(pair.getFirst(),
                        futuresCopy.containsKey(pair.getLast()) ? futuresCopy.get(pair.getLast()).size() : 0))
                .filter(pair -> pair.getLast() != null)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
    }

    Map<SubscriberKey, Integer> threadReport() {
        Map<SubscriberKey, MessageHandlerLauncherPojo> executorsSnapshot;
        synchronized (configureLock) {
            executorsSnapshot = ImmutableMap.copyOf(this.executors);
        }
        Map<SubscriberKey, MessageHandlerLauncherPojo> executorsCopy = executorsSnapshot;
        return handlerFactories.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(),
                        executorsCopy.containsKey(entry.getKey()) ? executorsCopy.get(entry.getKey()).getCancellableTaskExecutorService() : null))
                .filter(pair -> pair.getLast() != null)
                .map(pair -> Pair.of(pair.getFirst(), pair.getLast() == null ? 0 : pair.getLast().getCorePoolSize()))
                .filter(pair -> pair.getLast() != 0)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
    }

    private void addMessageHandlerFactory(SubscriberKey key, MessageHandlerFactory factory) {
        if (appService.getAppServer().map(AppServer::isActive).orElse(false)) {
            Optional<SubscriberExecutionSpec> subscriberExecutionSpec = findSubscriberExecutionSpec(key);
            subscriberExecutionSpec
                    .filter(SubscriberExecutionSpec::isActive)
                    .ifPresent(executionSpec -> {
                        synchronized (configureLock) {
                            launch(key, factory, executionSpec.getThreadCount(), executionSpec.getSubscriberSpec());
                        }
                    });
        }
    }

    private void launch(SubscriberKey key, MessageHandlerFactory factory, int threadCount, SubscriberSpec subscriberSpec) {
        CancellableTaskExecutorService executorService = newExecutorService(SubscriberKey.of(subscriberSpec), threadCount);
        executors.put(key, new MessageHandlerLauncherPojo(factory, executorService));
        List<Future<?>> submittedFutures = new ArrayList<>(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                Future<?> future = submitTask(executorService, subscriberSpec, factory);
                submittedFutures.add(future);
            }
            futures.put(executorService, new CopyOnWriteArrayList<>(submittedFutures));
        } catch (RuntimeException e) {
            // we can't allow corrupt MessageHandlerFactories to disrupt our configuration
            if (submittedFutures.isEmpty()) {
                executors.remove(key);
                executorService.shutdownNow();
            }

            LOGGER.log(Level.SEVERE, e, () -> "MessageHandlerFactory for subscriber " + subscriberSpec.getDestination()
                    .getName() + " : " + subscriberSpec.getName() + " threw an exception while creating a new Messagehandler.");
        }
    }

    private Future<?> submitTask(CancellableTaskExecutorService executorService, SubscriberSpec subscriberSpec, MessageHandlerFactory factory) {
        MessageHandlerTask task = newMessageHandlerTask(factory, subscriberSpec);
        return executorService.submit(withBatchPrincipal(task));
    }

    private ProvidesCancellableFuture withBatchPrincipal(MessageHandlerTask task) {
        return new RunMessageHandlerTaskAs(task, threadPrincipalService, getBatchPrincipal());
    }

    private Principal getBatchPrincipal() {
        if (batchPrincipal == null) {
            String batchExecutorName = "batch executor";
            batchPrincipal = userService.findUser(batchExecutorName).orElse(null);
        }
        return batchPrincipal;
    }

    private CancellableTaskExecutorService newExecutorService(SubscriberKey key, int threadCount) {
        return new CancellableTaskExecutorService(threadCount, getThreadFactory(key));
    }

    private MessageHandlerTask newMessageHandlerTask(MessageHandlerFactory factory, SubscriberSpec subscriberSpec) {
        return new MessageHandlerTask(subscriberSpec, factory.newMessageHandler(), transactionService, getThesaurus());
    }

    private Optional<SubscriberExecutionSpec> findSubscriberExecutionSpec(SubscriberKey key) {
        return getAppService().getSubscriberExecutionSpecs().stream()
                .filter(key::matches)
                .findFirst();
    }

    private void shutDownServiceWithCancelling(MessageHandlerLauncherPojo pojo) {
        CancellableTaskExecutorService executorService = pojo.getCancellableTaskExecutorService();
        cancelFutureTask(executorService);
        executorService.shutdownNow();
        //awaitTermination was removed because we waited 1 minute while each message task completes execution after a shutdown request.
        //This resulted in a very long service shutdown.
        //Interrupted message tasks will be re-run after next startup.
    }

    private void cancelFutureTask(CancellableTaskExecutorService executorService) {
        if (futures.isEmpty()) {
            LOGGER.info("futures is empty for executorService " + executorService);
            return;
        }
        for (Future<?> future : futures.get(executorService)) {
            future.cancel(false);
        }
    }

    private ThreadFactory getThreadFactory(SubscriberKey key) {
        return new AppServerThreadFactory(threadGroup, new LoggingUncaughtExceptionHandler(getThesaurus()), appService, key::toString);
    }

    @Override
    public void notify(AppServerCommand command) {
        switch (command.getCommand()) {
            case CONFIG_CHANGED:
                if (active) {
                    reconfigure();
                } else {
                    reconfigureNeeded = true;
                }
                return;
            case NEW_QUEUE_ADDED:
                SubscriberExecutionSpec subscriberExecutionSpec = (SubscriberExecutionSpec) command.getProperties().get(AppServiceImpl.SUBSCRIBER_EXECUTION_SPEC);
                if (!subscriberExecutionSpec.getSubscriberSpec().getDestination().isDefault()) {
                    String defaultDestination = subscriberExecutionSpec.getSubscriberSpec().getDestination().getQueueTypeName();
                    SubscriberKey subscriberKey = SubscriberKey.of(subscriberExecutionSpec);
                    handlerFactories.entrySet().stream()
                            .filter(entry -> entry.getKey().getDestination().equals(defaultDestination))
                            .findAny()
                            .map(Map.Entry::getValue)
                            .ifPresent(messageHandlerFactory -> addNewMessageHandlerFactory(subscriberKey, messageHandlerFactory));
                }
                return;
            default:
        }
    }

    private void reconfigure() {
        synchronized (configureLock) {
            appService.getAppServer().map(appServer -> (Runnable) () -> {
                if (!appServer.isActive()) {
                    stopLaunched();
                    return;
                }
                doReconfigure(appServer.getSubscriberExecutionSpecs());
            }).orElse(this::appServerStopped).run();
        }
    }

    private void doReconfigure(List<? extends SubscriberExecutionSpec> subscriberExecutionSpec) {
        subscriberExecutionSpec
                .stream()
                .filter(SubscriberExecutionSpec::isActive)
                .forEach(this::doReconfigure);
        subscriberExecutionSpec
                .stream()
                .filter(not(SubscriberExecutionSpec::isActive))
                .map(SubscriberKey::of)
                .filter(key -> handlerFactories.get(key) != null)
                .forEach(this::stopServing);
        Set<SubscriberKey> toRemove = executors.keySet().stream()
                .filter(key -> subscriberExecutionSpec.stream()
                        .map(SubscriberKey::of)
                        .filter(subKey -> handlerFactories.get(subKey) != null)
                        .noneMatch(subKey -> subKey.equals(key)))
                .collect(Collectors.toSet());
        toRemove.forEach(this::stopServing);
    }

    private void doReconfigure(SubscriberExecutionSpec subscriberExecutionSpec) {
        SubscriberKey key = SubscriberKey.of(subscriberExecutionSpec);
        MessageHandlerFactory messageHandlerFactory = handlerFactories.get(key);
        CancellableTaskExecutorService executorService = null;

        if (messageHandlerFactory == null) {
            return;
        }

        if (executors.get(key) != null) {
            executorService = executors.get(key).getCancellableTaskExecutorService();
        }

        if (executorService == null) {
            launch(key, messageHandlerFactory, subscriberExecutionSpec.getThreadCount(), subscriberExecutionSpec.getSubscriberSpec());
            return;
        }

        addFutureTask(subscriberExecutionSpec, executorService, messageHandlerFactory);
    }

    private void addFutureTask(SubscriberExecutionSpec subscriberExecutionSpec, CancellableTaskExecutorService executorService, MessageHandlerFactory messageHandlerFactory) {
        int target = subscriberExecutionSpec.getThreadCount();
        List<Future<?>> currentTasks = futures.get(executorService);
        int current = currentTasks.size();
        int change = target - current;

        if (change > 0) {
            executorService.setMaximumPoolSize(target);
            executorService.setCorePoolSize(target);
            for (int i = 0; i < change; i++) {
                Future<?> future = submitTask(executorService, subscriberExecutionSpec.getSubscriberSpec(), messageHandlerFactory);
                currentTasks.add(future);
            }
        }

        if (change < 0) {
            for (int i = 0; i < -change; i++) {
                Future<?> removed = currentTasks.remove(0);
                removed.cancel(true);
            }
            executorService.setCorePoolSize(target);
            executorService.setMaximumPoolSize(target);
        }
    }

    private class MessageHandlerLauncherPojo {

        private MessageHandlerFactory messageHandlerFactory;
        private CancellableTaskExecutorService cancellableTaskExecutorService;

        MessageHandlerLauncherPojo(MessageHandlerFactory messageHandlerFactory, CancellableTaskExecutorService cancellableTaskExecutorService) {
            this.messageHandlerFactory = messageHandlerFactory;
            this.cancellableTaskExecutorService = cancellableTaskExecutorService;
        }

        MessageHandlerFactory getMessageHandlerFactory() {
            return messageHandlerFactory;
        }

        CancellableTaskExecutorService getCancellableTaskExecutorService() {
            return cancellableTaskExecutorService;
        }

    }

}


