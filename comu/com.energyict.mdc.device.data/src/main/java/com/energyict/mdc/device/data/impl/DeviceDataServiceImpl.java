package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.impl.DeviceConfigurationServiceImpl;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides a straightforward implementation of the {@link com.energyict.mdc.device.data.DeviceDataService} interface
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/02/14
 * Time: 10:43
 */
@Component(name = "com.energyict.mdc.device.data", service = {DeviceDataService.class, InstallService.class}, property = "name=" + DeviceDataService.COMPONENTNAME)
public class DeviceDataServiceImpl implements DeviceDataService, InstallService {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile MeteringService meteringService;

    public DeviceDataServiceImpl() {
        super();
    }

    @Inject
    public DeviceDataServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setMeteringService(meteringService);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            this.install(true);
        }
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceDataService.class).toInstance(DeviceDataServiceImpl.this);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MeteringService.class).toInstance(meteringService);
                bind(DataModel.class).toInstance(dataModel);
            }
        };
    }

    @Override
    public void install() {
        this.install(false);
    }

    private void install(boolean exeuteDdl) {
        new InstallerImpl(getDataModel(), getThesaurus(), getEventService()).install(exeuteDdl);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Device and it's Data");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public EventService getEventService() {
        return eventService;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name) {
        return dataModel.getInstance(DeviceImpl.class).initialize(deviceConfiguration, name);
    }

    @Override
    public Device findDeviceById(long id) {
        return dataModel.mapper(Device.class).getUnique("id", id).orNull();
    }

    @Override
    public Device findDeviceByExternalName(String externalName) {
        return dataModel.mapper(Device.class).getUnique("externalName", externalName).orNull();
    }

    @Override
    public Device getPrototypeDeviceFor(DeviceConfiguration deviceConfiguration) {
        return null;
    }

    @Override
    public boolean deviceHasLogBookForLogBookSpec(Device device, LogBookSpec logBookSpec) {
        //TODO properly implement when the persistence of LogBook has finished
        return false;
    }

    @Override
    public List<BaseDevice<Channel, LoadProfile, Register>> findPhysicalConnectedDevicesFor(Device device) {
        Condition condition = Where.where("gateway").isEqualTo(device).and(Where.where("interval").isEffective());
        List<PhysicalGatewayReference> physicalGatewayReferences = this.dataModel.mapper(PhysicalGatewayReference.class).select(condition);
        if(!physicalGatewayReferences.isEmpty()){
            List<BaseDevice<Channel, LoadProfile, Register>> baseDevices = new ArrayList<>();
            for (PhysicalGatewayReference physicalGatewayReference : physicalGatewayReferences) {
                baseDevices.add(physicalGatewayReference.getOrigin());
            }
            return baseDevices;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<BaseDevice<Channel, LoadProfile, Register>> findCommunicationReferencingDevicesFor(Device device) {
        Condition condition = Where.where("gateway").isEqualTo(device).and(Where.where("interval").isEffective());
        List<CommunicationGatewayReference> communicationGatewayReferences = this.dataModel.mapper(CommunicationGatewayReference.class).select(condition);
        if(!communicationGatewayReferences.isEmpty()){
            List<BaseDevice<Channel, LoadProfile, Register>> baseDevices = new ArrayList<>();
            for (CommunicationGatewayReference communicationGatewayReference : communicationGatewayReferences) {
                baseDevices.add(communicationGatewayReference.getOrigin());
            }
            return baseDevices;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public LoadProfile findLoadProfileById(long id) {
        return dataModel.mapper(LoadProfile.class).getUnique("id", id).orNull();
    }
}
