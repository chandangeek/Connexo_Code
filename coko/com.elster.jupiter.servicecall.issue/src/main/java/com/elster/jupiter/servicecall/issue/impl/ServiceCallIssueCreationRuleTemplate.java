/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.ServiceCallInfoPropertyFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.issue.IssueServiceCallService;
import com.elster.jupiter.servicecall.issue.OpenIssueServiceCall;
import com.elster.jupiter.servicecall.issue.TranslationKeys;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.servicecall.issue.impl.ServiceCallIssueCreationRuleTemplate",
        property = {"name=" + ServiceCallIssueCreationRuleTemplate.NAME},
        service = CreationRuleTemplate.class, immediate = true)
public class ServiceCallIssueCreationRuleTemplate implements CreationRuleTemplate {

    static final String NAME = "ServiceCallIssueCreationRuleTemplate";

    public static final String SERVICE_CALL_CONFIGURATIONS = NAME + ".serviceCallConfigurations";

    private volatile IssueServiceCallService issueServiceCallService;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile ServiceCallService serviceCallService;
    private Optional<String> appKey = Optional.empty();

    //for OSGI
    public ServiceCallIssueCreationRuleTemplate() {
    }

    @Inject
    public ServiceCallIssueCreationRuleTemplate(IssueServiceCallService issueServiceCallService, IssueService issueService, PropertySpecService propertySpecService,
                                                ServiceCallService serviceCallService,
                                                NlsService nlsService) {
        this();
        setIssueServiceCallService(issueServiceCallService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setServiceCallService(serviceCallService);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return TranslationKeys.SERVICE_CALL_ISSUE_RULE_TEMPLATE_NAME.getTranslated(thesaurus);
    }

    @Override
    public String getDescription() {
        return TranslationKeys.SERVICE_CALL_ISSUE_RULE_TEMPLATE_DESCRIPTION.getTranslated(thesaurus);
    }

    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.datavalidation\n" +
                "import com.energyict.mdc.issue.datavalidation.impl.event.CannotEstimateDataEvent;\n" +
                "import com.energyict.mdc.issue.datavalidation.impl.event.SuspectDeletedEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Data validation rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : CannotEstimateDataEvent(deviceConfigurationId in (@{" + SERVICE_CALL_CONFIGURATIONS + "}))\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by datavalidation rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n" +
                "\n" +
                "rule \"Autoresolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent: SuspectDeletedEvent(deviceConfigurationId in (@{" + SERVICE_CALL_CONFIGURATIONS + "}))\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to resolve issue by datavalidation rule [id = @{ruleId}]\");\n" +
                "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
                "end\n";
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueServiceCallService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIssueServiceCallService(IssueServiceCallService issueServiceCallService) {
        this.issueServiceCallService = issueServiceCallService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(
                propertySpecService.specForValuesOf(new ServiceCallInfoValueFactory())
                .named(TranslationKeys.SERVICE_CALL_TYPE_STATE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .markMultiValued(":")
                .addValues(Arrays.stream(DefaultState.values()).filter(defaultState -> !defaultState.isOpen()).map(defaultState -> new DefaultStateInfo(defaultState, thesaurus)).collect(Collectors.toList()))
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        builder.add(
                propertySpecService.specForValuesOf(new ServiceCallInfoValueFactory())
                        .named(TranslationKeys.SERVICE_CALL_TYPE_HANDLER)
                        .describedAs(TranslationKeys.SERVICE_CALL_TYPE_HANDLER_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .markMultiValued(",")
                        .addValues(serviceCallService.getServiceCallTypes().stream().map(ServiceCallTypeInfo::new).toArray(ServiceCallTypeInfo[]::new))
                        .markExhaustive(PropertySelectionMode.LIST)
                        .finish());
        return builder.build();
    }

    @Override
    public IssueType getIssueType() {
        return issueService.findIssueType(IssueServiceCallService.ISSUE_TYPE_NAME).get();
    }

    @Override
    public OpenIssueServiceCall createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        return issueServiceCallService.createIssue(baseIssue, issueEvent);
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssueServiceCall issueDataValidation = (OpenIssueServiceCall) issue.get();
            event.apply(issueDataValidation);
            if (issueDataValidation.getNotEstimatedBlocks().isEmpty()) {
                return Optional.of(issueDataValidation.close(issueService.findStatus(IssueStatus.RESOLVED).get()));
            } else {
                issueDataValidation.update();
                return Optional.of(issueDataValidation);
            }
        }
        return issue;
    }

    @Override
    public void setAppKey(String appKey) {
        this.appKey = Optional.of(appKey);
    }

    private class ServiceCallInfoValueFactory implements ValueFactory<HasIdAndName>, ServiceCallInfoPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return serviceCallService.getServiceCallTypes().stream().filter(serviceCallType -> String.valueOf(serviceCallType.getId()).equals(stringValue))
                    .map(ServiceCallTypeInfo::new).findFirst().orElse(null);
        }

        @Override
        public String toStringValue(HasIdAndName object) {
            return object.getName();
        }

        @Override
        public Class<HasIdAndName> getValueType() {
            return HasIdAndName.class;
        }

        @Override
        public HasIdAndName valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(HasIdAndName object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, HasIdAndName value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, HasIdAndName value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    @XmlRootElement
    static class DefaultStateInfo extends HasIdAndName {

        private transient DefaultState defaultState;
        private transient Thesaurus thesaurus;

        DefaultStateInfo(DefaultState defaultState, Thesaurus thesaurus) {
            this.defaultState = defaultState;
            this.thesaurus = thesaurus;
        }

        @Override
        public Long getId() {
            return (long) defaultState.ordinal();
        }

        @Override
        public String getName() {
            return defaultState.getDisplayName(thesaurus);
        }

        @Override
        public boolean equals(Object o) {
            if (this != o) {
                return false;
            }
            if (getClass() != o.getClass()) {
                return false;
            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + Long.hashCode(defaultState.ordinal());
            return result;
        }
    }

    @XmlRootElement
    static class ServiceCallTypeInfo extends HasIdAndName {

        private transient ServiceCallType serviceCallType;

        ServiceCallTypeInfo(ServiceCallType serviceCallType) {
            this.serviceCallType = serviceCallType;
        }

        @Override
        public Long getId() {
            return serviceCallType.getId();
        }

        @Override
        public String getName() {
            return serviceCallType.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this != o) {
                return false;
            }
            if (getClass() != o.getClass()) {
                return false;
            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + Long.hashCode(serviceCallType.getId());
            return result;
        }
    }
}
