package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.kpi.DataValidationReportService;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataValidationReportServiceImpl implements DataValidationReportService {

    private final ValidationService validationService;
    private final Clock clock;

    public DataValidationReportServiceImpl(ValidationService validationService, Clock clock){
        this.validationService = validationService;
        this.clock = clock;
    }

    @Override
    public Map<String, List<DataValidationStatus>> getRegisterSuspects(EndDeviceGroup deviceGroup, Range<Instant> range) {
        Map<String, List<DataValidationStatus>> registerSuspects = new HashMap<>();
        if(!validationService.getDataValidationAssociatinProviders().isEmpty()) {
            registerSuspects = deviceGroup.getMembers(Instant.now(clock)).stream()
                    .collect(Collectors.toMap(endDevice -> DataValidationKpiMemberTypes.REGISTER.fieldName() + endDevice.getId(),
                            endDevice -> validationService.getDataValidationAssociatinProviders()
                                    .get(0)
                                    .getRegisterSuspects(endDevice.getMRID(), range)));
        }
        return registerSuspects;
    }

    @Override
    public Map<String, List<DataValidationStatus>> getChannelsSuspects(EndDeviceGroup deviceGroup, Range<Instant> range) {
        Map<String, List<DataValidationStatus>> channelsSuspects = new HashMap<>();
        if(!validationService.getDataValidationAssociatinProviders().isEmpty()) {
            channelsSuspects = deviceGroup.getMembers(Instant.now(clock)).stream()
                    .collect(Collectors.toMap(endDevice -> DataValidationKpiMemberTypes.CHANNEL.fieldName() + endDevice.getId(),
                            endDevice -> validationService.getDataValidationAssociatinProviders()
                                    .get(0)
                                    .getChannelsSuspects(endDevice.getMRID(), range)));
        }
        return channelsSuspects;
    }

    @Override
    public Map<String, Boolean> getAllDataValidated(EndDeviceGroup deviceGroup, Range<Instant> range){
        Map<String, Boolean> allDataValidated = new HashMap<>();
        if(!validationService.getDataValidationAssociatinProviders().isEmpty()) {
            allDataValidated = deviceGroup.getMembers(Instant.now(clock)).stream()
                    .collect(Collectors.toMap(endDevice -> DataValidationKpiMemberTypes.ALLDATAVALIDATED.fieldName() + endDevice.getId(),
                            endDevice -> validationService.getDataValidationAssociatinProviders()
                                    .get(0)
                                    .isAllDataValidated(endDevice.getMRID(), range)));
        }
        return allDataValidated;
    }
}
