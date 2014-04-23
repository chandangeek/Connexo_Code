package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.conditions.Where.where;

public class CreationRuleImpl extends EntityImpl implements CreationRule {
    private static final String PARAM_RULE_ID = "ruleId";

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min=1, max = 256, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_256 + "}")
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
    private UtcInstant obsoleteTime;

    private List<CreationRuleParameter> parameters = new ArrayList<>();
    private List<CreationRuleAction> actions = new ArrayList<>();

    private IssueService issueService;
    private IssueCreationService issueCreationService;

    @Inject
    public CreationRuleImpl(DataModel dataModel, IssueService issueService, IssueCreationService issueCreationService) {
        super(dataModel);
        this.issueService = issueService;
        this.issueCreationService = issueCreationService;
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
        return issueCreationService.findCreationRuleTemplate(getTemplateUuid()).orNull();
    }

    @Override
    public void setTemplateUuid(String templateName) {
        this.templateUuid = templateName;
    }

    @Override
    public UtcInstant getObsoleteTime() {
        return obsoleteTime;
    }

    @Override
    public void setObsoleteTime(UtcInstant obsoleteTime) {
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
    public CreationRuleAction addAction(CreationRuleActionType type, CreationRuleActionPhase phase){
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
        List<BaseIssue> referencedIssues = issueService.query(BaseIssue.class).select(condition);
        if (referencedIssues.size() == 0){
            super.delete(); // delete from table
        } else {
            this.setObsoleteTime(new UtcInstant(new Date())); // mark obsolete
            this.update();
        }
    }
}
