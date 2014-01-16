package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
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

    private IssueServiceImpl issueService;


    @BeforeClass
    public static void initializeUserEnvironment() {
        UserEnvironment.setDefault(userEnvironment);
        when(userEnvironment.getErrorMsg(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }
        });
    }

    @AfterClass
    public static void cleanupUserEnvironment() {
        UserEnvironment.setDefault(null);
    }

    @Before
    public void initializeEnvironment() {
        Environment.DEFAULT.set(this.environment);
        when(this.environment.getErrorMsg(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });
        issueService = new IssueServiceImpl();
        issueService.setClock(new DefaultClock());
        com.energyict.mdc.issues.Bus.setIssueService(issueService); }

    @After
    public void cleanupEnvironment() {
        Environment.DEFAULT.set(null);
        com.energyict.mdc.issues.Bus.clearIssueService(issueService);
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
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(null);
        assertNotNull("Should not get a null object back", smartMeterProtocolRegisterAdapter.readRegisters(null));
        assertEquals("Should at least get an empty list", Collections.emptyList(), smartMeterProtocolRegisterAdapter.readRegisters(null));
        assertNotNull("Should not get a null object back", smartMeterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>()));
        assertEquals("Should at least get an empty list", Collections.emptyList(), smartMeterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>()));
    }

    @Test
    public void deviceDoesNotSupportRegisterRequests() {
        OfflineRegister register = getMockedRegister();
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol);
        final List<CollectedRegister> collectedRegisters = smartMeterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));

        assertEquals("Size of the collected registers should be 1", 1, collectedRegisters.size());
        assertEquals("ResultType should be 'Not supported'", ResultType.NotSupported, collectedRegisters.get(0).getResultType());
        assertEquals("Register should not be supported", Environment.DEFAULT.get().getErrorMsg("registerXnotsupported"), collectedRegisters.get(0).getIssues().get(0).getDescription());
        assertTrue("The Problem should have the source ObisCode", collectedRegisters.get(0).getIssues().get(0).getSource() instanceof ObisCode);
    }

    @Test
    public void unSupportedSizeTest(){
        OfflineRegister register = getMockedRegister();
        final int registerListSize = 10;
        List<OfflineRegister> registerList = new ArrayList<OfflineRegister>(registerListSize);
        for(int i = 0; i < registerListSize; i++){
            registerList.add(register);
        }
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol);
        final List<CollectedRegister> collectedRegisters = smartMeterProtocolRegisterAdapter.readRegisters(registerList);
        assertEquals("Should return " + registerListSize + " objects in the list", registerListSize, collectedRegisters.size());
    }

    @Test(expected = LegacyProtocolException.class)
    public void ioExceptionTest() throws IOException {
        OfflineRegister register = getMockedRegister();

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.readRegisters(Matchers.<List<Register>>any())).thenThrow(new IOException("Failure during the reading"));
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol);
        final List<CollectedRegister> collectedRegisters = smartMeterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));
    }

    @Test
    public void getSuccessfulRegistersTest() throws IOException {
        OfflineRegister register = getMockedRegister();
        RegisterValue registerValue = getMockedRegisterValue();

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.readRegisters(Matchers.<List<Register>>any())).thenReturn(Arrays.asList(registerValue));

        SmartMeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));

        assertEquals("FromDate should be the same", fromDate, collectedRegisters.get(0).getFromTime());
        assertEquals("ToDate should be the same", toDate, collectedRegisters.get(0).getToTime());
        assertEquals("EventDate should be the same", eventDate, collectedRegisters.get(0).getEventTime());
        assertEquals("ReadDate should be the same", readDate, collectedRegisters.get(0).getReadTime());
        assertEquals("RegisterValue text should be the same", text, collectedRegisters.get(0).getText());
        assertEquals("RegisterValue quantity should be the same", quantity, collectedRegisters.get(0).getCollectedQuantity());
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


        SmartMeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register1, register2, register3));

        assertEquals("Size should be the same", 3, collectedRegisters.size());
        assertEquals("The first register should be supported", ResultType.Supported, collectedRegisters.get(0).getResultType());
        assertEquals("The second register should not be supported", ResultType.NotSupported, collectedRegisters.get(1).getResultType());
        assertEquals("The third register should be supported", ResultType.Supported, collectedRegisters.get(2).getResultType());
    }
}
