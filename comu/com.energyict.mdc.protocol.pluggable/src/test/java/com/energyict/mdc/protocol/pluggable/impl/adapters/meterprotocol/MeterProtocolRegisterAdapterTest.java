package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.mocks.MockCollectedRegister;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.mock.RegisterSupportedMeterProtocol;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link MeterProtocolRegisterAdapter}
 *
 * @author gna
 * @since 4/04/12 - 15:50
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterProtocolRegisterAdapterTest {

    private final Date fromDate = mock(Date.class);
    private final Date toDate = mock(Date.class);
    private final Date readDate = mock(Date.class);
    private final Date eventDate = mock(Date.class);
    private final String text = "Some Text From a Register";
    private final Quantity quantity = mock(Quantity.class);

    @Mock
    private IssueService issueService;
    @Mock
    private Environment environment;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private CollectedDataFactory collectedDataFactory;

    @Before
    public void initializeIssueService () {
        when(this.issueService.newProblem(anyString(), anyString(), anyVararg())).thenReturn(mock(Problem.class));
    }

    @Before
    public void setUpEnvironment () {
        when(this.environment.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(this.environment.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(this.environment.getApplicationContext()).thenReturn(this.applicationContext);
        when(this.collectedDataFactory.createCollectedRegisterForAdapter(any(RegisterIdentifier.class), readingType)).
            thenAnswer(new Answer<CollectedRegister>() {
                @Override
                public CollectedRegister answer(InvocationOnMock invocationOnMock) throws Throwable {
                    RegisterIdentifier registerIdentifier = (RegisterIdentifier) invocationOnMock.getArguments()[0];
                    MockCollectedRegister collectedRegister = new MockCollectedRegister(registerIdentifier, readingType);
                    collectedRegister.setResultType(ResultType.Supported);
                    return collectedRegister;
                }
            });
        when(this.collectedDataFactory.createDefaultCollectedRegister(any(RegisterIdentifier.class), readingType)).
            thenAnswer(new Answer<CollectedRegister>() {
                @Override
                public CollectedRegister answer(InvocationOnMock invocationOnMock) throws Throwable {
                    RegisterIdentifier registerIdentifier = (RegisterIdentifier) invocationOnMock.getArguments()[0];
                    return new MockCollectedRegister(registerIdentifier, readingType);
                }
            });
        CollectedDataFactoryProvider collectedDataFactoryProvider = mock(CollectedDataFactoryProvider.class);
        when(collectedDataFactoryProvider.getCollectedDataFactory()).thenReturn(this.collectedDataFactory);
        CollectedDataFactoryProvider.instance.set(collectedDataFactoryProvider);
        when(this.applicationContext.getModulesImplementing(CollectedDataFactory.class)).thenReturn(Arrays.asList(this.collectedDataFactory));
        Environment.DEFAULT.set(this.environment);
    }

    @After
    public void tearDownEnvironment () {
        Environment.DEFAULT.set(null);
    }

    @After
    public void resetCollectedDataFactoryProvider() {
        CollectedDataFactoryProvider.instance.set(null);
    }

    private static OfflineRegister getMockedRegister() {
        OfflineRegister register = mock(OfflineRegister.class);
        ObisCode obisCode = mock(ObisCode.class);
        when(register.getObisCode()).thenReturn(obisCode);
        return register;
    }

    private RegisterValue getMockedRegisterValue() {
        RegisterValue registerValue = mock(RegisterValue.class);
        when(registerValue.getEventTime()).thenReturn(eventDate);
        when(registerValue.getFromTime()).thenReturn(fromDate);
        when(registerValue.getReadTime()).thenReturn(readDate);
        when(registerValue.getToTime()).thenReturn(toDate);
        when(registerValue.getText()).thenReturn(text);
        when(registerValue.getQuantity()).thenReturn(quantity);
        return registerValue;
    }

    @Test
    public void getRegisterEmptyCallTest() {
        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(null, issueService);
        assertThat(meterProtocolRegisterAdapter.readRegisters(null)).isNotNull();
        assertThat(meterProtocolRegisterAdapter.readRegisters(null)).isEmpty();
        assertThat(meterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>())).isNotNull();
        assertThat(meterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>())).isEmpty();

        RegisterSupportedMeterProtocol meterProtocol = mock(RegisterSupportedMeterProtocol.class);
        meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(meterProtocol, issueService);
        assertThat(meterProtocolRegisterAdapter.readRegisters(null)).isNotNull();
        assertThat(meterProtocolRegisterAdapter.readRegisters(null)).isEmpty();
        assertThat(meterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>())).isNotNull();
        assertThat(meterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>())).isEmpty();
    }

    @Test
    public void deviceDoesNotSupportRegisterRequests() {
        when(this.issueService.newProblem(any(ObisCode.class), eq("registerXnotsupported"), anyVararg())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Problem registerXnotsupportedProblem = mock(Problem.class);
                when(registerXnotsupportedProblem.getDescription()).thenReturn("registerXnotsupported");
                when(registerXnotsupportedProblem.getSource()).thenReturn((invocation.getArguments()[0]));
                return registerXnotsupportedProblem;
            }
        });

        OfflineRegister register = getMockedRegister();

        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(null, issueService);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));

        assertThat(collectedRegisters).hasSize(1);
        assertThat(collectedRegisters.get(0).getResultType()).isEqualTo(ResultType.NotSupported);
        assertThat(collectedRegisters.get(0).getIssues().get(0).getDescription()).isEqualTo("registerXnotsupported");
        assertThat(collectedRegisters.get(0).getIssues().get(0).getSource()).isInstanceOf(ObisCode.class);
    }

    @Test
    public void unSupportedSizeTest(){
        OfflineRegister register = getMockedRegister();
        final int registerListSize = 10;
        List<OfflineRegister> registerList = new ArrayList<>(registerListSize);
        for(int i = 0; i < registerListSize; i++){
            registerList.add(register);
        }
        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(null, issueService);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(registerList);
        assertThat(collectedRegisters.size()).isEqualTo(registerListSize);
    }

    @Test(expected = LegacyProtocolException.class)
    public void exceptionTest() throws IOException {
        OfflineRegister register = getMockedRegister();

        RegisterSupportedMeterProtocol meterProtocol = mock(RegisterSupportedMeterProtocol.class);
        when(meterProtocol.readRegister(Matchers.<ObisCode>any())).thenThrow(new IOException("Failure during the reading"));
        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(meterProtocol, issueService);
        meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));
    }

    @Test
    public void getSuccessfulRegistersTest() throws IOException {
        OfflineRegister register = getMockedRegister();
        RegisterValue registerValue = getMockedRegisterValue();

        RegisterSupportedMeterProtocol meterProtocol = mock(RegisterSupportedMeterProtocol.class);
        when(meterProtocol.readRegister(Matchers.<ObisCode>any())).thenReturn(registerValue);

        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(meterProtocol, issueService);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));

        CollectedRegister collectedRegister = collectedRegisters.get(0);
        assertThat(collectedRegister.getFromTime()).isEqualTo(fromDate);
        assertThat(collectedRegister.getToTime()).isEqualTo(toDate);
        assertThat(collectedRegister.getEventTime()).isEqualTo(eventDate);
        assertThat(collectedRegister.getReadTime()).isEqualTo(readDate);
        assertThat(collectedRegister.getText()).isEqualTo(text);
        assertThat(collectedRegister.getCollectedQuantity()).isEqualTo(quantity);
    }

    @Test
    public void getMixtureRegisterTest() throws IOException {
        OfflineRegister register1 = getMockedRegister();
        OfflineRegister register2 = getMockedRegister();
        OfflineRegister register3 = getMockedRegister();
        RegisterValue registerValue = getMockedRegisterValue();

        RegisterSupportedMeterProtocol meterProtocol = mock(RegisterSupportedMeterProtocol.class);
        when(meterProtocol.readRegister(Matchers.<ObisCode>any())).thenReturn(registerValue);
        when(meterProtocol.readRegister(register2.getObisCode())).thenThrow(new UnsupportedException("Register Not supported"));

        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(meterProtocol, issueService);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register1, register2, register3));

        assertThat(collectedRegisters).hasSize(3);
        assertThat(collectedRegisters.get(0).getResultType()).isEqualTo(ResultType.Supported);
        assertThat(collectedRegisters.get(1).getResultType()).isEqualTo(ResultType.NotSupported);
        assertThat(collectedRegisters.get(2).getResultType()).isEqualTo(ResultType.Supported);
    }
}
