package com.energyict.mdc.device.topology.rest.demo.layer;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.EndDeviceEventCreatedEvent;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.layer.LayerNames;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;

import com.google.inject.Injector;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Having the devices with issues
 * Copyrights EnergyICT
 * Date: 31/08/2017
 * Time: 15:08
 */
public class IssuesAndAlarmsLayerBuilder implements GraphLayerBuilder{

    private final NlsService nlsService;
    private final TimeService timeService;
    private final Clock clock;
    private final MeteringService meteringService;
    private final IssueService issueService;
    private final IssueCreationService issueCreationService;
    private final DeviceAlarmService deviceAlarmService;
    private final DeviceService deviceService;
    private final Injector injector;

    public IssuesAndAlarmsLayerBuilder(MeteringService meteringService, IssueService issueService, IssueCreationService issueCreationService, DeviceAlarmService deviceAlarmService, DeviceService deviceService, NlsService nlsService, TimeService timeService, Clock clock, Injector injector){
        this.meteringService = meteringService;
        this.issueService = issueService;
        this.issueCreationService = issueCreationService;
        this.deviceAlarmService = deviceAlarmService;

        this.deviceService = deviceService;
        this.nlsService = nlsService;
        this.timeService = timeService;
        this.clock = clock;
        this.injector = injector;
    }

    @Override
    public boolean isGraphLayerCompatible(GraphLayer layer) {
        return LayerNames.IssuesAndAlarmLayer.fullName().equals(layer.getName());
    }

    @Override
    public void buildLayer(Device device) {
        int numberOfIssuesToCreate = new Random().nextInt(5);     // create 0 to 5 issues
        if (numberOfIssuesToCreate > 0) {
            Optional<Long> dataCollectionIssueRuleId = getDataCollectionIssueRuleId();
            if (dataCollectionIssueRuleId.isPresent()) {
                for (int i = 0; i < numberOfIssuesToCreate; i++) {
                    issueCreationService.processIssueCreationEvent(dataCollectionIssueRuleId.get(), new FakedDeviceCommunicationFailureEvent(device));
                }
            }
        }
        int numberOfAlarmsToCreate = new Random().nextInt(5);     // create 0 to 5 issues
        if (numberOfAlarmsToCreate > 0) {
            Optional<Long> alarmRuleId = getAlarmRuleId();
            if (alarmRuleId.isPresent()) {
                for (int i = 0; i < numberOfAlarmsToCreate; i++) {
                    // For creating an alarm a deviceEvent is expected
                    Instant eventTimestamp = clock.instant().minus(1, ChronoUnit.DAYS);
                    createDeviceEventForAlarm(device, eventTimestamp);
                    issueCreationService.processAlarmCreationEvent(alarmRuleId.get().intValue(), new FakedDeviceAlarmEvent(device, eventTimestamp), true);
                }
            }
        }
    }

    private Optional<Long> getDataCollectionIssueRuleId(){
        return getRuleId("DCI");
    }

    private Optional<Long> getAlarmRuleId(){
        return getRuleId("ALM");
    }

    private Optional<Long> getRuleId(String issueType){
        List<CreationRule> rules = issueCreationService.getCreationRuleQuery(IssueReason.class, IssueType.class).select(where("active").isEqualTo(true).and(where("reason.issueType.prefix").isEqualToIgnoreCase(issueType)));
        if (!rules.isEmpty()) {
            Collections.shuffle(rules);
            return Optional.of(rules.get(0).getId());
        }
        return Optional.empty();
    }

    private void createDeviceEventForAlarm(Device device, Instant eventTimeStamp){
         MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
         EndDeviceEventImpl endDeviceEvent = EndDeviceEventImpl.of("3.0.0.79", eventTimeStamp);
         endDeviceEvent.setMrid(device.getmRID());
         endDeviceEvent.setName(device.getName());
         endDeviceEvent.setAliasName(device.getSerialNumber());
         endDeviceEvent.setDescription("DEMO NETWORK TOPOLOGY");
         endDeviceEvent.setReason("DEMO PURPOSES");
         meterReading.addEndDeviceEvent(endDeviceEvent);
         device.store(meterReading);
     };

    /**
     * Faked DeviceCommunicationFailureEvent: stripped of for demo purposes
     */
    private class FakedDeviceCommunicationFailureEvent implements IssueEvent{

        private Optional<EndDevice> endDevice;
        private Optional<ConnectionTask<?, ?>> connectionTask = Optional.empty();
        // private Optional<ComSession> comSession = Optional.empty();
        private Optional<ComTaskExecution> comTaskExecution = Optional.empty();

        FakedDeviceCommunicationFailureEvent(Device device){
            Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
            if (amrSystemRef.isPresent()) {
                this.endDevice = Optional.ofNullable(amrSystemRef.get().findMeter(String.valueOf(device.getId())).get());
            }
            this.connectionTask = device.getConnectionTasks().stream().filter(ConnectionTask::isDefault).findFirst();
            List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
            Collections.shuffle(comTaskExecutions);
            this.comTaskExecution = comTaskExecutions.stream().findFirst();
        }

        @Override
        public String getEventType() {
            return "com/energyict/mdc/connectiontask/COMPLETION";
        }

        @Override
        public Optional<EndDevice> getEndDevice() {
            return endDevice;
        }

        @Override
        public Optional<? extends OpenIssue> findExistingIssue() {
            return Optional.empty();     // Always create a new Issue
        }

        @Override
        public void apply(Issue issue) {
            if (issue instanceof OpenIssueDataCollection) {
                OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
                connectionTask.ifPresent(dcIssue::setConnectionTask);
                comTaskExecution.ifPresent(dcIssue::setCommunicationTask);
            }
        }
    }

    private class FakedDeviceAlarmEvent extends EndDeviceEventCreatedEvent{

        private long endDeviceId;
        private String endDeviceEventType;
        private Instant eventTimestamp;


        FakedDeviceAlarmEvent(Device device, Instant eventTimestamp){
            super(deviceAlarmService, issueService, meteringService, deviceService, nlsService.getThesaurus(DeviceAlarmService.COMPONENT_NAME, Layer.DOMAIN), timeService, clock, injector);
            setDevice(device);
            this.endDeviceId = device.getId();
            this.endDeviceEventType = "3.0.0.79"; // "DEMO NETWORK TOPOLOGY";
            this.eventTimestamp = eventTimestamp;
        }

        @Override
        public void apply(Issue issue) {
            if (issue instanceof OpenDeviceAlarm) {
                OpenDeviceAlarm deviceAlarm = (OpenDeviceAlarm) issue;
                deviceAlarm.addRelatedAlarmEvent(endDeviceId, endDeviceEventType, eventTimestamp);
            }
        }
    }

}
