package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Transaction;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides functionality to have access to the CES_DEVICECACHE table.
 *
 * Copyrights EnergyICT
 * Date: 20-mei-2010
 * Time: 11:29:25
 */
public class RTUCache {

    /** The RTU id from the rtu */
    int rtuid;

    /**
     * Creates a new instance of RtuDLMS
     */
    public RTUCache(int rtuid) {
        this.rtuid = rtuid;
    }

    /**
     * @return the blob object from the given rtuId
     * @throws IOException
     */
    public Object getCacheObject() throws IOException {
		Object cacheObject = null;
		SqlBuilder builder = new SqlBuilder("select content from ces_devicecache where deviceid = ? ");
        builder.bindInt(rtuid);
        PreparedStatement stmnt;
		try (Connection connection = Environment.DEFAULT.get().getConnection()) {
			stmnt = builder.getStatement(connection);
	        try {
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
                                e.printStackTrace();
                            }
                        }
                    }
                }
	        } finally {
	              stmnt.close();
        }
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		return cacheObject;
    }

    /**
     * Write the cache object to the database.
     *
     * @param cacheObject
     *               any object to write in the database
     *
     * @throws SQLException if the update failed
     * @throws BusinessException if a business error occurred
     */
    public synchronized void setBlob(final Object cacheObject) throws SQLException, BusinessException {
		Transaction tr = new SetBlobTransaction(cacheObject);
		try {
			Environment.DEFAULT.get().execute(tr);
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException("Failed to execute the stopCacheMechanism." + e);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Failed to execute the stopCacheMechanism." + e);
		}
	}

    private class SetBlobTransaction implements Transaction {
        private final Object cacheObject;

        private SetBlobTransaction (Object cacheObject) {
            this.cacheObject = cacheObject;
        }

        public Object doExecute() throws SQLException, BusinessException {
            createOrUpdateDeviceCache();
            updateCacheContent();
            return null;
        }

        /**
         * Select the CES_DEVICECACHE from the DB, if it doens't exist, create(an empty) one so later you can do an update
         * @throws SQLException
         */
        private void createOrUpdateDeviceCache() throws SQLException {
            SqlBuilder builder = new SqlBuilder("select content from ces_devicecache where deviceid = ?");
            builder.bindInt(rtuid);
            try (PreparedStatement stmnt = builder.getStatement(Environment.DEFAULT.get().getConnection())) {
                ResultSet rs = stmnt.executeQuery();
                if (!rs.next()) {
                    builder = new SqlBuilder("insert into ces_devicecache (deviceid, content, mod_date) values (?,empty_blob(),sysdate)");
                    builder.bindInt(rtuid);
                    try (PreparedStatement insertStmnt = builder.getStatement(Environment.DEFAULT.get().getConnection())) {
                        insertStmnt.executeUpdate();
                    }
                }
            }
        }

        /**
         * Select the CES_DEVICECACHE from the DB and update the content
         * @throws SQLException
         */
        private void updateCacheContent() throws SQLException {
            SqlBuilder builder = new SqlBuilder("select content from ces_devicecache where deviceid = ? for update");
            builder.bindInt(rtuid);
            try (PreparedStatement stmnt = builder.getStatement(Environment.DEFAULT.get().getConnection())) {
                ResultSet rs = stmnt.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Record not found");
                }
                try {
                    Blob blob = rs.getBlob(1);
                    ObjectOutputStream out = new ObjectOutputStream(blob.setBinaryStream(0L));
                    out.writeObject(cacheObject);
                    out.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    rs.close();
                }
            }
        }
    }

}