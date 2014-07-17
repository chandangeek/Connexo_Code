package com.elster.jupiter.issue.datacollection.templates.params;

import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.cep.*;
import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventTypeParameter extends TranslatedParameter {

    private static class EventTypeParameterConstraint implements ParameterConstraint{
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
            throw new IllegalAccessError("This method shouldn't be called!");
        }

        public List<ParameterViolation> validate(String value, String paramKey, MeteringService meteringService) {
            List<ParameterViolation> errors = new ArrayList<ParameterViolation>();
            if (value == null){
                errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_ABSENT.getKey(), ModuleConstants.COMPONENT_NAME, value));
                return errors;
            }
            if (!validateValueInEventDescriptions(value) && !validateValueInEndDeviceEventTypes(meteringService, value)){
                errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_INCORRECT.getKey(), ModuleConstants.COMPONENT_NAME, value));
                return errors;
            }
            return errors;
        }

        private boolean validateValueInEventDescriptions(String value) {
            for (DataCollectionEventDescription eventDescription : DataCollectionEventDescription.values()) {
                if (eventDescription.getErrorType() == null){
                    continue;
                }
                if (eventDescription.getTopic().equalsIgnoreCase(value)) {
                    return true;
                }
            }
            return false;
        }


        private boolean validateValueInEndDeviceEventTypes(MeteringService meteringService, String value) {
            // TODO search in DB and set return value to FALSE by default
            return true;
        }
    }

    private static final EventTypeParameterConstraint CONSTRAINT = new EventTypeParameterConstraint();

    private List<Object> eventTypes = new ArrayList<>();
    private final MeteringService meteringService;
    private String defaultValue = "";

    public EventTypeParameter(Thesaurus thesaurus, MeteringService meteringService) {
        super(thesaurus);
        this.meteringService = meteringService;
        setEventTypes(" "); // TODO remove when live seacrh wull be supported
    }

    private void setEventTypes(String userValue){
        searchEventTypesInEventDescriptions(userValue);
        searchEventTypesInEndDeviceEventTypes(userValue);
    }

    private void setDefaultValue(String userValue){
        defaultValue = userValue;
    }

    private void searchEventTypesInEventDescriptions(String userValue) {
        for (DataCollectionEventDescription eventDescription : DataCollectionEventDescription.values()) {
            if (eventDescription.getErrorType() == null){
                continue;
            }
            String title = getString(eventDescription.getTitle());
            if (title.contains(userValue)) {
                ComboBoxControl.Values info = new ComboBoxControl.Values();
                info.id = eventDescription.getTopic();
                info.title = getString(eventDescription.getTitle());
                eventTypes.add(info);
            }
        }
    }

    private void searchEventTypesInEndDeviceEventTypes(String userValue){
        String dbSearchString = "%" + userValue + "%";
        /*
        TODO search in DB
        getAvailableEndDeviceEventTypes() retruns more than 10000 records! So we need to have an ability to search
        EndDeviceEventTypes by name / id
        for(EndDeviceEventType endDeviceEventType : meteringService.getAvailableEndDeviceEventTypes()) {
            ComboBoxControl.Values info = new ComboBoxControl.Values();
            info.id = endDeviceEventType.getMRID();
            info.title = endDeviceEventType.getName();
            eventTypes.add(info);
        }
        For now we use only this two, see the
        http://confluence.eict.vpdc/display/JUP/Create+an+issue+for+the+event%2C+mapping+of+event+to+issue?focusedCommentId=26674337#comment-26674337
        */
        ComboBoxControl.Values info = new ComboBoxControl.Values();
        info.id = "0.36.116.85";
        info.title = "Time sync failed";
        eventTypes.add(info);

        info = new ComboBoxControl.Values();
        info.id = "0.26.0.85";
        info.title = "Power Outage";
        eventTypes.add(info);
    }


    @Override
    public boolean isDependent() {
        return true;
    }

    @Override
    public ParameterDefinition getValue(Map<String, Object> parameters) {
        Object incomingValue = parameters.get(this.getKey());
        if (incomingValue != null) {
            String stringIncomingValue = (String) incomingValue;
            EventTypeParameter definition = new EventTypeParameter(getThesaurus(), meteringService);
            definition.setEventTypes(stringIncomingValue);
            definition.setDefaultValue(defaultValue);
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
        return CONSTRAINT.validate(value, context.wrapKey(getKey()), meteringService);
    }
}