/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.bpm.impl;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.BpmProcessPropertyFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component(name = "UsagePointProcessAssociationProvider",
        service = {ProcessAssociationProvider.class, TranslationKeyProvider.class},
        property = "name=UsagePointProcessAssociationProvider", immediate = true)
public class UsagePointProcessAssociationProvider implements ProcessAssociationProvider, TranslationKeyProvider {
    public static final String APP_KEY = "INS";
    public static final String COMPONENT_NAME = "MBP";
    public static final String ASSOCIATION_TYPE = "usagepoint";

    private volatile PropertySpecService propertySpecService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile Thesaurus thesaurus;
    private volatile License license;

    @SuppressWarnings(value = "unused")//for osgi needs
    public UsagePointProcessAssociationProvider() {
    }

    @Inject
    public UsagePointProcessAssociationProvider(PropertySpecService propertySpecService, MetrologyConfigurationService metrologyConfigurationService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + APP_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(TranslationKeys.USAGE_POINT_ASSOCIATION_PROVIDER).format();
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
        return Arrays.asList(getMetrologyConfigurationsPropertySpec(), getConnectionStatePropertySpec());
    }

    private PropertySpec getMetrologyConfigurationsPropertySpec() {
        MetrologyConfigurationInfo[] possibleValues =
                this.metrologyConfigurationService
                        .findAllMetrologyConfigurations().stream()
                        .map(MetrologyConfigurationInfo::new)
                        .sorted(Comparator.comparing(MetrologyConfigurationInfo::getName, String.CASE_INSENSITIVE_ORDER))
                        .toArray(MetrologyConfigurationInfo[]::new);

        return this.propertySpecService
                .specForValuesOf(new MetrologyConfigurationInfoValuePropertyFactory())
                .named(TranslationKeys.METROLOGY_CONFIGURATION_PROPERTY.getKey(), TranslationKeys.METROLOGY_CONFIGURATION_PROPERTY)
                .fromThesaurus(this.thesaurus)
                .addValues(possibleValues)
                .markMultiValued(",")
                .markExhaustive(PropertySelectionMode.LIST)
                .finish();
    }

    private PropertySpec getConnectionStatePropertySpec() {
        ConnectionStateInfo[] possibleValues =
                Arrays.asList(ConnectionState.CONNECTED, ConnectionState.PHYSICALLY_DISCONNECTED, ConnectionState.LOGICALLY_DISCONNECTED)
                        .stream()
                        .map(s -> new ConnectionStateInfo(s, thesaurus))
                        .toArray(ConnectionStateInfo[]::new);

        return this.propertySpecService
                .specForValuesOf(new ConnectionStateInfoValuePropertyFactory())
                .named(TranslationKeys.CONNECTION_STATE_PROPERTY.getKey(), TranslationKeys.CONNECTION_STATE_PROPERTY)
                .fromThesaurus(this.thesaurus)
                .addValues(possibleValues)
                .markRequired()
                .markMultiValued(",")
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

    class MetrologyConfigurationInfoValuePropertyFactory extends AbstractValueFactory<HasIdAndName> implements BpmProcessPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return metrologyConfigurationService
                    .findMetrologyConfiguration(Long.parseLong(stringValue))
                    .map(MetrologyConfigurationInfo::new)
                    .orElse(null);
        }

        @Override
        public String toStringValue(HasIdAndName object) {
            return String.valueOf(object.getId());
        }

        @Override
        protected int getJdbcType() {
            return Types.VARCHAR;
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
    }

    @XmlRootElement
    private static class MetrologyConfigurationInfo extends HasIdAndName {

        private MetrologyConfiguration metrologyConfiguration;

        public MetrologyConfigurationInfo(MetrologyConfiguration metrologyConfiguration) {
            this.metrologyConfiguration = metrologyConfiguration;
        }

        @Override
        public Object getId() {
            return metrologyConfiguration.getId();
        }

        @Override
        public String getName() {
            return metrologyConfiguration.getName();
        }
    }

    @XmlRootElement
    private static class ConnectionStateInfo extends HasIdAndName {

        private ConnectionState connectionState;
        private Thesaurus thesaurus;

        public ConnectionStateInfo(ConnectionState connectionState, Thesaurus thesaurus) {
            this.connectionState = connectionState;
            this.thesaurus = thesaurus;
        }

        @Override
        public Object getId() {
            return connectionState.getId();
        }

        @Override
        public String getName() {
            return thesaurus.getFormat(connectionState).format();
        }
    }

    class ConnectionStateInfoValuePropertyFactory extends AbstractValueFactory<HasIdAndName> implements BpmProcessPropertyFactory {
        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return Arrays.stream(ConnectionState.values())
                    .filter(s -> s.getId().equals(stringValue))
                    .findFirst()
                    .map(s -> new ConnectionStateInfo(s, thesaurus))
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
        protected int getJdbcType() {
            return Types.VARCHAR;
        }
    }
}
