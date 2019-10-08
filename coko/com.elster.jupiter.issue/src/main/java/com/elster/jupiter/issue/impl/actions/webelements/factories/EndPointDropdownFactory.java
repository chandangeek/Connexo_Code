package com.elster.jupiter.issue.impl.actions.webelements.factories;

import com.elster.jupiter.issue.share.entity.values.EndPointValue;
import com.elster.jupiter.issue.share.entity.values.factories.EndPointValueFactory;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import javax.inject.Inject;

public class EndPointDropdownFactory extends DropdownFactory<EndPointValue> {

    private final IssueService issueService;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public EndPointDropdownFactory(final PropertySpecService propertySpecService, final IssueService issueService, final Thesaurus thesaurus, final EndPointConfigurationService endPointConfigurationService) {
        super(propertySpecService, thesaurus);
        this.issueService = issueService;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    ValueFactory<EndPointValue> getValueFactory() {
        return new EndPointValueFactory(endPointConfigurationService);
    }

    @Override
    EndPointValue[] getPossibleValues() {
        return endPointConfigurationService.findEndPointConfigurations().stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration ->
                        ((IssueServiceImpl) issueService).getIssueWebServiceClients().stream()
                                .anyMatch(issueWebServiceClient1 -> issueWebServiceClient1.getWebServiceName().compareTo(endPointConfiguration.getWebServiceName()) == 0))
                .map(EndPointValue::new).toArray(EndPointValue[]::new);
    }

    @Override
    public PropertyType getType() {
        return PropertyType.ENDPOINT_COMBOBOX;
    }
}
