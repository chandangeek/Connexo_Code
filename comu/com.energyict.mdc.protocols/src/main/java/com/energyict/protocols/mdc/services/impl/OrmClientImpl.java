/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.NotFoundException;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.UniversalObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link OrmClient} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-01 (08:46)
 */
@LiteralSql
public class OrmClientImpl implements OrmClient {

    private static final Logger LOGGER = Logger.getLogger(OrmClientImpl.class.getName());
    private static final String INTEGRITY_CONSTRAINT_VIOLATION_SQL_STATE_CODE = "23000";

    private final DataModel dataModel;
    private final TransactionService transactionService;
    private final Clock clock;

    public OrmClientImpl(DataModel dataModel, TransactionService transactionService, Clock clock) {
        super();
        this.dataModel = dataModel;
        this.transactionService = transactionService;
        this.clock = clock;
    }

    @Override
    public Object getCacheObject(int deviceId) throws IOException {
        Object cacheObject = null;
        SqlBuilder builder = new SqlBuilder("select content from ces_devicecache where deviceid =");
        builder.addInt(deviceId);
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement stmnt = builder.prepare(connection)) {
            InputStream in;
            try (ResultSet resultSet = stmnt.executeQuery()) {
                if (resultSet.next()) {
                    Blob blob = resultSet.getBlob(1);
                    if (blob.length() > 0) {
                        in = blob.getBinaryStream();
                        try (ObjectInputStream ois = new ObjectInputStream(in)) {
                            cacheObject = ois.readObject();
                        }
                        catch (ClassNotFoundException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return cacheObject;
    }

    @Override
    public void setCacheObject(final int deviceId, final Object cacheObject) throws SQLException {
        try {
            this.transactionService.execute(new VoidTransaction() {
                @Override
                protected void doPerform() {
                    createOrUpdateDeviceCache(deviceId);
                    updateCacheContent(deviceId, cacheObject);
                }
            });
        }
        catch (LocalSQLException e) {
            throw e.getCause();
        }
    }

    /**
     * Select the CES_DEVICECACHE from the DB, if it doens't exist, create(an empty) one so later you can do an update.
     */
    private void createOrUpdateDeviceCache(final int deviceId) {
        SqlBuilder builder = new SqlBuilder("select content from ces_devicecache where deviceid =");
        builder.addInt(deviceId);
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement stmnt = builder.prepare(connection)) {
            try (ResultSet rs = stmnt.executeQuery()) {
                if (!rs.next()) {
                    builder = new SqlBuilder("insert into ces_devicecache (deviceid, content, modTime) values (");
                    builder.addLong(deviceId);
                    builder.append(",empty_blob(),");
                    builder.addLong(this.clock.instant().toEpochMilli());
                    builder.append(")");
                    try (PreparedStatement insertStmnt = builder.prepare(connection)) {
                        insertStmnt.executeUpdate();
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new LocalSQLException(e);
        }
    }

    /**
     * Select the CES_DEVICECACHE from the DB and update the content.
     */
    private void updateCacheContent(final int deviceId, final Object cacheObject) {
        SqlBuilder builder = new SqlBuilder("select content from ces_devicecache where deviceid =");
        builder.addInt(deviceId);
        builder.append("for update");
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement stmnt = builder.prepare(connection)) {
            ResultSet rs = stmnt.executeQuery();
            if (!rs.next()) {
                throw new LocalSQLException(new SQLException("Record not found"));
            }
            try {
                Blob blob = rs.getBlob(1);
                ObjectOutputStream out = new ObjectOutputStream(blob.setBinaryStream(0L));
                out.writeObject(cacheObject);
                out.close();
            }
            catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new LocalSQLException(new SQLException("Underlying IOException", e));
            }
            finally {
                rs.close();
            }
        }
        catch (SQLException e) {
            throw new LocalSQLException(e);
        }
    }

    @Override
    public int getConfProgChange(final int deviceId) throws SQLException {
        int result;
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT confprogchange FROM eisdlms WHERE rtuid =");
        sqlBuilder.addInt(deviceId);
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new NotFoundException("ERROR: No device record found!");
                }
                result = resultSet.getInt(1);
                if (resultSet.next()) {
                    // There is another matching row found
                    throw new NotFoundException("ERROR: NR of records found > 1!");
                }
                return result;
            }
        }
    }

    @Override
    public void setConfProgChange(final int deviceId, final int confProgChange) throws SQLException {
        try {
            this.createConfProgChange(deviceId, confProgChange);
        }
        catch (SQLException e) {
            if (isIntegrityConstraintViolation(e)) {
                this.updateConfProgChange(deviceId, confProgChange);
            }
            else {
                throw e;
            }
        }
    }

    private void createConfProgChange(final int deviceId, final int confProgChange) throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder("INSERT INTO eisdlms (RTUID, CONFPROGCHANGE) VALUES(");
        sqlBuilder.addInt(deviceId);
        sqlBuilder.append(",");
        sqlBuilder.addInt(confProgChange);
        sqlBuilder.append(")");
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            statement.executeUpdate();
        }
    }

    private void updateConfProgChange(final int deviceId, final int confProgChange) throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE eisdlms SET confprogchange=");
        sqlBuilder.addInt(confProgChange);
        sqlBuilder.append("where rtuid=");
        sqlBuilder.addInt(deviceId);
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            statement.executeUpdate();
        }
    }

    @Override
    public UniversalObject[] getUniversalObjectList(final int deviceId) throws SQLException {
        List<UniversalObject> universalObjects = new ArrayList<>();
        SqlBuilder sqlBuilder = new SqlBuilder("select logicaldevice,longname,shortname,classid,version,associationlevel,objectdescription from eisdlmscache where rtuid =");
        sqlBuilder.addInt(deviceId);
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UniversalObject universalObject = new UniversalObject(UniversalObject.getSNObjectListEntrySize());
                    universalObject.setBaseName(resultSet.getInt(3));
                    universalObject.setClassID(resultSet.getInt(4));
                    universalObject.setVersion(resultSet.getInt(5));
                    universalObject.setLN(resultSet.getString(2));
                    universalObjects.add(universalObject);
                }
            }
        }
        if (universalObjects.isEmpty()) {
            return null;
        }
        else {
            return universalObjects.toArray(new UniversalObject[universalObjects.size()]);
        }
    }

    @Override
    public void saveUniversalObjectList(int deviceId, UniversalObject... universalObjects) throws SQLException {
        for (UniversalObject universalObject : universalObjects) {
            try {
                this.createUniversalObject(deviceId, universalObject);
            }
            catch (SQLException e) {
                if (isIntegrityConstraintViolation(e)) {
                    this.updateUniversalObject(deviceId, universalObject);
                }
                else {
                    throw e;
                }
            }
        }
    }

    private void createUniversalObject(int deviceId, UniversalObject universalObject) throws SQLException {
        this.createUniversalObject(
                deviceId,
                universalObject.getLN(),
                universalObject.getBaseName(),
                universalObject.getClassID(),
                universalObject.getVersion(),
                DLMSUtils.getInfoLN(universalObject.getLNArray()));
    }

    private void createUniversalObject(int deviceId, String longName, int shortName, int classId, int version, String objectDescription) throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder("INSERT INTO eisdlmscache (RTUID, LOGICALDEVICE, LONGNAME, SHORTNAME, CLASSID, VERSION, ASSOCIATIONLEVEL, OBJECTDESCRIPTION) VALUES(");
        sqlBuilder.addInt(deviceId);
        sqlBuilder.append(",0,");
        sqlBuilder.addObject(longName);
        sqlBuilder.append(",");
        sqlBuilder.addInt(shortName);
        sqlBuilder.append(",");
        sqlBuilder.addInt(classId);
        sqlBuilder.append(",");
        sqlBuilder.addInt(version);
        sqlBuilder.append(",0,");
        sqlBuilder.addObject(objectDescription);
        sqlBuilder.append(")");
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            statement.executeUpdate();
        }
    }

    private void updateUniversalObject(int deviceId, UniversalObject universalObject) throws SQLException {
        this.updateUniversalObject(
                deviceId,
                universalObject.getLN(),
                universalObject.getBaseName(),
                universalObject.getClassID(),
                universalObject.getVersion(),
                DLMSUtils.getInfoLN(universalObject.getLNArray()));
    }

    private void updateUniversalObject(int deviceId, String longname, int shortName, int classId, int version, String objectDescription) throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE eisdlmscache SET shortName =");
        sqlBuilder.addInt(shortName);
        sqlBuilder.append(", classid=");
        sqlBuilder.addInt(classId);
        sqlBuilder.append(", version=");
        sqlBuilder.addInt(version);
        sqlBuilder.append(", associationlevel=");
        sqlBuilder.addInt(0);
        sqlBuilder.append(", objectDescription=");
        sqlBuilder.addObject(objectDescription);
        sqlBuilder.append("where rtuid=");
        sqlBuilder.addInt(deviceId);
        sqlBuilder.append("and longname=");
        sqlBuilder.addObject(longname);
        try (Connection connection = this.dataModel.getConnection(true);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            statement.executeUpdate();
        }
    }

    @Override
    public <T> T execute(Transaction<T> transaction) {
        return this.transactionService.execute(transaction);
    }

    private boolean isIntegrityConstraintViolation(SQLException e) {
        return INTEGRITY_CONSTRAINT_VIOLATION_SQL_STATE_CODE.equals(e.getSQLState());
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