package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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
    private volatile IssueService issueService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;

    public DeviceProtocolServiceImpl() {
        super();
    }

    @Activate
    public void activate() {
        this.dataModel.register(getModule());
    }

    @Inject
    public DeviceProtocolServiceImpl(IssueService issueService, Clock clock, OrmService ormService, NlsService nlsService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setIssueService(issueService);
        this.setClock(clock);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            this.install();
        }
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
            throw new ProtocolCreationException (javaClassName);
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
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(DeviceProtocolService.COMPONENT_NAME, "DeviceProtocol service 1");
        this.dataModel = dataModel;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.thesaurus).install(true);
    }
}