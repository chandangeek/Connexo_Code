package com.energyict.mdc.issue.datacollection.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.callback.InstallService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component(name = "com.energyict.mdc.issue.datacollection", service = {InstallService.class, TranslationKeyProvider.class, IssueDataCollectionService.class}, property = "name=" + IssueDataCollectionService.COMPONENT_NAME, immediate = true)
public class IssueDataCollectionServiceImpl implements InstallService, TranslationKeyProvider, IssueDataCollectionService {
    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile MessageService messageService;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;

    private volatile TopologyService topologyService;
    private volatile DeviceService deviceService;
    private volatile DataModel dataModel;

    // For OSGi framework
    @SuppressWarnings("unused")
    public IssueDataCollectionServiceImpl() {}

    // For unit testing
    @Inject
    public IssueDataCollectionServiceImpl(IssueService issueService, IssueActionService issueActionService, MessageService messageService, NlsService nlsService, OrmService ormService, QueryService queryService, TopologyService topologyService, DeviceService deviceService){
        setMessageService(messageService);
        setIssueService(issueService);
        setIssueActionService(issueActionService);
        setNlsService(nlsService);
        setOrmService(ormService);
        setQueryService(queryService);
        setTopologyService(topologyService);
        setDeviceService(deviceService);

        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Activate
    public final void activate(){
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
        new Installer(dataModel, issueService, issueActionService, messageService).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS", "ISU", "MSG", "ORM", "DDC");
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public final void setIssueActionService(IssueActionService issueActionService) {
        this.issueActionService = issueActionService;
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

    public DataModel getDataModel(){
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

    @Override
    public Optional<IssueDataCollection> findIssue(long id) {
        Optional<? extends IssueDataCollection> issue = findOpenIssue(id);
        if (!issue.isPresent()){
            issue = findHistoricalIssue(id);
        }
        return (Optional<IssueDataCollection>) issue;
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
        instance.init(baseIssue);
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
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }
}
