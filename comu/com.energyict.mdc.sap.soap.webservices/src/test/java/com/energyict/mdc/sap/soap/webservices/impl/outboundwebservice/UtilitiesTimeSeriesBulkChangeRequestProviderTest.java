package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.MeterReadingValidationData;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.ReadingNumberPerMessageProvider;
import com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.UtilitiesTimeSeriesBulkChangeRequestProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilitiesTimeSeriesERPItemBulkChangeRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilsTmeSersERPItmBulkChgReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeRangeSet;
import com.google.inject.AbstractModule;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UtilitiesTimeSeriesBulkChangeRequestProviderTest extends AbstractOutboundWebserviceTest<UtilitiesTimeSeriesERPItemBulkChangeRequestCOut> {
    private static final String DEVICE_NAME_1 = "DEVICE_1";
    private static final String MRID_1 = "MRID_1";
    private static final String DEVICE_NAME_2 = "DEVICE_2";
    private static final String MRID_2 = "MRID_2";

    @Mock
    private DataExportWebService.ExportContext exportContext;
    @Mock
    private SAPCustomPropertySets sapCustomPropertySets;
    @Mock
    private Clock clock;
    @Mock
    private WebServiceActivator webServiceActivator;
    @Mock
    private DeviceService deviceService;
    @Mock
    private ReadingNumberPerMessageProvider readingNumberPerMessageProvider;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadingType readingType;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MeterReading meterReading1, meterReading2;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Channel channel1, channel2;
    @Mock
    private Reading reading1, reading2, reading3, reading4, reading5, reading6;
    private MeterReadingValidationData validationData = new MeterReadingValidationData(Collections.emptyMap());

    private List<MeterReadingData> dataExportList = new ArrayList<>();
    private Map<String, RangeSet<Instant>> profileIdIntervals = new HashMap<>();
    private RangeSet<Instant> rangeSet = TreeRangeSet.create();
    private Map<Instant, String> readingStatuses = new HashMap<>();

    private UtilitiesTimeSeriesBulkChangeRequestProvider provider;

    @Before
    public void setUp() {
        List<Reading> readingsList1 = new ArrayList<>();
        List<Reading> readingsList2 = new ArrayList<>();

        List<ChannelsContainer> channelsContainerList1 = new ArrayList<>();
        List<ChannelsContainer> channelsContainerList2 = new ArrayList<>();

        rangeSet.add(Range.open(Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS)));
        profileIdIntervals.put("ProfileID", rangeSet);

        /*Mock sapCustomPropertySet. It is needed for getTimeSlicedProfileId*/
        when(sapCustomPropertySets.getProfileId(anyObject(), anyObject())).thenReturn(profileIdIntervals);

        /* Configure number of readings per one message */
        when(readingNumberPerMessageProvider.getNumberOfReadingsPerMsg()).thenReturn(2);

        provider = getProviderInstance(UtilitiesTimeSeriesBulkChangeRequestProvider.class,  new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(SAPCustomPropertySets.class).toInstance(sapCustomPropertySets);
                bind(ReadingNumberPerMessageProvider.class).toInstance(readingNumberPerMessageProvider);
                bind(PropertySpecService.class).toInstance(mock(PropertySpecService.class));
                bind(DataExportServiceCallType.class).toInstance(mock(DataExportServiceCallType.class));
                bind(WebServiceActivator.class).toInstance(webServiceActivator);
                bind(DeviceService.class).toInstance(deviceService);
            }
        });
        when(webServiceActivator.getMeteringSystemId()).thenReturn("HON");

        /* Prepare reading for 1st data*/
        when(reading1.getTimeStamp()).thenReturn(Instant.now());
        when(reading1.getValue()).thenReturn(new BigDecimal(100500));
        readingsList1.add(reading1);

        when(reading2.getTimeStamp()).thenReturn(Instant.now());
        when(reading2.getValue()).thenReturn(new BigDecimal(100501));
        readingsList1.add(reading2);

        when(reading3.getTimeStamp()).thenReturn(Instant.now());
        when(reading3.getValue()).thenReturn(new BigDecimal(100502));
        readingsList1.add(reading3);

        when(meterReading1.getIntervalBlocks()).thenReturn(Collections.emptyList());
        when(meterReading1.getReadings()).thenReturn(readingsList1);

        MeterReadingData meterReadingData1 = mock(MeterReadingData.class, RETURNS_DEEP_STUBS);
        when(meterReadingData1.getItem().getDomainObject().getName()).thenReturn(DEVICE_NAME_1);
        when(meterReadingData1.getItem().getReadingType().getMRID()).thenReturn(MRID_1);
        when(meterReadingData1.getItem().getReadingType()).thenReturn(readingType);
        when(meterReadingData1.getItem().getRequestedReadingInterval()).thenReturn(Optional.empty());
        when(meterReadingData1.getValidationData()).thenReturn(validationData);
        when(meterReadingData1.getMeterReading()).thenReturn(meterReading1);
        when(meterReadingData1.getReadingStatuses()).thenReturn(readingStatuses);

        ChannelsContainer channelContainer1 = mock(ChannelsContainer.class, RETURNS_DEEP_STUBS);
        when(channelContainer1.getInterval()).thenReturn(Interval.of(Range.open(Instant.now().minus(2, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS))));
        when(channelContainer1.getChannel(anyObject())).thenReturn(Optional.of(channel1));

        channelsContainerList1.add(channelContainer1);

        when(meterReadingData1.getItem().getReadingContainer().getChannelsContainers()).thenReturn(channelsContainerList1);

        /* Prepare reading for 2nd data*/
        when(reading4.getTimeStamp()).thenReturn(Instant.now());
        when(reading4.getValue()).thenReturn(new BigDecimal(100503));
        readingsList2.add(reading4);

        when(reading5.getTimeStamp()).thenReturn(Instant.now());
        when(reading5.getValue()).thenReturn(new BigDecimal(100504));
        readingsList2.add(reading5);

        when(reading6.getTimeStamp()).thenReturn(Instant.now());
        when(reading6.getValue()).thenReturn(new BigDecimal(100505));
        readingsList2.add(reading6);

        when(meterReading2.getIntervalBlocks()).thenReturn(Collections.emptyList());
        when(meterReading2.getReadings()).thenReturn(readingsList2);

        MeterReadingData meterReadingData2 = mock(MeterReadingData.class, RETURNS_DEEP_STUBS);
        when(meterReadingData2.getItem().getDomainObject().getName()).thenReturn(DEVICE_NAME_2);
        when(meterReadingData2.getItem().getReadingType().getMRID()).thenReturn(MRID_2);
        when(meterReadingData2.getItem().getReadingType()).thenReturn(readingType);
        when(meterReadingData2.getItem().getRequestedReadingInterval()).thenReturn(Optional.empty());
        when(meterReadingData2.getValidationData()).thenReturn(validationData);
        when(meterReadingData2.getMeterReading()).thenReturn(meterReading2);
        when(meterReadingData2.getReadingStatuses()).thenReturn(readingStatuses);

        ChannelsContainer channelContainer2 = mock(ChannelsContainer.class, RETURNS_DEEP_STUBS);
        when(channelContainer2.getInterval()).thenReturn(Interval.of(Range.open(Instant.now().minus(2, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS))));
        when(channelContainer2.getChannel(anyObject())).thenReturn(Optional.of(channel2));

        channelsContainerList2.add(channelContainer2);
        when(meterReadingData2.getItem().getReadingContainer().getChannelsContainers()).thenReturn(channelsContainerList2);

        when(readingType.getIntervalLength()).thenReturn(Optional.empty());
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.MICRO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.METER);

        dataExportList.add(meterReadingData1);
        dataExportList.add(meterReadingData2);
    }

    @After
    public void after(){
        dataExportList.clear();
    }

    @Test
    public void testNumberOfReadingsPerMsgIsLessThanNumberOfReadingsToSend() {
        /*Here we have situation when number of readings per msg is less than number of readings in data source.
        * We have two data source. So two message should be sent(Two call of send() method). */
        provider.call(outboundEndPointConfiguration, dataExportList.stream(), exportContext);

        SetMultimap<String,String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(),
                "ProfileID");
        verify(webServiceCallOccurrence, times(2)).saveRelatedAttributes(values);
        verify(endpoint, times(2)).utilitiesTimeSeriesERPItemBulkChangeRequestCOut(any(UtilsTmeSersERPItmBulkChgReqMsg.class));
    }

    @Test
    public void testNumberOfReadingsPerMsgMoreThanReadings() {
        /*Configure situation when number of readings per msg more than number of readings to send.
        * In this case all readings will be sent in one message. It means one call on send() method */
        when(readingNumberPerMessageProvider.getNumberOfReadingsPerMsg()).thenReturn(10);
        provider.setReadingNumberPerMessageProvider(readingNumberPerMessageProvider);

        provider.call(outboundEndPointConfiguration, dataExportList.stream(), exportContext);

        SetMultimap<String,String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(),
                "ProfileID");
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        verify(endpoint).utilitiesTimeSeriesERPItemBulkChangeRequestCOut(any(UtilsTmeSersERPItmBulkChgReqMsg.class));
    }

    @Test
    public void testNumberOfReadingsPerMsgEqualNumberOfReadingsInDataSource() {
        /* We have 3 readings in each data source. Configure number of readings per msg equal to 3.
        * Two messages should be sent */
        when(readingNumberPerMessageProvider.getNumberOfReadingsPerMsg()).thenReturn(3);
        provider.setReadingNumberPerMessageProvider(readingNumberPerMessageProvider);

        provider.call(outboundEndPointConfiguration, dataExportList.stream(), exportContext);

        SetMultimap<String,String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(),
                "ProfileID");
        verify(webServiceCallOccurrence, times(2)).saveRelatedAttributes(values);
        verify(endpoint, times(2)).utilitiesTimeSeriesERPItemBulkChangeRequestCOut(any(UtilsTmeSersERPItmBulkChgReqMsg.class));
    }
}
