package com.elster.jupiter.issue.datacollection.templates.params;

import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.ParameterViolation;
import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.List;

public class IssueEventTopicParameter extends TranslatedParameter {

    private ParameterConstraint constraint;
    private List<Object> eventTypes;

    public IssueEventTopicParameter(Thesaurus thesaurus) {
        super(thesaurus);
        eventTypes = new ArrayList<>();
        for (DataCollectionEventDescription eventDescription : DataCollectionEventDescription.values()) {
            if (eventDescription.equals(DataCollectionEventDescription.DEVICE_EVENT)){
                continue;
            }
            ComboBoxControl.Values info = new ComboBoxControl.Values();
            info.id = eventDescription.getTopic();
            info.title = getString(eventDescription.getTitle());
            eventTypes.add(info);
        }
        defineConstraint();
    }

    private final void defineConstraint() {
        constraint = new ParameterConstraint() {
            @Override
            public boolean isOptional() { return false; }

            @Override
            public String getRegexp() { return null; }

            @Override
            public Integer getMin() { return null; }

            @Override
            public Integer getMax() { return null; }

            @Override
            public List<ParameterViolation> validate(String value, String paramKey) {
                List<ParameterViolation> errors = new ArrayList<ParameterViolation>();
                if (value == null){
                    errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_ABSENT.getKey(), ModuleConstants.COMPONENT_NAME, value));
                    return errors;
                }
                boolean matched = false;
                for (Object eventType : eventTypes) {
                    if (value.equalsIgnoreCase(((ComboBoxControl.Values) eventType).id.toString())){
                        matched = true;
                        break;
                    }
                }
                if (!matched){
                    errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_INCORRECT.getKey(), ModuleConstants.COMPONENT_NAME, value));
                    return errors;
                }
                return errors;
            }
        };
    }

    @Override
    public String getKey() {
        return "eventTopic";
    }

    @Override
    public ParameterControl getControl() {
        return ComboBoxControl.COMBOBOX;
    }

    @Override
    public List<Object> getDefaultValues() {
        return eventTypes;
    }

    @Override
    public Object getDefaultValue() {
        return eventTypes.get(0);
    }

    @Override
    public ParameterConstraint getConstraint() {
        return constraint;
    }

    @Override
    public String getLabel() {
        return getString(MessageSeeds.PARAMETER_NAME_EVENT_TOPIC);
    }
}
