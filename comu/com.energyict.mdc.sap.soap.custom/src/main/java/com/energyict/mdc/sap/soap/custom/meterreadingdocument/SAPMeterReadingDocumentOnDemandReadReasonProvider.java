/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.meterreadingdocument;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.RegistersTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingComTaskExecutionHelper;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentCollectionData;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Singleton;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Singleton
@Component(name = "com.energyict.mdc.sap.soap.custom.meterreadingdocument.ondemandreadreason.provider",
        service = SAPMeterReadingDocumentReason.class, immediate = true, property = "name=ReadingReasonCode2")
public class SAPMeterReadingDocumentOnDemandReadReasonProvider implements SAPMeterReadingDocumentReason {

    private static final String REASON_CODES_ONDEMAND = "com.elster.jupiter.sap.reasoncodes.ondemand";
    private static final String REASON_CODES_ONDEMAND_DEFAULT_VALUE = "2";
    private static final String SCHEDULED_METER_READING_DATE_SHIFT_ONDEMAND = "com.elster.jupiter.sap.sheduledmeterreadingdateshift.ondemand";
    private static final int SCHEDULED_METER_READING_DATE_SHIFT_ONDEMAND_DEFAULT_VALUE = 1;

    private static List<String> codes;
    private static int dateShift = SCHEDULED_METER_READING_DATE_SHIFT_ONDEMAND_DEFAULT_VALUE;

    private volatile DeviceService deviceService;
    private volatile SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper;

    @Activate
    public void activate(BundleContext bundleContext) {
        String valueCodes = bundleContext.getProperty(REASON_CODES_ONDEMAND);
        if (Checks.is(valueCodes).emptyOrOnlyWhiteSpace()) {
            codes = Collections.singletonList(REASON_CODES_ONDEMAND_DEFAULT_VALUE);
        }else{
            codes = Arrays.asList((valueCodes.split(",")));
        }

        Optional.ofNullable(bundleContext.getProperty(SCHEDULED_METER_READING_DATE_SHIFT_ONDEMAND))
                .ifPresent(property->dateShift = Integer.valueOf(property));
    }


    @Reference
    public final void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setSapMeterReadingComTaskExecutionHelper(SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper) {
        this.sapMeterReadingComTaskExecutionHelper = sapMeterReadingComTaskExecutionHelper;
    }

    @Override
    public List<String> getCodes() {
        return codes;
    }

    @Override
    public boolean hasCollectionInterval() {
        return false;
    }

    @Override
    public long getShiftDate() {
        return dateShift* SECONDS_IN_DAY;
    }

    @Override
    public boolean shouldUseCurrentDateTime() {
        return true;
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
        if (hasCommunicationConnection(collectionData.getServiceCall(),
                collectionData.getDeviceName(), collectionData.isRegular(), collectionData.getScheduledReadingDate())) {
            collectionData.calculate();
        }
    }

    private boolean hasCommunicationConnection(ServiceCall serviceCall, String deviceName, boolean isRegular,
                                               Instant scheduledReadingDate) {
        Optional<ComTaskExecution> comTaskExecution = findLastTaskExecution(deviceName, isRegular);

        if(comTaskExecution.isPresent()) {
            return hasLastTaskExecutionTimestamp(comTaskExecution.get(), scheduledReadingDate)
                    ? checkTaskStatus(serviceCall, comTaskExecution.get())
                    : runTask(serviceCall, comTaskExecution.get());
        }else{
            serviceCall.log(LogLevel.SEVERE, "A communication task to execute the device messages couldn't be located");
            serviceCall.transitionWithLockIfPossible(DefaultState.WAITING);
            return false;
        }
    }

    private Optional<ComTaskExecution> findLastTaskExecution(String deviceName, boolean isRegular) {
        return deviceService
                .findDeviceByName(deviceName)
                .map(Device::getComTaskExecutions)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(comTaskExecution -> comTaskExecution.getComTask().isManualSystemTask())
                .filter(comTaskExecution -> comTaskExecution.getComTask().getProtocolTasks()
                        .stream()
                        .allMatch(protocolTask -> isRegular
                                ? protocolTask instanceof LoadProfilesTask
                                : protocolTask instanceof RegistersTask))
                .min(Comparator.nullsLast((e1, e2) -> e2.getLastSuccessfulCompletionTimestamp()
                        .compareTo(e1.getLastSuccessfulCompletionTimestamp())));
    }

    private boolean hasLastTaskExecutionTimestamp(ComTaskExecution comTaskExecution, Instant scheduledReadingDate) {
        return comTaskExecution.getLastExecutionStartTimestamp() != null &&
                scheduledReadingDate.isBefore(comTaskExecution.getLastExecutionStartTimestamp());
    }

    private boolean checkTaskStatus(ServiceCall serviceCall, ComTaskExecution comTaskExecution) {
        if (comTaskExecution.isOnHold()) {
            serviceCall.log(LogLevel.SEVERE, "The communication task '" + comTaskExecution.getComTask().getName() +
                    "' is inactive on device '" + comTaskExecution.getDevice().getName() + "'");
            serviceCall.transitionWithLockIfPossible(DefaultState.WAITING);
            return false;
        } else if (comTaskExecution.getStatus().equals(TaskStatus.Busy)) {
            sapMeterReadingComTaskExecutionHelper.setComTaskExecutionId(serviceCall, comTaskExecution.getId());
            return false;
        }
        return true;
    }

    private boolean runTask(ServiceCall serviceCall, ComTaskExecution comTaskExecution) {
        if (comTaskExecution.isOnHold()) {
            serviceCall.log(LogLevel.SEVERE, "The communication task '" + comTaskExecution.getComTask().getName() +
                    "' is inactive on device '" + comTaskExecution.getDevice().getName() + "'");
            serviceCall.transitionWithLockIfPossible(DefaultState.WAITING);
        } else {
            sapMeterReadingComTaskExecutionHelper.setComTaskExecutionId(serviceCall, comTaskExecution.getId());
            comTaskExecution.runNow();
        }
        return false;
    }
}