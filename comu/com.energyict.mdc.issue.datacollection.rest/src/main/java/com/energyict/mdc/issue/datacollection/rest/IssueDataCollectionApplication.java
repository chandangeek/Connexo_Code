/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.rest.resource.IssueResourceHelper;
import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.rest.i18n.DataCollectionIssueTranslationKeys;
import com.energyict.mdc.issue.datacollection.rest.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.rest.resource.IssueResource;
import com.energyict.mdc.issue.datacollection.rest.response.DataCollectionIssueInfoFactory;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.energyict.mdc.issue.datacollection.rest", service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/idc", "app=MDC", "name=" + IssueDataCollectionApplication.ISSUE_DATACOLLECTION_REST_COMPONENT})
public class IssueDataCollectionApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {
    public static final String APP_KEY = "MDC";
    public static final String ISSUE_DATACOLLECTION_REST_COMPONENT = "IDR";

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile IssueService issueService;
    private volatile IssueDataCollectionService issueDataCollectionService;
    private volatile IssueActionService issueActionService;
    private volatile MeteringService meteringService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceService deviceService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile MessageService messageService;
    private volatile AppService appService;
    private volatile JsonService jsonService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile BpmService bpmService;
    private volatile PropertyValueInfoService propertyValueInfoService;

    public IssueDataCollectionApplication() {
    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                IssueResource.class);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
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
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
    }

    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus domainThesaurus = nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN);
        Thesaurus restThesaurus = nlsService.getThesaurus(ISSUE_DATACOLLECTION_REST_COMPONENT, Layer.REST);
        this.thesaurus = domainThesaurus.join(restThesaurus);
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(DataCollectionIssueTranslationKeys.values());
    }

    @Override
    public String getComponentName() {
        return ISSUE_DATACOLLECTION_REST_COMPONENT;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(issueService).to(IssueService.class);
            bind(issueActionService).to(IssueActionService.class);
            bind(issueDataCollectionService).to(IssueDataCollectionService.class);
            bind(userService).to(UserService.class);
            bind(bpmService).to(BpmService.class);
            bind(transactionService).to(TransactionService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(meteringService).to(MeteringService.class);
            bind(nlsService).to(NlsService.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(deviceService).to(DeviceService.class);
            bind(messageService).to(MessageService.class);
            bind(appService).to(AppService.class);
            bind(jsonService).to(JsonService.class);
            bind(DataCollectionIssueInfoFactory.class).to(DataCollectionIssueInfoFactory.class);
            bind(IssueResourceHelper.class).to(IssueResourceHelper.class);
            bind(IssueActionInfoFactory.class).to(IssueActionInfoFactory.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(communicationTaskService).to(CommunicationTaskService.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(deviceLifeCycleConfigurationService).to(DeviceLifeCycleConfigurationService.class);
        }
    }
}
