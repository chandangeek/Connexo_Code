package com.energyict.protocolimpl.dlms;

import com.energyict.dlms.UniversalObject;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.sql.SQLException;

/**
 * @author : Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         This class retrieves and updates the EISDLMSCACHE table. It is used by the meterprotocol to cache up
 *         the short name references to cosem objects in DLMS when using short name referencing.
 *         <BR>
 *         <B>Changes :</B><BR>
 *         KV 30102002 : Initial version 1.0<BR>
 * @version : 1.0
 */
public class RtuDLMSCache {
    private int deviceId;

    public RtuDLMSCache(int deviceId) {
        this.deviceId = deviceId;
    }

    public synchronized void saveObjectList(UniversalObject[] universalObjects) throws SQLException {
        Bus.getOrmClient().saveUniversalObjectList(this.deviceId, universalObjects);
    }

    public UniversalObject[] getObjectList() throws SQLException {
        return Bus.getOrmClient().getUniversalObjectList(this.deviceId);
    }

}