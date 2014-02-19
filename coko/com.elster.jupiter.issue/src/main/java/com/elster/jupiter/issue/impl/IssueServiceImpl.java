package com.elster.jupiter.issue.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.*;
import com.elster.jupiter.issue.database.GroupingOperation;
import com.elster.jupiter.issue.database.TableSpecs;
import com.elster.jupiter.issue.exception.IssueClosingException;
import com.elster.jupiter.issue.module.Installer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DoesNotExistException;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.issue", service = {IssueService.class, InstallService.class}, property = "name=" + IssueService.COMPONENT_NAME)
public class IssueServiceImpl implements IssueService, InstallService {
    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile QueryService queryService;
    private volatile MeteringService meteringService;
    private static Logger LOG = Logger.getLogger(IssueServiceImpl.class.getName());

    public IssueServiceImpl(){
    }

    @Inject
    public IssueServiceImpl(OrmService ormService, QueryService queryService, MeteringService meteringService) {
        setOrmService(ormService);
        setQueryService(queryService);
        setMeteringService(meteringService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(OrmService.class).toInstance(ormService);
                bind(QueryService.class).toInstance(queryService);
                bind(MeteringService.class).toInstance(meteringService);
            }
        });
    }

    @Override
    public void install() {
        new Installer(this.dataModel).install(true, true);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.newDataModel(IssueService.COMPONENT_NAME, "Issue Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public Optional<Issue> getIssueById(long issueId) {
        Query<Issue> query = queryService.wrap(dataModel.query(Issue.class));
        return query.get(issueId);
    }

    @Override
    public Query<Issue> getIssueListQuery() {
        return queryService.wrap(dataModel.query(Issue.class,IssueAssignee.class));
    }

    @Override
    public Map<String, Long> getIssueGroupList (String groupColumn, boolean isAsc, long start, long limit) {
        IssueGroupColumns orderBy = IssueGroupColumns.fromString(groupColumn);
        if (orderBy == null){
            return Collections.<String, Long>emptyMap();
        }
        Map<String, Long> groups = GroupingOperation.init(this.dataModel)
                .setLimit(limit)
                .setStart(start)
                .setGroupColumn(orderBy)
                .setOrderDirection(isAsc)
                .execute();
        return groups;
    }

    @Override
    public void closeIssue(long issueId, long version, IssueStatus newStatus, String comment) throws IssueClosingException{
        Issue issueForClose = this.getIssueById(issueId).orNull();
        if (issueForClose == null){
            String identifier = "id = " + String.valueOf(issueId);
            throw new IssueClosingException(new DoesNotExistException(identifier), "with " + identifier);
        }
        if (version != issueForClose.getVersion()){
            // TODO check, may be we already have ConcurentModificationException or something like that
            throw new IssueClosingException(issueForClose.getTitle());
        }
        IssueImpl.class.cast(issueForClose).setStatus(newStatus);
        HistoricalIssue historicalIssue = new HistoricalIssueImpl(issueForClose);
        dataModel.mapper(HistoricalIssue.class).persist(historicalIssue);
        dataModel.mapper(Issue.class).remove(issueForClose);
    }
}