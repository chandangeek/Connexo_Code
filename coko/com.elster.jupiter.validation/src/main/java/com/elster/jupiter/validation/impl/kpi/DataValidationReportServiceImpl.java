package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.kpi.DataValidationReportService;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class DataValidationReportServiceImpl implements DataValidationReportService {

    private final ValidationService validationService;

    public DataValidationReportServiceImpl(ValidationService validationService){
        this.validationService = validationService;
    }

    @Override
    public Map<String, BigDecimal> getRegisterSuspects(EndDeviceGroup deviceGroup, Range<Instant> range) {
        Map<String, BigDecimal> registerSuspects = new HashMap<>();
        if(!validationService.getDataValidationAssociatinProviders().isEmpty()) {
            registerSuspects = deviceGroup.getMembers(Instant.now()).stream()
                    .collect(Collectors.toMap(endDevice -> DataValidationKpiImpl.DataValidationKpiMembers.REGISTER.fieldName() + endDevice.getId(),
                            endDevice -> validationService.getDataValidationAssociatinProviders()
                                    .get(0)
                                    .getRegisterSuspects(endDevice.getMRID(), range)));
        }
        return registerSuspects;
    }

    @Override
    public Map<String, BigDecimal> getChannelsSuspects(EndDeviceGroup deviceGroup, Range<Instant> range) {
        Map<String, BigDecimal> channelsSuspects = new HashMap<>();
        if(!validationService.getDataValidationAssociatinProviders().isEmpty()) {
            channelsSuspects = deviceGroup.getMembers(Instant.now()).stream()
                    .collect(Collectors.toMap(endDevice -> DataValidationKpiImpl.DataValidationKpiMembers.CHANNELS.fieldName() + endDevice.getId(),
                            endDevice -> validationService.getDataValidationAssociatinProviders()
                                    .get(0)
                                    .getChannelsSuspects(endDevice.getMRID(), range)));
        }
        return channelsSuspects;
    }
}
