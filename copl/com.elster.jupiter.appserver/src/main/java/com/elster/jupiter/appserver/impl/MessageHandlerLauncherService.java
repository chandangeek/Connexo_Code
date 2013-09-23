package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.google.common.base.Optional;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component(name = "com.elster.jupiter.appserver.messagehandlerlauncher", immediate = true)
public class MessageHandlerLauncherService {

    private volatile AppService appService;

    private Map<MessageHandlerFactory, ExecutorService> executors = new ConcurrentHashMap<>();
    private Map<ExecutorService, List<Future<?>>> futures = new ConcurrentHashMap<>();

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

    public void activate(ComponentContext context) {
    }

    public void deactivate(ComponentContext context) {
        for (ExecutorService executorService : executors.values()) {
            shutDownServiceWithCancelling(executorService);
        }
        Bus.setServiceLocator(null);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResource(MessageHandlerFactory factory, Map<String, Object> map) {
        String subscriberName = (String) map.get("subscriber");
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
        return new RunMessageHandlerTaskAs(task, Bus.getThreadPrincipalService(), getBatchPrincipal());
    }

    private Principal getBatchPrincipal() {
        if (batchPrincipal == null) {
            String batchExecutorName = "batch executor";
            batchPrincipal = Bus.getUserService().findUser(batchExecutorName).get();
        }
        return batchPrincipal;
    }

    private ExecutorService newExecutorService(int threadCount) {
        return new CancellableTaskExecutorService(threadCount);
    }

    private MessageHandlerTask newMessageHandlerTask(MessageHandlerFactory factory, SubscriberSpec subscriberSpec) {
        return new MessageHandlerTask(subscriberSpec, factory.newMessageHandler());
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


}
