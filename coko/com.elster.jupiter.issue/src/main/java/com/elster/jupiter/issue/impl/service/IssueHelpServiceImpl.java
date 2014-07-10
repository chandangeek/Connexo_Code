package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.impl.records.FakeMDCEventSource;
import com.elster.jupiter.issue.share.service.IssueHelpService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

// TODO delete when events will be defined by MDC
/**
 * This class can be used only in test purpose while MDC hasn't correct implementation
 */
@Deprecated
@Component(name = "com.elster.jupiter.issue.help", service = IssueHelpService.class, immediate = true)
public class IssueHelpServiceImpl implements IssueHelpService {
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile IssueService issueService;
    private volatile OrmService ormService;

    public IssueHelpServiceImpl(){}

    @Inject
    public IssueHelpServiceImpl(EventService eventService, MeteringService meteringService, IssueService issueService) {
        setEventService(eventService);
        setMeteringService(meteringService);
        setIssueService(issueService);
    }

    @Reference
    public final void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
    @Reference
    public final void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }
    @Reference
    public final void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Override
    public void postEvent(long timestamp, String topic, String comPortName, String comServerName, String deviceIdentifier, long connectionTypePluggableClassId, String comTaskId, long discoveryProtocolId, String masterDeviceId) {
        eventService.postEvent(topic, new FakeMDCEventSource(timestamp, topic, comPortName, comServerName, deviceIdentifier, connectionTypePluggableClassId, comTaskId, discoveryProtocolId, masterDeviceId));
    }
}
