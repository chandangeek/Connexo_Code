package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.google.common.base.Optional;
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
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Component(name = "com.elster.jupiter.appserver.messagehandlerlauncher", immediate = true)
public class MessageHandlerLauncherService {

    private volatile AppService appService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile TransactionService transactionService;

    private final ThreadGroup threadGroup = new ThreadGroup(MessageHandlerLauncherService.class.getSimpleName());
    private ThreadFactory threadFactory;

    private final Map<MessageHandlerFactory, ExecutorService> executors = new ConcurrentHashMap<>();
    private final Map<ExecutorService, List<Future<?>>> futures = new ConcurrentHashMap<>();
    private final Queue<Pair<String, MessageHandlerFactory>> toBeLaunched = new LinkedList<>();

    private Principal batchPrincipal;

    public MessageHandlerLauncherService() {
    }

    public AppService getAppService() {
        return appService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
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
        for (Pair<String, MessageHandlerFactory> pair : toBeLaunched) {
            addMessageHandlerFactory(pair.getFirst(), pair.getLast());
        }
    }

    private Thesaurus getThesaurus() {
        return ((AppServiceImpl) appService).getThesaurus();
    }

    @Deactivate
    public void deactivate() {
        for (ExecutorService executorService : executors.values()) {
            shutDownServiceWithCancelling(executorService);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(MessageHandlerFactory factory, Map<String, Object> map) {
        String subscriberName = (String) map.get("subscriber");
        if (transactionService == null || threadPrincipalService == null) {
            toBeLaunched.add(Pair.of(subscriberName, factory));
            return;
        }
        addMessageHandlerFactory(subscriberName, factory);
    }
    
    public void removeResource(MessageHandlerFactory factory) {
        ExecutorService executorService = executors.get(factory);
        shutDownServiceWithCancelling(executorService);
    }

    private void addMessageHandlerFactory(String subscriberName, MessageHandlerFactory factory) {
        Optional<SubscriberExecutionSpec> subscriberExecutionSpec = findSubscriberExecutionSpec(subscriberName);
        if (subscriberExecutionSpec.isPresent()) {
            SubscriberExecutionSpec executionSpec = subscriberExecutionSpec.get();
            launch(factory, executionSpec.getThreadCount(), executionSpec.getSubscriberSpec());
        }
    }

    private void launch(MessageHandlerFactory factory, int threadCount, SubscriberSpec subscriberSpec) {
        ExecutorService executorService = newExecutorService(threadCount);
        executors.put(factory, executorService);
        List<Future<?>> submittedFutures = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            MessageHandlerTask task = newMessageHandlerTask(factory, subscriberSpec);
            Future<?> future = executorService.submit(withBatchPrincipal(task));
            submittedFutures.add(future);
        }
        futures.put(executorService, submittedFutures);
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

    private ExecutorService newExecutorService(int threadCount) {
        return new CancellableTaskExecutorService(threadCount, getThreadFactory());
    }

    private MessageHandlerTask newMessageHandlerTask(MessageHandlerFactory factory, SubscriberSpec subscriberSpec) {
        return new MessageHandlerTask(subscriberSpec, factory.newMessageHandler(), transactionService, getThesaurus());
    }

    private Optional<SubscriberExecutionSpec> findSubscriberExecutionSpec(String subscriberName) {
        for (SubscriberExecutionSpec candidate : getAppService().getSubscriberExecutionSpecs()) {
            if (candidate.getSubscriberSpec().getName().equals(subscriberName)) {
                return Optional.of(candidate);
            }
        }
        return Optional.absent();
    }

    private void shutDownServiceWithCancelling(ExecutorService executorService) {
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


    private ThreadFactory getThreadFactory() {
        if (threadFactory == null) {
            threadFactory = new AppServerThreadFactory(threadGroup, new LoggingUncaughtExceptionHandler(getThesaurus()), appService);
        }
        return threadFactory;
    }

}
