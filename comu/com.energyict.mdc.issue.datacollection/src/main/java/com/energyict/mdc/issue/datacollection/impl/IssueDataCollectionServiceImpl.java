package com.energyict.mdc.issue.datacollection.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionFilter;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.database.TableSpecs;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.energyict.mdc.issue.datacollection.impl.install.Installer;
import com.energyict.mdc.issue.datacollection.impl.records.OpenIssueDataCollectionImpl;
import com.google.inject.AbstractModule;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.issue.datacollection", service = {InstallService.class, TranslationKeyProvider.class, IssueDataCollectionService.class, IssueProvider.class}, property = "name=" + IssueDataCollectionService.COMPONENT_NAME, immediate = true)
public class IssueDataCollectionServiceImpl implements InstallService, TranslationKeyProvider, IssueDataCollectionService, IssueProvider {
    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile MessageService messageService;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;

    private volatile TopologyService topologyService;
    private volatile DeviceService deviceService;
    private volatile DataModel dataModel;

    // For OSGi framework
    public IssueDataCollectionServiceImpl() {
    }

    // For unit testing
    @Inject
    public IssueDataCollectionServiceImpl(IssueService issueService,
                                          MessageService messageService,
                                          NlsService nlsService,
                                          OrmService ormService,
                                          QueryService queryService,
                                          TopologyService topologyService,
                                          DeviceService deviceService,
                                          EventService eventService) {
        setMessageService(messageService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setOrmService(ormService);
        setQueryService(queryService);
        setTopologyService(topologyService);
        setDeviceService(deviceService);
        setEventService(eventService);

        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Activate
    public final void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MessageService.class).toInstance(messageService);
                bind(IssueService.class).toInstance(issueService);
                bind(QueryService.class).toInstance(queryService);
                bind(TopologyService.class).toInstance(topologyService);
                bind(DeviceService.class).toInstance(deviceService);
            }
        });
    }

    @Override
    public void install() {
        new Installer(dataModel, issueService, issueActionService, messageService, eventService, thesaurus).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS", "ISU", "MSG", "ORM", "DDC", "MTR", "CES", "DDC");
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(IssueDataCollectionService.COMPONENT_NAME, "Issue Datacollection");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public final void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public Optional<? extends IssueDataCollection> findIssue(long id) {
        Optional<OpenIssueDataCollection> issue = findOpenIssue(id);
        if (issue.isPresent()) {
            return issue;
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<OpenIssueDataCollection> findOpenIssue(long id) {
        return find(OpenIssueDataCollection.class, id, OpenIssue.class);
    }

    @Override
    public Optional<HistoricalIssueDataCollection> findHistoricalIssue(long id) {
        return find(HistoricalIssueDataCollection.class, id, HistoricalIssue.class);
    }

    @Override
    public OpenIssueDataCollection createIssue(Issue baseIssue) {
        OpenIssueDataCollectionImpl instance = dataModel.getInstance(OpenIssueDataCollectionImpl.class);
        instance.setIssue((OpenIssue) baseIssue);
        return instance;
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object key, Class<?>... eagers) {
        return queryService.wrap(dataModel.query(clazz, eagers)).get(key);
    }

    @Override
    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    @Override
    public Finder<? extends IssueDataCollection> findIssues(IssueDataCollectionFilter filter, Class<?>... eagers) {
        Condition condition = buildConditionFromFilter(filter);
        List<Class<?>> eagerClasses = determineMainApiClass(filter);
        if (eagers == null) {
            eagerClasses.addAll(Arrays.asList(eagers));
        } else {
            eagerClasses.addAll(Arrays.asList(IssueStatus.class, EndDevice.class, User.class, IssueReason.class, IssueType.class));
        }
        return DefaultFinder.of((Class<IssueDataCollection>)eagerClasses.remove(0), condition, dataModel, eagerClasses.toArray(new Class<?>[eagerClasses.size()]));
    }

    private List<Class<?>> determineMainApiClass(IssueDataCollectionFilter filter) {
        List<Class<?>> eagerClasses = new ArrayList<>();
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().allMatch(status -> !status.isHistorical())) {
            eagerClasses.add(OpenIssueDataCollection.class);
            eagerClasses.add(OpenIssue.class);
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(HistoricalIssueDataCollection.class);
            eagerClasses.add(HistoricalIssue.class);
        } else {
            eagerClasses.add(IssueDataCollection.class);
            eagerClasses.add(Issue.class);
        }
        return eagerClasses;
    }

    private Condition buildConditionFromFilter(IssueDataCollectionFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by assignee
        Condition assigneeCondition = Condition.TRUE;
        if (filter.getAssignee().isPresent()) {
            assigneeCondition = where("baseIssue.user").isEqualTo(filter.getAssignee().get());
        }
        if (filter.isUnassignedOnly()) {
            assigneeCondition = where("baseIssue.user").isNull();
        }
        condition = condition.and(assigneeCondition);
        //filter by reason
        if (!filter.getIssueReasons().isEmpty()) {
            condition = condition.and(where("baseIssue.reason").in(filter.getIssueReasons()));
        }
        //filter by device
        if (!filter.getDevices().isEmpty()) {
            condition = condition.and(where("baseIssue.device").in(filter.getDevices()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where("baseIssue.status").in(filter.getStatuses()));
        }
        return condition;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>(Arrays.asList(MessageSeeds.values()));
        translationKeys.add(new SimpleTranslationKey(ModuleConstants.AQ_DATA_COLLECTION_EVENT_SUBSC, ModuleConstants.AQ_DATA_COLLECTION_EVENT_DISPLAYNAME));
        return translationKeys;
    }

    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
        return issue instanceof OpenIssueDataCollection ? Optional.of(issue) : findOpenIssue(issue.getId());
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        return issue instanceof HistoricalIssueDataCollection ? Optional.of(issue) : findHistoricalIssue(issue.getId());
    }
}
