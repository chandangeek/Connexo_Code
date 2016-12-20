package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.I18N;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidator;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.UsagePointTypeInfo;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.impl.aggregation.CalculatedReadingRecordFactory;
import com.elster.jupiter.metering.impl.aggregation.CalculatedReadingRecordFactoryImpl;
import com.elster.jupiter.metering.impl.aggregation.DataAggregationServiceImpl;
import com.elster.jupiter.metering.impl.aggregation.InstantTruncaterFactory;
import com.elster.jupiter.metering.impl.aggregation.ServerDataAggregationService;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationServiceImpl;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;
import com.elster.jupiter.metering.impl.search.UsagePointRequirementsSearchDomain;
import com.elster.jupiter.metering.impl.upgraders.UpgraderV10_2;
import com.elster.jupiter.metering.impl.upgraders.UpgraderV10_2_1;
import com.elster.jupiter.metering.impl.upgraders.UpgraderV10_3;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;

@Component(name = "com.elster.jupiter.metering.model",
        service = {MeteringDataModelService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true)
@Singleton
public class MeteringDataModelServiceImpl implements MeteringDataModelService, MessageSeedProvider, TranslationKeyProvider {

    private volatile IdsService idsService;
    private volatile QueryService queryService;
    private volatile PartyService partyService;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile EventService eventService;
    private volatile DataModel dataModel;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile SearchService searchService;
    private volatile PropertySpecService propertySpecService;
    private volatile LicenseService licenseService;
    private volatile UpgradeService upgradeService;
    private volatile TimeService timeService;
    private volatile UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    private List<HeadEndInterface> headEndInterfaces = new CopyOnWriteArrayList<>();
    private List<CustomUsagePointMeterActivationValidator> customValidators = new CopyOnWriteArrayList<>();
    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    private MeteringServiceImpl meteringService;
    private MeteringTranslationServiceImpl meteringTranslationService;
    private InstantTruncaterFactory truncaterFactory;
    private DataAggregationService dataAggregationService;
    private UsagePointRequirementsSearchDomain usagePointRequirementsSearchDomain;
    private MetrologyConfigurationServiceImpl metrologyConfigurationService;

    private boolean createAllReadingTypes;
    private String[] requiredReadingTypes;

    @SuppressWarnings("unused")
    public MeteringDataModelServiceImpl() {
        this.createAllReadingTypes = true;
    }

    @Inject // Tests only!
    public MeteringDataModelServiceImpl(@Named("createReadingTypes") boolean createAllReadingTypes,
                                        @Named("requiredReadingTypes") String requiredReadingTypes,
                                        @Named("dataAggregationMock") Provider<DataAggregationService> dataAggregationMockProvider,
                                        BundleContext bundleContext,
                                        IdsService idsService, QueryService queryService,
                                        PartyService partyService, Clock clock, UserService userService, EventService eventService, NlsService nlsService,
                                        MessageService messageService, JsonService jsonService, FiniteStateMachineService finiteStateMachineService,
                                        CustomPropertySetService customPropertySetService, SearchService searchService, PropertySpecService propertySpecService,
                                        LicenseService licenseService, UpgradeService upgradeService, OrmService ormService, TimeService timeService,
                                        UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        setIdsService(idsService);
        setQueryService(queryService);
        setPartyService(partyService);
        setClock(clock);
        setUserService(userService);
        setEventService(eventService);
        setNlsService(nlsService);
        setMessageService(messageService);
        setJsonService(jsonService);
        setFiniteStateMachineService(finiteStateMachineService);
        setCustomPropertySetService(customPropertySetService);
        setSearchService(searchService);
        setPropertySpecService(propertySpecService);
        setLicenseService(licenseService);
        setUpgradeService(upgradeService);
        setTimeService(timeService);
        setOrmService(ormService);
        setUsagePointLifeCycleConfigurationService(usagePointLifeCycleConfigurationService);

        this.createAllReadingTypes = createAllReadingTypes;
        this.requiredReadingTypes = requiredReadingTypes.split(";");
        if (dataAggregationMockProvider.get() != null) {
            this.dataAggregationService = dataAggregationMockProvider.get();
        }
        activate(bundleContext, true);
    }

    @Activate
    public final void activate(BundleContext bundleContext) {
        this.activate(bundleContext, false);
    }

    private void activate(BundleContext bundleContext, boolean createDefaultLocationTemplate) {
        createServices(bundleContext, createDefaultLocationTemplate);
        registerDatabaseTables();
        registerDataModel(bundleContext);
        registerUsagePointSearchDoamin();
        installDataModel();
        registerServices(bundleContext);
    }

    private void registerDatabaseTables() {
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(this.dataModel);
        }
    }

    private void createServices(BundleContext bundleContext, boolean createDefaultLocationTemplate) {
        this.meteringService = new MeteringServiceImpl(this, getDataModel(), getThesaurus(), getClock(), this.idsService,
                this.eventService, this.queryService, this.messageService, this.jsonService, this.upgradeService);
        this.meteringService.defineLocationTemplates(bundleContext, createDefaultLocationTemplate); // This call has effect on resulting table spec!
        this.meteringTranslationService = new MeteringTranslationServiceImpl(this.thesaurus);
        this.truncaterFactory = new InstantTruncaterFactory(this.meteringService);
        if (this.dataAggregationService == null) { // It is possible that service was already set to mocked instance.
            this.dataAggregationService = new DataAggregationServiceImpl(this.meteringService, this.truncaterFactory, this.customPropertySetService);
        }
        this.metrologyConfigurationService = new MetrologyConfigurationServiceImpl(this, this.dataModel, this.thesaurus);
        this.usagePointRequirementsSearchDomain = new UsagePointRequirementsSearchDomain(this.propertySpecService, this.meteringService, this.meteringTranslationService, this.metrologyConfigurationService, this.clock, this.licenseService);
    }

    private void registerDataModel(BundleContext bundleContext) {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(BundleContext.class).toInstance(bundleContext);
                bind(MeteringDataModelService.class).toInstance(MeteringDataModelServiceImpl.this);
                bind(MeteringDataModelServiceImpl.class).toInstance(MeteringDataModelServiceImpl.this);
                bind(ChannelBuilder.class).to(ChannelBuilderImpl.class);
                bind(MeteringServiceImpl.class).toInstance(meteringService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(ServerMeteringService.class).toInstance(meteringService);
                bind(MeteringTranslationService.class).toInstance(meteringTranslationService);
                bind(InstantTruncaterFactory.class).toInstance(truncaterFactory);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(IdsService.class).toInstance(idsService);
                bind(PartyService.class).toInstance(partyService);
                bind(UserService.class).toInstance(userService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Clock.class).toInstance(clock);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(MetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
                bind(ServerMetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
                bind(CalculatedReadingRecordFactory.class).to(CalculatedReadingRecordFactoryImpl.class);
                bind(UsagePointRequirementsSearchDomain.class).toInstance(usagePointRequirementsSearchDomain);
                bind(SearchService.class).toInstance(searchService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(LicenseService.class).toInstance(licenseService);
                bind(MessageService.class).toInstance(messageService);
                bind(MetrologyConfigurationServiceImpl.class).toInstance(metrologyConfigurationService);
                bind(DataAggregationService.class).toInstance(dataAggregationService);
                bind(ServerDataAggregationService.class).toInstance((ServerDataAggregationService) dataAggregationService);
                bind(UsagePointLifeCycleConfigurationService.class).toInstance(usagePointLifeCycleConfigurationService);
                bind(TimeService.class).toInstance(timeService);
            }
        });
    }

    private void installDataModel() {
        this.upgradeService.register(
                identifier("Pulse", MeteringDataModelService.COMPONENT_NAME),
                dataModel,
                InstallerImpl.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class,
                        version(10, 2, 1), UpgraderV10_2_1.class,
                        version(10, 3), UpgraderV10_3.class
                ));
        this.meteringService.readLocationTemplatesFromDatabase();
    }

    private void registerServices(BundleContext bundleContext) {
        registerMeteringService(bundleContext);
        registerMeteringTranslationService(bundleContext);
        registerTruncationFactory(bundleContext);
        registerDataAggregationService(bundleContext);
        registerMetrologyConfigurationService(bundleContext); // Search domain must already be registered
    }

    private Dictionary<String, Object> noServiceProperties() {
        return new Hashtable<>();
    }

    private void registerMeteringService(BundleContext bundleContext) {
        if (bundleContext != null) {
            this.serviceRegistrations.add(
                    bundleContext.registerService(
                            new String[]{
                                    MeteringService.class.getName(),
                                    ServerMeteringService.class.getName()},
                            this.meteringService,
                            noServiceProperties()));
        }
    }

    private void registerMeteringTranslationService(BundleContext bundleContext) {
        if (bundleContext != null) {
            this.serviceRegistrations.add(
                    bundleContext.registerService(
                            MeteringTranslationService.class,
                            this.meteringTranslationService,
                            noServiceProperties()));
        }
    }

    private void registerTruncationFactory(BundleContext bundleContext) {
        if (bundleContext != null) {
            this.serviceRegistrations.add(
                    bundleContext.registerService(
                            new String[]{InstantTruncaterFactory.class.getName()},
                            this.truncaterFactory,
                            noServiceProperties()));
        }
    }

    private void registerDataAggregationService(BundleContext bundleContext) {
        if (bundleContext != null) {
            this.serviceRegistrations.add(
                    bundleContext.registerService(
                            new String[]{DataAggregationService.class.getName()},
                            this.dataAggregationService,
                            noServiceProperties()));
        }
    }

    private void registerUsagePointSearchDoamin() {
        this.searchService.register(this.usagePointRequirementsSearchDomain);
    }

    private void registerMetrologyConfigurationService(BundleContext bundleContext) {
        if (bundleContext != null) {
            this.serviceRegistrations.add(
                    bundleContext.registerService(
                            new String[]{
                                    MetrologyConfigurationService.class.getName(),
                                    ServerMetrologyConfigurationService.class.getName()},
                            this.metrologyConfigurationService,
                            noServiceProperties()));
        }
    }

    @Deactivate
    public final void deactivate() {
        if (!this.serviceRegistrations.isEmpty()) {
            this.serviceRegistrations.forEach(ServiceRegistration::unregister);
        }
        if (this.usagePointRequirementsSearchDomain != null) {
            this.searchService.unregister(this.usagePointRequirementsSearchDomain);
        }
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        Arrays.stream(ConnectionState.values()).forEach(translationKeys::add);
        Arrays.stream(DefaultTranslationKey.values()).forEach(translationKeys::add);
        Arrays.stream(DefaultMeterRole.values()).forEach(translationKeys::add);
        Arrays.stream(ServiceKind.values()).forEach(translationKeys::add);
        Arrays.stream(Privileges.values()).forEach(translationKeys::add);
        Arrays.stream(PropertyTranslationKeys.values()).forEach(translationKeys::add);
        Arrays.stream(com.elster.jupiter.metering.impl.search.enddevice.PropertyTranslationKeys.values()).forEach(translationKeys::add);
        Arrays.stream(UsagePointConnectedKind.values()).forEach(translationKeys::add);
        Arrays.stream(AmiBillingReadyKind.values()).forEach(translationKeys::add);
        Arrays.stream(BypassStatus.values()).forEach(translationKeys::add);
        Arrays.stream(GasDayOptions.RelativePeriodTranslationKey.values()).forEach(translationKeys::add);
        Arrays.stream(YesNoAnswer.values()).map(YesNoAnswerTranslationKey::new).forEach(translationKeys::add);
        translationKeys.addAll(ReadingTypeTranslationKeys.allKeys());
        translationKeys.addAll(Arrays.asList(DefaultMetrologyPurpose.Translation.values()));
        translationKeys.addAll(Arrays.asList(MetrologyConfigurationStatus.Translation.values()));
        translationKeys.addAll(Arrays.asList(UsagePointTypeInfo.UsagePointType.Translation.values()));
        return translationKeys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENT_NAME, "CIM Metering");
    }

    @Reference
    public final void setIdsService(IdsService idsService) {
        this.idsService = idsService;
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public final void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public final void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public final void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public final void setFiniteStateMachineService(FiniteStateMachineService service) {
        // method was added to make sure that the MeteringService is started after the FiniteStateMachineService
        // because the Metering datamodel depends on the FiniteStateMachine datamodel
        this.finiteStateMachineService = service;
    }

    @Reference
    public final void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public final void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public final void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public final void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCustomUsagePointMeterActivationValidator(CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator) {
        this.customValidators.add(customUsagePointMeterActivationValidator);
    }

    @Override
    public void removeCustomUsagePointMeterActivationValidator(CustomUsagePointMeterActivationValidator customUsagePointMeterActivationValidator) {
        this.customValidators.remove(customUsagePointMeterActivationValidator);
    }


    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    @Reference(name = "ZHeadEndInterface", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addHeadEndInterface(HeadEndInterface headEndInterface) {
        this.headEndInterfaces.add(headEndInterface);
    }

    @Override
    public void removeHeadEndInterface(HeadEndInterface headEndInterface) {
        headEndInterfaces.remove(headEndInterface);
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus myThesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
        Thesaurus cboThesaurus = nlsService.getThesaurus(I18N.COMPONENT_NAME, Layer.DOMAIN);
        this.thesaurus = myThesaurus.join(cboThesaurus);
    }

    @Reference
    public void setUsagePointLifeCycleConfigurationService(UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    @Override
    public List<HeadEndInterface> getHeadEndInterfaces() {
        return Collections.unmodifiableList(headEndInterfaces);
    }

    @Override
    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Override
    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Override
    public Clock getClock() {
        return this.clock;
    }

    @Override
    public void validateUsagePointMeterActivation(MeterRole meterRole, Meter meter, UsagePoint usagePoint) throws
            CustomUsagePointMeterActivationValidationException {
        this.customValidators.forEach(validator -> validator.validateActivation(meterRole, meter, usagePoint));
    }

    @Override
    public void copyKeyIfMissing(NlsKey name, String localKey) {
        if (!this.thesaurus.hasKey(localKey)) {
            this.nlsService.copy(name, COMPONENT_NAME, Layer.DOMAIN, key -> localKey);
        }
    }

    @Override
    public ServerMeteringService getMeteringService() {
        return this.meteringService;
    }

    @Override
    public MeteringTranslationServiceImpl getMeteringTranslationService() {
        return meteringTranslationService;
    }

    @Override
    public DataAggregationService getDataAggregationService() {
        return this.dataAggregationService;
    }

    @Override
    public ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return this.metrologyConfigurationService;
    }

    boolean isCreateAllReadingTypes() {
        return this.createAllReadingTypes;
    }

    String[] getRequiredReadingTypes() {
        return this.requiredReadingTypes;
    }

    private static class YesNoAnswerTranslationKey implements TranslationKey {
        private final YesNoAnswer answer;

        YesNoAnswerTranslationKey(YesNoAnswer answer) {
            this.answer = answer;
        }

        @Override
        public String getKey() {
            return answer.toString();
        }

        @Override
        public String getDefaultFormat() {
            return answer.toString();
        }
    }
}
