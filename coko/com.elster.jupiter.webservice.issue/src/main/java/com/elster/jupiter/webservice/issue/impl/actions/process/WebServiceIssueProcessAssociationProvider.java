/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.actions.process;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.BpmProcessPropertyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;

import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;


@Component(name = "WebServiceIssueProcessAssociationProvider",
        service = {ProcessAssociationProvider.class, TranslationKeyProvider.class},
        property = "name=WebServiceIssueProcessAssociationProvider", immediate = true)
public class WebServiceIssueProcessAssociationProvider implements ProcessAssociationProvider, TranslationKeyProvider {
    public static final String APP_KEY = "MDC";
    public static final String COMPONENT_NAME = "BPM";
    public static final String ASSOCIATION_TYPE = "webserviceissue";
    public static final String PROPERTY_REASONS = "issueReasons";

    private volatile License license;
    private volatile Thesaurus thesaurus;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;

    public WebServiceIssueProcessAssociationProvider() {
        // For OSGi purposes
    }

    // For testing purposes
    @Inject
    public WebServiceIssueProcessAssociationProvider(Thesaurus thesaurus, IssueService issueService, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.issueService = issueService;
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.WEBSERVICE_ISSUE_ASSOCIATION_PROVIDER).format();
    }

    @Override
    public String getType() {
        return ASSOCIATION_TYPE;
    }

    @Override
    public String getAppKey() {
        return APP_KEY;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getWebServiceIssueReasonsPropertySpec());
        return builder.build();
    }

    @Override
    public Optional<PropertySpec> getPropertySpec(String name) {
        return (TranslationKeys.WEBSERVICE_ISSUE_REASON_TITLE.getKey().equals(name)) ? Optional.of(getWebServiceIssueReasonsPropertySpec()) : Optional.empty();
    }

    private PropertySpec getWebServiceIssueReasonsPropertySpec() {
        IssueType issueType = issueService.findIssueType(WebServiceIssueService.ISSUE_TYPE_NAME).orElse(null);

        IssueReasonInfo[] possibleValues = issueService.query(IssueReason.class)
                .select(where("issueType").isEqualTo(issueType))
                .stream()
                .map(IssueReasonInfo::new)
                .sorted(Comparator.comparing(IssueReasonInfo::getName, String.CASE_INSENSITIVE_ORDER))
                .toArray(IssueReasonInfo[]::new);

        return this.propertySpecService
                .specForValuesOf(new IssueReasonInfoValuePropertyFactory())
                .named(PROPERTY_REASONS, TranslationKeys.WEBSERVICE_ISSUE_REASON_TITLE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .markMultiValued(",")
                .addValues(possibleValues)
                .markExhaustive(PropertySelectionMode.LIST)
                .finish();
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @XmlRootElement
    static class IssueReasonInfo extends HasIdAndName {
        private transient IssueReason issueReason;

        IssueReasonInfo(IssueReason issueReason) {
            this.issueReason = issueReason;
        }

        @Override
        public String getId() {
            return issueReason.getKey();
        }

        @Override
        public String getName() {
            return issueReason.getName();
        }

        @Override
        public boolean equals(Object o) {
            return this == o
                    || o instanceof IssueReasonInfo
                    && super.equals(o)
                    && issueReason.getId() == ((IssueReasonInfo) o).issueReason.getId();
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + issueReason.getKey().hashCode();
            return result;
        }
    }

    private class IssueReasonInfoValuePropertyFactory implements ValueFactory<HasIdAndName>, BpmProcessPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return issueService
                    .findReason(stringValue)
                    .map(IssueReasonInfo::new)
                    .orElse(null);
        }

        @Override
        public String toStringValue(HasIdAndName object) {
            return String.valueOf(object.getId());
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
}
