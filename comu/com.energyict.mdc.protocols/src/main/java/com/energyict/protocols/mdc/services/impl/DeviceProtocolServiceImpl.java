package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.UserFileFactory;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
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
    private volatile MdcReadingTypeUtilService readingTypeUtilService;
    private volatile SocketService socketService;
    private volatile SerialComponentService serialComponentService;
    private volatile IdentificationService identificationService;
    private volatile CollectedDataFactory collectedDataFactory;
    private volatile LoadProfileFactory loadProfileFactory;
    private volatile CodeFactory codeFactory;
    private volatile UserFileFactory userFileFactory;

    private Injector injector;

    // For OSGi purposes
    public DeviceProtocolServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceProtocolServiceImpl(IssueService issueService, Clock clock, OrmService ormService, NlsService nlsService, PropertySpecService propertySpecService, TopologyService topologyService, SocketService socketService, SerialComponentService serialComponentService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, LoadProfileFactory loadProfileFactory, CodeFactory codeFactory, UserFileFactory userFileFactory, TransactionService transactionService) {
        this();
        this.setTransactionService(transactionService);
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setIssueService(issueService);
        this.setClock(clock);
        this.setPropertySpecService(propertySpecService);
        this.setTopologyService(topologyService);
        this.setSocketService(socketService);
        this.setSerialComponentService(serialComponentService);
        this.setReadingTypeUtilService(readingTypeUtilService);
        this.setIdentificationService(identificationService);
        this.setCollectedDataFactory(collectedDataFactory);
        this.setLoadProfileFactory(loadProfileFactory);
        this.setCodeFactory(codeFactory);
        this.setUserFileFactory(userFileFactory);
        this.activate();
        this.install();
    }

    @Activate
    public void activate() {
        Module module = this.getModule();
        this.dataModel.register(module);
        this.injector = Guice.createInjector(module);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(IssueService.class).toInstance(issueService);
                bind(Clock.class).toInstance(clock);
                bind(OrmClient.class).toInstance(ormClient);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(SocketService.class).toInstance(socketService);
                bind(SerialComponentService.class).toInstance(serialComponentService);
                bind(TopologyService.class).toInstance(topologyService);
                bind(MdcReadingTypeUtilService.class).toInstance(readingTypeUtilService);
                bind(IdentificationService.class).toInstance(identificationService);
                bind(CollectedDataFactory.class).toInstance(collectedDataFactory);
                bind(LoadProfileFactory.class).toInstance(loadProfileFactory);
                bind(CodeFactory.class).toInstance(codeFactory);
                bind(UserFileFactory.class).toInstance(userFileFactory);
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
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
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
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setReadingTypeUtilService(MdcReadingTypeUtilService readingTypeUtilService) {
        this.readingTypeUtilService = readingTypeUtilService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setSocketService(SocketService socketService) {
        this.socketService = socketService;
    }

    @Reference
    public void setSerialComponentService(SerialComponentService serialComponentService) {
        this.serialComponentService = serialComponentService;
    }

    @Reference
    public void setIdentificationService(IdentificationService identificationService) {
        this.identificationService = identificationService;
    }

    @Reference
    public void setCollectedDataFactory(CollectedDataFactory collectedDataFactory) {
        this.collectedDataFactory = collectedDataFactory;
    }

    @Reference
    public void setLoadProfileFactory(LoadProfileFactory loadProfileFactory) {
        this.loadProfileFactory = loadProfileFactory;
    }

    @Reference
    public void setCodeFactory(CodeFactory codeFactory) {
        this.codeFactory = codeFactory;
    }

    @Reference
    public void setUserFileFactory(UserFileFactory userFileFactory) {
        this.userFileFactory = userFileFactory;
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