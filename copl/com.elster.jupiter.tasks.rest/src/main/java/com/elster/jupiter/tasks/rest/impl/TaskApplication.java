package com.elster.jupiter.tasks.rest.impl;

        import com.elster.jupiter.nls.Layer;
        import com.elster.jupiter.nls.NlsService;
        import com.elster.jupiter.nls.Thesaurus;
        import com.elster.jupiter.rest.util.RestQueryService;
        import com.elster.jupiter.tasks.TaskService;
        import com.elster.jupiter.transaction.TransactionService;
        import com.google.common.collect.ImmutableSet;
        import org.glassfish.hk2.utilities.binding.AbstractBinder;
        import org.osgi.service.component.annotations.Component;
        import org.osgi.service.component.annotations.Reference;

        import javax.ws.rs.core.Application;
        import java.util.HashSet;
        import java.util.Set;

@Component(name = "com.elster.jupiter.tasks.rest", service = Application.class, immediate = true, property = {"alias=/tsk", "app=SYS", "name=" + TaskApplication.COMPONENT_NAME})
public class TaskApplication extends Application {
    public static final String COMPONENT_NAME = "TSK";

    private volatile TaskService taskService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile Thesaurus thesaurus;

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(TaskResource.class);
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();
        singletons.addAll(super.getSingletons());
        singletons.add(new HK2Binder());
        return singletons;
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(restQueryService).to(RestQueryService.class);
            bind(transactionService).to(TransactionService.class);
            bind(taskService).to(TaskService.class);
            bind(thesaurus).to(Thesaurus.class);
        }
    }
}