package com.energyict.protocolimpl.dlms;

import com.energyict.dlms.UniversalObject;

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
    private final int deviceId;
    private final OrmClient ormClient;

    public RtuDLMSCache(int deviceId, OrmClient ormClient) {
        this.deviceId = deviceId;
        this.ormClient = ormClient;
    }

    public synchronized void saveObjectList(UniversalObject[] universalObjects) throws SQLException {
        this.ormClient.saveUniversalObjectList(this.deviceId, universalObjects);
    }

    public UniversalObject[] getObjectList() throws SQLException {
        return this.ormClient.getUniversalObjectList(this.deviceId);
    }

}