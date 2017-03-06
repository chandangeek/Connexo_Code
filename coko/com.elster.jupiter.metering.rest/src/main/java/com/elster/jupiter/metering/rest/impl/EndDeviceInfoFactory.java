/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create Info objects. This class will register on the InfoFactoryWhiteboard and is used by DynamicSearch.
 * Created by bvn on 6/9/15.
 */
@Component(name="enddevice.info.factory", service = { InfoFactory.class }, immediate = true)
public class EndDeviceInfoFactory implements InfoFactory<EndDevice> {

    private volatile Thesaurus thesaurus;

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringApplication.COMPONENT_NAME, Layer.REST);
    }

    @Override
    public Object from(EndDevice endDevice) {
        MeterInfo info = new MeterInfo();
        info.mRID = endDevice.getMRID();
        info.id = endDevice.getId();
        info.aliasName = endDevice.getAliasName();
        info.description = endDevice.getDescription();
        info.name = endDevice.getName();
        info.serialNumber = endDevice.getSerialNumber();
        info.utcNumber = endDevice.getUtcNumber();
        info.modelNbr = endDevice.getModelNumber();
        info.modelVersion = endDevice.getModelVersion();
        info.utcNumber = endDevice.getUtcNumber();

        if (endDevice.getElectronicAddress() != null) {
            info.eMail1 = endDevice.getElectronicAddress().getEmail1();
            info.eMail2 = endDevice.getElectronicAddress().getEmail2();
        }
        info.amrSystemName = endDevice.getAmrSystem().getName();
        info.version = endDevice.getVersion();
        LifecycleDates lcd = endDevice.getLifecycleDates();
        if (lcd != null) {
            lcd.getInstalledDate().map(Instant::getEpochSecond).ifPresent(date -> info.installedDate = date);
            lcd.getRemovedDate().map(Instant::getEpochSecond).ifPresent(date -> info.removedDate = date);
            lcd.getRetiredDate().map(Instant::getEpochSecond).ifPresent(date -> info.retiredDate = date);
        }
        return info;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        List<PropertyDescriptionInfo> infos = new ArrayList<>();
        infos.add(createDescription(TranslationSeeds.NAME, String.class));
        infos.add(createDescription(TranslationSeeds.ALIAS_NAME, String.class));
        infos.add(createDescription(TranslationSeeds.DESCRIPTION, String.class));
        infos.add(createDescription(TranslationSeeds.MRID, String.class));
        infos.add(createDescription(TranslationSeeds.SERIALNUMBER, String.class));
        infos.add(createDescription(TranslationSeeds.MANUFACTURER, String.class));
        infos.add(createDescription(TranslationSeeds.MODEL_NBR, String.class));
        infos.add(createDescription(TranslationSeeds.MODEL_VERSION, String.class));
        infos.add(createDescription(TranslationSeeds.UTCNUMBER, String.class));
        infos.add(createDescription(TranslationSeeds.EMAIL1, String.class));
        infos.add(createDescription(TranslationSeeds.EMAIL2, String.class));
        infos.add(createDescription(TranslationSeeds.AMRSYSTEMNAME, String.class));
        infos.add(createDescription(TranslationSeeds.INSTALLATION_TIME, String.class));
        infos.add(createDescription(TranslationSeeds.REMOVEDDATE, String.class));
        infos.add(createDescription(TranslationSeeds.RETIREDDATE, String.class));
        return infos;
    }

    private PropertyDescriptionInfo createDescription(TranslationSeeds propertyName, Class<?> aClass) {
        return new PropertyDescriptionInfo(propertyName.getKey(), aClass, thesaurus.getString(propertyName.getKey(), propertyName.getDefaultFormat()));
    }

    @Override
    public Class<EndDevice> getDomainClass() {
        return EndDevice.class;
    }
}
