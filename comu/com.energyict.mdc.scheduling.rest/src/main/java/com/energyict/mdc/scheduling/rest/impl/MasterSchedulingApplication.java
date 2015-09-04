package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.sch.rest", service = {Application.class, MessageSeedProvider.class}, immediate = true, property = {"alias=/scr", "app=MDC", "name=" + MasterSchedulingApplication.COMPONENT_NAME})
public class MasterSchedulingApplication extends Application implements MessageSeedProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "SCR";

    private volatile TransactionService transactionService;
    private volatile MeteringService meteringService;
    private volatile NlsService nlsService;
    private volatile JsonService jsonService;
    private volatile Thesaurus thesaurus;
    private volatile SchedulingService schedulingService;
    private volatile DeviceService deviceService;
    private volatile TaskService taskService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile Clock clock;
    private volatile License license;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                TransactionWrapper.class,
                ExceptionLogger.class,
                SchedulingResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(transactionService).to(TransactionService.class);
            bind(schedulingService).to(SchedulingService.class);
            bind(meteringService).to(MeteringService.class);
            bind(deviceService).to(DeviceService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(nlsService).to(NlsService.class);
            bind(taskService).to(TaskService.class);
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(jsonService).to(JsonService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(clock).to(Clock.class);
        }
    }

}