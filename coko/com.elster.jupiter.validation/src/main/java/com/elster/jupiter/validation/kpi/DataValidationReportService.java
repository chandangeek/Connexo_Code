package com.elster.jupiter.validation.kpi;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.validation.DataValidationStatus;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@ProviderType
public interface DataValidationReportService {

    Map<String, List<DataValidationStatus>> getRegisterSuspects(EndDeviceGroup deviceGroup, Range<Instant> range);

    Map<String, List<DataValidationStatus>> getChannelsSuspects(EndDeviceGroup deviceGroup, Range<Instant> range);

    Map<String, Boolean> getAllDataValidated(EndDeviceGroup deviceGroup, Range<Instant> range);

}