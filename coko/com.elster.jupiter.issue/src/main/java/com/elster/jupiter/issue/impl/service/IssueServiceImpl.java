package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.database.groups.IssuesGroupOperation;
import com.elster.jupiter.issue.impl.records.IssueReasonImpl;
import com.elster.jupiter.issue.impl.records.IssueStatusImpl;
import com.elster.jupiter.issue.impl.records.IssueTypeImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeRoleImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeTeamImpl;
import com.elster.jupiter.issue.impl.records.assignee.types.AssigneeType;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.issue", service = IssueService.class)
public class IssueServiceImpl implements IssueService {
    private static final Logger LOG = Logger.getLogger(IssueServiceImpl.class.getName());

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile UserService userService;
    private volatile Thesaurus thesaurus;

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
        return find(Issue.class, id);
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
    public List<IssueGroup> getIssueGroupList(IssueGroupFilter builder) {
        return IssuesGroupOperation.init(builder, this.dataModel).execute();
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

}