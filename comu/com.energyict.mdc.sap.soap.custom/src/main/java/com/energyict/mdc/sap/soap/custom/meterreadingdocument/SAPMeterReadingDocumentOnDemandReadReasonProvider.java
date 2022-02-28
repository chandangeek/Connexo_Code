/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.meterreadingdocument;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.common.tasks.RegistersTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingComTaskExecutionHelper;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentCollectionData;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;

import com.google.common.collect.ImmutableList;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
@Component(name = "com.energyict.mdc.sap.soap.custom.meterreadingdocument.ondemandreadreason.provider",
        service = SAPMeterReadingDocumentReason.class, immediate = true, property = "name=ReadingReasonCode2")
public class SAPMeterReadingDocumentOnDemandReadReasonProvider implements SAPMeterReadingDocumentReason {
    private static final Logger LOGGER = Logger.getLogger(SAPMeterReadingDocumentOnDemandReadReasonProvider.class.getName());
    private static final String REASON_CODES_ONDEMAND = "com.elster.jupiter.sap.reasoncodes.ondemand";
    private static final String REASON_CODES_ONDEMAND_DEFAULT_VALUE = "2";
    private static final String SCHEDULED_METER_READING_DATE_SHIFT_ONDEMAND = "com.elster.jupiter.sap.sheduledmeterreadingdateshift.ondemand";
    private static final int SCHEDULED_METER_READING_DATE_SHIFT_ONDEMAND_DEFAULT_VALUE = 1;
    private static final String ONDEMAND_SKIP_COMMUNICATION = "com.elster.jupiter.sap.ondemand.skipcommunication";
    private static final List<String> DATA_SOURCE_TYPE_CODES = ImmutableList.of("1");

    private static List<String> reasonCodeCodes;
    private static int dateShift = SCHEDULED_METER_READING_DATE_SHIFT_ONDEMAND_DEFAULT_VALUE;
    private static List<CIMCodePattern> skipCommunicationPatterns;

    private volatile DeviceService deviceService;
    private volatile SAPMeterReadingComTaskExecutionHelper sapMeterReadingComTaskExecutionHelper;

    @Activate
    public void activate(BundleContext bundleContext) {
        initReasonCodes(bundleContext);
        initDateShift(bundleContext);
        initSkipCommunicationWaterMeters(bundleContext);
    }

    private void initReasonCodes(BundleContext bundleContext) {
        String valueCodes = bundleContext.getProperty(REASON_CODES_ONDEMAND);
        reasonCodeCodes = Checks.is(valueCodes).emptyOrOnlyWhiteSpace() ?
                Collections.singletonList(REASON_CODES_ONDEMAND_DEFAULT_VALUE) :
                Arrays.asList(valueCodes.split(","));
    }

    private void initDateShift(BundleContext bundleContext) {
        Optional.ofNullable(bundleContext.getProperty(SCHEDULED_METER_READING_DATE_SHIFT_ONDEMAND))
                .ifPresent(property -> dateShift = Integer.valueOf(property));
    }

    private void initSkipCommunicationWaterMeters(BundleContext bundleContext) {
        try {
            String value = bundleContext.getProperty(ONDEMAND_SKIP_COMMUNICATION);
            skipCommunicationPatterns = Checks.is(value).emptyOrOnlyWhiteSpace() ?
                    Collections.emptyList() :
                    Arrays.stream(value.split(","))
                            .map(String::trim)
                            .map(item -> {
                                String[] codes = item.split("\\.");
                                return CIMCodePattern.parseFromString(codes);
                            })
                            .collect(Collectors.toList());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error while loading property " + ONDEMAND_SKIP_COMMUNICATION +
                    ": " + ex.getLocalizedMessage());
            skipCommunicationPatterns = Collections.emptyList();
        }
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
    public List<String> getReasonCodeCodes() {
        return reasonCodeCodes;
    }

    @Override
    public List<String> getDataSourceTypeCodeCodes() {
        return DATA_SOURCE_TYPE_CODES;
    }

    @Override
    public boolean hasCollectionInterval() {
        return false;
    }

    @Override
    public long getShiftDate() {
        return dateShift * SECONDS_IN_DAY;
    }

    @Override
    public Optional<Pair<String, String>> getExtraDataSourceMacroAndMeasuringCodes() {
        return Optional.empty();
    }

    @Override
    public boolean shouldUseCurrentDateTime() {
        return true;
    }

    @Override
    public void process(SAPMeterReadingDocumentCollectionData collectionData) {
        if (hasCommunicationConnection(collectionData)) {
            collectionData.calculate();
        }
    }

    @Override
    public boolean validateComTaskExecutionIfNeeded(Device device, boolean isRegular, ReadingType readingType) {
        if (skipCommunicationForWaterMeters(readingType)) {
            return true;
        } else {
            return findOrCreateComTaskExecution(device, isRegular, readingType).isPresent();
        }
    }

    private Optional<ComTaskExecution> findOrCreateComTaskExecution(Device device, boolean isRegular, ReadingType readingType) {
        Optional<ComTaskExecution> comTaskExecution = findLastTaskExecution(device, isRegular, readingType);
        ComTaskExecution execution = null;
        if (comTaskExecution.isPresent()) {
            execution = comTaskExecution.get();
        } else {
            Optional<ComTaskEnablement> comTaskEnablement = getComTaskEnablementForDevice(device, isRegular, readingType);
            if (comTaskEnablement.isPresent()) {
                execution = createAdHocComTaskExecution(device, comTaskEnablement.get());
            }
        }
        return Optional.ofNullable(execution);
    }

    private boolean hasCommunicationConnection(SAPMeterReadingDocumentCollectionData collectionData) {
        ServiceCall serviceCall = collectionData.getServiceCall();
        String deviceName = collectionData.getDeviceName();
        boolean isRegular = collectionData.isRegular();
        Instant scheduledReadingDate = collectionData.getScheduledReadingDate();
        Optional<ReadingType> meterReadingType = collectionData.getMeterReadingType();

        if (!meterReadingType.isPresent()) {
            // unreachable case
            serviceCall.log(LogLevel.SEVERE, "A reading type isn't defined for finding communication task");
            serviceCall.transitionWithLockIfPossible(DefaultState.WAITING);
            return false;
        }

        if (skipCommunicationForWaterMeters(meterReadingType.get())) {
            return true;
        }

        Optional<Device> device = deviceService.findDeviceByName(deviceName);
        if (device.isPresent()) {
            Optional<ComTaskExecution> comTaskExecution = findOrCreateComTaskExecution(device.get(), isRegular, meterReadingType.get());

            if (comTaskExecution.isPresent()) {
                return hasLastTaskExecutionTimestamp(comTaskExecution.get(), scheduledReadingDate)
                        ? checkTaskStatus(serviceCall, (comTaskExecution.get()))
                        : runTask(serviceCall, comTaskExecution.get());
            } else {
                serviceCall.log(LogLevel.SEVERE, "A communication task to execute the device messages couldn't be located");
                serviceCall.transitionWithLockIfPossible(DefaultState.WAITING);
                return false;
            }
        } else {
            serviceCall.log(LogLevel.SEVERE, "Couldn't find device " + deviceName);
            serviceCall.transitionWithLockIfPossible(DefaultState.WAITING);
            return false;
        }

    }

    private boolean skipCommunicationForWaterMeters(ReadingType readingType) {
        return skipCommunicationPatterns.stream().anyMatch(cimCodePattern -> cimCodePattern.matches(readingType));
    }

    private Optional<ComTaskExecution> findLastTaskExecution(Device device, boolean isRegular, ReadingType readingType) {
        return device.getComTaskExecutions()
                .stream()
                .filter(comTaskExecution -> comTaskExecution.getComTask().isManualSystemTask())
                .filter(comTaskExecution -> comTaskExecution.getComTask().getProtocolTasks()
                        .stream()
                        .anyMatch(protocolTask -> {
                            if (isRegular) {
                                return protocolTask instanceof LoadProfilesTask && hasReadingType((LoadProfilesTask) protocolTask, readingType);
                            } else {
                                return protocolTask instanceof RegistersTask && hasReadingType((RegistersTask) protocolTask, readingType);
                            }
                        }))
                .min(Comparator.nullsLast((e1, e2) -> e2.getLastSuccessfulCompletionTimestamp()
                        .compareTo(e1.getLastSuccessfulCompletionTimestamp())));
    }

    private Optional<ComTaskEnablement> getComTaskEnablementForDevice(Device device, boolean isRegular, ReadingType readingType) {
        return device.getDeviceConfiguration()
                .getComTaskEnablements()
                .stream()
                .filter(cte -> cte.getComTask().isManualSystemTask())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks()
                        .stream()
                        .anyMatch(protocolTask -> {
                            if (isRegular) {
                                return protocolTask instanceof LoadProfilesTask && hasReadingType((LoadProfilesTask) protocolTask, readingType);
                            } else {
                                return protocolTask instanceof RegistersTask && hasReadingType((RegistersTask) protocolTask, readingType);
                            }
                        }))
                .findFirst();
    }

    private ComTaskExecution createAdHocComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .findFirst()
                    .ifPresent(comTaskExecutionBuilder::connectionTask);
        }
        ComTaskExecution manuallyScheduledComTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
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

    private boolean hasReadingType(LoadProfilesTask loadProfilesTask, ReadingType readingType) {
        return loadProfilesTask.getLoadProfileTypes().stream()
                .anyMatch(loadProfile -> loadProfile.getChannelTypes().stream()
                        .anyMatch(channelType -> channelType.getReadingType().equals(readingType)));
    }

    private boolean hasReadingType(RegistersTask registersTask, ReadingType readingType) {
        return registersTask.getRegisterGroups().stream()
                .anyMatch(registerGroup -> registerGroup.getRegisterTypes().stream()
                        .anyMatch(registerType -> registerType.getReadingType().equals(readingType)));
    }
}
