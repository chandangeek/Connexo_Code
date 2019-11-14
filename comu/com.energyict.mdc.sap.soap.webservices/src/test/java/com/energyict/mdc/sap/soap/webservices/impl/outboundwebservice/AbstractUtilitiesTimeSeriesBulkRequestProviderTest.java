package com.energyict.mdc.sap.soap.webservices.impl.outboundwebservice;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractOutboundWebserviceTest;
import com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.UtilitiesTimeSeriesBulkChangeRequestProvider;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceERPSmartMeterCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilitiesTimeSeriesERPItemBulkCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiestimeseriesbulkcreaterequest.UtilsTmeSersERPItmCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeRangeSet;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class AbstractUtilitiesTimeSeriesBulkRequestProviderTest extends AbstractOutboundWebserviceTest<UtilitiesTimeSeriesERPItemBulkCreateRequestCOut> {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UtilsTmeSersERPItmCrteReqMsg confirmationMessage;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadingType readingType;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MeterReading meterReading;

    @Mock
    private UtilitiesDeviceERPSmartMeterCreateConfirmationCOut port;
    @Mock
    private UtilsTmeSersERPItmBulkCrteReqMsg outboundMessage;

    private UtilitiesTimeSeriesBulkChangeRequestProvider provider;

    @Mock
    private SAPCustomPropertySets sapCustomPropertySets;

    @Mock
    private Clock clock;
    @Mock
    private EndPointConfiguration endPointConfiguration ;

    private static List<MeterReadingData> dataExportList = new ArrayList<>();

    private static final String DEVICE_NAME_1 = "DEVICE_1";
    private static final String MRID_1 = "MRID_1";
    private static final String DEVICE_NAME_2 = "DEVICE_2";
    private static final String MRID_2 = "MRID_2";
    private static final String NAME_PART_1 = "Part_1";
    private static final String NAME_PART_2 = "Part_2";


    private List<ChannelsContainer> channelsContainerList1 = new ArrayList<>();

    private List<ChannelsContainer> channelsContainerList2 = new ArrayList<>();

    /*@Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelsContainer channelContainer1;*/
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Channel channel1;

    Map<String, RangeSet<Instant>> profileIdIntervals = new HashMap<>();
    RangeSet<Instant> rangeSet = TreeRangeSet.create();


    @Before
    public void setUp() {
        provider = spy(new UtilitiesTimeSeriesBulkChangeRequestProvider());

        provider.setClock(clock);
        rangeSet.add(Range.open(Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS)));
        //Range.open(Instant.now().minus(2, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS))
        profileIdIntervals.put("ProfileID", rangeSet);

        when(sapCustomPropertySets.getProfileId(anyObject(), anyObject())).thenReturn(profileIdIntervals);
        provider.setSapCustomPropertySets(sapCustomPropertySets);

        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        //inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", getThesaurus());
        //inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        //when(requestSender.toEndpoints(any(EndPointConfiguration.class))).thenReturn(requestSender);
        //when(requestSender.withRelatedAttributes(any(SetMultimap.class))).thenReturn(requestSender);
        //when(outboundMessage.getUtilitiesTimeSeriesERPItemCreateRequestMessage()).thenReturn(confirmationMessage);
        //when(webServiceActivator.getThesaurus()).thenReturn(getThesaurus());
        //when(confirmationMessage.getUtilitiesDevice().getID().getValue()).thenReturn("UtilDeviceID");

        when(meterReading.getIntervalBlocks()).thenReturn(Collections.emptyList());
        when(meterReading.getReadings()).thenReturn(Collections.emptyList());


        MeterReadingData meterReadingData1 = mock(MeterReadingData.class, RETURNS_DEEP_STUBS);
        when(meterReadingData1.getItem().getDomainObject().getName()).thenReturn(DEVICE_NAME_1);
        when(meterReadingData1.getItem().getReadingType().getMRID()).thenReturn(MRID_1);
        when(meterReadingData1.getItem().getReadingType()).thenReturn(readingType);
        when(meterReadingData1.getMeterReading()).thenReturn(meterReading);

            /*@Mock(answer = Answers.RETURNS_DEEP_STUBS)*/
        ChannelsContainer channelContainer1 = mock(ChannelsContainer.class, RETURNS_DEEP_STUBS);

        when(channelContainer1.getInterval()).thenReturn(Interval.of(Range.open(Instant.now().minus(2, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS))));

        when(channelContainer1.getChannel(anyObject())).thenReturn(Optional.of(channel1));

        //when(channelContainer1.getReadingTypes(any(Range.class))).thenReturn()

        //when(channelContainer1.getInterval()).thenReturn(Interval.of(Range.open(Instant.EPOCH, Instant.MAX)));

        //when(channelContainer1.getChannel(any())).thenReturn(Optional.of(channel1));

        channelsContainerList1.add(channelContainer1);

        when(meterReadingData1.getItem().getReadingContainer().getChannelsContainers()).thenReturn(channelsContainerList1);






        MeterReadingData meterReadingData2 = mock(MeterReadingData.class, RETURNS_DEEP_STUBS);
        when(meterReadingData2.getItem().getDomainObject().getName()).thenReturn(DEVICE_NAME_2);
        when(meterReadingData2.getItem().getReadingType().getMRID()).thenReturn(MRID_2);
        when(meterReadingData2.getItem().getReadingType()).thenReturn(readingType);
        when(meterReadingData2.getMeterReading()).thenReturn(meterReading);

        when(meterReadingData2.getItem().getReadingContainer().getChannelsContainers()).thenReturn(channelsContainerList2);

        when(readingType.getIntervalLength()).thenReturn(Optional.empty());
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.MICRO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.METER);

        dataExportList.add(meterReadingData1);
        //dataExportList.add(meterReadingData2);

        /*item.getItem().getReadingContainer().getChannelsContainers().stream()
                .filter(cc -> cc.getInterval().toOpenClosedRange().isConnected(allReadingsRange))
                .map(cc -> Pair.of(cc, cc.getInterval().toOpenClosedRange().intersection(allReadingsRange)))
                .filter(ccAndRange -> !ccAndRange.getLast().isEmpty())
                .map(ccAndRange -> ccAndRange.getFirst().getChannel(readingType)
                        .map(channel -> Pair.of(channel, ccAndRange.getLast())))
                .flatMap(Functions.asStream())
                .flatMap(channelAndRange -> getTimeSlicedProfileId(channelAndRange.getFirst(), channelAndRange.getLast()).entrySet().stream())
                .map(profileIdAndRange -> createRequestItem(profileIdAndRange.getKey(), profileIdAndRange.getValue(), meterReading, interval, unit, now, item.getValidationData()))
                .forEach(timeSeriesList::add);*/
    }

    @Test
    public void testCall() {
       // when(provider.using(anyString())).thenReturn(requestSender);
        Map<String, Object> properties = new HashMap<>();
       // properties.put(WebServiceActivator.URL_PROPERTY, getURL());
       // properties.put("epcId", 1l);

        //provider.addRequestConfirmationPort(port, properties);
        System.out.println("DATA ="+dataExportList.get(0));
        System.out.println("NAME ="+dataExportList.get(0).getItem().getDomainObject().getName());
        List<ServiceCall> list = provider.call(endPointConfiguration, dataExportList.stream());

        System.out.println("LISt = "+list);

        /*verify(provider).using("utilitiesDeviceERPSmartMeterCreateConfirmationCOut");
        SetMultimap<String,String> values = HashMultimap.create();
        values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                "UtilDeviceID");*/
        //verify(requestSender).withRelatedAttributes(values);
        //verify(requestSender).send(confirmationMessage);
    }

/*    @Test
    public void testCallWithoutPort() {
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(anyString())).thenReturn(new ArrayList());
        expectedException.expect(LocalizedException.class);


        expectedException.expectMessage("No web service endpoints are available to send the request using 'SAP UtilitiesDeviceERPSmartMeterCreateConfirmation_C_Out'.");




        provider.call(outboundMessage);
    }

    @Test
    public void testGetService() {
        Assert.assertEquals(provider.getService(), UtilitiesDeviceERPSmartMeterCreateConfirmationCOut.class);
    }

    @Test
    public void testGet() {
        Assert.assertEquals(provider.get().getClass(), UtilitiesDeviceERPSmartMeterCreateConfirmationCOutService.class);
    }*/

}
