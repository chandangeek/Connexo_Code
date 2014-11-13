package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.records.IssueActionTypeImpl;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionFactory;
import com.elster.jupiter.issue.share.cep.IssueActionResult;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import java.util.Optional;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.issue.action", service = {IssueActionService.class}, immediate = true)
public class IssueActionServiceImpl implements IssueActionService {
    private static final Logger LOG = Logger.getLogger(IssueActionService.class.getName());
    private volatile QueryService queryService;
    private volatile TransactionService transactionService;
    private volatile DataModel dataModel;
    private Map<String, IssueActionFactory> registeredFactories = new HashMap<>();

    public IssueActionServiceImpl() {
    }

    @Inject
    public IssueActionServiceImpl(QueryService queryService, IssueMappingService issueMappingService, TransactionService transactionService) {
        setQueryService(queryService);
        setIssueMappingService(issueMappingService);
        setTransactionService(transactionService);
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public final void setIssueMappingService(IssueMappingService issueMappingService) {
        dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public final void addIssueActionFactory(IssueActionFactory issueActionFactory, Map<String, Object> map) {
        registeredFactories.put(issueActionFactory.getId(), issueActionFactory);
    }

    @Reference
    public final void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public final void removeIssueActionFactory(IssueActionFactory issueActionFactory) {
        registeredFactories.remove(issueActionFactory.getId());
    }

    @Override
    public List<IssueActionFactory> getRegisteredFactories() {
        List<IssueActionFactory> factories = new ArrayList<>();
        factories.addAll(registeredFactories.values());
        return factories;
    }

    @Override
    public IssueAction createIssueAction(String factoryId, String issueActionClassName) {
        IssueActionFactory actionFactory = registeredFactories.get(factoryId);
        if (actionFactory == null) {
            throw new IllegalArgumentException("Action Factory with provided factoryId: " + factoryId + " doesn't exist");
        }
        return actionFactory.createIssueAction(issueActionClassName);
    }

    @Override
    public IssueActionType createActionType(String factoryId, String className, IssueType issueType) {
        IssueActionTypeImpl type = findOrCreateActionType(factoryId, className);
        type.init(factoryId, className, issueType);
        type.save();
        return type;
    }


    @Override
    public IssueActionType createActionType(String factoryId, String className, IssueReason issueReason) {
        IssueActionTypeImpl type = findOrCreateActionType(factoryId, className);
        type.init(factoryId, className, issueReason);
        type.save();
        return type;
    }

    private IssueActionTypeImpl findOrCreateActionType(String factoryId, String className){
        List<IssueActionType> actionTypes = query(IssueActionType.class).select(where("factoryId").isEqualTo(factoryId).and(where("className").isEqualTo(className)));
        IssueActionTypeImpl actionType = null;
        if (actionTypes.size() > 0){
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
    public IssueActionResult executeAction(IssueActionType type, Issue issue, Map<String, String> actionParams) {
        IssueActionResult result = null;
        try(TransactionContext context = transactionService.getContext()){
            result = createIssueAction(type.getFactoryId(), type.getClassName()).execute(issue, actionParams);
            context.commit();
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
