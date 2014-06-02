package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.mocks.MockCollectedRegister;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link SmartMeterProtocolRegisterAdapter}
 *
 * @author gna
 * @since 10/04/12 - 16:04
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartMeterProtocolRegisterAdapterTest {

    private static final ObisCode OBIS_CODE = mock(ObisCode.class);
    private static final String METER_SERIAL_NUMBER = "METER_SERIAL_NUMBER";

    private final Date fromDate = mock(Date.class);
    private final Date toDate = mock(Date.class);
    private final Date readDate = mock(Date.class);
    private final Date eventDate = mock(Date.class);
    private final String text = "Some Text From a Register";
    private final Quantity quantity = mock(Quantity.class);

    private static OfflineRegister getMockedRegister() {
        OfflineRegister register = mock(OfflineRegister.class);
        when(register.getObisCode()).thenReturn(OBIS_CODE);
        when(register.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        return register;
    }

    @Mock
    private static UserEnvironment userEnvironment = mock(UserEnvironment.class);
    @Mock
    private Environment environment;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private IssueService issueService;

    @Before
    public void initializeEnvironment() {
        when(this.collectedDataFactory.createCollectedRegisterForAdapter(any(RegisterIdentifier.class))).
                thenAnswer(new Answer<CollectedRegister>() {
                    @Override
                    public CollectedRegister answer(InvocationOnMock invocationOnMock) throws Throwable {
                        RegisterIdentifier registerIdentifier = (RegisterIdentifier) invocationOnMock.getArguments()[0];
                        MockCollectedRegister collectedRegister = new MockCollectedRegister(registerIdentifier);
                        collectedRegister.setResultType(ResultType.Supported);
                        return collectedRegister;
                    }
                });
        when(this.collectedDataFactory.createDefaultCollectedRegister(any(RegisterIdentifier.class))).
                thenAnswer(new Answer<CollectedRegister>() {
                    @Override
                    public CollectedRegister answer(InvocationOnMock invocationOnMock) throws Throwable {
                        RegisterIdentifier registerIdentifier = (RegisterIdentifier) invocationOnMock.getArguments()[0];
                        return new MockCollectedRegister(registerIdentifier);
                    }
                });
        when(this.applicationContext.getModulesImplementing(CollectedDataFactory.class)).thenReturn(Arrays.asList(this.collectedDataFactory));
        when(this.environment.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(this.environment.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(this.environment.getApplicationContext()).thenReturn(this.applicationContext);
        Environment.DEFAULT.set(this.environment);
        CollectedDataFactoryProvider collectedDataFactoryProvider = mock(CollectedDataFactoryProvider.class);
        when(collectedDataFactoryProvider.getCollectedDataFactory()).thenReturn(this.collectedDataFactory);
        CollectedDataFactoryProvider.instance.set(collectedDataFactoryProvider);
    }

    @After
    public void cleanupEnvironment() {
        Environment.DEFAULT.set(null);
    }

    @After
    public void resetCollectedDataFactoryProvider() {
        CollectedDataFactoryProvider.instance.set(null);
    }

    @Before
    public void initializeIssueService () {
        when(this.issueService.newWarning(any(), anyString(), anyVararg())).thenAnswer(new Answer<Warning>() {
            @Override
            public Warning answer(InvocationOnMock invocationOnMock) throws Throwable {
                Warning warning = mock(Warning.class);
                when(warning.getSource()).thenReturn(invocationOnMock.getArguments()[0]);
                when(warning.getDescription()).thenReturn((String) invocationOnMock.getArguments()[1]);
                return warning;
            }
        });
    }

    private RegisterValue getMockedRegisterValue() {
        RegisterValue registerValue = mock(RegisterValue.class);
        when(registerValue.getEventTime()).thenReturn(eventDate);
        when(registerValue.getFromTime()).thenReturn(fromDate);
        when(registerValue.getReadTime()).thenReturn(readDate);
        when(registerValue.getToTime()).thenReturn(toDate);
        when(registerValue.getText()).thenReturn(text);
        when(registerValue.getQuantity()).thenReturn(quantity);
        when(registerValue.getObisCode()).thenReturn(OBIS_CODE);
        when(registerValue.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        return registerValue;
    }

    @Test
    public void getRegisterEmptyCallTest() {
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(null, issueService);
        assertThat(smartMeterProtocolRegisterAdapter.readRegisters(null)).isNotNull();
        assertThat(smartMeterProtocolRegisterAdapter.readRegisters(null)).isEmpty();
        assertThat(smartMeterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>())).isNotNull();
        assertThat(smartMeterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>())).isEmpty();
    }

    @Test
    public void deviceDoesNotSupportRegisterRequests() {
        OfflineRegister register = getMockedRegister();
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol, issueService);
        final List<CollectedRegister> collectedRegisters = smartMeterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));

        assertThat(collectedRegisters).hasSize(1);
        assertThat(collectedRegisters.get(0).getResultType()).isEqualTo(ResultType.NotSupported);
        assertThat(collectedRegisters.get(0).getIssues()).hasSize(1);
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
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol, issueService);
        final List<CollectedRegister> collectedRegisters = smartMeterProtocolRegisterAdapter.readRegisters(registerList);
        assertThat(collectedRegisters.size()).isEqualTo(registerListSize);
    }

    @Test(expected = LegacyProtocolException.class)
    public void ioExceptionTest() throws IOException {
        OfflineRegister register = getMockedRegister();

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.readRegisters(Matchers.<List<Register>>any())).thenThrow(new IOException("Failure during the reading"));
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol, issueService);
        smartMeterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));
    }

    @Test
    public void getSuccessfulRegistersTest() throws IOException {
        OfflineRegister register = getMockedRegister();
        RegisterValue registerValue = getMockedRegisterValue();

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.readRegisters(Matchers.<List<Register>>any())).thenReturn(Arrays.asList(registerValue));

        SmartMeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol, issueService);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));

        assertThat(collectedRegisters.get(0).getFromTime()).isEqualTo(fromDate);
        assertThat(collectedRegisters.get(0).getToTime()).isEqualTo(toDate);
        assertThat(collectedRegisters.get(0).getEventTime()).isEqualTo(eventDate);
        assertThat(collectedRegisters.get(0).getReadTime()).isEqualTo(readDate);
        assertThat(collectedRegisters.get(0).getText()).isEqualTo(text);
        assertThat(collectedRegisters.get(0).getCollectedQuantity()).isEqualTo(quantity);
    }

    @Test
    public void getMixtureRegisterTest() throws IOException {
        OfflineRegister register1 = getMockedRegister();
        OfflineRegister register2 = getMockedRegister();
        when(register2.getObisCode()).thenReturn(ObisCode.fromString("1.1.1.1.1.1"));
        OfflineRegister register3 = getMockedRegister();
        when(register3.getObisCode()).thenReturn(ObisCode.fromString("2.2.2.2.2.2"));
        RegisterValue registerValue1 = getMockedRegisterValue();
        RegisterValue registerValue2 = getMockedRegisterValue();
        when(registerValue2.getObisCode()).thenReturn(ObisCode.fromString("2.2.2.2.2.2"));

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.readRegisters(Matchers.<List<Register>>any())).thenReturn(Arrays.asList(registerValue1, registerValue2));


        SmartMeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol, issueService);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register1, register2, register3));

        assertThat(collectedRegisters.size()).isEqualTo(3);
        assertThat(collectedRegisters.get(0).getResultType()).isEqualTo(ResultType.Supported);
        assertThat(collectedRegisters.get(1).getResultType()).isEqualTo(ResultType.NotSupported);
        assertThat(collectedRegisters.get(2).getResultType()).isEqualTo(ResultType.Supported);
    }
}
