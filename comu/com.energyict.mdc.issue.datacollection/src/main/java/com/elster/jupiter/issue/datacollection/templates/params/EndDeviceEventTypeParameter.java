package com.elster.jupiter.issue.datacollection.templates.params;

import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.ParameterViolation;
import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.List;

public class EndDeviceEventTypeParameter extends TranslatedParameter {
    private List<Object> eventTypes;
    private ParameterConstraint constraint;

    public EndDeviceEventTypeParameter(Thesaurus thesaurus) {
        super(thesaurus);
        defineEventTypes();
        defineConstraint();
    }

    private final void defineEventTypes() {
        eventTypes = new ArrayList<>();
        /*
        TODO
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

    private final void defineConstraint() {
        constraint = new ParameterConstraint(){
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
        return "endDeviceEventType";
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
        return getString(MessageSeeds.PARAMETER_NAME_END_DEVICE_TYPE);
    }
}
