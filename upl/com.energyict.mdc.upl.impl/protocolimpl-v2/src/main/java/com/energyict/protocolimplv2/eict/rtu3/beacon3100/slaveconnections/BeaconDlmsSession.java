package com.energyict.protocolimplv2.eict.rtu3.beacon3100.slaveconnections;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.DeviceProtocolDialect;

import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.connection.DlmsV2Connection;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;

/**
 * The BeaconDlmsSession adds functionality to select specific DlmsConnections based on the dialect that is used.
 * Depending on the dialect another connection will be used and different behavior will be applied, mainly for exception handling.
 * <p>
 * Copyrights EnergyICT
 * Date: 08.04.16
 * Time: 10:43
 */
public class BeaconDlmsSession extends DlmsSession {

    public BeaconDlmsSession(ComChannel comChannel, DlmsSessionProperties properties) {
        super(comChannel, properties);
    }

    public BeaconDlmsSession(ComChannel comChannel, DlmsSessionProperties properties, HHUSignOnV2 hhuSignOn, String deviceId) {
        super(comChannel, properties, hhuSignOn, deviceId);
    }

    @Override
    protected DlmsV2Connection defineTransportDLMSConnection() {
        if (ComChannelType.SocketComChannel.is(getComChannel())) {
            if (useBeaconMirrorDeviceDialect()) {
                return new MirrorTCPIPConnection(getComChannel(), getProperties());
            } else {
                return new GatewayTCPIPConnection(getComChannel(), getProperties());
            }
        } else {
            return super.defineTransportDLMSConnection();
        }
    }

    private boolean useBeaconMirrorDeviceDialect() {
        String dialectName = getProperties().getProperties().getStringProperty(DeviceProtocolDialect.DEVICE_PROTOCOL_DIALECT_NAME);
        return dialectName != null && dialectName.equals(DeviceProtocolDialectNameEnum.BEACON_MIRROR_TCP_DLMS_PROTOCOL_DIALECT_NAME.getName());
    }
}
