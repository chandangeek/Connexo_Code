package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.impl.records.FakeMDCEventSource;
import com.elster.jupiter.issue.impl.records.FakeMDCReadingEventSource;
import com.elster.jupiter.issue.share.service.IssueHelpService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;

import static com.elster.jupiter.cbo.Accumulation.DELTADELTA;
import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.FORWARD;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;

// TODO delete when events will be defined by MDC
/**
 * This class can be used only in test purpose while MDC hasn't correct implementation
 */
@Deprecated
@Component(name = "com.elster.jupiter.issue.help", service = {IssueHelpService.class, IssueHelpServiceImpl.class}, property = {"osgi.command.scope=issue.help", "osgi.command.function=addReading"}, immediate = true)
public class IssueHelpServiceImpl implements IssueHelpService {
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile IssueService issueService;
    private volatile OrmService ormService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

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
    @Reference
    public final void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public void postEvent(long timestamp, String topic, String comPortName, String comServerName, String deviceIdentifier, long connectionTypePluggableClassId, String comTaskId, long discoveryProtocolId, String masterDeviceId) {
        eventService.postEvent(topic, new FakeMDCEventSource(timestamp, topic, comPortName, comServerName, deviceIdentifier, connectionTypePluggableClassId, comTaskId, discoveryProtocolId, masterDeviceId));
    }

    public void addReading(long meterId, String readingTypeCode, long timestamp, BigDecimal value){
        try (TransactionContext context = transactionService.getContext()){
            threadPrincipalService.set(new Principal() {
                @Override
                public String getName() {
                    return "console";
                }
            });
            IntervalBlockImpl meterReadingBlock = new IntervalBlockImpl(readingTypeCode);
            meterReadingBlock.addIntervalReading(new IntervalReadingImpl(new Date(timestamp), value));

            MeterReadingImpl meterReading = new MeterReadingImpl();
            meterReading.addIntervalBlock(meterReadingBlock);
            getMeterById(meterId).store(meterReading);
            context.commit();
        }
    }

    private Meter getMeterById(long meterId) {
        Optional<Meter> meterRef = meteringService.findMeter(meterId);
        if(!meterRef.isPresent()){
            throw new RuntimeException("Meter with id = " + meterId + " doesn't exist");
        }
        return meterRef.get();
    }
}
