package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class CreationRuleActionTypeInfo {
    private long id;
    private String name;
    private IssueTypeInfo issueType;
    @XmlJavaTypeAdapter(ParametersAdapter.class)
    private Map<String, ParameterInfo> parameters;

    public CreationRuleActionTypeInfo(){}

    public CreationRuleActionTypeInfo(IssueActionType type) {
        if (type == null) {
            throw new IllegalArgumentException("CreationRuleActionTypeInfo is initialized with the null IssueActionType value");
        }
        this.id = type.getId();
        IssueAction action = type.createIssueAction();
        this.name = action.getLocalizedName();
        this.issueType = new IssueTypeInfo(type.getIssueType());
        initParameters(action);
    }

    private final void initParameters(IssueAction action) {
        if (!action.getParameterDefinitions().isEmpty()) {
            parameters = new LinkedHashMap<>();
        }
        for (Map.Entry<String, ParameterDefinition> parameter : action.getParameterDefinitions().entrySet()) {
            parameters.put(parameter.getKey(), new ParameterInfo(parameter.getValue()));
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public IssueTypeInfo getIssueType() {
        return issueType;
    }

    public Map<String, ParameterInfo> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    /**
     * Workaround for specific case. We don't want to receive the parameters description from front-end
     * when user saves a creation rule with actions. @JsonIgnore on setter doesn't work.
     */
    public static class ParametersAdapter extends XmlAdapter<Object, Map<String, ParameterInfo>> {
        @Override
        public Map<String, ParameterInfo> unmarshal(Object jsonValue) throws Exception {
            return null;
        }
        @Override
        public Map<String, ParameterInfo> marshal(Map<String, ParameterInfo> parameter) throws Exception {
            return parameter;
        }
    }

}