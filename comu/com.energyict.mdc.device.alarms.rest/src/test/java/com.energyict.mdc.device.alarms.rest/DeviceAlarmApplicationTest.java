package com.energyict.mdc.device.alarms.rest;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.masterdata.LogBookType;

import javax.ws.rs.core.Application;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.mockito.Mock;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceAlarmApplicationTest extends FelixRestApplicationJerseyTest {

    private final Instant now = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();

    @Mock
    DeviceService deviceService;
    @Mock
    DeviceAlarmService deviceAlarmService;
    @Mock
    LogBookService logBookService;

    @Override
    protected Application getApplication() {
        DeviceAlarmApplication deviceAlarmApplication = new DeviceAlarmApplication();
        deviceAlarmApplication.setTransactionService(transactionService);
        deviceAlarmApplication.setDeviceService(deviceService);
        deviceAlarmApplication.setDeviceAlarmService(deviceAlarmService);
        deviceAlarmApplication.setLogBookService(logBookService);
        return deviceAlarmApplication;
    }

    protected IssueStatus mockStatus(String key, String name, boolean isFinal) {
        IssueStatus status = mock(IssueStatus.class);
        when(status.isHistorical()).thenReturn(isFinal);
        when(status.getName()).thenReturn(name);
        when(status.getKey()).thenReturn(key);
        return status;
    }

    protected IssueStatus getDefaultStatus() {
        return mockStatus("1", "open", false);
    }

    protected IssueReason mockReason(String key, String name) {
        IssueReason reason = mock(IssueReason.class);
        when(reason.getKey()).thenReturn(key);
        when(reason.getName()).thenReturn(name);
        return reason;
    }

    protected IssueReason getDefaultReason() {
        return mockReason("1", "Reason");
    }


    protected IssueAssignee mockAssignee(long userId, String userName, long workGroupId, String workGroupName){
        IssueAssignee assignee = mock(IssueAssignee.class);
        User user = mock(User.class);
        WorkGroup workGroup = mock(WorkGroup.class);
        when(workGroup.getId()).thenReturn(workGroupId);
        when(workGroup.getName()).thenReturn(workGroupName);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(userName);
        when(assignee.getUser()).thenReturn(user);
        when(assignee.getWorkGroup()).thenReturn(workGroup);
        return assignee;
    }

    protected IssueAssignee getDefaultAssignee() {
        return mockAssignee(1L, "Admin", 1L, "WorkGroup");
    }

    protected Meter mockDevice(long id, String name) {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(id);
        when(meter.getAmrId()).thenReturn(String.valueOf(id));
        when(meter.getSerialNumber()).thenReturn("0.0.0.0.0.0.0.0");
        Optional<? extends MeterActivation> optionalMA = Optional.empty();
        doReturn(optionalMA).when(meter).getCurrentMeterActivation();
        AmrSystem amrSystem = mock(AmrSystem.class);
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(amrSystem.is(KnownAmrSystem.MDC)).thenReturn(true);
        return meter;
    }

    protected DeviceAlarm getDefaultAlarm() {
        return mockAlarm(1L, getDefaultReason(), getDefaultStatus(), getDefaultAssignee(), getDeviceAlarmRelatedEvent(), mockDevice(1, "DefaultDevice"));
    }

    protected DeviceAlarm mockAlarm(long id, IssueReason reason, IssueStatus status, IssueAssignee assingee, List<DeviceAlarmRelatedEvent> events, Meter meter) {
        DeviceAlarm alarm = mock(DeviceAlarm.class);
        when(alarm.getId()).thenReturn(id);
        when(alarm.getIssueId()).thenReturn("ALM-001");
        when(alarm.isStatusCleared()).thenReturn(true);
        when(alarm.getReason()).thenReturn(reason);
        when(alarm.getStatus()).thenReturn(status);
        when(alarm.getDueDate()).thenReturn(null);
        when(alarm.getDevice()).thenReturn(meter);
        when(alarm.getAssignee()).thenReturn(assingee);
        when(alarm.getCreateTime()).thenReturn(Instant.EPOCH);
        when(alarm.getModTime()).thenReturn(Instant.EPOCH);
        when(alarm.getVersion()).thenReturn(1L);
        when(alarm.getDeviceAlarmRelatedEvents()).thenReturn(events);
        return alarm;
    }

    protected List<DeviceAlarmRelatedEvent> getDeviceAlarmRelatedEvent(){
        DeviceAlarmRelatedEvent event = mock(DeviceAlarmRelatedEvent.class);
        EndDeviceEventRecord eventRecord = mock(EndDeviceEventRecord.class);
        LogBookType logBookType = mock(LogBookType.class);
        LogBook logBook = mock(LogBook.class);
        EndDeviceEventType endDeviceEventType = mock(EndDeviceEventType.class);

        when(event.getEventRecord()).thenReturn(eventRecord);
        when(eventRecord.getCreateTime()).thenReturn(now);
        when(eventRecord.getLogBookId()).thenReturn(1L);
        when(logBookService.findById(anyLong())).thenReturn(Optional.of(logBook));
        when(logBook.getId()).thenReturn(1L);
        when(logBook.getLogBookType()).thenReturn(logBookType);
        when(logBookType.getName()).thenReturn("LogBookName");
        when(eventRecord.getEventType()).thenReturn(endDeviceEventType);
        when(endDeviceEventType.getType()).thenReturn(EndDeviceType.COLLECTOR);
        when(endDeviceEventType.getDomain()).thenReturn(EndDeviceDomain.BATTERY);
        when(endDeviceEventType.getSubDomain()).thenReturn(EndDeviceSubDomain.ACTIVATION);
        when(endDeviceEventType.getEventOrAction()).thenReturn(EndDeviceEventOrAction.ABORTED);

        return Collections.singletonList(event);
    }

}
