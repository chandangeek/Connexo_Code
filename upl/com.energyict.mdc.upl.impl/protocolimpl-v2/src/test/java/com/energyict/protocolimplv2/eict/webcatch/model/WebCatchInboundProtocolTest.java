package com.energyict.protocolimplv2.eict.webcatch.model;

import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.eict.webcatch.WebCatchInboundProtocol;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class WebCatchInboundProtocolTest {

    @Mock
    PropertySpecService propertySpecServiceMock;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    InboundDiscoveryContext context;
    @Mock
    CollectedDataFactory collectedDataFactory;
    @Mock
    LoadProfileIdentifier loadProfileIdentifier;

    MockCollectedLoadProfile mockCollectedLoadProfile = new MockCollectedLoadProfile(loadProfileIdentifier);

    @Before
    public void init() throws IOException {
        Mockito.when(context.getCollectedDataFactory()).thenReturn(collectedDataFactory);
        Mockito.when(collectedDataFactory.createCollectedLoadProfile(Mockito.any())).thenReturn(mockCollectedLoadProfile);
    }

    @Test
    public void testParsingCorrectOrder() throws IOException {
        Mockito.when(request.getInputStream()).thenReturn(new ServletInputStreamCorrectOrder());
        WebCatchInboundProtocol inboundProtocol = new WebCatchInboundProtocol(propertySpecServiceMock);
        inboundProtocol.init(request, response);
        inboundProtocol.initializeDiscoveryContext(context);
        inboundProtocol.doDiscovery();
        inboundProtocol.getCollectedData();
        System.out.println(mockCollectedLoadProfile.getChannelInfo());
        System.out.println(mockCollectedLoadProfile.getCollectedIntervalData());
        BigDecimal expectedChannel1 = new BigDecimal("1070715.000");
        BigDecimal expectedChannel2 = new BigDecimal("50054.000");
        BigDecimal expectedChannel3 = new BigDecimal("4.000");
        BigDecimal expectedChannel4 = new BigDecimal("2.000");
        assertEquals(ObisCode.fromString("0.1.128.0.0.255"), mockCollectedLoadProfile.getChannelInfo().get(0).getChannelObisCode());
        assertEquals(ObisCode.fromString("0.2.128.0.0.255"), mockCollectedLoadProfile.getChannelInfo().get(1).getChannelObisCode());
        assertEquals(ObisCode.fromString("0.3.128.0.0.255"), mockCollectedLoadProfile.getChannelInfo().get(2).getChannelObisCode());
        assertEquals(ObisCode.fromString("0.4.128.0.0.255"), mockCollectedLoadProfile.getChannelInfo().get(3).getChannelObisCode());
        assertEquals(expectedChannel1, mockCollectedLoadProfile.getCollectedIntervalData().get(0).getIntervalValues().get(0).getNumber());
        assertEquals(expectedChannel2, mockCollectedLoadProfile.getCollectedIntervalData().get(0).getIntervalValues().get(1).getNumber());
        assertEquals(expectedChannel3, mockCollectedLoadProfile.getCollectedIntervalData().get(0).getIntervalValues().get(2).getNumber());
        assertEquals(expectedChannel4, mockCollectedLoadProfile.getCollectedIntervalData().get(0).getIntervalValues().get(3).getNumber());
    }

    @Test
    public void testParsingCorrectIncorrectOrder() throws IOException {
        Mockito.when(request.getInputStream()).thenReturn(new ServletInputStreamIncorrectOrder());
        WebCatchInboundProtocol inboundProtocol = new WebCatchInboundProtocol(propertySpecServiceMock);
        inboundProtocol.init(request, response);
        inboundProtocol.initializeDiscoveryContext(context);
        inboundProtocol.doDiscovery();
        inboundProtocol.getCollectedData();
        System.out.println(mockCollectedLoadProfile.getChannelInfo());
        System.out.println(mockCollectedLoadProfile.getCollectedIntervalData());
        BigDecimal expectedChannel1 = new BigDecimal("1070715.000");
        BigDecimal expectedChannel2 = new BigDecimal("50054.000");
        BigDecimal expectedChannel3 = new BigDecimal("4.000");
        BigDecimal expectedChannel4 = new BigDecimal("2.000");
        assertEquals(ObisCode.fromString("0.1.128.0.0.255"), mockCollectedLoadProfile.getChannelInfo().get(0).getChannelObisCode());
        assertEquals(ObisCode.fromString("0.2.128.0.0.255"), mockCollectedLoadProfile.getChannelInfo().get(1).getChannelObisCode());
        assertEquals(ObisCode.fromString("0.3.128.0.0.255"), mockCollectedLoadProfile.getChannelInfo().get(2).getChannelObisCode());
        assertEquals(ObisCode.fromString("0.4.128.0.0.255"), mockCollectedLoadProfile.getChannelInfo().get(3).getChannelObisCode());
        assertEquals(expectedChannel1, mockCollectedLoadProfile.getCollectedIntervalData().get(0).getIntervalValues().get(0).getNumber());
        assertEquals(expectedChannel2, mockCollectedLoadProfile.getCollectedIntervalData().get(0).getIntervalValues().get(1).getNumber());
        assertEquals(expectedChannel3, mockCollectedLoadProfile.getCollectedIntervalData().get(0).getIntervalValues().get(2).getNumber());
        assertEquals(expectedChannel4, mockCollectedLoadProfile.getCollectedIntervalData().get(0).getIntervalValues().get(3).getNumber());
    }

}
