package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.appserver.messagehandlerlauncher", service = MessageHandlerLauncherService.class, immediate = true)
public class MessageHandlerLauncherService implements IAppService.CommandListener {

    private volatile IAppService appService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile boolean active = false;
    private volatile boolean reconfigureNeeded = false;

    private ThreadGroup threadGroup;

    private final Map<MessageHandlerFactory, CancellableTaskExecutorService> executors = new ConcurrentHashMap<>();
    private final Map<CancellableTaskExecutorService, List<Future<?>>> futures = new ConcurrentHashMap<>();
    private final Queue<SubscriberKey> toBeLaunched = new LinkedList<>();
    private final Map<SubscriberKey, MessageHandlerFactory> handlerFactories = new ConcurrentHashMap<>();

    private Principal batchPrincipal;
    private Registration commandRegistration;

    public MessageHandlerLauncherService() {
    }

    public AppService getAppService() {
        return appService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = (IAppService) appService;
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

    @Activate
    public void activate() {
        threadGroup = new ThreadGroup(MessageHandlerLauncherService.class.getSimpleName());
        toBeLaunched.forEach(launch());
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
        if (!executors.isEmpty()) {
            throw new IllegalStateException();
        }
        handlerFactories.keySet().forEach(launch());
    }

    void appServerStopped() {
        stopLaunched();
    }

    private Thesaurus getThesaurus() {
        return ((IAppService) appService).getThesaurus();
    }

    @Deactivate
    public void deactivate() {
        commandRegistration.unregister();
        stopLaunched();
        final ThreadGroup toClean = threadGroup;
        threadGroup = null;
        Thread groupCleaner = new Thread(() -> destroyThreadGroup(toClean));
        groupCleaner.setDaemon(true);
        groupCleaner.start();
        active = false;
    }

    private void stopLaunched() {
        executors.values().forEach(this::shutDownServiceWithCancelling);
        executors.clear();
        futures.clear();
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
        handlerFactories.put(subscriberKey, factory);
        if (transactionService == null || threadPrincipalService == null) {
            toBeLaunched.add(subscriberKey);
            return;
        }
        addMessageHandlerFactory(subscriberKey, factory);
    }

    private SubscriberKey getSubscriberKey(Map<String, Object> map) {
        String destinationName = (String) map.get("destination");
        String subscriberName = (String) map.get("subscriber");
        return SubscriberKey.of(destinationName, subscriberName);
    }

    public void removeResource(MessageHandlerFactory factory) {
        handlerFactories.entrySet().removeIf(entry -> entry.getValue().equals(factory));
        stopServing(factory);
    }

    private void stopServing(MessageHandlerFactory factory) {
        CancellableTaskExecutorService executorService = executors.get(factory);
        if (executorService != null) {
            shutDownServiceWithCancelling(executorService);
        }
        executors.remove(factory);
        futures.remove(executorService);
    }

    Map<SubscriberKey, Integer> futureReport() {
        return handlerFactories.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue() == null ? null : executors.get(entry.getValue())))
                .filter(pair -> pair.getLast() != null)
                .map(pair -> Pair.of(pair.getFirst(), pair.getLast() == null ? 0 : futures.get(pair.getLast()).size()))
                .filter(pair -> pair.getLast() != null)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
    }

    Map<SubscriberKey, Integer> threadReport() {
        return handlerFactories.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), executors.get(entry.getValue())))
                .filter(pair -> pair.getLast() != null)
                .map(pair -> Pair.of(pair.getFirst(), pair.getLast() == null ? 0 : ((CancellableTaskExecutorService) pair.getLast()).getCorePoolSize()))
                .filter(pair -> pair.getLast() != 0)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
    }

    private void addMessageHandlerFactory(SubscriberKey key, MessageHandlerFactory factory) {
        if (appService.getAppServer().map(AppServer::isActive).orElse(false)) {
            Optional<SubscriberExecutionSpec> subscriberExecutionSpec = findSubscriberExecutionSpec(key);
            subscriberExecutionSpec.ifPresent(executionSpec -> launch(factory, executionSpec.getThreadCount(), executionSpec.getSubscriberSpec()));
        }
    }

    private void launch(MessageHandlerFactory factory, int threadCount, SubscriberSpec subscriberSpec) {
        CancellableTaskExecutorService executorService = newExecutorService(SubscriberKey.of(subscriberSpec), threadCount);
        executors.put(factory, executorService);
        List<Future<?>> submittedFutures = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Future<?> future = submitTask(executorService, subscriberSpec, factory);
            submittedFutures.add(future);
        }
        futures.put(executorService, new CopyOnWriteArrayList<>(submittedFutures));
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
            batchPrincipal = userService.findUser(batchExecutorName).get();
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

    private void shutDownServiceWithCancelling(CancellableTaskExecutorService executorService) {
        for (Future<?> future : futures.get(executorService)) {
            future.cancel(false);
        }
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ThreadFactory getThreadFactory(SubscriberKey key) {
        return new AppServerThreadFactory(threadGroup, new LoggingUncaughtExceptionHandler(getThesaurus()), appService, () -> key.toString());
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
                default:
            }
    }

    private void reconfigure() {
        appService.getAppServer().map(appServer -> (Runnable) () -> {
            if (!appServer.isActive()) {
                stopLaunched();
                return;
            }
            doReconfigure(appServer.getSubscriberExecutionSpecs());
        }).orElse(this::appServerStopped).run();
    }

    private void doReconfigure(List<? extends SubscriberExecutionSpec> subscriberExecutionSpec) {
        subscriberExecutionSpec.forEach(this::doReconfigure);
        Set<MessageHandlerFactory> toRemove = executors.keySet().stream()
                .filter(factory -> subscriberExecutionSpec.stream()
                        .map(SubscriberKey::of)
                        .map(handlerFactories::get)
                        .filter(Objects::nonNull)
                        .noneMatch(f -> f.equals(factory)))
                .collect(Collectors.toSet());
        toRemove.forEach(this::stopServing);
    }

    private void doReconfigure(SubscriberExecutionSpec subscriberExecutionSpec) {
        SubscriberKey key = SubscriberKey.of(subscriberExecutionSpec);
        MessageHandlerFactory messageHandlerFactory = handlerFactories.get(key);
        if (messageHandlerFactory != null) {
            CancellableTaskExecutorService executorService = executors.get(messageHandlerFactory);
            if (executorService == null) {
                launch(messageHandlerFactory, subscriberExecutionSpec.getThreadCount(), subscriberExecutionSpec.getSubscriberSpec());
                return;
            }
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
            } else if (change < 0) {
                for (int i = 0; i < -change; i++) {
                    Future<?> removed = currentTasks.remove(0);
                    removed.cancel(true);
                }
                executorService.setCorePoolSize(target);
                executorService.setMaximumPoolSize(target);
            }
        }
    }

}
