package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.cache.ProtocolCacheFetchException;
import com.energyict.mdc.upl.cache.ProtocolCacheUpdateException;

import com.energyict.cpo.SqlBuilder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides functionality to have access to the EISDEVICECACHE table
 *
 * Copyrights EnergyICT
 * Date: 20-mei-2010
 * Time: 11:29:25
 */
public class RTUCache {

    private int rtuid;  // Would love to rename this to deviceId but objects of this type may already have been serialized and saved in a BLOB database field

    /**
     * Creates a new instance of RtuDLMS
     */
    public RTUCache(int rtuid) {
        this.rtuid = rtuid;
    }

    /**
     * @return the blob object from the given rtuId
     */
    public Serializable getCacheObject(Connection connection) throws ProtocolCacheFetchException, SQLException {
		Serializable cacheObject = null;
		SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ? ");
        builder.bindInt(rtuid);
		try (PreparedStatement statement = builder.getStatement(connection)) {
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					Blob blob = resultSet.getBlob(1);
					if (blob.length() > 0) {
                        try (ObjectInputStream ois = new ObjectInputStream(blob.getBinaryStream())) {
							cacheObject = (Serializable) ois.readObject();
						} catch (ClassNotFoundException | IOException e) {
							throw new ProtocolCacheFetchException(e);
						}
					}
				}
			}
		}
		return cacheObject;
    }

    /**
     * Write the cache object to the database.
     *
     * @param cacheObject any object to write in the database
     *
     * @throws SQLException if the update failed
     */
    public synchronized void setBlob(Serializable cacheObject, Connection connection) throws SQLException, ProtocolCacheUpdateException {
	    createOrUpdateDeviceCache(connection);
	    updateCacheContent(cacheObject, connection);
	}

	/**
	 * Select the EISDEVICECACHE from the DB, if it doens't exist, create(an empty) one so later you can do an update
	 */
	private void createOrUpdateDeviceCache(Connection connection) throws SQLException {
		SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ?");
		builder.bindInt(rtuid);
		try (PreparedStatement stmnt = builder.getStatement(connection)) {
			try (ResultSet rs = stmnt.executeQuery()) {
                if (!rs.next()) {
                    builder = new SqlBuilder("insert into eisdevicecache (rtuid, content, mod_date) values (?,empty_blob(),sysdate)");
                    builder.bindInt(rtuid);
                    try (PreparedStatement insertStmnt = builder.getStatement(connection)) {
                        insertStmnt.executeUpdate();
                    }
                }
            }
		}
	}

	/**
	 * Select the EISDEVICECACHE from the DB and update the content
	 */
	private void updateCacheContent(Serializable cacheObject, Connection connection) throws SQLException, ProtocolCacheUpdateException {
		SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ? for update");
		builder.bindInt(rtuid);
		try (PreparedStatement stmnt = builder.getStatement(connection)) {
            try (ResultSet rs = stmnt.executeQuery()) {
                if (!rs.next()) {
                    throw new ProtocolCacheUpdateException("Record not found");
                }
                try {
                    Blob blob = rs.getBlob(1);
                    ObjectOutputStream out = new ObjectOutputStream(blob.setBinaryStream(0L));
                    out.writeObject(cacheObject);
                    out.close();
                } catch (IOException e) {
                    throw new ProtocolCacheUpdateException(e);
                }
            }
        }
	}

}
