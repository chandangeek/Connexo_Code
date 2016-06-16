package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;

import javax.ws.rs.core.Application;
import java.time.Clock;

import org.mockito.Mock;

public class UsagePointConfigurationRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    Clock clock;
    @Mock
    JsonService jsonService;
    @Mock
    ValidationService validationService;
    @Mock
    UsagePointConfigurationService usagePointConfigurationService;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    MeteringService meteringService;
    @Mock
    TimeService timeService;

    @Override
    protected Application getApplication() {
        UsagePointConfigurationApplication application = new UsagePointConfigurationApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setJsonService(jsonService);
        application.setValidationService(validationService);
        application.setClockService(clock);
        application.setUsagePointConfigurationService(usagePointConfigurationService);
        application.setMetrologyConfigurationService(metrologyConfigurationService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setMeteringService(meteringService);
        application.setTimeService(timeService);
        return application;
    }
}