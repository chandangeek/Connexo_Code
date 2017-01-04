package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.mdc.protocol.inbound.InboundDeviceProtocol;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.BusinessException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.GPRSFrame;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.IdentificationResponseStructure;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 26/10/12 (13:36)
 */
@RunWith(PowerMockRunner.class)
public class CtrInboundDeviceProtocolTest {

    private CtrInboundDeviceProtocol inboundDeviceProtocol;

    @Mock
    private RequestFactory requestFactory;
    @Mock
    private PropertySpecService propertySpecService;
    private byte[] rawFrame = ProtocolTools.getBytesFromHexString("0A0000002930006655443322110044264420456C657474726F20494D504C52494D504C52302E3131314242425231303201010003B64178787800000130303030303030303030303030021300000581BFB7DC7641C23BB000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000095ED7C859A6D0D", "");

    @Before
    public void initializeMocksAndFactories() throws CTRException {
        inboundDeviceProtocol = new CtrInboundDeviceProtocol(propertySpecService);

        GPRSFrame frame = new GPRSFrame().parse(rawFrame, 0);
        IdentificationResponseStructure identificationResponseStructure = (IdentificationResponseStructure) frame.getData();
        when(this.requestFactory.getIdentificationStructure()).thenReturn(identificationResponseStructure);
    }

    @After
    public void cleanUp() throws BusinessException, SQLException {
    }

    @Test
    public void testDiscovery() {
        inboundDeviceProtocol.setRequestFactory(requestFactory);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = inboundDeviceProtocol.doDiscovery();
        DeviceIdentifier deviceIdentifier = inboundDeviceProtocol.getDeviceIdentifier();

        assertEquals(InboundDeviceProtocol.DiscoverResultType.IDENTIFIER, discoverResultType);
        assertTrue(deviceIdentifier instanceof CTRDialHomeIdDeviceIdentifier);
        assertEquals("device with call home id 66554433221100", deviceIdentifier.toString());
    }
}
