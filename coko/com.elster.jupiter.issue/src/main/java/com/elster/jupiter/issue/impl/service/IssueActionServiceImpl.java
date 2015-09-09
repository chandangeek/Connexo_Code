package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.records.IssueActionTypeImpl;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

public class IssueActionServiceImpl implements IssueActionService {
    private static final Logger LOG = Logger.getLogger(IssueActionService.class.getName());

    private volatile DataModel dataModel;
    private volatile IssueService issueService;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;

    @Inject
    public IssueActionServiceImpl(DataModel dataModel, IssueService issueService, QueryService queryService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.queryService = queryService;
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<IssueAction> createIssueAction(String factoryId, String issueActionClassName) {
        IssueActionFactory actionFactory = issueService.getIssueActionFactories().get(factoryId);
        if (actionFactory == null) {
            LOG.info("Action Factory with provided factoryId: " + factoryId + " doesn't exist");//Probably, because not licensed anymore
            return Optional.empty();
        }
        return Optional.of(actionFactory.createIssueAction(issueActionClassName));
    }

    @Override
    public IssueActionType createActionType(String factoryId, String className, IssueType issueType) {
        return this.createActionType(factoryId, className, issueType, null);//any phase
    }

    @Override
    public IssueActionType createActionType(String factoryId, String className, IssueReason issueReason) {
        return this.createActionType(factoryId, className, issueReason, null);//any phase
    }

    @Override
    public IssueActionType createActionType(String factoryId, String className, IssueType issueType, CreationRuleActionPhase phase) {
        IssueActionTypeImpl type = findOrCreateActionType(factoryId, className, issueType, null);
        type.init(factoryId, className, issueType, phase);
        type.save();
        return type;
    }

    @Override
    public IssueActionType createActionType(String factoryId, String className, IssueReason issueReason, CreationRuleActionPhase phase) {
        IssueActionTypeImpl type = findOrCreateActionType(factoryId, className, issueReason.getIssueType(), issueReason);
        type.init(factoryId, className, issueReason, phase);
        type.save();
        return type;
    }

    private IssueActionTypeImpl findOrCreateActionType(String factoryId, String className, IssueType issueType, IssueReason issueReason) {
        List<IssueActionType> actionTypes = query(IssueActionType.class).select(
                    (where("factoryId").isEqualTo(factoryId))
                .and(where("className").isEqualTo(className))
                .and(issueType != null ? where("issueType").isEqualTo(issueType) : where("issueType").isNull())
                .and(issueReason != null ? where("issueReason").isEqualTo(issueReason) : where("issueReason").isNull()));
        IssueActionTypeImpl actionType = null;
        if (actionTypes.size() > 0) {
            LOG.info("You are trying to create action type which is already presented in system");
            actionType = (IssueActionTypeImpl) actionTypes.get(0);
        } else {
            actionType = dataModel.getInstance(IssueActionTypeImpl.class);
        }
        return actionType;
    }

    @Override
    public Optional<IssueActionType> findActionType(long id) {
        return find(IssueActionType.class, id);
    }

    @Override
    public Query<IssueActionType> getActionTypeQuery() {
        return query(IssueActionType.class, IssueType.class, IssueReason.class);
    }

    @Override
    public List<IssueActionFactory> getRegisteredFactories() {
        List<IssueActionFactory> factories = new ArrayList<>();
        factories.addAll(issueService.getIssueActionFactories().values());
        return factories;
    }

    @Override
    public IssueActionResult executeAction(IssueActionType type, Issue issue, Map<String, Object> props) {
        IssueActionResult result = null;
        Optional<IssueAction> issueAction = createIssueAction(type.getFactoryId(), type.getClassName());
        if (issueAction.isPresent()) {
            result = issueAction.get().initAndValidate(props).execute(issue);
        } else {
            IssueActionResult.DefaultActionResult failedResult = new IssueActionResult.DefaultActionResult();
            failedResult.fail(thesaurus.getFormat(MessageSeeds.ISSUE_ACTION_CLASS_LOAD_FAIL).format(type.getClassName(), type.getId()));
            result = failedResult;
        }
        return result;
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object... key) {
        return queryService.wrap(dataModel.query(clazz)).get(key);
    }

    private  <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }
}
