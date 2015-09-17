package com.energyict.mdc.multisense.api.redknee.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.multisense.api.redknee.impl.PrepaymentApplication;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.time.Clock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class MultisensePrepaymentApiJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    DeviceService deviceService;
    @Mock
    Clock clock;
    @Mock
    MeteringService meteringService;

    @Override
    protected Application getApplication() {
        PrepaymentApplication application = new PrepaymentApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceService(deviceService);
        application.setMeteringService(meteringService);
        application.setClock(clock);
        return application;
    }

//    public ReadingType mockReadingType(String mrid) {
//        ReadingType readingType = mock(ReadingType.class);
//        when(readingType.getMRID()).thenReturn(mrid);
//        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
//        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
//        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
//        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
//        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
//        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
//        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
//        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1, 2));
//        when(readingType.getArgument()).thenReturn(new RationalNumber(1, 2));
//        when(readingType.getTou()).thenReturn(3);
//        when(readingType.getCpp()).thenReturn(4);
//        when(readingType.getConsumptionTier()).thenReturn(5);
//        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
//        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
//        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
//        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
//        when(readingType.getCalculatedReadingType()).thenReturn(Optional.<ReadingType>empty());
//        when(readingType.isCumulative()).thenReturn(true);
//        return readingType;
//    }
//
//    <T> Finder<T> mockFinder(List<T> list) {
//        Finder<T> finder = mock(Finder.class);
//
//        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
//        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
//        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
//        when(finder.find()).thenReturn(list);
//        when(finder.stream()).thenReturn(list.stream());
//        return finder;
//    }


}