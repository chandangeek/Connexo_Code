package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleProperty;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleUpdater;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.HasValidProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@HasUniqueName(message = "{" + MessageSeeds.Keys.CREATION_RULE_UNIQUE_NAME + "}")
@HasValidProperties(requiredPropertyMissingMessage = "{" + MessageSeeds.Keys.PROPERTY_MISSING + "}",
                    propertyNotInSpecMessage = "{" + MessageSeeds.Keys.PROPERTY_NOT_IN_PROPERTYSPECS + "}")
public class CreationRuleImpl extends EntityImpl implements CreationRule {
    
    private static final String PARAM_RULE_ID = "ruleId";

    @NotEmpty(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    private String comment;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String content = "no content";
    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<IssueType>  issueType = ValueReference.absent();//transient in fact - needed for form validation
    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<IssueReason> reason = ValueReference.absent();
    private long dueInValue;
    private DueInType dueInType;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private String template;//creation rule template class name
    private Instant obsoleteTime;
    
    @Valid
    private List<CreationRuleProperty> properties = new ArrayList<>();
    @Valid
    private List<CreationRuleAction> actions = new ArrayList<>();//for validation
    private List<CreationRuleAction> persistentActions = new ArrayList<>();
    
    private final IssueService issueService;

    @Inject
    public CreationRuleImpl(DataModel dataModel, IssueService issueService) {
        super(dataModel);
        this.issueService = issueService;
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public String getComment() {
        return comment;
    }

    void setComment(String comment) {
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

    private void setContent(String content) {
        this.content = content;
    }

    @Override
    public IssueReason getReason() {
        return reason.get();
    }

    void setReason(IssueReason reason) {
        this.reason.set(reason);
    }

    @Override
    public IssueType getIssueType() {
        return getReason().getIssueType();
    }

    void setIssueType(IssueType issueType) {
        this.issueType.set(issueType);
    }

    @Override
    public long getDueInValue() {
        return dueInValue;
    }

    void setDueInValue(long dueInValue) {
        this.dueInValue = dueInValue;
    }

    @Override
    public DueInType getDueInType() {
        return dueInType;
    }

    void setDueInType(DueInType dueInType) {
        this.dueInType = dueInType;
    }

    @Override
    public String getTemplateImpl() {
        return template;
    }

    @Override
    public CreationRuleTemplate getTemplate() {
        return issueService.getIssueCreationService().findCreationRuleTemplate(getTemplateImpl()).orElse(null);
    }

    void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public Instant getObsoleteTime() {
        return obsoleteTime;
    }

    @Override
    public List<CreationRuleProperty> getCreationRuleProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public List<CreationRuleAction> getActions() {
        return Collections.unmodifiableList(persistentActions);
    }

    CreationRuleProperty addProperty(String name, Object value) {
        CreationRuleProperty newProperty = getDataModel().getInstance(CreationRulePropertyImpl.class).init(this, name, value);
        properties.add(newProperty);
        return newProperty;
    }

    void addAction(CreationRuleAction action) {
        actions.add(action);
    }

    @Override
    public boolean validateUniqueName(boolean caseSensitive) {
        Query<CreationRule> query = this.issueService.getIssueCreationService().getCreationRuleQuery();
        return query.select(getUniqueNameWhereCondition(caseSensitive).and(where("id").isNotEqual(getId()))).isEmpty();
    }

    private Condition getUniqueNameWhereCondition(boolean caseSensitive) {
        return caseSensitive ? where("name").isEqualTo(this.getName()) : where("name").isEqualToIgnoreCase(this.getName());
    }
    
    @Override
    public List<PropertySpec> getPropertySpecs() {
        CreationRuleTemplate template = getTemplate();
        return template != null ? template.getPropertySpecs() : Collections.emptyList();
    }
    
    @Override
    public CreationRuleUpdater startUpdate() {
        return new CreationRuleUpdaterImpl(getDataModel(), this);
    }
    
    @Override
    public Map<String, Object> getProperties() {
        return properties.stream().collect(Collectors.toMap(CreationRuleProperty::getName, CreationRuleProperty::getValue));
    }
    
    void setProperties(Map<String, Object> propertyMap) {
        Map<String, CreationRuleProperty> originalProps = properties.stream().collect(Collectors.toMap(CreationRuleProperty::getName, Function.identity()));
        DiffList<String> entryDiff = ArrayDiffList.fromOriginal(originalProps.keySet());
        entryDiff.clear();
        entryDiff.addAll(propertyMap.keySet());

        for (String property : entryDiff.getRemovals()) {
            CreationRuleProperty creationRuleProperty = originalProps.get(property);
            properties.remove(creationRuleProperty);
        }

        for (String property : entryDiff.getRemaining()) {
            CreationRuleProperty creationRuleProperty = originalProps.get(property);
            creationRuleProperty.setValue(propertyMap.get(property));
            getDataModel().mapper(CreationRuleProperty.class).update(creationRuleProperty);
        }

        for (String property : entryDiff.getAdditions()) {
            addProperty(property, propertyMap.get(property));
        }
    }
    
    void removeActions() {
        persistentActions.clear();
        actions.clear();
    }
    
    @Override
    public void save() {
        if (this.getCreateTime() == null) {
            Save.CREATE.save(getDataModel(), this);
        }
        updateIssueType();
        updateContent();
        Save.UPDATE.save(getDataModel(), this);
        persistentActions.addAll(actions);
        issueService.getIssueCreationService().reReadRules();
    }
    
    @Override
    public void delete() {
        Condition condition = where("rule").isEqualTo(this);
        List<Issue> referencedIssues = issueService.query(Issue.class).select(condition);
        if (referencedIssues.size() == 0) {
            super.delete(); // delete from table
        } else {
            this.setObsoleteTime(Instant.now()); // mark obsolete
            this.save();
        }
        issueService.getIssueCreationService().reReadRules();
    }

    @SuppressWarnings("unchecked")
    private void updateContent() {
        CreationRuleTemplate template = getTemplate();
        if (template == null ){
            return;
        }
        String rawContent = template.getContent();
        for (CreationRuleProperty property : properties) {
            PropertySpec propertySpec = getPropertySpec(property.getName());
            rawContent = replaceParameterInContent(rawContent, property.getName(), propertySpec.getValueFactory().toStringValue(property.getValue()));
        }
        rawContent = replaceParameterInContent(rawContent, PARAM_RULE_ID, String.valueOf(getId()));
        setContent(rawContent);
    }

    private void updateIssueType() {
        this.reason.getOptional().ifPresent(reason -> this.setIssueType(reason.getIssueType()));
    }

    private String replaceParameterInContent(String source, String key, String value) {
        String result = source;
        key = "@{" + key + "}";
        if (result != null && result.contains(key)) {
            result = result.replace(key, value);
        }
        return result;
    }

    private void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }
}
