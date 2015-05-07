package com.energyict.mdc.tasks.rest;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.rest.impl.ComTaskResource;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.energyict.mdc.tasks.rest", service = Application.class, immediate = true, property = {"alias=/cts", "app=MDC", "name=" + ComTasksApplication.COMPONENT_NAME})
public class ComTasksApplication extends Application implements BinderProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "CTS";

    private volatile TransactionService transactionService;
    private volatile TaskService taskService;
    private volatile MasterDataService masterDataService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile License license;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                ComTaskResource.class,
                TransactionWrapper.class,
                ConstraintViolationExceptionMapper.class,
                JsonMappingExceptionMapper.class
        );
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(transactionService).to(TransactionService.class);
                bind(taskService).to(TaskService.class);
                bind(masterDataService).to(MasterDataService.class);
                bind(nlsService).to(NlsService.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(deviceMessageSpecificationService).to(DeviceMessageSpecificationService.class);
            }
        };
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus("CTS", Layer.REST);
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference(target="(com.elster.jupiter.license.rest.key=" + APP_KEY  + ")")
    public void setLicense(License license) {
        this.license = license;
    }
}