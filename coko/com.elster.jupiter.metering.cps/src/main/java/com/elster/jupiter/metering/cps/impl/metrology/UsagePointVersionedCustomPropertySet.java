package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class UsagePointVersionedCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointVersionedPersistentDomainExtension> {
    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    public UsagePointVersionedCustomPropertySet() {
    }

    public UsagePointVersionedCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointVersionedPersistentDomainExtension> getPersistenceSupport() {
        return new UsagePointVersionedPersistentSupport(thesaurus);
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
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.of(EditPrivilege.LEVEL_1);
    }

    @Override
    public String getId() {
        return "c.e.j.m.cps.impl.metrology.UsagePointVersionedCustomPropertySet";
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(TranslationKeys.VERSIONED_CPS_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        PropertySpec nameSpec = this.propertySpecService.stringSpec()
                .named(UsagePointSimplePersistentDomainExtension.Fields.NAME.javaName(), TranslationKeys.CPS_PROPERTIES_NAME)
                .describedAs(TranslationKeys.CPS_PROPERTIES_NAME_DESCRIPTION)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish();
        PropertySpec enhancedSupportSpec = this.propertySpecService.booleanSpec()
                .named(UsagePointSimplePersistentDomainExtension.Fields.ENHANCED_SUPPORT.javaName(), TranslationKeys.CPS_PROPERTIES_ENHANCED_SUPPORT)
                .describedAs(TranslationKeys.CPS_PROPERTIES_ENHANCED_SUPPORT_DESCRIPTION)
                .fromThesaurus(this.thesaurus)
                .finish();
        return Arrays.asList(nameSpec, enhancedSupportSpec);
    }

}