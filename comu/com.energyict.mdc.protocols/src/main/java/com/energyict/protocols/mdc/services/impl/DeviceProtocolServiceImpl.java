package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import java.time.Clock;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceProtocolService} interface
 * and registers as a OSGi component.
 * <p>
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:03
 */
@Component(name = "com.energyict.mdc.service.deviceprotocols", service = {DeviceProtocolService.class, InstallService.class}, immediate = true, property = "name=" + DeviceProtocolService.COMPONENT_NAME)
public class DeviceProtocolServiceImpl implements DeviceProtocolService, InstallService {

    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile OrmClient ormClient;
    private volatile IssueService issueService;
    private volatile PropertySpecService propertySpecService;
    private volatile TopologyService topologyService;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;

    private Injector injector;

    // For OSGi purposes
    public DeviceProtocolServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceProtocolServiceImpl(IssueService issueService, Clock clock, OrmService ormService, NlsService nlsService, PropertySpecService propertySpecService, TopologyService topologyService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setIssueService(issueService);
        this.setClock(clock);
        this.setPropertySpecService(propertySpecService);
        this.setTopologyService(topologyService);
        this.activate();
        this.install();
    }

    @Activate
    public void activate() {
        Module module = this.getModule();
        this.dataModel.register(module);
        this.injector = Guice.createInjector(module);
    }

    @Deactivate
    public void deactivate() {
        Bus.clearOrmClient(this.ormClient);
        Bus.clearClock(this.clock);
        Bus.clearIssueService(this.issueService);
        Bus.clearMdcReadingTypeUtilService(this.mdcReadingTypeUtilService);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(IssueService.class).toInstance(issueService);
                bind(Clock.class).toInstance(clock);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(TopologyService.class).toInstance(topologyService);
                bind(DeviceProtocolService.class).toInstance(DeviceProtocolServiceImpl.this);
            }
        };
    }

    @Override
    public Object createProtocol(String className) {
        try {
            // Attempt to load the class to verify that this class is managed by this bundle
            Class<?> protocolClass = this.getClass().getClassLoader().loadClass(className);
            return this.injector.getInstance(protocolClass);
        }
        catch (ClassNotFoundException | ConfigurationException | ProvisionException e) {
            throw new ProtocolCreationException(MessageSeeds.UNSUPPORTED_LEGACY_PROTOCOL_TYPE, className);
        }
    }

    public IssueService getIssueService() {
        return issueService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        Bus.setIssueService(issueService);
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
        Bus.setClock(clock);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
        this.createOrmClientIfAllDependenciesHaveBeenResolved();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(DeviceProtocolService.COMPONENT_NAME, "DeviceProtocol service 1");
        this.createOrmClientIfAllDependenciesHaveBeenResolved();
    }

    private void createOrmClientIfAllDependenciesHaveBeenResolved() {
        if (this.transactionService != null && this.dataModel != null) {
            this.setOrmClient(new OrmClientImpl(this.dataModel, this.transactionService));
        }
    }

    private void setOrmClient(OrmClient ormClient) {
        this.ormClient = ormClient;
        Bus.setOrmClient(ormClient);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setMdcReadingTypeUtilService(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
        Bus.setMdcReadingTypeUtilService(mdcReadingTypeUtilService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public MdcReadingTypeUtilService getMdcReadingTypeUtilService() {
        return mdcReadingTypeUtilService;
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.thesaurus).install(true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "NLS");
    }
}