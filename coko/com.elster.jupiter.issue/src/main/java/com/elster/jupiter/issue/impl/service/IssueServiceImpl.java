package com.elster.jupiter.issue.impl.service;

import static com.elster.jupiter.util.conditions.Where.where;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.database.groups.IssuesGroupOperation;
import com.elster.jupiter.issue.impl.records.IssueReasonImpl;
import com.elster.jupiter.issue.impl.records.IssueStatusImpl;
import com.elster.jupiter.issue.impl.records.IssueTypeImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeRoleImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeTeamImpl;
import com.elster.jupiter.issue.impl.records.assignee.types.AssigneeType;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.NotUniqueKeyException;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueProvider;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;

public class IssueServiceImpl implements IssueService {
    private static final Logger LOG = Logger.getLogger(IssueServiceImpl.class.getName());

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile UserService userService;
    private volatile Thesaurus thesaurus;
    
    private List<IssueProvider> registeredIssueProviders = new ArrayList<>();

    public IssueServiceImpl() {
    }

    @Inject
    public IssueServiceImpl(QueryService queryService,
                            UserService userService,
                            IssueMappingService issueMappingService,
                            NlsService nlsService) {
        setQueryService(queryService);
        setIssueMappingService(issueMappingService);
        setUserService(userService);
        setNlsService(nlsService);
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public final void setIssueMappingService(IssueMappingService issueMappingService) {
        dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    private void installEntityTranslation(MessageSeed seed) {
        if (seed == null){
            throw new IllegalArgumentException("Translation for the new entity can't be null");
        }
        if (thesaurus.getTranslations().get(seed.getKey()) == null) {
            try {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(IssueService.COMPONENT_NAME, Layer.DOMAIN, seed.getKey()).defaultMessage(seed.getDefaultFormat());
                thesaurus.addTranslations(Collections.singletonList(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, seed.getDefaultFormat())));
            } catch (Exception ex) {
                LOG.warning("Unable to setup translation for: key = " + seed.getKey() + ", value = " + seed.getDefaultFormat());
            }
        }
    }

    @Override
    public Optional<Issue> findIssue(long id) {
        Optional<? extends Issue> issue = findOpenIssue(id);
        if (!issue.isPresent()){
            issue = findHistoricalIssue(id);
        }
        return (Optional<Issue>) issue;
    }

    @Override
    public Optional<OpenIssue> findOpenIssue(long id) {
        return find(OpenIssue.class, id);
    }

    @Override
    public Optional<HistoricalIssue> findHistoricalIssue(long id) {
        return find(HistoricalIssue.class, id);
    }

    @Override
    public Optional<IssueStatus> findStatus(String key) {
        return find(IssueStatus.class, key);
    }

    @Override
    public Optional<IssueReason> findReason(String key) {
        return find(IssueReason.class, key);
    }

    @Override
    public Optional<IssueComment> findComment(long id) {
        return find(IssueComment.class, id);
    }

    @Override
    public Optional<IssueType> findIssueType(String key) {
        return find(IssueType.class, key);
    }

    @Override
    public IssueAssignee findIssueAssignee(String type, long id) {
        AssigneeType assigneeType = AssigneeType.fromString(type);
        if (assigneeType != null) {
            return assigneeType.getAssignee(this, userService, id);
        }
        return null;
    }

    @Override
    public boolean checkIssueAssigneeType(String type) {
        return AssigneeType.fromString(type) != null;
    }

    @Override
    public Optional<AssigneeRole> findAssigneeRole(long id) {
        return find(AssigneeRole.class, id);
    }

    @Override
    public Optional<AssigneeTeam> findAssigneeTeam(long id) {
        return find(AssigneeTeam.class, id);
    }

    @Override
    public IssueStatus createStatus(String key, boolean isHistorical, MessageSeed seed) {
        if(findStatus(key).isPresent()){
            throw new NotUniqueKeyException(thesaurus, key);
        }
        installEntityTranslation(seed);
        IssueStatusImpl status = dataModel.getInstance(IssueStatusImpl.class);
        status.init(key, isHistorical, seed).save();
        return status;
    }

    @Override
    public IssueReason createReason(String key, IssueType type, MessageSeed seed) {
        if(findReason(key).isPresent()){
            throw new NotUniqueKeyException(thesaurus, key);
        }
        installEntityTranslation(seed);
        IssueReasonImpl reason = dataModel.getInstance(IssueReasonImpl.class);
        reason.init(key, type, seed).save();
        return reason;
    }

    @Override
    public IssueType createIssueType(String key, MessageSeed seed) {
        if(findIssueType(key).isPresent()){
            throw new NotUniqueKeyException(thesaurus, key);
        }
        installEntityTranslation(seed);
        IssueTypeImpl issueType = dataModel.getInstance(IssueTypeImpl.class);
        issueType.init(key, seed).save();
        return issueType;
    }

    @Override
    public AssigneeRole createAssigneeRole() {
        return dataModel.getInstance(AssigneeRoleImpl.class);
    }

    @Override
    public AssigneeTeam createAssigneeTeam() {
        return dataModel.getInstance(AssigneeTeamImpl.class);
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object... key) {
        return queryService.wrap(dataModel.query(clazz)).get(key);
    }

    @Override
    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    @Override
    public List<IssueGroup> getIssueGroupList(IssueGroupFilter filter) {
        return IssuesGroupOperation.from(filter, this.dataModel, thesaurus).execute();
    }

    @Override
    public int countOpenDataCollectionIssues(String mRID) {
        Optional<IssueType> issueType = findIssueType("datacollection");
        if (issueType.isPresent()) {
            Condition condition = Where.where("reason.issueType").isEqualTo(issueType.get())
                    .and(where("device.mRID").isEqualTo(mRID));
            List<OpenIssue> issues = dataModel.query(OpenIssue.class, IssueReason.class, EndDevice.class)
                    .select(condition);
            return issues.size();
        } else {
            return 0;
        }
    }
    
    @Reference(name = "ZIssueProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addIssueProvider(IssueProvider issueProvider) {
        registeredIssueProviders.add(issueProvider);
    }
    
    public void removeIssueProvider(IssueProvider issueProvider) {
        registeredIssueProviders.remove(issueProvider);
    }
    
    @Override
    public List<IssueProvider> getIssueProviders() {
        return registeredIssueProviders;
    }
    
}