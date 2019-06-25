/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TimeOfUseCampaignCustomPropertySet implements CustomPropertySet<ServiceCall, TimeOfUseCampaignDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_ID = TimeOfUseCampaignDomainExtension.class.getName();

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    private final TimeOfUseCampaignServiceImpl timeOfUseCampaignService;

    @Inject
    public TimeOfUseCampaignCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService,
                                              TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Override
    public String getId() {
        return CUSTOM_PROPERTY_SET_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.TIME_OF_USE_CAMPAIGN_CPS).format();
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
    public PersistenceSupport<ServiceCall, TimeOfUseCampaignDomainExtension> getPersistenceSupport() {
        return new TimeOfUseCampaignPersistenceSupport(timeOfUseCampaignService);
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
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.NAME_OF_CAMPAIGN.javaName(), TranslationKeys.NAME_OF_CAMPAIGN)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                propertySpecService
                        .referenceSpec(DeviceType.class)
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.DEVICE_TYPE.javaName(), TranslationKeys.DEVICE_TYPE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.DEVICE_GROUP.javaName(), TranslationKeys.DEVICE_GROUP)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.UPLOAD_PERIOD_START.javaName(), TranslationKeys.ACTIVATION_START)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.UPLOAD_PERIOD_END.javaName(), TranslationKeys.ACTIVATION_END)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .referenceSpec(Calendar.class)
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.CALENDAR.javaName(), TranslationKeys.CALENDAR)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.ACTIVATION_OPTION.javaName(), TranslationKeys.ACTIVATION_OPTION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.ACTIVATION_DATE.javaName(), TranslationKeys.ACTIVATION_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.UPDATE_TYPE.javaName(), TranslationKeys.UPDATE_TYPE)
                        .fromThesaurus(thesaurus)
                        .addValues(TranslationKeys.SPECIAL_DAYS.getKey(), TranslationKeys.FULL_CALENDAR.getKey()).markExhaustive()
                        .finish(),
                propertySpecService
                        .longSpec()
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.VALIDATION_TIMEOUT.javaName(), TranslationKeys.VALIDATION_TIMEOUT)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .booleanSpec()
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.WITH_UNIQUE_CALENDAR_NAME.javaName(), TranslationKeys.WITH_UNIQUE_CALENDAR_NAME)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .longSpec()
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.SEND_CALENDAR_COMTASK_ID.javaName(), TranslationKeys.SEND_CALENDAR_COMTASK_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .longSpec()
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.VALIDATION_COMTASK_ID.javaName(), TranslationKeys.VALIDATION_COMTASK_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .longSpec()
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.SEND_CALENDAR_CONNECTIONSTRATEGY_ID.javaName(), TranslationKeys.SEND_CALENDAR_CONNECTIONSTRATEGY_ID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                propertySpecService
                        .longSpec()
                        .named(TimeOfUseCampaignDomainExtension.FieldNames.VALIDATION_CONNECTIONSTRATEGY_ID.javaName(), TranslationKeys.VALIDATION_CONNECTIONSTRATEGY_ID)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }
}