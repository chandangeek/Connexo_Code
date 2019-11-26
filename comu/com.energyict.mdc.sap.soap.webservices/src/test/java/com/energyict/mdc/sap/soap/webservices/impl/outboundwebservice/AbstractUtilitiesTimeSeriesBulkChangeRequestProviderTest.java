package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.MeterReadingValidationData;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationResult;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.ReadingNumberPerMessageProvider;
import com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.UtilitiesTimeSeriesBulkChangeRequestProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkchangerequest.UtilsTmeSersERPItmBulkChgReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilitiesTimeSeriesERPItemBulkCreateRequestCOut;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeRangeSet;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractUtilitiesTimeSeriesBulkChangeRequestProviderTest extends AbstractOutboundWebserviceTest<UtilitiesTimeSeriesERPItemBulkCreateRequestCOut> {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UtilsTmeSersERPItmBulkChgReqMsg confirmationMessage;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadingType readingType;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MeterReading meterReading1;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MeterReading meterReading2;

    private UtilitiesTimeSeriesBulkChangeRequestProvider provider;

    @Mock
    private SAPCustomPropertySets sapCustomPropertySets;

    @Mock
    private Clock clock;
    @Mock
    private EndPointConfiguration endPointConfiguration ;

    @Mock
    ReadingNumberPerMessageProvider readingNumberPerMessageProvider;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OutboundEndPointProvider.RequestSender requestSender;


    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    MeterReadingValidationData validationData;

    private static List<MeterReadingData> dataExportList = new ArrayList<>();

    private static final String DEVICE_NAME_1 = "DEVICE_1";
    private static final String MRID_1 = "MRID_1";
    private static final String DEVICE_NAME_2 = "DEVICE_2";
    private static final String MRID_2 = "MRID_2";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Channel channel1;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Channel channel2;


    Map<String, RangeSet<Instant>> profileIdIntervals = new HashMap<>();
    RangeSet<Instant> rangeSet = TreeRangeSet.create();

    @Mock
    Reading reading1, reading2, reading3;
    @Mock
    Reading reading4, reading5, reading6;



    @Before
    public void setUp() {
        List<Reading> readingsList1 = new ArrayList<>();
        List<Reading> readingsList2 = new ArrayList<>();

        List<ChannelsContainer> channelsContainerList1 = new ArrayList<>();
        List<ChannelsContainer> channelsContainerList2 = new ArrayList<>();

        provider = spy(new UtilitiesTimeSeriesBulkChangeRequestProvider());

        provider.setClock(clock);
        rangeSet.add(Range.open(Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS)));
        profileIdIntervals.put("ProfileID", rangeSet);

        /*Mock sapCustomPropertySet. It is needed for getTimeSlicedProfileId*/
        when(sapCustomPropertySets.getProfileId(anyObject(), anyObject())).thenReturn(profileIdIntervals);
        provider.setSapCustomPropertySets(sapCustomPropertySets);

        /* Configure number of readings per one message */
        when(readingNumberPerMessageProvider.getNumberOfReadingsPerMsg()).thenReturn(2);
        provider.setReadingNumberPerMessageProvider(readingNumberPerMessageProvider);

        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);


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
        when(validationData.getValidationStatus(anyObject()).getValidationResult()).thenReturn(ValidationResult.ACTUAL);
        when(meterReadingData1.getValidationData()).thenReturn(validationData);
        when(meterReadingData1.getMeterReading()).thenReturn(meterReading1);


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
        when(validationData.getValidationStatus(anyObject()).getValidationResult()).thenReturn(ValidationResult.ACTUAL);
        when(meterReadingData2.getValidationData()).thenReturn(validationData);
        when(meterReadingData2.getMeterReading()).thenReturn(meterReading2);

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


        /* Setting for send */
        when(provider.using(anyString())).thenReturn(requestSender);
        when(requestSender.withRelatedAttributes(any(SetMultimap.class ))).thenReturn(requestSender);
        when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        Set<EndPointConfiguration> set  = new HashSet<>();
        set.add(endPointConfiguration);

        Map responseMap = new HashMap();
        responseMap.put(endPointConfiguration, confirmationMessage);
        when(requestSender.send(any())).thenReturn(responseMap);

    }

    @After
    public void after(){
        dataExportList.clear();
    }

    @Test
    public void testNumberOfReadingsPerMsgIsLessThanNumberOfReadingsToSend() {

        /*Here we have situation when number of readings per msg is less than number of readings in data source.
        * We have two data source. So two message should be sent(Two call of send() method). */
        provider.call(endPointConfiguration, dataExportList.stream());

        SetMultimap<String,String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(),
                "ProfileID");
        verify(requestSender, times(2)).withRelatedAttributes(values);
        verify(requestSender, times(2)).send(any(UtilsTmeSersERPItmBulkChgReqMsg.class));
    }

    @Test
    public void testNumberOfReadingsPerMsgMoreThanReadings() {
        /*Configure situation when number of readings per msg more than number of readings to send.
        * In this case all readings will be sent in one message. It means one call on send() method */
        when(readingNumberPerMessageProvider.getNumberOfReadingsPerMsg()).thenReturn(10);
        provider.setReadingNumberPerMessageProvider(readingNumberPerMessageProvider);

        List<ServiceCall> list = provider.call(endPointConfiguration, dataExportList.stream());

        SetMultimap<String,String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(),
                "ProfileID");
        verify(requestSender).withRelatedAttributes(values);
        verify(requestSender).send(any(UtilsTmeSersERPItmBulkChgReqMsg.class));
    }

    @Test
    public void testNumberOfReadingsPerMsgEqaulNumberOfReadingsInDataSource() {
        /* We have 3 readings in each data source. Configure number of readings per msg equal to 3.
        * Two messages should be sent */
        when(readingNumberPerMessageProvider.getNumberOfReadingsPerMsg()).thenReturn(3);
        provider.setReadingNumberPerMessageProvider(readingNumberPerMessageProvider);

        provider.call(endPointConfiguration, dataExportList.stream());

        SetMultimap<String,String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(),
                "ProfileID");
        verify(requestSender, times(2)).withRelatedAttributes(anyObject());
        verify(requestSender, times(2)).send(any(UtilsTmeSersERPItmBulkChgReqMsg.class));
    }
}
