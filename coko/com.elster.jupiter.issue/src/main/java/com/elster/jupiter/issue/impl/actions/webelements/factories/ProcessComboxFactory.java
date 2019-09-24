package com.elster.jupiter.issue.impl.actions.webelements.factories;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.issue.share.entity.values.ProcessValue;
import com.elster.jupiter.issue.share.entity.values.factories.ProcessValueFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;

import javax.inject.Inject;

public class ProcessComboxFactory extends DropdownFactory<ProcessValue> {

    private final BpmService bpmService;

    @Inject
    protected ProcessComboxFactory(final PropertySpecService propertySpecService, final Thesaurus thesaurus, final BpmService bpmService) {
        super(propertySpecService, thesaurus);
        this.bpmService = bpmService;
    }

    @Override
    ValueFactory<ProcessValue> getValueFactory() {
        return new ProcessValueFactory(bpmService);
    }

    @Override
    ProcessValue[] getPossibleValues() {
        return bpmService.getActiveBpmProcessDefinitions()
                .stream()
                .filter(bpmProcessDefinition -> bpmProcessDefinition.getAssociation().equals("datacollectionissue"))
                .map(ProcessValue::new)
                .toArray(ProcessValue[]::new);
    }

    @Override
    public PropertyType getType() {
        return PropertyType.PROCESS_COMBOBOX;
    }
}
