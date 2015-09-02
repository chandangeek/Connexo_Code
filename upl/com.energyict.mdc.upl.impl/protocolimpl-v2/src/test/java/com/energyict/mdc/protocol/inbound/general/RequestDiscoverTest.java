package com.energyict.mdc.protocol.inbound.general;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedDataFactory;
import com.energyict.mdc.meterdata.CollectedDataFactoryProvider;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedRegisterList;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDeviceProtocol;
import com.energyict.mdw.core.LogBookSpec;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.util.IssueFactory;
import com.energyict.util.IssueFactoryProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
    protected LogBookSpec logBookSpec;
    @Mock
    protected CollectedDataFactoryProvider collectedDataFactoryProvider;
    @Mock
    protected CollectedDataFactory collectedDataFactory;
    @Mock
    protected IssueFactoryProvider issueFactoryProvider;
    @Mock
    protected IssueFactory issueFactory;
    @Mock
    private CollectedRegisterList collectedRegisterList;

    protected int count;
    protected byte[] inboundFrame;

    private static final int LOGBOOK_ID = 10;

    @Before
    public void initialize() {
        when(collectedDataFactoryProvider.getCollectedDataFactory()).thenReturn(collectedDataFactory);
        CollectedDataFactoryProvider.instance.set(collectedDataFactoryProvider);

        when(issueFactoryProvider.getIssueFactory()).thenReturn(issueFactory);
        when(collectedDataFactory.createCollectedRegisterList(any(DeviceIdentifier.class))).thenReturn(this.collectedRegisterList);
        IssueFactoryProvider.instance.set(issueFactoryProvider);
    }

    @Test
    public void testRegisters() throws IOException {
        CollectedRegister defaultCollectedRegister = mock(CollectedRegister.class);
        CollectedRegister maxDemandCollectedRegister = mock(CollectedRegister.class);
        when(collectedDataFactory.createDefaultCollectedRegister(any(RegisterIdentifier.class))).thenReturn(defaultCollectedRegister);
        when(collectedDataFactory.createMaximumDemandCollectedRegister(any(RegisterIdentifier.class))).thenReturn(maxDemandCollectedRegister);
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

        assertThat(discoverResultType).isEqualTo(InboundDeviceProtocol.DiscoverResultType.IDENTIFIER);
        assertThat(requestDiscover.getCollectedData()).isNullOrEmpty();
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
        verify(collectedLogBook).setCollectedMeterEvents(argThat(new ArgumentMatcher<List<MeterProtocolEvent>>() {
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

        verify(collectedLogBook).setCollectedMeterEvents(argThat(new ArgumentMatcher<List<MeterProtocolEvent>>() {
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

    private RequestDiscover getProtocolInstance() {
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(AbstractDiscover.TIMEOUT_KEY, TimeDuration.seconds(1));
        properties.setProperty(AbstractDiscover.RETRIES_KEY, BigDecimal.ZERO);
        RequestDiscover requestDiscover = new RequestDiscover();
        requestDiscover.addProperties(properties);
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