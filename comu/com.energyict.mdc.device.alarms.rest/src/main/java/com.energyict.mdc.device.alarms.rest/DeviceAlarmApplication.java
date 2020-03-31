/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.rest.response.IssueActionInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleActionInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleExclGroupInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleInfoFactory;
import com.elster.jupiter.issue.rest.response.cep.CreationRuleTemplateInfoFactory;
import com.elster.jupiter.issue.share.IssueResourceUtility;
import com.elster.jupiter.issue.share.entity.CreationRuleExclGroup;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.rest.i18n.DeviceAlarmTranslationKeys;
import com.energyict.mdc.device.alarms.rest.i18n.MessageSeeds;
import com.energyict.mdc.device.alarms.rest.resource.*;
import com.energyict.mdc.device.alarms.rest.response.DeviceAlarmInfoFactory;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component(name = "com.energyict.mdc.device.com.energyict.mdc.device.alarms.rest", service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/dal", "app=MDC", "name=" + DeviceAlarmApplication.DEVICE_ALARMS_REST_COMPONENT})
public class DeviceAlarmApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {

    public static final String APP_KEY = "MDC";
    public static final String DEVICE_ALARMS_REST_COMPONENT = "DAR";

    private volatile MeteringGroupsService meteringGroupsService;
    private volatile TransactionService transactionService;
    private volatile DeviceAlarmService deviceAlarmService;
    private volatile LogBookService logBookService;
    private volatile MeteringService meteringService;
    private volatile UserService userService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile IssueCreationService issueCreationService;
    private volatile IssueAssignmentService issueAssignmentService;
    private volatile BpmService bpmService;
    private volatile TimeService timeService;
    private volatile Clock clock;
    private volatile IssueResourceUtility issueResourceUtility;


    public DeviceAlarmApplication() {

    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                DeviceAlarmResource.class,
                StatusResource.class,
                WorkGroupsResource.class,
                ReasonResource.class,
                MeterResource.class,
                DeviceGroupResource.class,
                DeviceAlarmPriorityResource.class,
                TopAlarmsResource.class,
                UserResource.class,
                DeviceAlarmCreationRuleResource.class,
                AlarmRuleResource.class,
                HistoryResource.class,
                ActionResource.class);
    }

    @Override
    public String getComponentName() {
        return DEVICE_ALARMS_REST_COMPONENT;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(DeviceAlarmTranslationKeys.values()),
                Arrays.stream(DefaultRelativePeriodDefinition.RelativePeriodTranslationKey.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
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

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
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
    public void setLogBookService(LogBookService logBookService) {
        this.logBookService = logBookService;
    }

    @Reference
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
        this.issueCreationService = issueService.getIssueCreationService();
        this.issueAssignmentService = issueService.getIssueAssignmentService();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(DEVICE_ALARMS_REST_COMPONENT, Layer.REST);
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setIssueResourceUtility(IssueResourceUtility issueResourceUtility){
        this.issueResourceUtility = issueResourceUtility;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(transactionService).to(TransactionService.class);
            bind(deviceAlarmService).to(DeviceAlarmService.class);
            bind(DeviceAlarmInfoFactory.class).to(DeviceAlarmInfoFactory.class);
            bind(logBookService).to(LogBookService.class);
            bind(issueService).to(IssueService.class);
            bind(issueActionService).to(IssueActionService.class);
            bind(issueAssignmentService).to(IssueAssignmentService.class);
            bind(issueCreationService).to(IssueCreationService.class);
            bind(meteringService).to(MeteringService.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
            bind(userService).to(UserService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(nlsService).to(NlsService.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(bpmService).to(BpmService.class);
            bind(timeService).to(TimeService.class);
            bind(clock).to(Clock.class);
            bind(issueResourceUtility).to(IssueResourceUtility.class);
            bind(CreationRuleInfoFactory.class).to(CreationRuleInfoFactory.class);
            bind(CreationRuleTemplateInfoFactory.class).to(CreationRuleTemplateInfoFactory.class);
            bind(CreationRuleActionInfoFactory.class).to(CreationRuleActionInfoFactory.class);
            bind(CreationRuleExclGroupInfoFactory.class).to(CreationRuleExclGroupInfoFactory.class);
            bind(IssueActionInfoFactory.class).to(IssueActionInfoFactory.class);
        }
    }
}
