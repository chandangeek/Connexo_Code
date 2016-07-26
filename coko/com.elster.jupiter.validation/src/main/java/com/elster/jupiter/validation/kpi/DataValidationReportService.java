package com.elster.jupiter.validation.kpi;


import com.elster.jupiter.metering.groups.EndDeviceGroup;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public interface DataValidationReportService {

    Map<String, BigDecimal> getRegisterSuspects(EndDeviceGroup deviceGroup, Range<Instant> range);

    Map<String, BigDecimal> getChannelsSuspects(EndDeviceGroup deviceGroup, Range<Instant> range);

    Map<String, Boolean> getAllDataValidated(EndDeviceGroup deviceGroup, Range<Instant> range);

}
