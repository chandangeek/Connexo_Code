package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class UsagePointSimpleCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointSimplePersistentDomainExtension> {

    private PropertySpecService propertySpecService;
    private Thesaurus thesaurus;

    public UsagePointSimpleCustomPropertySet() {
    }

    public UsagePointSimpleCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointSimplePersistentDomainExtension> getPersistenceSupport() {
        return new UsagePointSimplePersistentSupport(thesaurus);
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
        return UsagePointSimpleCustomPropertySet.class.getName();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(TranslationKeys.SIMPLE_CPS_NAME).format();
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
        PropertySpec combobox = this.propertySpecService.stringSpec()
                .named(UsagePointSimplePersistentDomainExtension.Fields.COMBOBOX.javaName(), TranslationKeys.CPS_PROPERTIES_COMBOBOX)
                .describedAs(TranslationKeys.CPS_PROPERTIES_COMBOBOX_DESCRIPTION)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue("Value 2")
                .addValues("Value 1", "Value 2", "Value 3")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .markRequired()
                .finish();
        return Arrays.asList(nameSpec, enhancedSupportSpec, combobox);
    }

}