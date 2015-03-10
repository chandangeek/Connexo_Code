package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareUpgradeOptions;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.jupiter.export", service = {FirmwareService.class, InstallService.class}, property = "name=" + FirmwareService.COMPONENTNAME, immediate = true)
public class FirmwareServiceImpl implements FirmwareService, InstallService, TranslationKeyProvider {
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile DeviceConfigurationService deviceConfigurationService;

    public FirmwareServiceImpl() {}

    @Inject
    public FirmwareServiceImpl(OrmService ormService, NlsService nlsService, DeviceConfigurationService deviceConfigurationService, QueryService queryService) {
        setOrmService(ormService);
        setNlsService(nlsService);
        setQueryService(queryService);
        setDeviceConfigurationService(deviceConfigurationService);
        if (!dataModel.isInstalled()) {
            install();
        }
        activate();
    }

    @Override
    public Query<? extends FirmwareVersion> getFirmwareVersionQuery() {
        return queryService.wrap(dataModel.query(FirmwareVersion.class));
    }

    @Override
    public Finder<FirmwareVersion> findAllFirmwareVersions(Condition condition) {
        return DefaultFinder.of(FirmwareVersion.class, condition, dataModel);
    }

    @Override
    public Optional<FirmwareVersion> getFirmwareVersionById(long id) {
        return dataModel.mapper(FirmwareVersion.class).getUnique("id", id);
    }

    @Override
    public void saveFirmwareVersion(FirmwareVersion firmwareVersion) {
        FirmwareVersionImpl.class.cast(firmwareVersion).save();
    }

    @Override
    public void deprecateFirmwareVersion(FirmwareVersion firmwareVersion) {
        FirmwareVersionImpl.class.cast(firmwareVersion).deprecate();
    }

    @Override
    public Optional<FirmwareUpgradeOptions> findFirmwareUpgradeOptionsByDeviceType(DeviceType deviceType) {
        return dataModel.mapper(FirmwareUpgradeOptions.class).getUnique("deviceType", deviceType);
    }

    @Activate
    public final void activate() {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(FirmwareService.class).toInstance(FirmwareServiceImpl.this);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(QueryService.class).toInstance(queryService);
                    bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                }
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public final void deactivate() {
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(FirmwareService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(FirmwareService.COMPONENTNAME, "Firmware configuration");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(OrmService.COMPONENTNAME, NlsService.COMPONENTNAME, DeviceConfigurationService.COMPONENTNAME);
    }

    @Override
    public void install() {
        Installer installer = new Installer(dataModel, thesaurus);
        installer.install();
    }

    @Override
    public String getComponentName() {
        return FirmwareService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            keys.add(messageSeed);
        }
        return keys;
    }
}
