package com.energyict.protocolimplv2.dlms.a1860.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.MockDeviceRegister;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.RegisterIdentifierById;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.a1860.A1860;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class A1860RegisterFactoryTest {

    @Mock A1860 protocolMock;
    @Mock CollectedDataFactory collectedDataFactoryMock;
    @Mock IssueFactory issueFactoryMock;
    @Mock OfflineRegister offlineRegisterMock;
    @Mock DlmsSession dlmsSessionMock;
    @Mock DLMSMeterConfig meterConfigMock;
    @Mock UniversalObject universalObjectMock;
    @Mock CosemObjectFactory cosemObjectFactoryMock;
    @Mock Register registerMock;

    @Before
    public void init() throws IOException {
        when(offlineRegisterMock.getObisCode()).thenReturn(ObisCode.fromString("1.1.13.7.0.255"));
        when(offlineRegisterMock.getRegisterId()).thenReturn(1L);
        when(offlineRegisterMock.getDeviceIdentifier()).thenReturn(new DeviceIdentifierBySerialNumber("SN"));
        when(protocolMock.getDlmsSession()).thenReturn(dlmsSessionMock);
        when(dlmsSessionMock.getMeterConfig()).thenReturn(meterConfigMock);
        when(dlmsSessionMock.getCosemObjectFactory()).thenReturn(cosemObjectFactoryMock);
        when(meterConfigMock.findObject(ObisCode.fromString("1.1.13.7.0.255"))).thenReturn(universalObjectMock);
        when(universalObjectMock.getClassID()).thenReturn(DLMSClassId.REGISTER.getClassId());
        when(cosemObjectFactoryMock.getRegister(ObisCode.fromString("1.1.13.7.0.255"))).thenReturn(registerMock);
        when(registerMock.getValueAttr()).thenReturn(new Integer32(10000));
        when(registerMock.getScalerUnit()).thenReturn(new ScalerUnit(-4, BaseUnit.UNITLESS));
        when(collectedDataFactoryMock.createMaximumDemandCollectedRegister(any()))
                .thenReturn(new MockDeviceRegister(new RegisterIdentifierById(1L,ObisCode.fromString("1.1.13.7.0.255"), new DeviceIdentifierBySerialNumber("SN"))));
    }

    @Test
    public void testThatScaleIsAppliedForUnitlessRegisterValues() {
        A1860RegisterFactory a1860RegisterFactory = new A1860RegisterFactory(protocolMock, collectedDataFactoryMock, issueFactoryMock);
        List<CollectedRegister> collectedDataList = a1860RegisterFactory.readRegisters(Arrays.asList(offlineRegisterMock));
        Assert.assertEquals(1, collectedDataList.size());
        Assert.assertEquals(new BigDecimal("1.0000"), collectedDataList.get(0).getCollectedQuantity().getAmount());
        Assert.assertEquals(Unit.get(255, 0), collectedDataList.get(0).getCollectedQuantity().getUnit());
    }

}
