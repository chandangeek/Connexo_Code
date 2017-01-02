package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.cbo.Quantity;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.mocks.MockCollectedRegister;
import com.energyict.mdc.upl.issue.Warning;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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

    private final Date fromDate = new Date(1389612600000L); // Mon, 13 Jan 2014 11:30:00 GMT
    private final Date toDate = new Date(1389613500000L);   // Mon, 13 Jan 2014 11:45:00 GMT
    private final Date readDate = new Date(1389664800000L); // Tue, 14 Jan 2014 02:00:00 GMT
    private final Date eventDate = new Date(1389613500000L);// Same as toDate
    private final String text = "Some Text From a Register";
    private final Quantity quantity = mock(Quantity.class);

    private static OfflineRegister getMockedRegister() {
        OfflineRegister register = mock(OfflineRegister.class);
        when(register.getObisCode()).thenReturn(OBIS_CODE);
        when(register.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        return register;
    }

    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private IssueService issueService;

    @Before
    public void initializeEnvironment() {
        when(this.collectedDataFactory.createCollectedRegisterForAdapter(any(RegisterIdentifier.class), any(String.class))).
                thenAnswer(invocationOnMock -> {
                    RegisterIdentifier registerIdentifier = (RegisterIdentifier) invocationOnMock.getArguments()[0];
                    ReadingType readingType = (ReadingType) invocationOnMock.getArguments()[1];

                    MockCollectedRegister collectedRegister = new MockCollectedRegister(registerIdentifier, readingType.getMRID());
                    collectedRegister.setResultType(ResultType.Supported);
                    return collectedRegister;
                });
        when(this.collectedDataFactory.createDefaultCollectedRegister(any(RegisterIdentifier.class), any(String.class))).
                thenAnswer(invocationOnMock -> {
                    RegisterIdentifier registerIdentifier = (RegisterIdentifier) invocationOnMock.getArguments()[0];
                    ReadingType readingType = (ReadingType) invocationOnMock.getArguments()[1];
                    return new MockCollectedRegister(registerIdentifier, readingType.getMRID());
                });
    }

    @Before
    public void initializeIssueService () {
        when(this.issueService.newWarning(any(), any(), anyVararg())).thenAnswer(invocationOnMock -> {
            Warning warning = mock(Warning.class);
            when(warning.getSource()).thenReturn(invocationOnMock.getArguments()[0]);
            when(warning.getDescription()).thenReturn(String.valueOf(invocationOnMock.getArguments()[1]));
            return warning;
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
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(null, issueService, collectedDataFactory);
        assertThat(smartMeterProtocolRegisterAdapter.readRegisters(null)).isNotNull();
        assertThat(smartMeterProtocolRegisterAdapter.readRegisters(null)).isEmpty();
        assertThat(smartMeterProtocolRegisterAdapter.readRegisters(new ArrayList<>())).isNotNull();
        assertThat(smartMeterProtocolRegisterAdapter.readRegisters(new ArrayList<>())).isEmpty();
    }

    @Test
    public void deviceDoesNotSupportRegisterRequests() {
        OfflineRegister register = getMockedRegister();
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol, issueService, collectedDataFactory);
        final List<CollectedRegister> collectedRegisters = smartMeterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));

        assertThat(collectedRegisters).hasSize(1);
        assertThat(collectedRegisters.get(0).getResultType()).isEqualTo(ResultType.NotSupported);
        assertThat(collectedRegisters.get(0).getIssues()).hasSize(1);
        assertThat(collectedRegisters.get(0).getIssues().get(0).getDescription()).isEqualTo(String.valueOf(MessageSeeds.REGISTER_NOT_SUPPORTED));
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
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol, issueService, collectedDataFactory);
        final List<CollectedRegister> collectedRegisters = smartMeterProtocolRegisterAdapter.readRegisters(registerList);
        assertThat(collectedRegisters.size()).isEqualTo(registerListSize);
    }

    @Test(expected = LegacyProtocolException.class)
    public void ioExceptionTest() throws IOException {
        OfflineRegister register = getMockedRegister();

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.readRegisters(Matchers.<List<Register>>any())).thenThrow(new IOException("Failure during the reading"));
        SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol, issueService, collectedDataFactory);
        smartMeterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));
    }

    @Test
    public void getSuccessfulRegistersTest() throws IOException {
        OfflineRegister register = getMockedRegister();
        RegisterValue registerValue = getMockedRegisterValue();

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.readRegisters(Matchers.<List<Register>>any())).thenReturn(Arrays.asList(registerValue));

        SmartMeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol, issueService, collectedDataFactory);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register));

        assertThat(collectedRegisters.get(0).getFromTime()).isEqualTo(fromDate.toInstant());
        assertThat(collectedRegisters.get(0).getToTime()).isEqualTo(toDate.toInstant());
        assertThat(collectedRegisters.get(0).getEventTime()).isEqualTo(eventDate.toInstant());
        assertThat(collectedRegisters.get(0).getReadTime()).isEqualTo(readDate.toInstant());
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


        SmartMeterProtocolRegisterAdapter meterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(smartMeterProtocol, issueService, collectedDataFactory);
        final List<CollectedRegister> collectedRegisters = meterProtocolRegisterAdapter.readRegisters(Arrays.asList(register1, register2, register3));

        assertThat(collectedRegisters.size()).isEqualTo(3);
        assertThat(collectedRegisters.get(0).getResultType()).isEqualTo(ResultType.Supported);
        assertThat(collectedRegisters.get(1).getResultType()).isEqualTo(ResultType.NotSupported);
        assertThat(collectedRegisters.get(2).getResultType()).isEqualTo(ResultType.Supported);
    }
}
