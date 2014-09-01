package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link DeviceProtocolService} interface
 * and registers as a OSGi component.
 *
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:03
 */
@Component(name = "com.energyict.mdc.service.deviceprotocols", service = {DeviceProtocolService.class, InstallService.class}, immediate = true, property = "name=" + DeviceProtocolService.COMPONENT_NAME)
public class DeviceProtocolServiceImpl implements DeviceProtocolService, InstallService {

    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile IssueService issueService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private volatile OrmClient ormClient;

    public DeviceProtocolServiceImpl() {
        super();
    }

    @Activate
    public void activate() {
        this.dataModel.register(getModule());
    }

    @Deactivate
    public void deactivate() {
        Bus.clearOrmClient(this.ormClient);
        Bus.clearClock(this.clock);
        Bus.clearIssueService(this.issueService);
        Bus.clearMdcReadingTypeUtilService(this.mdcReadingTypeUtilService);
        Bus.clearThesaurus(this.thesaurus);
    }

    @Inject
    public DeviceProtocolServiceImpl(IssueService issueService, Clock clock, OrmService ormService, NlsService nlsService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setIssueService(issueService);
        this.setClock(clock);
        this.activate();
        this.install();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(IssueService.class).toInstance(issueService);
                bind(Clock.class).toInstance(clock);
                bind(DeviceProtocolService.class).toInstance(DeviceProtocolServiceImpl.this);
            }
        };
    }

    @Override
    public Class loadProtocolClass(String javaClassName) {
        try {
            return this.getClass().getClassLoader().loadClass(javaClassName);
        }
        catch (ClassNotFoundException e) {
            throw new ProtocolCreationException (MessageSeeds.UNSUPPORTED_LEGACY_PROTOCOL_TYPE, javaClassName);
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
        Bus.setThesaurus(this.thesaurus);
    }

    @Reference
    public void setMdcReadingTypeUtilService(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
        Bus.setMdcReadingTypeUtilService(mdcReadingTypeUtilService);
    }

    public MdcReadingTypeUtilService getMdcReadingTypeUtilService() {
        return mdcReadingTypeUtilService;
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.thesaurus).install(true);
    }

}