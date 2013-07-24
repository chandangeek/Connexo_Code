package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.consumer.MessageHandlerFactory;
import com.google.common.base.Optional;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component(name = "com.elster.jupiter.appserver.messagehandlerlauncher", immediate=true )
public class MessageHandlerLauncherService {

    private volatile AppService appService;
    private volatile LogService logService;

    private Map<MessageHandlerFactory, ExecutorService> executors = new HashMap<>();

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
        Bus.setServiceLocator(null);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE , policy = ReferencePolicy.DYNAMIC)
    public void addResource(MessageHandlerFactory factory, Map<String, Object> map) {
        try {
            String subscriberName = (String) map.get("subscriber");
            addMessageHandlerFactory(subscriberName, factory);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void addMessageHandlerFactory(String subscriberName, MessageHandlerFactory factory) {
        Optional<SubscriberExecutionSpec> subscriberExecutionSpec = findSubscriberExecutionSpec(subscriberName);
        if (subscriberExecutionSpec.isPresent()) {
            SubscriberExecutionSpec executionSpec = subscriberExecutionSpec.get();
            launch(factory, executionSpec.getThreadCount(), executionSpec.getSubscriberSpec());
        }
    }

    private void launch(MessageHandlerFactory factory, int threadCount, SubscriberSpec subscriberSpec) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        executors.put(factory, executorService);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(newMessageHandlerTask(factory, subscriberSpec));
        }
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

    public void removeResource(MessageHandlerFactory factory) {
        ExecutorService executorService = executors.remove(factory);
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    LogService getLogService() {
        return logService;
    }

    @Reference
    public void setLogService(LogService logService) {
        this.logService = logService;
    }


}
