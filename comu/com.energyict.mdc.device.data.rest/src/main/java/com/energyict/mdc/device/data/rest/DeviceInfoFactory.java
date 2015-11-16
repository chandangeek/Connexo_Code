package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.impl.DeviceApplication;
import com.energyict.mdc.device.data.rest.impl.DeviceInfo;
import com.energyict.mdc.device.data.rest.impl.DeviceSearchModelTranslationKeys;
import com.energyict.mdc.device.data.rest.impl.DeviceTopologyInfo;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component(name="device.info.factory", service = { InfoFactory.class }, immediate = true)
public class DeviceInfoFactory implements InfoFactory<Device> {

    private Thesaurus thesaurus;
    private BatchService batchService;
    private TopologyService topologyService;
    private IssueService issueService;
    private IssueDataValidationService issueDataValidationService;
    private MeteringService meteringService;

    public DeviceInfoFactory() {}

    @Inject
    public DeviceInfoFactory(Thesaurus thesaurus, BatchService batchService, TopologyService topologyService, IssueService issueService, IssueDataValidationService issueDataValidationService, MeteringService meteringService) {
        this.thesaurus = thesaurus;
        this.batchService = batchService;
        this.topologyService = topologyService;
        this.issueService = issueService;
        this.issueDataValidationService = issueDataValidationService;
        this.meteringService = meteringService;
    }

    @Reference
    public void setBatchService(BatchService batchService) {
        this.batchService = batchService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceApplication.COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setIssueDataValidationService(IssueDataValidationService issueDataValidationService) {
        this.issueDataValidationService = issueDataValidationService;
    }

    public List<DeviceInfo> from(List<Device> devices) {
        return devices.stream().map(this::from).collect(Collectors.toList());
    }

    @Override
    public DeviceInfo from(Device device) {
        return from(device, Collections.emptyList());
    }

    public DeviceInfo from(Device device, List<DeviceTopologyInfo> slaveDevices) {
        return DeviceInfo.from(device, slaveDevices, batchService, topologyService, issueService, issueDataValidationService, meteringService, thesaurus);
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        List<PropertyDescriptionInfo> infos = new ArrayList<>(21);
        infos.add(createDescription("deviceTypeId", Long.class));
        infos.add(createDescription("deviceConfigurationId", Long.class));
        infos.add(createDescription("deviceProtocolPluggeableClassId", Long.class));
        infos.add(createDescription("yearOfCertification", Integer.class));
        infos.add(createDescription("batch", String.class));
        infos.add(createDescription("masterDevicemRID", String.class));
        infos.add(createDescription("masterDeviceId", Long.class));
        infos.add(createDescription("nbrOfDataCollectionIssues", Integer.class));
        infos.add(createDescription("openDataValidationIssue", Long.class));
        infos.add(createDescription("hasRegisters", Boolean.class));
        infos.add(createDescription("hasLogBooks", Boolean.class));
        infos.add(createDescription("hasLoadProfiles", Boolean.class));
        infos.add(createDescription("isDirectlyAddressed", Boolean.class));
        infos.add(createDescription("isGateway", Boolean.class));
        infos.add(createDescription("serviceCategory", String.class));
        Collections.sort(infos, Comparator.comparing(pdi -> pdi.propertyName));

        // Default columns in proper order
        infos.add(0, new PropertyDescriptionInfo("state.name", String.class, thesaurus.getFormat(DeviceSearchModelTranslationKeys.STATE).format()));
        infos.add(0, createDescription("deviceConfigurationName", String.class));
        infos.add(0, createDescription("deviceTypeName", String.class));
        infos.add(0, createDescription("serialNumber", String.class));
        infos.add(0, createDescription("mRID", String.class));
        return infos;
    }

    private PropertyDescriptionInfo createDescription(String propertyName, Class<?> aClass) {
        return new PropertyDescriptionInfo(propertyName, aClass, thesaurus.getString(DeviceSearchModelTranslationKeys.Keys.PREFIX + propertyName, propertyName));
    }

}
