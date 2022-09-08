/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.energyict.mdc.device.data.importers.DeviceDataImporterContext", service = {DeviceDataImporterContext.class})
public class DeviceDataImporterContext {
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile DeviceService deviceService;
    private volatile TopologyService topologyService;
    private volatile MeteringService meteringService;
    private volatile DeviceLifeCycleService deviceLifeCycleService;
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile SecurityManagementService securityManagementService;
    private volatile FileImportService fileImportService;
    private volatile Clock clock;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringTranslationService meteringTranslationService;
    private volatile DataModel dataModel;

    public DeviceDataImporterContext() {
    }

    @Inject
    public DeviceDataImporterContext(PropertySpecService propertySpecService,
                                     NlsService nlsService,
                                     DeviceConfigurationService deviceConfigurationService,
                                     DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                     DeviceService deviceService,
                                     TopologyService topologyService,
                                     MeteringService meteringService,
                                     DeviceLifeCycleService deviceLifeCycleService,
                                     FiniteStateMachineService finiteStateMachineService,
                                     UserService userService,
                                     ThreadPrincipalService threadPrincipalService,
                                     SecurityManagementService securityManagementService,
                                     FileImportService fileImportService,
                                     Clock clock,
                                     MetrologyConfigurationService metrologyConfigurationService,
                                     OrmService ormService,
                                     CustomPropertySetService customPropertySetService,
                                     MeteringTranslationService meteringTranslationService) {
        setPropertySpecService(propertySpecService);
        setNlsService(nlsService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        setDeviceService(deviceService);
        setTopologyService(topologyService);
        setMeteringService(meteringService);
        setDeviceLifeCycleService(deviceLifeCycleService);
        setFiniteStateMachineService(finiteStateMachineService);
        setUserService(userService);
        setThreadPrincipalService(threadPrincipalService);
        setSecurityManagementService(securityManagementService);
        setFileImportService(fileImportService);
        setClock(clock);
        setMetrologyConfigurationService(metrologyConfigurationService);
        setOrmService(ormService);
        setCustomPropertySetService(customPropertySetService);
        setMeteringTranslationService(meteringTranslationService);
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public final void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataImporterMessageHandler.COMPONENT, Layer.DOMAIN);
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        this.dataModel = ormService.getDataModels().get(0);
    }

    public MeteringTranslationService getMeteringTranslationService() {
        return meteringTranslationService;
    }

    @Reference
    public final void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    public DeviceConfigurationService getDeviceConfigurationService() {
        return deviceConfigurationService;
    }

    @Reference
    public final void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public DeviceLifeCycleConfigurationService getDeviceLifeCycleConfigurationService() {
        return deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    public DeviceService getDeviceService() {
        return deviceService;
    }

    @Reference
    public final void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public TopologyService getTopologyService() {
        return topologyService;
    }

    @Reference
    public final void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public MeteringService getMeteringService() {
        return meteringService;
    }

    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public DeviceLifeCycleService getDeviceLifeCycleService() {
        return deviceLifeCycleService;
    }

    @Reference
    public final void setDeviceLifeCycleService(DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    public FiniteStateMachineService getFiniteStateMachineService() {
        return finiteStateMachineService;
    }

    @Reference
    public final void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    public UserService getUserService() {
        return userService;
    }

    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    @Reference
    public final void setSecurityManagementService(SecurityManagementService securityManagementService){
        this.securityManagementService = securityManagementService;
    }

    public SecurityManagementService getSecurityManagementService() {
        return securityManagementService;
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }

    public MetrologyConfigurationService getMetrologyConfigurationService() {
        return this.metrologyConfigurationService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public CustomPropertySetService getCustomPropertySetService() {
        return this.customPropertySetService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    public FileImportService getFileImportService(){return fileImportService;}

    @Reference
    public void setFileImportService(FileImportService fileImportService) { this.fileImportService = fileImportService;}

}
