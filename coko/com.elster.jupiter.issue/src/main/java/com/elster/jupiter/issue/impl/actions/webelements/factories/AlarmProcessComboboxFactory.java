package com.elster.jupiter.issue.impl.actions.webelements.factories;

import javax.inject.Inject;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.issue.share.entity.values.ProcessValue;
import com.elster.jupiter.issue.share.entity.values.factories.ProcessValueFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;

public class AlarmProcessComboboxFactory extends DropdownFactory<ProcessValue> {

    public static final String DEVICE_ALARM_ASSOCIATION = "devicealarm";
    private final BpmService bpmService;

    @Inject
    protected AlarmProcessComboboxFactory(final PropertySpecService propertySpecService, final Thesaurus thesaurus,
            final BpmService bpmService) {
        super(propertySpecService, thesaurus);
        this.bpmService = bpmService;
    }

    @Override
    ValueFactory<ProcessValue> getValueFactory() {
        return new ProcessValueFactory(bpmService);
    }

    @Override
    ProcessValue[] getPossibleValues() {
        return bpmService.getActiveBpmProcessDefinitions().stream()
                .filter(bpmProcessDefinition -> bpmProcessDefinition.getAssociation().equals(DEVICE_ALARM_ASSOCIATION))
                .map(ProcessValue::new).toArray(ProcessValue[]::new);
    }

    @Override
    public PropertyType getType() {
        return PropertyType.ALARM_PROCESS_COMBOBOX;
    }

}
