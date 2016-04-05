package com.energyict.protocolimplv2.eict.webrtuz3;

import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractDlmsSlaveProtocol;
import com.energyict.protocolimplv2.eict.webrtuz3.messages.emeter.WebRTUZ3EMeterMessaging;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 28/04/2015 - 16:29
 */
public class EMeter extends AbstractDlmsSlaveProtocol {

    private final AbstractDlmsProtocol masterProtocol = new WebRTUZ3();
    private final WebRTUZ3EMeterMessaging eMeterMessaging = new WebRTUZ3EMeterMessaging(masterProtocol);

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU Z3 DLMS E-meter slave V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-01-25 15:02:12 +0100 (Mon, 25 Jan 2016)$";
    }

    protected DeviceProtocolSecurityCapabilities getSecurityCapabilities() {
        return masterProtocol;
    }

    protected DeviceMessageSupport getDeviceMessageSupport() {
        return eMeterMessaging;
    }
}