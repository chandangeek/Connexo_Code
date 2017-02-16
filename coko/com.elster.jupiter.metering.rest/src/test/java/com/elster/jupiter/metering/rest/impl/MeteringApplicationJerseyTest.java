package com.elster.jupiter.metering.rest.impl;

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
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Clock;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeteringApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    static SecurityContext securityContext;
    @Mock
    MeteringService meteringService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    Clock clock;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;
    @Mock
    LicenseService licenseService;
    @Mock
    LocationService locationService;
    @Mock
    ThreadPrincipalService threadPrincipalService;


    ReadingTypeInfoFactory readingTypeInfoFactory;



    @Provider
    @Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }

    @Override
    protected Application getApplication() {
        when(thesaurus.join(any(Thesaurus.class))).thenReturn(thesaurus);
        MeteringApplication app = new MeteringApplication() {
            //to mock security context
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> hashSet = new HashSet<>(super.getClasses());
                hashSet.add(SecurityRequestFilter.class);
                return Collections.unmodifiableSet(hashSet);
            }
        };
        app.setClock(clock);
        app.setTransactionService(transactionService);
        app.setRestQueryService(restQueryService);
        app.setMeteringService(meteringService);
        app.setNlsService(nlsService);
        app.setServiceCallService(serviceCallService);
        app.setMetrologyConfigurationService(metrologyConfigurationService);
        app.setLicenseService(licenseService);
        app.setPropertyValueInfoService(propertyValueInfoService);
        app.setLocationService(locationService);
        app.setThreadPrincipalService(threadPrincipalService);
        return app;
    }

    protected ReadingType mockReadingType(String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
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