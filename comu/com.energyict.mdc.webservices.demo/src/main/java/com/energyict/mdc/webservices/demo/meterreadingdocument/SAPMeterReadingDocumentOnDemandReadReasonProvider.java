/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.webservices.demo.meterreadingdocument;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentCollectionData;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.RegistersTask;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Singleton;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;

@Singleton
@Component(name = "com.energyict.mdc.sap.meterreadingdocument.ondemandreadreason.provider",
        service = SAPMeterReadingDocumentReason.class, immediate = true, property = "name=ReadingReasonCode2")
public class SAPMeterReadingDocumentOnDemandReadReasonProvider implements SAPMeterReadingDocumentReason {

    private static final String READING_REASON_CODE = "2";

    private volatile DeviceService deviceService;

    @Reference
    public final void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public String getCode() {
        return READING_REASON_CODE;
    }

    @Override
    public boolean hasCollectionInterval() {
        return false;
    }

    @Override
    public boolean isBulk() {
        return false;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public void process(SAPMeterReadingDocumentCollectionData collectionData) {
        if (collectionData.isPastCase() || hasCommunicationConnection(collectionData.getServiceCall(),
                collectionData.getDeviceName(), collectionData.isRegular(), collectionData.getScheduledReadingDate())) {
            collectionData.calculate();
        }
    }

    private boolean hasCommunicationConnection(ServiceCall serviceCall, String deviceName, boolean isRegular,
                                               Instant scheduledReadingDate) {
        ComTaskExecution comTaskExecution = findLastTaskExecution(deviceName, isRegular);
        return hasLastTaskExecutionTimestamp(comTaskExecution, scheduledReadingDate)
                ? checkTaskStatus(serviceCall, comTaskExecution)
                : runTask(serviceCall, comTaskExecution);
    }

    private ComTaskExecution findLastTaskExecution(String deviceName, boolean isRegular) {
        return deviceService
                .findDeviceByName(deviceName)
                .map(Device::getComTaskExecutions)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(comTaskExecution -> comTaskExecution.getComTask().getProtocolTasks()
                        .stream()
                        .allMatch(protocolTask -> isRegular
                                ? protocolTask instanceof LoadProfilesTask
                                : protocolTask instanceof RegistersTask))
                .min(Comparator.nullsLast((e1, e2) -> e2.getLastSuccessfulCompletionTimestamp()
                        .compareTo(e1.getLastSuccessfulCompletionTimestamp())))
                .orElseThrow(IllegalArgumentException::new);
    }

    private boolean hasLastTaskExecutionTimestamp(ComTaskExecution comTaskExecution, Instant scheduledReadingDate) {
        return comTaskExecution.getLastExecutionStartTimestamp() != null &&
                scheduledReadingDate.isBefore(comTaskExecution.getLastExecutionStartTimestamp());
    }

    private boolean checkTaskStatus(ServiceCall serviceCall, ComTaskExecution comTaskExecution) {
        if (comTaskExecution.isOnHold()) {
            serviceCall.requestTransition(DefaultState.FAILED);
            return false;
        } else if (comTaskExecution.getStatus().equals(TaskStatus.Busy)) {
            serviceCall.requestTransition(DefaultState.PAUSED);
            return false;
        }
        return true;
    }

    private boolean runTask(ServiceCall serviceCall, ComTaskExecution comTaskExecution) {
        if (comTaskExecution.isOnHold()) {
            serviceCall.requestTransition(DefaultState.FAILED);
        } else {
            comTaskExecution.runNow();
            serviceCall.requestTransition(DefaultState.PAUSED);
        }
        return false;
    }
}