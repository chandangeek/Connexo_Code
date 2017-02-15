/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms;

import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.dlms.UniversalObject;

import java.sql.SQLException;

public class RtuDLMS {

    private final OrmClient ormClient;
    private int deviceId;

    public RtuDLMS(int deviceId, OrmClient ormClient) {
        super();
        this.deviceId = deviceId;
        this.ormClient = ormClient;
    }

    public int getConfProgChange() throws SQLException {
        return this.ormClient.getConfProgChange(this.deviceId);
    }

    public synchronized void setConfProgChange(int confprogchange) throws SQLException {
        this.ormClient.setConfProgChange(this.deviceId, confprogchange);
    }

    public void saveObjectList(final int confProgChange, final UniversalObject[] universalObject) throws SQLException {
        try {
            this.ormClient.execute(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    try {
                        RtuDLMSCache rtuCache = new RtuDLMSCache(deviceId, ormClient);
                        rtuCache.saveObjectList(universalObject);
                        RtuDLMS.this.setConfProgChange(confProgChange);
                    }
                    catch (SQLException e) {
                        throw new LocalSQLException(e);
                    }
                }
            });
        }
        catch (LocalSQLException e) {
            throw e.getCause();
        }
    }

    private class LocalSQLException extends RuntimeException {
        private LocalSQLException(SQLException cause) {
            super(cause);
        }

        @Override
        public SQLException getCause() {
            return (SQLException) super.getCause();
        }
    }

}