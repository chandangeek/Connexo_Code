package com.energyict.protocolimpl.dlms;

import com.energyict.mdc.common.BusinessException;

import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.dlms.UniversalObject;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.sql.SQLException;

public class RtuDLMS {

    private int deviceId;

    public RtuDLMS(int deviceId) {
        super();
        this.deviceId = deviceId;
    }

    public int getConfProgChange() throws SQLException, BusinessException {
        return Bus.getOrmClient().getConfProgChange(this.deviceId);
    }

    public synchronized void setConfProgChange(int confprogchange) throws SQLException {
        Bus.getOrmClient().setConfProgChange(this.deviceId, confprogchange);
    }

    public void saveObjectList(final int confProgChange, final UniversalObject[] universalObject) throws BusinessException, SQLException {
        try {
            Bus.getOrmClient().execute(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    try {
                        RtuDLMSCache rtuCache = new RtuDLMSCache(deviceId);
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