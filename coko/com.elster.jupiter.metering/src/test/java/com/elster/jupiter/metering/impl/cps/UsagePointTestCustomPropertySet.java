/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.cps;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class UsagePointTestCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointTestPersistentDomainExtension> {

    private final PropertySpecService propertySpecService;
    private final PersistenceSupport<UsagePoint, UsagePointTestPersistentDomainExtension> persistentSupport;

    public UsagePointTestCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        this.persistentSupport = new UsagePointTestPersistentSupport();
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointTestPersistentDomainExtension> getPersistenceSupport() {
        return this.persistentSupport;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isVersioned() {
        return true;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.of(ViewPrivilege.LEVEL_1);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.of(EditPrivilege.LEVEL_1);
    }

    @Override
    public String getId() {
        return UsagePointTestCustomPropertySet.class.getName();
    }

    @Override
    public String getName() {
        return UsagePointTestCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.getDomainClass().getName();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        PropertySpec nameSpec = this.propertySpecService.stringSpec()
                .named(CustomPropertySetAttributes.NAME.propertyKey(), CustomPropertySetAttributes.NAME.propertyKey())
                .describedAs(CustomPropertySetAttributes.NAME.propertyKey())
                .markRequired()
                .finish();
        PropertySpec enhancedSupportSpec = this.propertySpecService.booleanSpec()
                .named(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey(), CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())
                .describedAs(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())
                .finish();
        return Arrays.asList(nameSpec, enhancedSupportSpec);
    }

}