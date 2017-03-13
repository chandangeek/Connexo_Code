/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

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
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.devtools.tests.Matcher;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandCustomPropertySet;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyValueInfoService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import com.google.common.collect.Range;

import javax.ws.rs.core.Application;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/19/14.
 */
public class PlatformPublicApiJerseyTest extends FelixRestApplicationJerseyTest {

    public static final String MRID = "e9c161ce-2734-4b65-992b-8aec57acea7b";

    public static final Clock clock = Clock.fixed(LocalDateTime.of(2016, 5, 1, 12, 0).toInstant(ZoneOffset.UTC), ZoneId.systemDefault());
    @Mock
    MeteringService meteringService;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    MessageService messageService;
    @Mock
    ServiceCallService serviceCallService;
    @Mock
    EffectiveMetrologyConfigurationInfoFactory effectiveMetrologyConfigurationInfoFactory;
    @Mock
    MeterActivationInfoFactory meterActivationInfoFactory;
    @Mock
    PropertySpecService propertySpecService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;

    @Override
    protected Application getApplication() {
        PublicRestApplication application = new PublicRestApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setClock(clock);
        application.setCustomPropertySetService(customPropertySetService);
        application.setMetrologyConfigurationService(metrologyConfigurationService);
        application.setMeteringService(meteringService);
        application.setMessageService(messageService);
        application.setServiceCallService(serviceCallService);
        application.setPropertyValueInfoService(propertyValueInfoService);
        return application;
    }

    public ReadingType mockReadingType(String mrid) {
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
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.<ReadingType>empty());
        when(readingType.isCumulative()).thenReturn(true);
        when(readingType.getVersion()).thenReturn(3333L);
        return readingType;
    }

    protected UsagePoint mockUsagePoint(String mRID, long version, ServiceKind serviceKind) {
        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        when(extension.getAllPropertySets()).thenReturn(Collections.emptyList());
        UsagePointDetail detail;
        switch (serviceKind) {
            case ELECTRICITY:
                detail = mock(ElectricityDetail.class);
                break;
            case GAS:
                detail = mock(GasDetail.class);
                break;
            case WATER:
                detail = mock(WaterDetail.class);
                break;
            case HEAT:
                detail = mock(HeatDetail.class);
                break;
            default:
                throw new IllegalArgumentException("Service kind is not supported");
        }
        when(detail.getRange()).thenReturn(Range.atLeast(clock.instant()));
        return mockUsagePoint(mRID, version, extension, serviceKind, detail);
    }

    protected UsagePoint mockUsagePoint(String mRID, long version, ServiceKind serviceKind, UsagePointDetail detail) {
        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        when(extension.getAllPropertySets()).thenReturn(Collections.emptyList());
        return mockUsagePoint(mRID, version, extension, serviceKind, detail);
    }

    private UsagePoint mockUsagePoint(String mRID, long version, UsagePointCustomPropertySetExtension extension, ServiceKind serviceKind, UsagePointDetail detail) {
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getLocation()).thenReturn(Optional.empty());
        when(usagePoint.getVersion()).thenReturn(version);
        when(usagePoint.getMRID()).thenReturn(mRID);
        when(usagePoint.getAliasName()).thenReturn("alias " + mRID);
        when(usagePoint.getDescription()).thenReturn("usage point desc");
        when(usagePoint.getOutageRegion()).thenReturn("outage region");
        when(usagePoint.getReadRoute()).thenReturn("read route");
        when(usagePoint.getServiceLocationString()).thenReturn("location");
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getKind()).thenReturn(serviceKind);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        doReturn(Optional.ofNullable(detail)).when(usagePoint).getDetail(any(Instant.class));
        doReturn(Collections.singletonList(detail)).when(usagePoint).getDetails();
        doReturn(Collections.singletonList(detail)).when(usagePoint).getDetail(eq(Range.all()));
        when(usagePoint.getInstallationTime()).thenReturn(LocalDateTime.of(2016, 3, 20, 11, 0).toInstant(ZoneOffset.UTC));
        when(usagePoint.getServiceDeliveryRemark()).thenReturn("remark");
        when(usagePoint.getServicePriority()).thenReturn("service priority");
        when(usagePoint.getEffectiveMetrologyConfiguration(any())).thenReturn(Optional.empty());
        when(usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        UsagePointConnectionState usagePointConnectionState = mockUsagePointConnectionState(ConnectionState.CONNECTED);
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.of(usagePointConnectionState));

        when(usagePoint.forCustomProperties()).thenReturn(extension);
        when(meteringService.findUsagePointByMRID(mRID)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByMRIDAndVersion(eq(mRID), longThat(Matcher.matches(v -> v != version)))).thenReturn(Optional.empty());
        when(meteringService.findAndLockUsagePointByMRIDAndVersion(mRID, version)).thenReturn(Optional.of(usagePoint));
        when(detail.getUsagePoint()).thenReturn(usagePoint);
        return usagePoint;
    }

    protected UsagePointConnectionState mockUsagePointConnectionState(ConnectionState connectionState) {
        UsagePointConnectionState usagePointConnectionState = mock(UsagePointConnectionState.class);
        when(usagePointConnectionState.getConnectionState()).thenReturn(connectionState);
        return usagePointConnectionState;
    }

    protected UsagePointPropertySet mockUsagePointPropertySet(long id, CustomPropertySet cps, UsagePoint usagePoint, UsagePointCustomPropertySetExtension extension) {
        UsagePointPropertySet mock = mock(UsagePointPropertySet.class);
        when(mock.getUsagePoint()).thenReturn(usagePoint);
        when(mock.getCustomPropertySet()).thenReturn(cps);
        when(mock.getId()).thenReturn(id);
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty("name", "Valerie");
        values.setProperty("age", BigDecimal.valueOf(21));
        when(mock.getValues()).thenReturn(values);
        when(extension.getPropertySet(id)).thenReturn(mock);
        return mock;
    }


    protected UsagePointMetrologyConfiguration mockMetrologyConfiguration(long id, String name, long version) {
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(id);
        when(metrologyConfiguration.getName()).thenReturn(name);
        when(metrologyConfiguration.getVersion()).thenReturn(version);
        when(metrologyConfigurationService.findMetrologyConfiguration(id)).thenReturn(Optional.of(metrologyConfiguration));
        return metrologyConfiguration;
    }

    PropertySpec mockBigDecimalPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("decimal.property");
        when(propertySpec.getValueFactory()).thenReturn(new BigDecimalFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn(BigDecimal.ONE);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        return propertySpec;
    }

    PropertySpec mockStringPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getName()).thenReturn("string.property");
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getDefault()).thenReturn("default");
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);

        return propertySpec;
    }

    <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

    protected void mockCommands() {
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        UsagePointCommandCustomPropertySet customPropertySet = new UsagePointCommandCustomPropertySet(propertySpecService, thesaurus);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        ServiceCall serviceCall = mock(ServiceCall.class);
        ServiceCallType serviceCallType = mock(ServiceCallType.class);
        when(serviceCallService.findServiceCallType(anyString(), anyString())).thenReturn(Optional.of(serviceCallType));
        ServiceCallBuilder serviceCallBuilder = mock(ServiceCallBuilder.class);
        when(serviceCallType.newServiceCall()).thenReturn(serviceCallBuilder);
        when(serviceCallBuilder.origin(anyString())).thenReturn(serviceCallBuilder);
        when(serviceCallBuilder.extendedWith(any())).thenReturn(serviceCallBuilder);
        when(serviceCallBuilder.targetObject(any())).thenReturn(serviceCallBuilder);
        when(serviceCallBuilder.create()).thenReturn(serviceCall);

        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(messageService.getDestinationSpec(anyString())).thenReturn(Optional.of(destinationSpec));

        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        when(destinationSpec.message(anyString())).thenReturn(messageBuilder);
    }

}
