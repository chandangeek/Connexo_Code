package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.common.Quantity;
import com.energyict.comserver.adapters.meterprotocol.mock.RegisterSupportedMeterProtocol;
import com.energyict.comserver.exceptions.LegacyProtocolException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.Bus;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.MeterProtocolRegisterAdapter;
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

    @Before
    public void initializeIssueService () {
        Bus.setIssueService(this.issueService);
        when(this.issueService.newProblem(anyString(), anyString(), anyVararg())).thenReturn(mock(Problem.class));
    }

    @After
    public void cleanupIssueService () {
        Bus.clearIssueService(this.issueService);
    }

    @Before
    public void setUpEnvironment () {
        Environment.DEFAULT.set(this.environment);
        when(this.environment.getErrorMsg(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });
    }

    @After
    public void tearDownEnvironment () {
        Environment.DEFAULT.set(null);
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
        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(null);
        assertNotNull("Should not get a null object back", meterProtocolRegisterAdapter.readRegisters(null));
        assertEquals("Should at least get an empty list", Collections.emptyList(), meterProtocolRegisterAdapter.readRegisters(null));
        assertNotNull("Should not get a null object back", meterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>()));
        assertEquals("Should at least get an empty list", Collections.emptyList(), meterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>()));

        RegisterSupportedMeterProtocol meterProtocol = mock(RegisterSupportedMeterProtocol.class);
        meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(meterProtocol);
        assertNotNull("Should not get a null object back", meterProtocolRegisterAdapter.readRegisters(null));
        assertEquals("Should at least get an empty list", Collections.emptyList(), meterProtocolRegisterAdapter.readRegisters(null));
        assertNotNull("Should not get a null object back", meterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>()));
        assertEquals("Should at least get an empty list", Collections.emptyList(), meterProtocolRegisterAdapter.readRegisters(new ArrayList<OfflineRegister>()));
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

        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(null);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));

        assertEquals("Size of the collected registers should be 1", 1, collectedRegisters.size());
        assertEquals("ResultType should be 'Not supported'", ResultType.NotSupported, collectedRegisters.get(0).getResultType());
        assertEquals("registerXnotsupported", collectedRegisters.get(0).getIssues().get(0).getDescription());
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
        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(null);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(registerList);
        assertEquals("Should return " + registerListSize + " objects in the list", registerListSize, collectedRegisters.size());
    }

    @Test(expected = LegacyProtocolException.class)
    public void exceptionTest() throws IOException {
        OfflineRegister register = getMockedRegister();

        RegisterSupportedMeterProtocol meterProtocol = mock(RegisterSupportedMeterProtocol.class);
        when(meterProtocol.readRegister(Matchers.<ObisCode>any())).thenThrow(new IOException("Failure during the reading"));
        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(meterProtocol);
        meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));
    }

    @Test
    public void getSuccessfulRegistersTest() throws IOException {
        OfflineRegister register = getMockedRegister();
        RegisterValue registerValue = getMockedRegisterValue();

        RegisterSupportedMeterProtocol meterProtocol = mock(RegisterSupportedMeterProtocol.class);
        when(meterProtocol.readRegister(Matchers.<ObisCode>any())).thenReturn(registerValue);

        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(meterProtocol);
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
        OfflineRegister register3 = getMockedRegister();
        RegisterValue registerValue = getMockedRegisterValue();

        RegisterSupportedMeterProtocol meterProtocol = mock(RegisterSupportedMeterProtocol.class);
        when(meterProtocol.readRegister(Matchers.<ObisCode>any())).thenReturn(registerValue);
        when(meterProtocol.readRegister(register2.getObisCode())).thenThrow(new UnsupportedException("Register Not supported"));

        MeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(meterProtocol);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register1, register2, register3));

        assertEquals("Size should be the same", 3, collectedRegisters.size());
        assertEquals("The first register should be supported", ResultType.Supported, collectedRegisters.get(0).getResultType());
        assertEquals("The second register should not be supported", ResultType.NotSupported, collectedRegisters.get(1).getResultType());
        assertEquals("The third register should be supported", ResultType.Supported, collectedRegisters.get(2).getResultType());
    }
}
