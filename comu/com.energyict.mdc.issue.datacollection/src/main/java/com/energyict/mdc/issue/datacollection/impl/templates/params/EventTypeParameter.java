package com.energyict.mdc.issue.datacollection.impl.templates.params;

import com.elster.jupiter.issue.share.cep.*;
import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EventTypeParameter extends TranslatedParameter {

    private static final EventTypeParameterConstraint CONSTRAINT = new EventTypeParameterConstraint();
    public static final String EVENT_TYPE_PARAMETER_KEY = "eventType";

    private List<Object> eventTypes;
    private final MeteringService meteringService;
    private Object defaultValue;
    private boolean isAggregation;

    public EventTypeParameter(boolean isAggregation, Thesaurus thesaurus, MeteringService meteringService) {
        super(thesaurus);
        this.meteringService = meteringService;
        this.isAggregation = isAggregation;
        setEventTypes(null);
    }

    private void setEventTypes(String userInput) {
        eventTypes = new ArrayList<>();
        searchEventTypesInEventDescriptions(userInput);
        searchEventTypesInEndDeviceEventTypes(userInput);
    }

    private void setDefaultValue(String userValue) {
        if (userValue != null) {
            for (Object eventType : eventTypes) {
                if (((ComboBoxControl.Values) eventType).id.equals(userValue)) {
                    defaultValue = eventType;
                    break;
                }
            }
        }
    }

    private void searchEventTypesInEventDescriptions(String userValue) {
        for (DataCollectionEventDescription eventDescription : DataCollectionEventDescription.values()) {
            // Always skip the Meter event because it will be replaced by set of end device event types
            // or skip event type which doesn't support the aggregation (of course when we request aggregation)
            if (isAggregation && !eventDescription.canBeAggregated()
                    || DataCollectionEventDescription.METER_EVENT.equals(eventDescription)) {
                continue;
            }
            String title = getString(eventDescription.getTitle());
            // TODO remove 'true' when search will be implemented
            if (userValue == null || title.contains(userValue) || true) {
                ComboBoxControl.Values info = new ComboBoxControl.Values();
                info.id = eventDescription.getUniqueKey();
                info.title = getString(eventDescription.getTitle());
                eventTypes.add(info);
            }
        }
    }

    private void searchEventTypesInEndDeviceEventTypes(String userValue) {
        if (userValue != null) {
            /*
            //TODO search in DB
            //getAvailableEndDeviceEventTypes() retruns more than 10000 records! So we need to have an ability to search
            //EndDeviceEventTypes by name / id
            String dbSearchString = "%" + userValue + "%";
            for(EndDeviceEventType endDeviceEventType : meteringService.getAvailableEndDeviceEventTypes()) {
                ComboBoxControl.Values info = new ComboBoxControl.Values();
                info.id = endDeviceEventType.getMRID();
                info.title = endDeviceEventType.getName();
                eventTypes.add(info);
            }
            //For now we use only this two, see the
            //http://confluence.eict.vpdc/display/JUP/Create+an+issue+for+the+event%2C+mapping+of+event+to+issue?focusedCommentId=26674337#comment-26674337
            */
            addDefaultEventTypes();
        } else {
            addDefaultEventTypes();
        }
    }

    private void addDefaultEventTypes() {
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
    public List<String> getDependOn() {
        return Collections.singletonList(getKey());
    }

    @Override
    public ParameterDefinition getValue(Map<String, Object> parameters) {
        Object incomingValue = parameters.get(this.getKey());
        if (incomingValue != null) {
            String stringIncomingValue = (String) incomingValue;
            EventTypeParameter definition = new EventTypeParameter(isAggregation, getThesaurus(), meteringService);
            definition.setEventTypes(stringIncomingValue);
            definition.setDefaultValue(stringIncomingValue);
            return definition;
        }
        return super.getValue(parameters);
    }

    @Override
    public String getKey() {
        return EVENT_TYPE_PARAMETER_KEY;
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
        return CONSTRAINT.validate(value, context.wrapKey(getKey()), meteringService, isAggregation);
    }
}