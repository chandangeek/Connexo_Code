package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.eventhandling.XemexEventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.messages.XemexMessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.messages.XemexMessaging;

/**
 * @author sva
 * @since 29/01/13 - 14:52
 */
public class REMIDatalogger extends E350 {

    /**
     * Search for local slave devices so a general topology can be build up
     */
    public void searchForSlaveDevices() throws ConnectionException {
        getMeterTopology().searchForSlaveDevices();
    }

    @Override
    public String getVersion() {
        return "$Date: 2012-04-02 13:07:50 +0200 (ma, 02 apr 2012) $";
    }

    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new XemexProperties();
        }
        return this.properties;
    }

    @Override
    public EventProfile getEventProfile() {
        if (this.eventProfile == null) {
            this.eventProfile = new XemexEventProfile(this);
        }
        return this.eventProfile;
    }

    @Override
    public BulkRegisterProtocol getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new XemexRegisterFactory(this);
        }
        return this.registerFactory;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new XemexMessaging(new XemexMessageExecutor(this));
    }

    @Override
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        int address;

        if (obisCode.equalsIgnoreBChannel(monthlyObisCode)) {
            address = 0;
        } else {
            address = getPhysicalAddressFromSerialNumber(serialNumber);
        }
        if ((address == 0 && obisCode.getB() != -1) || obisCode.getB() == 128) { // then don't correct the obisCode
            return obisCode;
        }
        if (address != -1) {
            return ProtocolTools.setObisCodeField(obisCode, ObisCodeBFieldIndex, (byte) address);
        }
        return null;
    }
}
