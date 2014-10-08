package com.energyict.mdc.issue.datacollection.impl.templates.params;

import com.elster.jupiter.issue.share.cep.*;
import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataCollectionEventsParameter extends TranslatedParameter {

    private static class DataCollectionEventsConstraint implements ParameterConstraint {
        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public String getRegexp() {
            return null;
        }

        @Override
        public Integer getMin() {
            return null;
        }

        @Override
        public Integer getMax() {
            return null;
        }

        @Override
        public List<ParameterViolation> validate(String value, String paramKey) {
            List<ParameterViolation> errors = new ArrayList<ParameterViolation>();
            if (value == null) {
                errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_ABSENT.getKey(), IssueDataCollectionService.COMPONENT_NAME));
                return errors;
            }
            if (!validateValueInEventDescriptions(value)) {
                errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_INCORRECT.getKey(), IssueDataCollectionService.COMPONENT_NAME, value));
                return errors;
            }
            return errors;
        }

        private boolean validateValueInEventDescriptions(String value) {
            for (DataCollectionEventDescription eventDescription : DataCollectionEventDescription.values()) {
                if (eventDescription.getErrorType() == null) {
                    continue;
                }
                if (eventDescription.getTopic().equalsIgnoreCase(value)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final DataCollectionEventsConstraint CONSTRAINT = new DataCollectionEventsConstraint();

    private List<Object> eventTypes;
    private Object defaultValue;

    public DataCollectionEventsParameter(Thesaurus thesaurus) {
        super(thesaurus);
        setEventTypes(null);
    }

    private void setEventTypes(String userValue) {
        eventTypes = new ArrayList<>();
        searchEventTypesInEventDescriptions(userValue);
    }

    private void setDefaultValue(String userValue) {
        if (userValue != null) {
            for (Object eventType : eventTypes) {
                if (((ComboBoxControl.Values) eventType).id.equals(userValue)) {
                    defaultValue = eventType;
                }
            }
        }
    }

    private void searchEventTypesInEventDescriptions(String userValue) {
        for (DataCollectionEventDescription eventDescription : DataCollectionEventDescription.values()) {
            if (eventDescription.getErrorType() == null) {
                continue;
            }
            String title = getString(eventDescription.getTitle());
            // TODO remove 'true' when search will be implemented
            if (userValue != null && title.contains(userValue) || true) {
                ComboBoxControl.Values info = new ComboBoxControl.Values();
                info.id = eventDescription.getTopic();
                info.title = getString(eventDescription.getTitle());
                eventTypes.add(info);
            }
        }
    }

    @Override
    public List<String> getDependOn() {
        return Collections.singletonList(getKey());
    }

    @Override
    public ParameterDefinition getValue(Map<String, Object> parameters) {
        Object incomingValue = parameters.get(this.getKey());
        if (incomingValue != null) {
            String stringIncomingValue = (String) incomingValue;
            DataCollectionEventsParameter definition = new DataCollectionEventsParameter(getThesaurus());
            definition.setEventTypes(stringIncomingValue);
            definition.setDefaultValue(stringIncomingValue);
            return definition;
        }
        return super.getValue(parameters);
    }

    @Override
    public String getKey() {
        return "eventType";
    }

    @Override
    public ParameterControl getControl() {
        return ComboBoxControl.COMBOBOX;
    }

    @Override
    public ParameterConstraint getConstraint() {
        return CONSTRAINT;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public List<Object> getDefaultValues() {
        return eventTypes;
    }

    @Override
    public String getLabel() {
        return getString(MessageSeeds.PARAMETER_NAME_EVENT_TYPE);
    }

    @Override
    public List<ParameterViolation> validate(String value, ParameterDefinitionContext context) {
        return CONSTRAINT.validate(value, context.wrapKey(getKey()));
    }
}