package com.elster.jupiter.validation.kpi;


import com.elster.jupiter.metering.groups.EndDeviceGroup;

import java.math.BigDecimal;
import java.util.Map;

public interface DataValidationReportService {

    Map<String, BigDecimal> getRegisterSuspects(EndDeviceGroup deviceGroup);

    Map<String, BigDecimal> getChannelsSuspects(EndDeviceGroup deviceGroup);


}
