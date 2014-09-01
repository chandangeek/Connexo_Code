package com.energyict.protocolimpl.dlms;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Transaction;

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
        Transaction tr = new Transaction() {
            public Object doExecute() throws SQLException {

                RtuDLMSCache rtuCache = new RtuDLMSCache(deviceId);
                rtuCache.saveObjectList(universalObject);
                RtuDLMS.this.setConfProgChange(confProgChange);

                return null;
            }
        };
        Environment.DEFAULT.get().execute(tr);
    }

}