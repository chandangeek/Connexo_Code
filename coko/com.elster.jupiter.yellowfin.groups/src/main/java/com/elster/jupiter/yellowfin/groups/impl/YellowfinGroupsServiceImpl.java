package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.jupiter.yellowfin.groups", service = {YellowfinGroupsService.class, InstallService.class}, property = "name=" + YellowfinGroupsService.COMPONENTNAME, immediate = true)
public class YellowfinGroupsServiceImpl implements YellowfinGroupsService, InstallService {

    private volatile DataModel dataModel;
    private volatile MeteringGroupsService meteringGroupsService;

    public YellowfinGroupsServiceImpl() {
    }

    @Inject
    public YellowfinGroupsServiceImpl(OrmService ormService, MeteringGroupsService meteringGroupsService) {
        setOrmService(ormService);
        setMeteringGroupsService(meteringGroupsService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Override
    public void install() {
        dataModel.install(true, true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "MTG");
    }

    @Activate
    public void activate() {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(YellowfinGroupsService.class).toInstance(YellowfinGroupsServiceImpl.this);
                    bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                    bind(DataModel.class).toInstance(dataModel);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public void deactivate() {
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "Yellowfin Metering Groups");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public Optional<DynamicDeviceGroupImpl> cacheDynamicDeviceGroup(String groupName) {
        Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroupByName(groupName);
        if(found.isPresent() && found.get().isDynamic()){
            QueryEndDeviceGroup group = (QueryEndDeviceGroup)found.get();

            DynamicDeviceGroupImpl cachedDeviceGroup = DynamicDeviceGroupImpl.from(dataModel, group.getId(), group.getMembers(Instant.now()));
            cachedDeviceGroup.save();
            return Optional.of(cachedDeviceGroup);
        }

        return Optional.empty();
    }

    @Override
    public Optional<AdHocDeviceGroupImpl> cacheAdHocDeviceGroup(List<Device> devices){
        AdHocDeviceGroupImpl cachedDeviceGroup = AdHocDeviceGroupImpl.from(dataModel, 0, devices);
        cachedDeviceGroup.save();
        return Optional.of(cachedDeviceGroup);
    }
}
