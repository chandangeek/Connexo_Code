/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;


import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FirmwareCampaignCustomPropertySet implements CustomPropertySet<ServiceCall, FirmwareCampaignDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_ID = FirmwareCampaignDomainExtension.class.getName();

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    private final FirmwareServiceImpl firmwareService;

    @Inject
    public FirmwareCampaignCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService,
                                             FirmwareServiceImpl firmwareService) {
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return CUSTOM_PROPERTY_SET_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.FIRMWARE_CAMPAIGN_CPS).format();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return thesaurus.getFormat(TranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, FirmwareCampaignDomainExtension> getPersistenceSupport() {
        return new FirmwareCampaignPersistenceSupport(firmwareService);
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                propertySpecService
                        .stringSpec()
                        .named(FirmwareCampaignDomainExtension.FieldNames.NAME.javaName(), TranslationKeys.NAME_OF_CAMPAIGN)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService
                        .referenceSpec(DeviceType.class)
                        .named(FirmwareCampaignDomainExtension.FieldNames.DEVICE_TYPE.javaName(), TranslationKeys.DEVICE_TYPE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(FirmwareCampaignDomainExtension.FieldNames.DEVICE_GROUP.javaName(), TranslationKeys.DEVICE_GROUP)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(FirmwareCampaignDomainExtension.FieldNames.UPLOAD_PERIOD_START.javaName(), TranslationKeys.UPDATE_START)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(FirmwareCampaignDomainExtension.FieldNames.UPLOAD_PERIOD_END.javaName(), TranslationKeys.UPDATE_END)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(FirmwareCampaignDomainExtension.FieldNames.ACTIVATION_DATE.javaName(), TranslationKeys.ACTIVATION_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .timeDurationSpec()
                        .named(FirmwareCampaignDomainExtension.FieldNames.VALIDATION_TIMEOUT.javaName(), TranslationKeys.VALIDATION_TIMEOUT)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .longSpec()
                        .named(FirmwareCampaignDomainExtension.FieldNames.FIRMWARE_UPLOAD_COMTASK_ID.javaName(), TranslationKeys.FIRMWARE_UPLOAD_COMTASK_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(FirmwareCampaignDomainExtension.FieldNames.FIRMWARE_UPLOAD_CONNECTIONSTRATEGY.javaName(), TranslationKeys.FIRMWARE_UPLOAD_CONNECTIONSTRATEGY)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .longSpec()
                        .named(FirmwareCampaignDomainExtension.FieldNames.VALIDATION_COMTASK_ID.javaName(), TranslationKeys.VALIDATION_COMTASK_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(FirmwareCampaignDomainExtension.FieldNames.VALIDATION_CONNECTIONSTRATEGY.javaName(), TranslationKeys.VALIDATION_CONNECTIONSTRATEGY)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .booleanSpec()
                        .named(FirmwareCampaignDomainExtension.FieldNames.MANUALLY_CANCELLED.javaName(), TranslationKeys.MANUALLY_CANCELLED)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .booleanSpec()
                        .named(FirmwareCampaignDomainExtension.FieldNames.WITH_UNIQUE_FIRMWARE_VERSION.javaName(), TranslationKeys.WITH_UNIQUE_FIRMWARE_VERSION)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }
}