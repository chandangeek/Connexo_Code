package com.elster.jupiter.issue.impl.records;

import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.conditions.Where.where;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.CreationRuleOrActionValidationException;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.CreationRuleParameter;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.conditions.Condition;

@HasUniqueName(message = "{" + MessageSeeds.Keys.CREATION_RULE_UNIQUE_NAME + "}")
public class CreationRuleImpl extends EntityImpl implements CreationRule, UniqueNamed {
    private static final String PARAM_RULE_ID = "ruleId";

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min=1, max = 256, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    private String comment;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String content;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<IssueReason> reason = ValueReference.absent();
    private long dueInValue;
    private DueInType dueInType;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 128, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_128 + "}")
    private String templateUuid;
    private Instant obsoleteTime;

    private List<CreationRuleParameter> parameters = new ArrayList<>();
    private List<CreationRuleAction> actions = new ArrayList<>();

    private IssueService issueService;
    private Thesaurus thesaurus;

    @Inject
    public CreationRuleImpl(DataModel dataModel, IssueService issueService, Thesaurus thesaurus) {
        super(dataModel);
        this.issueService = issueService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public byte[] getData() {
        return content != null ? content.getBytes(Charset.defaultCharset()) : new byte[]{};
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public IssueReason getReason() {
        return reason.orNull();
    }

    @Override
    public void setReason(IssueReason reason) {
        this.reason.set(reason);
    }

    @Override
    public long getDueInValue() {
        return dueInValue;
    }

    @Override
    public void setDueInValue(long dueInValue) {
        this.dueInValue = dueInValue;
    }

    @Override
    public DueInType getDueInType() {
        return dueInType;
    }

    @Override
    public void setDueInType(DueInType dueInType) {
        this.dueInType = dueInType;
    }

    @Override
    public String getTemplateUuid() {
        return templateUuid;
    }

    @Override
    public CreationRuleTemplate getTemplate() {
        return issueService.getIssueCreationService().findCreationRuleTemplate(getTemplateUuid()).orElse(null);
    }

    @Override
    public void setTemplateUuid(String templateName) {
        this.templateUuid = templateName;
    }

    @Override
    public Instant getObsoleteTime() {
        return obsoleteTime;
    }

    @Override
    public void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }

    public List<CreationRuleParameter> getParameters() {
        return parameters;
    }

    public List<CreationRuleAction> getActions() {
        return actions;
    }

    public void addParameter(String key, String value){
        if (!is(key).emptyOrOnlyWhiteSpace() && !is(value).emptyOrOnlyWhiteSpace()) {
            CreationRuleParameterImpl parameter = getDataModel().getInstance(CreationRuleParameterImpl.class);
            parameter.setKey(key);
            parameter.setValue(value);
            parameter.setRule(this);
            getParameters().add(parameter);
        }
    }

    @Override
    public CreationRuleAction addAction(IssueActionType type, CreationRuleActionPhase phase){
        if (type == null || phase == null) {
            throw new IllegalArgumentException("Type and phase parameters for rule action can't be null");
        }
        CreationRuleActionImpl action = getDataModel().getInstance(CreationRuleActionImpl.class);
        action.setRule(this);
        action.setPhase(phase);
        action.setType(type);
        getActions().add(action);
        return action;
    }

    @Override
    public void updateContent() {
        if(getId() == 0){
            throw new IllegalStateException("You can't call updateContent for CreationRule before it will be saved");
        }
        CreationRuleTemplate template = getTemplate();
        if (template != null) {
            String rawContent = template.getContent();
            for (CreationRuleParameter parameter : parameters) {
                rawContent = replaceParameterInConetent(rawContent, parameter.getKey(), parameter.getValue());
            }
            rawContent = replaceParameterInConetent(rawContent, PARAM_RULE_ID, getId());
            setContent(rawContent);
        }
    }

    private String replaceParameterInConetent(String source, String key, Object value){
        String result = source;
        key = "@{" + key + "}";
        if (result != null && result.contains(key)) {
            result = result.replace(key, String.valueOf(value));
        }
        return result;
    }

    @Override
    public void delete() {
        Condition condition = where("rule").isEqualTo(this);
        List<Issue> referencedIssues = issueService.query(Issue.class).select(condition);
        if (referencedIssues.size() == 0){
            super.delete(); // delete from table
        } else {
            this.setObsoleteTime(Instant.now()); // mark obsolete
            this.save();
        }
    }

    @Override
    public void validate() {
        CreationRuleOrActionValidationException exception = new CreationRuleOrActionValidationException(thesaurus, MessageSeeds.ISSUE_CREATION_RULE_VALIDATION_FAILED);

        Validator validator = getDataModel().getValidatorFactory().getValidator();
        for (ConstraintViolation<?> violation : validator.validate(this)) {
            exception.addError(violation.getPropertyPath().toString(), violation.getMessage(), IssueService.COMPONENT_NAME);
        }

        CreationRuleTemplate template = getTemplate();
        exception.addErrors(template.validate(this));

        for (CreationRuleAction action : actions) {
            IssueActionType actionType = action.getType();
            Optional<IssueAction> issueAction = actionType.createIssueAction();
            if (issueAction.isPresent()) {
                exception.addErrors(issueAction.get().validate(action));
            }
        }
        if (!exception.getErrors().isEmpty()) {
            throw exception;
        }
    }

    @Override
    public boolean validateUniqueName(boolean caseSensitive) {
        Query<CreationRule> query = this.issueService.getIssueCreationService().getCreationRuleQuery();
        return query.select(getUniqueNameWhereCondition(caseSensitive).and(where("id").isNotEqual(getId()))).isEmpty();
    }

    private Condition getUniqueNameWhereCondition(boolean caseSensitive) {
        return caseSensitive ? where("name").isEqualTo(this.getName()) : where("name").isEqualToIgnoreCase(this.getName());
    }
}
