package com.energyict.mdc.tasks.rest;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.rest.impl.ComTaskResource;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.energyict.mdc.tasks.rest", service = Application.class, immediate = true, property = {"alias=/cts"})
public class ComTasksApplication extends Application implements BinderProvider {
    private volatile TransactionService transactionService;
    private volatile TaskService taskService;
    private volatile MasterDataService masterDataService;
    private volatile NlsService nlsService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(ComTaskResource.class,
                TransactionWrapper.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedExceptionMapper.class);
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(transactionService).to(TransactionService.class);
                bind(taskService).to(TaskService.class);
                bind(masterDataService).to(MasterDataService.class);
                bind(nlsService).to(NlsService.class);
            }
        };
    }

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {
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
    }
}