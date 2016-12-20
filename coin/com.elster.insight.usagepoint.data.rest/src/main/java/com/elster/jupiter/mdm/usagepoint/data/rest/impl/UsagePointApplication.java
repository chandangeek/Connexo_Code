package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.rest.AppServerHelper;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverableFactory;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.spi.RelativePeriodCategoryTranslationProvider;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.usagepoint.calendar.UsagePointCalendarService;
import com.elster.jupiter.usagepoint.lifecycle.rest.BusinessProcessInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfoFactory;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleTransitionInfoFactory;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.DataValidationTaskInfoFactory;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.elster.insight.udr.rest",
        service = {Application.class, TranslationKeyProvider.class, MessageSeedProvider.class, RelativePeriodCategoryTranslationProvider.class},
        immediate = true,
        property = {"alias=/udr", "app=INS", "name=" + UsagePointApplication.COMPONENT_NAME})
public class UsagePointApplication extends Application implements TranslationKeyProvider, MessageSeedProvider, RelativePeriodCategoryTranslationProvider {
    private static final String APP_KEY = "INS";
    static final String COMPONENT_NAME = "UDR";

    private volatile TransactionService transactionService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;
    private volatile MeteringService meteringService;
    private volatile LocationService locationService;
    private volatile RestQueryService restQueryService;
    private volatile Clock clock;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile ValidationService validationService;
    private volatile EstimationService estimationService;
    private volatile UsagePointDataService usagePointDataService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile ServiceCallInfoFactory serviceCallInfoFactory;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile License license;
    private volatile IssueService issueService;
    private volatile BpmService bpmService;
    private volatile ServiceCallService serviceCallService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile ReadingTypeDeliverableFactory readingTypeDeliverableFactory;
    private volatile DataAggregationService dataAggregationService;
    private volatile TimeService timeService;
    private volatile LicenseService licenseService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory;
    private volatile UsagePointCalendarService usagePointCalendarService;
    private volatile CalendarService calendarService;
    private volatile CalendarInfoFactory calendarInfoFactory;
    private volatile AppService appService;
    private volatile JsonService jsonService;
    private volatile SearchService searchService;
    private volatile MessageService messageService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                UsagePointResource.class,
                DeviceResource.class,
                UsagePointCustomPropertySetResource.class,
                UsagePointOutputResource.class,
                GoingOnResource.class,
                RestValidationExceptionMapper.class,
                UsagePointCalendarResource.class,
                UsagePointCalendarHistoryResource.class,
                BulkScheduleResource.class,
                UsagePointGroupResource.class
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
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(ValidationService.COMPONENTNAME, Layer.REST))
                .join(nlsService.getThesaurus(TimeService.COMPONENT_NAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(UsagePointDataService.COMPONENT_NAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN))
                .join(nlsService.getThesaurus(com.elster.jupiter.rest.util.impl.MessageSeeds.COMPONENT_NAME, Layer.REST))
        ;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
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
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Collections.addAll(keys, DefaultTranslationKey.values());
        Collections.addAll(keys, LocationTranslationKeys.values());
        Collections.addAll(keys, UsagePointModelTranslationKeys.values());
        return keys;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMeteringGroupService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setClockService(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setUsagePointDataService(UsagePointDataService usagePointDataService) {
        this.usagePointDataService = usagePointDataService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setDataAggregationService(DataAggregationService dataAggregationService) {
        this.dataAggregationService = dataAggregationService;
    }

    @Reference
    public void setServiceCallInfoFactory(ServiceCallInfoFactory serviceCallInfoFactory) {
        this.serviceCallInfoFactory = serviceCallInfoFactory;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference(target = "(com.elster.jupiter.license.application.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setReadingTypeDeliverableFactory(ReadingTypeDeliverableFactory readingTypeDeliverableFactory) {
        this.readingTypeDeliverableFactory = readingTypeDeliverableFactory;
    }

    @Reference
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setPropertyValueInfoService(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Reference
    public void setCalendarOnUsagePointInfoFactory(CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory) {
        this.calendarOnUsagePointInfoFactory = calendarOnUsagePointInfoFactory;
    }

    @Reference
    public void setUsagePointCalendarService(UsagePointCalendarService usagePointCalendarService) {
        this.usagePointCalendarService = usagePointCalendarService;
    }

    @Reference
    public void setCalendarInfoFactory(CalendarInfoFactory calendarInfoFactory) {
        this.calendarInfoFactory = calendarInfoFactory;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
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
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(transactionService).to(TransactionService.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(timeService).to(TimeService.class);
            bind(meteringService).to(MeteringService.class);
            bind(locationService).to(LocationService.class);
            bind(meteringGroupsService).to(MeteringGroupsService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(clock).to(Clock.class);
            bind(usagePointConfigurationService).to(UsagePointConfigurationService.class);
            bind(validationService).to(ValidationService.class);
            bind(estimationService).to(EstimationService.class);
            bind(usagePointDataService).to(UsagePointDataService.class);
            bind(customPropertySetService).to(CustomPropertySetService.class);
            bind(serviceCallService).to(ServiceCallService.class);
            bind(dataAggregationService).to(DataAggregationService.class);
            bind(serviceCallInfoFactory).to(ServiceCallInfoFactory.class);
            bind(threadPrincipalService).to(ThreadPrincipalService.class);
            bind(metrologyConfigurationService).to(MetrologyConfigurationService.class);
            bind(issueService).to(IssueService.class);
            bind(bpmService).to(BpmService.class);
            bind(licenseService).to(LicenseService.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(ResourceHelper.class).to(ResourceHelper.class);
            bind(ReadingTypeInfoFactory.class).to(ReadingTypeInfoFactory.class);
            bind(ValidationRuleInfoFactory.class).to(ValidationRuleInfoFactory.class);
            bind(propertyValueInfoService).to(PropertyValueInfoService.class);
            bind(CustomPropertySetInfoFactory.class).to(CustomPropertySetInfoFactory.class);
            bind(UsagePointInfoFactory.class).to(UsagePointInfoFactory.class);
            bind(OutputChannelDataInfoFactory.class).to(OutputChannelDataInfoFactory.class);
            bind(OutputRegisterDataInfoFactory.class).to(OutputRegisterDataInfoFactory.class);
            bind(LocationInfoFactory.class).to(LocationInfoFactory.class);
            bind(GoingOnResource.class).to(GoingOnResource.class);
            bind(readingTypeDeliverableFactory).to(ReadingTypeDeliverableFactory.class);
            bind(ChannelDataValidationSummaryInfoFactory.class).to(ChannelDataValidationSummaryInfoFactory.class);
            bind(OutputInfoFactory.class).to(OutputInfoFactory.class);
            bind(PurposeInfoFactory.class).to(PurposeInfoFactory.class);
            bind(ValidationStatusFactory.class).to(ValidationStatusFactory.class);
            bind(DataValidationTaskInfoFactory.class).to(DataValidationTaskInfoFactory.class);
            bind(calendarOnUsagePointInfoFactory).to(CalendarOnUsagePointInfoFactory.class);
            bind(usagePointCalendarService).to(UsagePointCalendarService.class);
            bind(calendarService).to(CalendarService.class);
            bind(appService).to(AppService.class);
            bind(AppServerHelper.class).to(AppServerHelper.class);
            bind(jsonService).to(JsonService.class);
            bind(searchService).to(SearchService.class);
            bind(messageService).to(MessageService.class);
            bind(calendarInfoFactory).to(CalendarInfoFactory.class);
            bind(UsagePointGroupInfoFactory.class).to(UsagePointGroupInfoFactory.class);
            bind(UsagePointLifeCycleInfoFactory.class).to(UsagePointLifeCycleInfoFactory.class);
            bind(UsagePointLifeCycleStateInfoFactory.class).to(UsagePointLifeCycleStateInfoFactory.class);
            bind(UsagePointLifeCycleTransitionInfoFactory.class).to(UsagePointLifeCycleTransitionInfoFactory.class);
            bind(BusinessProcessInfoFactory.class).to(BusinessProcessInfoFactory.class);
        }
    }

}
