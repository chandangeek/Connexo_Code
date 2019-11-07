package com.elster.jupiter.issue.impl.actions.webelements.factories;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.issue.share.entity.values.ProcessValue;
import com.elster.jupiter.issue.share.entity.values.factories.ProcessValueFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;

import javax.inject.Inject;
import java.util.List;

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
                .filter(this::filterByIssueTypeIfPossible)
                .filter(this::filterByIssueReasonIfPossible)
                .map(ProcessValue::new)
                .toArray(ProcessValue[]::new);
    }

    private boolean filterByIssueReasonIfPossible(final BpmProcessDefinition processDefinition) {
        if (issueReason == null) {
            return true;
        }

        final List<HasIdAndName> issueReasons = (List<HasIdAndName>) processDefinition.getProperties().get("issueReasons");

        if (issueReasons == null) {
            return true;
        }

        return issueReasons.stream().anyMatch(issueReasonInfo -> issueReasonInfo.getName().equals(issueReason.getName()));
    }

    private boolean filterByIssueTypeIfPossible(final BpmProcessDefinition processDefinition) {
        if (issueType == null) {
            return true;
        }
        return processDefinition.getAssociation().contains(issueType.getKey());
    }

    @Override
    public PropertyType getType() {
        return PropertyType.PROCESS_COMBOBOX;
    }

}
