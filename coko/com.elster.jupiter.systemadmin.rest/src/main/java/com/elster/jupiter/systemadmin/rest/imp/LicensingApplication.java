package com.elster.jupiter.systemadmin.rest.imp;

import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.systemadmin.rest.imp.resource.DataPurgeResource;
import com.elster.jupiter.systemadmin.rest.imp.resource.LicenseResource;
import com.elster.jupiter.systemadmin.rest.imp.resource.MessageSeeds;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.validation.MessageInterpolator;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.systemadmin.rest",
        service = {Application.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/lic", "app=SYS", "name=" + LicensingApplication.COMPONENT_NAME})
public class LicensingApplication extends Application implements TranslationKeyProvider {
    public static final String COMPONENT_NAME = "LIC";

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile LicenseService licenseService;
    private volatile NlsService nlsService;
    private volatile LifeCycleService lifeCycleService;
    private volatile TaskService taskService;
    private volatile Thesaurus thesaurus;
    private volatile JsonService jsonService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(LicenseResource.class,
                DataPurgeResource.class,
                MultiPartFeature.class);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
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
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, getLayer());
    }

    @Reference
    public void setLifeCycleService(LifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(licenseService).to(LicenseService.class);
            bind(userService).to(UserService.class);
            bind(transactionService).to(TransactionService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(nlsService).to(NlsService.class);
            bind(lifeCycleService).to(LifeCycleService.class);
            bind(taskService).to(TaskService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(thesaurus).to(MessageInterpolator.class);
            bind(jsonService).to(JsonService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
        }
    }
}
