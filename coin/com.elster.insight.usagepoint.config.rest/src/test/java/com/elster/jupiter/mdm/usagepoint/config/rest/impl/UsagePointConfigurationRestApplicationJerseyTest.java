package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.ValidationService;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Currency;

import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 */
public class UsagePointConfigurationRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    static long firmwareComTaskId = 445632136865L;

    @Mock
    Clock clock;
    @Mock
    JsonService jsonService;
    @Mock
    ValidationService validationService;
    @Mock
    UsagePointConfigurationService usagePointConfigurationService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    MeteringService meteringService;

    @Override
    protected Application getApplication() {
        UsagePointConfigurationApplication application = new UsagePointConfigurationApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setJsonService(jsonService);
        application.setValidationService(validationService);
        application.setClockService(clock);
        application.setUsagePointConfigurationService(usagePointConfigurationService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setMeteringService(meteringService);
        return application;
    }

    public ReadingType mockReadingType(String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        return readingType;
    }

}