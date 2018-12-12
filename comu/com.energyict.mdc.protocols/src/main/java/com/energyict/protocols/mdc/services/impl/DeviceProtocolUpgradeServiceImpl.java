/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolUpgradeService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListener;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;

import static com.elster.jupiter.orm.Version.version;

/**
 * Implementation of the {@link DeviceProtocolUpgradeService} interface
 *
 * @author stijn
 * @since 23.05.17 - 13:54
 */
@Component(name = "com.energyict.mdc.service.deviceprotocol.upgrade",
        service = {DeviceProtocolUpgradeService.class},
        immediate = true,
        property = "name=" + DeviceProtocolUpgradeService.COMPONENT_NAME)
public class DeviceProtocolUpgradeServiceImpl implements DeviceProtocolUpgradeService, ProtocolDeploymentListener {

    private volatile DataModel dataModel;
    private volatile SecurityManagementService securityManagementService;
    private volatile DeviceService deviceService;
    private volatile UpgradeService upgradeService;
    private volatile DataVaultService dataVaultService;
    private volatile ProtocolPluggableService pluggableServicedeviceService;

    private volatile boolean installed = false;
    private volatile boolean upgradeAdded = false;

    // For OSGi purposes only
    public DeviceProtocolUpgradeServiceImpl() {
    }

    // For unit testing purposes
    public DeviceProtocolUpgradeServiceImpl(OrmService ormService, SecurityManagementService securityManagementService, DeviceService deviceService, UpgradeService upgradeService, DataVaultService dataVaultService, ProtocolPluggableService pluggableServicedeviceService) {
        this();
        setOrmService(ormService);
        setSecurityManagementService(securityManagementService);
        setDeviceService(deviceService);
        setUpgradeService(upgradeService);
        setDataVaultService(dataVaultService);
        setProtocolPluggableService(pluggableServicedeviceService);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(DeviceProtocolUpgradeService.COMPONENT_NAME, "Mdc protocols upgrade");
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.pluggableServicedeviceService = protocolPluggableService;
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        this.dataModel.getInstance(ProtocolPluggableService.class).register(this);
        this.installed = true;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceProtocolUpgradeService.class).toInstance(DeviceProtocolUpgradeServiceImpl.this);
                bind(DataModel.class).toInstance(dataModel);
                bind(SecurityManagementService.class).toInstance(securityManagementService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(DataVaultService.class).toInstance(dataVaultService);
                bind(ProtocolPluggableService.class).toInstance(pluggableServicedeviceService);
            }
        };
    }

    @Override
    public void deviceProtocolServiceDeployed(DeviceProtocolService service) {
        if (installed && !upgradeAdded) {
            upgradeService.register(
                    InstallIdentifier.identifier("MultiSense", DeviceProtocolUpgradeService.COMPONENT_NAME),
                    dataModel,
                    InstallerImpl.class,
                    Collections.emptyMap()
            );
            upgradeAdded = true;
        }
    }

    @Override
    public void deviceProtocolServiceUndeployed(DeviceProtocolService service) {
        // Not interested in these notifications
    }

    @Override
    public void inboundDeviceProtocolServiceDeployed(InboundDeviceProtocolService service) {
        // Not interested in these notifications
    }

    @Override
    public void inboundDeviceProtocolServiceUndeployed(InboundDeviceProtocolService service) {
        // Not interested in these notifications
    }

    @Override
    public void connectionTypeServiceDeployed(ConnectionTypeService service) {
        // Not interested in these notifications
    }

    @Override
    public void connectionTypeServiceUndeployed(ConnectionTypeService service) {
        // Not interested in these notifications
    }
}