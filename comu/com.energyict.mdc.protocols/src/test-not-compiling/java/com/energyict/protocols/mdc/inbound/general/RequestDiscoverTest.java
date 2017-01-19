package com.energyict.protocols.mdc.inbound.general;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueCollector;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.device.data.identifiers.CanFindRegister;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.NoLogBooksCollectedData;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdw.core.LogBook;
import com.energyict.mdw.core.LogBookFactory;
import com.energyict.mdw.core.LogBookFactoryProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests that mock the comchannel and stub an incoming frame to test the inbound parsing of the RequestDiscover implementation
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/06/12
 * Time: 11:34
 * Author: khe
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestDiscoverTest {

    @Mock
    protected ComChannel comChannel;
    @Mock
    private DeviceFactoryProvider deviceFactoryProvider;
    @Mock
    protected DeviceFactory deviceFactory;
    @Mock
    protected Device device;
    @Mock
    protected LogBook logBook;
    @Mock
    protected LogBookFactoryProvider logBookFactoryProvider;
    @Mock
    protected LogBookFactory logBookFactory;
    @Mock
    protected CollectedDataFactoryProvider collectedDataFactoryProvider;
    @Mock
    protected CollectedDataFactory collectedDataFactory;
    @Mock
    protected IssueCollector issueCollector;
    @Mock
    protected IssueService issueService;
    @Mock
    private CollectedRegisterList collectedRegisterList;

    protected int count;
    protected byte[] inboundFrame;

    private static final int LOGBOOK_ID = 10;

    @BeforeClass
    public static void  setupEnvironment(){
        Environment environment = mock(Environment.class);
        Environment.DEFAULT.set(environment);
        when(environment.getTranslation(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (String) args[0];
            }
        });
    }

    private final String TIMEOUT_KEY = Environment.DEFAULT.get().getTranslation("protocol.timeout");
    private final String RETRIES_KEY = Environment.DEFAULT.get().getTranslation("protocol.retries");

    @Before
    public void initialize() {
        ArrayList<LogBook> logBooks = new ArrayList<>();
        logBooks.add(logBook);
        when(logBook.getId()).thenReturn(LOGBOOK_ID);
        when(this.device.getLogBooks()).thenReturn(logBooks);
        when(this.deviceFactory.findBySerialNumber(Matchers.<String>any())).thenReturn(Arrays.asList(this.device));
        when(this.deviceFactoryProvider.getDeviceFactory()).thenReturn(deviceFactory);
        DeviceFactoryProvider.instance.set(deviceFactoryProvider);

        LogBookFactoryProvider.instance.set(logBookFactoryProvider);
        when(logBookFactoryProvider.getLogBookFactory()).thenReturn(logBookFactory);
        when(logBookFactory.findGenericLogBook(device)).thenReturn(logBook);
        when(logBookFactory.find(LOGBOOK_ID)).thenReturn(logBook);

        when(collectedDataFactoryProvider.getCollectedDataFactory()).thenReturn(collectedDataFactory);
        CollectedDataFactoryProvider.instance.set(collectedDataFactoryProvider);

        when(collectedDataFactory.createCollectedRegisterList(any(DeviceIdentifier.class))).thenReturn(this.collectedRegisterList);
        when(issueService.newIssueCollector()).thenReturn(issueCollector);
    }

    @Test
    public void testRegisters() throws IOException {
        CollectedRegister defaultCollectedRegister = mock(CollectedRegister.class);
        CollectedRegister maxDemandCollectedRegister = mock(CollectedRegister.class);
        when(collectedDataFactory.createDefaultCollectedRegister(any(CanFindRegister.class))).thenReturn(defaultCollectedRegister);
        when(collectedDataFactory.createMaximumDemandCollectedRegister(any(CanFindRegister.class))).thenReturn(maxDemandCollectedRegister);
        inboundFrame = "<REGISTER>serialId=204006174,1.1.1.8.6.255=1234 kW 080510121516, 1.1.3.8.0.255=4321 kvarh,readTime=080501214600</REGISTER>".getBytes();
        mockComChannel();

        RequestDiscover requestDiscover = getProtocolInstance();
        requestDiscover.initComChannel(comChannel);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = requestDiscover.doDiscovery();

        assertThat(discoverResultType).isEqualTo(InboundDeviceProtocol.DiscoverResultType.DATA);
        assertThat(requestDiscover.getCollectedData()).isNotNull();
        assertThat(requestDiscover.getCollectedData()).hasSize(1);
        assertThat(requestDiscover.getDeviceIdentifier().toString()).contains("204006174");
        verify(maxDemandCollectedRegister).setCollectedData(argThat(new ArgumentMatcher<Quantity>() {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof Quantity)) {
                    return false;
                }
                Quantity quantity = (Quantity) o;
                return quantity.getUnit().equals(Unit.get("kW")) && quantity.getAmount().intValue() == 1234;
            }
        }), any(String.class));
        verify(defaultCollectedRegister).setCollectedData(argThat(new ArgumentMatcher<Quantity>() {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof Quantity)) {
                    return false;
                }
                Quantity quantity = (Quantity) o;
                return quantity.getUnit().equals(Unit.get("kvarh")) && quantity.getAmount().intValue() == 4321;
            }
        }), any(String.class));
    }

    @Test
    public void testRequest() throws IOException {
        inboundFrame = "<REQUEST>serialId=1234567890,type=as220,ipAddress=10.0.0.255</REQUEST>".getBytes();
        mockComChannel();

        RequestDiscover requestDiscover = getProtocolInstance();
        requestDiscover.initComChannel(comChannel);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = requestDiscover.doDiscovery();

        assertThat(discoverResultType).isEqualTo(InboundDeviceProtocol.DiscoverResultType.DATA);
        assertThat(requestDiscover.getCollectedData()).isNotNull();
        assertThat(requestDiscover.getDeviceIdentifier().toString()).contains("1234567890");
    }

    @Test
    public void testDeploy() throws IOException {
        CollectedTopology collectedTopology = mock(CollectedTopology.class);
        when(collectedDataFactory.createCollectedTopology(any(DeviceIdentifier.class))).thenReturn(collectedTopology);
        inboundFrame = "<DEPLOY>serialId=1234567890,type=as220, ipAddress=10.0.0.255</DEPLOY>".getBytes();
        mockComChannel();

        RequestDiscover requestDiscover = getProtocolInstance();
        requestDiscover.initComChannel(comChannel);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = requestDiscover.doDiscovery();

        assertThat(discoverResultType).isEqualTo(InboundDeviceProtocol.DiscoverResultType.DATA);
        assertThat(requestDiscover.getCollectedData()).isNotNull();
        assertThat(requestDiscover.getCollectedData()).hasSize(1);
        assertThat(requestDiscover.getCollectedData().get(0)).isNotNull();
        assertThat(requestDiscover.getCollectedData().get(0)).isEqualTo(collectedTopology);
        verify(collectedTopology).setFailureInformation(argThat(new ArgumentMatcher<ResultType>() {
            @Override
            public boolean matches(Object o) {
                return o.equals(ResultType.InCompatible);
            }
        }), any(Issue.class));
        assertThat(requestDiscover.getDeviceIdentifier().toString()).contains("1234567890");
    }

    @Test
    public void testEventPO() throws IOException {
        CollectedLogBook collectedLogBook = mock(CollectedLogBook.class);
        when(collectedDataFactory.createCollectedLogBook(any(LogBookIdentifier.class))).thenReturn(collectedLogBook);

        inboundFrame = "<EVENTPO>meterType=AS230,serialId=1234567890,dbaseId=5,ipAddress=192.168.0.1,event=10000101100110</EVENTPO>".getBytes();
        mockComChannel();

        RequestDiscover requestDiscover = getProtocolInstance();
        requestDiscover.initComChannel(comChannel);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = requestDiscover.doDiscovery();

        assertThat(discoverResultType).isEqualTo(InboundDeviceProtocol.DiscoverResultType.DATA);
        assertThat(requestDiscover.getCollectedData()).isNotNull();
        assertThat(requestDiscover.getCollectedData()).hasSize(1);
        assertThat(requestDiscover.getDeviceIdentifier().toString()).contains("1234567890");
        assertThat(requestDiscover.getCollectedData()).hasSize(1);
        CollectedData collectedData = requestDiscover.getCollectedData().get(0);
        assertThat(collectedData).isEqualTo(collectedLogBook);
        verify(collectedLogBook).setMeterEvents(argThat(new ArgumentMatcher<List<MeterProtocolEvent>>() {
            @Override
            public boolean matches(Object o) {
                List<MeterProtocolEvent> meterProtocolEvents = (List<MeterProtocolEvent>) o;
                if (meterProtocolEvents.size() != 1) {
                    return false;
                }
                MeterProtocolEvent meterProtocolEvent = meterProtocolEvents.get(0);
                return meterProtocolEvent.getMessage().equalsIgnoreCase("Last gas power outage")
                        && meterProtocolEvent.getTime().getTime() == 1259629910000L;
            }
        }));
    }

    @Test
    public void testEventPOForDeviceHavingNoLogBooksConfigured() throws IOException {
        NoLogBooksCollectedData noLogBooksCollectedData = mock(NoLogBooksCollectedData.class);
        when(collectedDataFactory.createNoLogBookCollectedData(any(DeviceIdentifier.class))).thenReturn(noLogBooksCollectedData);
        inboundFrame = "<EVENTPO>meterType=AS230,serialId=1234567890,dbaseId=5,ipAddress=192.168.0.1,event=10000101100110</EVENTPO>".getBytes();
        when(this.device.getLogBooks()).thenReturn(new ArrayList<LogBook>());
        mockComChannel();

        RequestDiscover requestDiscover = getProtocolInstance();
        requestDiscover.initComChannel(comChannel);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = requestDiscover.doDiscovery();

        assertThat(discoverResultType).isEqualTo(InboundDeviceProtocol.DiscoverResultType.DATA);
        assertThat(requestDiscover.getCollectedData()).isNotNull();
        assertThat(requestDiscover.getCollectedData()).hasSize(1);
        assertThat(requestDiscover.getDeviceIdentifier().toString()).contains("1234567890");
        assertThat(requestDiscover.getCollectedData().get(0)).isEqualTo(noLogBooksCollectedData);
    }

    @Test
    public void testEvents() throws IOException {
        CollectedLogBook collectedLogBook = mock(CollectedLogBook.class);
        when(collectedDataFactory.createCollectedLogBook(any(LogBookIdentifier.class))).thenReturn(collectedLogBook);

        inboundFrame = "<EVENT>serialId=204006174,event0=123 8 080514092400, event1=20 6995 080514092505</EVENT>".getBytes();
        mockComChannel();

        RequestDiscover requestDiscover = getProtocolInstance();
        requestDiscover.initComChannel(comChannel);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = requestDiscover.doDiscovery();

        assertThat(discoverResultType).isEqualTo(InboundDeviceProtocol.DiscoverResultType.DATA);
        assertThat(requestDiscover.getCollectedData()).isNotNull();
        assertThat(requestDiscover.getCollectedData()).hasSize(1);
        CollectedData collectedData = requestDiscover.getCollectedData().get(0);
        assertThat(collectedData).isEqualTo(collectedLogBook);

        verify(collectedLogBook).setMeterEvents(argThat(new ArgumentMatcher<List<MeterProtocolEvent>>() {
            @Override
            public boolean matches(Object o) {
                List<MeterProtocolEvent> meterProtocolEvents = (List<MeterProtocolEvent>) o;
                if (meterProtocolEvents.size() != 2) {
                    return false;
                }
                MeterProtocolEvent meterProtocolEvent1 = meterProtocolEvents.get(0);
                MeterProtocolEvent meterProtocolEvent2 = meterProtocolEvents.get(1);
                return meterProtocolEvent1.getMessage().contains("8")
                        && meterProtocolEvent2.getMessage().contains("6995")
                        && meterProtocolEvent1.getProtocolCode() == 123
                        && meterProtocolEvent2.getProtocolCode() == 20
                        && meterProtocolEvent1.getEiCode() == 0
                        && meterProtocolEvent2.getEiCode() == 0
                        && meterProtocolEvent1.getTime().getTime() == 1210757040000L
                        && meterProtocolEvent2.getTime().getTime() == 1210757105000L;
            }
        }));
    }

    @Test
    public void testEventsForDeviceHavingNoLogBooksConfigured() throws IOException {
        NoLogBooksCollectedData noLogBooksCollectedData = mock(NoLogBooksCollectedData.class);
        when(collectedDataFactory.createNoLogBookCollectedData(any(DeviceIdentifier.class))).thenReturn(noLogBooksCollectedData);
        inboundFrame = "<EVENT>serialId=204006174,event0=123 8 080514092400, event1=20 6995 080514092505</EVENT>".getBytes();
        when(this.device.getLogBooks()).thenReturn(new ArrayList<LogBook>());
        mockComChannel();

        RequestDiscover requestDiscover = getProtocolInstance();
        requestDiscover.initComChannel(comChannel);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = requestDiscover.doDiscovery();

        assertThat(discoverResultType).isEqualTo(InboundDeviceProtocol.DiscoverResultType.DATA);
        assertThat(requestDiscover.getCollectedData()).isNotNull();
        assertThat(requestDiscover.getCollectedData()).hasSize(1);
        assertThat(requestDiscover.getDeviceIdentifier().toString()).contains("204006174");
        assertThat(requestDiscover.getCollectedData().get(0)).isEqualTo(noLogBooksCollectedData);
    }

    private RequestDiscover getProtocolInstance() {
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(TIMEOUT_KEY, new BigDecimal(1000));
        properties.setProperty(RETRIES_KEY, BigDecimal.ZERO);
        RequestDiscover requestDiscover = new RequestDiscover();
        requestDiscover.copyProperties(properties);
        return requestDiscover;
    }

    /**
     * Method that mocks the comChannel to return the bytes of field 'inboundFrame' and to return the available length.
     * This stubs an incoming frame on the inputstream of the comChannel.
     */
    private void mockComChannel() {
        count = 0;
        when(comChannel.read()).thenReturn(nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte());
        Integer[] values = new Integer[inboundFrame.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = 1;
        }
        values[inboundFrame.length - 1] = 0;

        when(comChannel.available()).thenReturn(1, values);
    }

    private Integer nextByte() {
        try {
            return (inboundFrame[count++] & 0xFF);
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }

}