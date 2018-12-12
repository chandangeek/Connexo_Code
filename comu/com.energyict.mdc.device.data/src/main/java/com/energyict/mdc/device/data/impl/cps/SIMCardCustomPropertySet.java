/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.cps;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component(name = SIMCardCustomPropertySet.CPS_ID, service = CustomPropertySet.class, immediate = true,
        property = {"name=" + SIMCardCustomPropertySet.CPS_ID})
public class SIMCardCustomPropertySet implements CustomPropertySet<Device, SIMCardDomainExtension> {

    public static final String CPS_ID = "com.energyict.mdc.device.data.impl.cps.SIMCardCustomPropertySet";

    // Common for all domain objects
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        // required for proper startup; do not delete
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(CustomPropertyTranslationKeys.CPS_SIM_CARD).format();
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return thesaurus.getFormat(CustomPropertyTranslationKeys.DOMAIN_NAME_DEVICE).format();
    }

    @Override
    public PersistenceSupport<Device, SIMCardDomainExtension> getPersistenceSupport() {
        return new SIMCardPeristenceSupport(thesaurus);
    }

    @Override
    public boolean isRequired() {
        return true;
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
        return EnumSet.allOf(EditPrivilege.class);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.propertySpecService
                        .stringSpec()
                        .named(SIMCardDomainExtension.FieldNames.ICCID.javaName(), CustomPropertyTranslationKeys.ICCID)
                        .describedAs(CustomPropertyTranslationKeys.ICCID_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SIMCardDomainExtension.FieldNames.PROVIDER.javaName(), CustomPropertyTranslationKeys.PROVIDER)
                        .describedAs(CustomPropertyTranslationKeys.PROVIDER_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SIMCardDomainExtension.FieldNames.ACTIVE_IMSI.javaName(), CustomPropertyTranslationKeys.ACTIVE_IMSI)
                        .describedAs(CustomPropertyTranslationKeys.ACTIVE_IMSI_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SIMCardDomainExtension.FieldNames.INACTIVE_IMSI_FIRST.javaName(), CustomPropertyTranslationKeys.INACTIVE_IMSI_FIRST)
                        .describedAs(CustomPropertyTranslationKeys.INACTIVE_IMSI_FIRST_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SIMCardDomainExtension.FieldNames.INACTIVE_IMSI_SECOND.javaName(), CustomPropertyTranslationKeys.INACTIVE_IMSI_SECOND)
                        .describedAs(CustomPropertyTranslationKeys.INACTIVE_IMSI_SECOND_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SIMCardDomainExtension.FieldNames.INACTIVE_IMSI_THIRD.javaName(), CustomPropertyTranslationKeys.INACTIVE_IMSI_THIRD)
                        .describedAs(CustomPropertyTranslationKeys.INACTIVE_IMSI_THIRD_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SIMCardDomainExtension.FieldNames.BATCH_ID.javaName(), CustomPropertyTranslationKeys.BATCH_ID)
                        .describedAs(CustomPropertyTranslationKeys.BATCH_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SIMCardDomainExtension.FieldNames.CARD_FORMAT.javaName(), CustomPropertyTranslationKeys.CARD_FORMAT)
                        .describedAs(CustomPropertyTranslationKeys.CARD_FORMAT_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .addValues(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_FULL_SIZE, thesaurus),
                                CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_MINI, thesaurus),
                                CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_MICRO, thesaurus),
                                CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_NANO, thesaurus),
                                CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_EMBEDDED, thesaurus),
                                CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.CARD_FORMAT_SW, thesaurus))
                        .markExhaustive()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SIMCardDomainExtension.FieldNames.STATUS.javaName(), CustomPropertyTranslationKeys.STATUS)
                        .describedAs(CustomPropertyTranslationKeys.STATUS_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .addValues(CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_ACTIVE, thesaurus),
                                CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_DEMOLISHED, thesaurus),
                                CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_INACTIVE, thesaurus),
                                CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_PRE_ACTIVE, thesaurus),
                                CustomPropertyTranslationKeys.translationFor(CustomPropertyTranslationKeys.STATUS_TEST, thesaurus))
                        .markExhaustive()
                        .finish());
    }
}