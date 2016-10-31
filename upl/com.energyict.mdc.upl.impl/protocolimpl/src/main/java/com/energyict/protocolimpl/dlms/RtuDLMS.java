package com.energyict.protocolimpl.dlms;

import com.energyict.mdc.upl.cache.ProtocolCacheFetchException;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.UniversalObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RtuDLMS {

    private int confProgChange;
    private int deviceId;

    public RtuDLMS(int deviceId) {
        confProgChange = -1;
        this.deviceId = deviceId;
    }

    public int getConfProgChange(Connection connection) throws SQLException, ProtocolCacheFetchException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT confprogchange FROM eisdlms WHERE rtuid = ?")) {
            statement.setInt(1, deviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new ProtocolCacheFetchException("ERROR: No rtu record found!");
                }
                int count = 0;
                do {
                    count++;
                    // Retrieve field values from ctable
                    confProgChange = resultSet.getInt(1);
                } while (resultSet.next());

                if (count > 1) {
                    // Should not occur if there is a primary key on eisdlms
                    throw new ProtocolCacheFetchException("ERROR: NR of records found > 1!");
                }
            }
        }
        return confProgChange;
    }

    public synchronized void setConfProgChange(int confprogchange, Connection connection) throws SQLException {
        try {
            doInsert(confprogchange, connection);
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                doUpdate(confprogchange, connection);
            } else {
                throw e;
            }
        }
    }

    private void doInsert(int confProgChange, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO eisdlms (RTUID, CONFPROGCHANGE) VALUES(?,?)")) {
            statement.setInt(1, deviceId);
            statement.setInt(2, confProgChange);
            statement.executeUpdate();
        }
    }

    private void doUpdate(int confprogchange, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE eisdlms SET confprogchange=? WHERE rtuid = ? ")) {
            statement.setInt(1, confprogchange);
            statement.setInt(2, deviceId);
            statement.executeUpdate();
        }
    }

    public void saveObjectList(int confProgChange, UniversalObject[] universalObject, Connection connection) throws BusinessException, SQLException {
        RtuDLMSCache rtuCache = new RtuDLMSCache(deviceId);
        rtuCache.saveObjectList(universalObject, connection);
        RtuDLMS.this.setConfProgChange(confProgChange, connection);
    }

}