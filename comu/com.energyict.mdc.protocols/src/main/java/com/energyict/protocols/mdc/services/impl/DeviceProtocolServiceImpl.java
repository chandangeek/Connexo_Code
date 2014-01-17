package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
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
@Component(name = "com.energyict.mdc.service.deviceprotocols", service = DeviceProtocolService.class)
public class DeviceProtocolServiceImpl implements DeviceProtocolService {

    private IssueService issueService;
    private Clock clock;

    public DeviceProtocolServiceImpl() {
        super();
    }

    @Inject
    public DeviceProtocolServiceImpl(IssueService issueService, Clock clock) {
        this();
        this.setIssueService(issueService);
        this.setClock(clock);
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
}