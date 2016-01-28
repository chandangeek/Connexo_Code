package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class MetrologyTestCustomPropertySet implements CustomPropertySet<MetrologyConfiguration, MetrologyTestPersistentDomainExtension> {

    private final PropertySpecService propertySpecService;
    private final PersistenceSupport<MetrologyConfiguration, MetrologyTestPersistentDomainExtension> persistentSupport;

    public MetrologyTestCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        this.persistentSupport = new MetrologyTestPersistentSupport();
    }

    @Override
    public PersistenceSupport<MetrologyConfiguration, MetrologyTestPersistentDomainExtension> getPersistenceSupport() {
        return this.persistentSupport;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.of(EditPrivilege.LEVEL_1);
    }

    @Override
    public String getId() {
        return MetrologyTestCustomPropertySet.class.getName();
    }

    @Override
    public String getName() {
        return MetrologyTestCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<MetrologyConfiguration> getDomainClass() {
        return MetrologyConfiguration.class;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        PropertySpec nameSpec = this.propertySpecService.stringSpec()
                .named(MetrologyTestPersistentDomainExtension.Fields.NAME.javaName(), MetrologyTestPersistentDomainExtension.Fields.NAME.javaName())
                .describedAs(MetrologyTestPersistentDomainExtension.Fields.NAME.javaName())
                .markRequired()
                .finish();
        PropertySpec enhancedSupportSpec = this.propertySpecService.booleanSpec()
                .named(MetrologyTestPersistentDomainExtension.Fields.ENHANCED_SUPPORT.javaName(), MetrologyTestPersistentDomainExtension.Fields.ENHANCED_SUPPORT.javaName())
                .describedAs(MetrologyTestPersistentDomainExtension.Fields.ENHANCED_SUPPORT.javaName())
                .finish();
        return Arrays.asList(nameSpec, enhancedSupportSpec);
    }

}