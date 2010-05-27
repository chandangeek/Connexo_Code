package com.energyict.protocolimpl.base;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.DatabaseException;
import com.energyict.cpo.Environment;
import com.energyict.cpo.SqlBuilder;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.core.MeteringWarehouse;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.*;

/**
 * Provides functionality to have access to the EISDEVICECACHE table
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
		SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ? ");
        builder.bindInt(rtuid);
        PreparedStatement stmnt;
		try {
			stmnt = builder.getStatement(Environment.getDefault().getConnection());

	        try {
	              InputStream in = null;
	              ResultSet resultSet = stmnt.executeQuery();
	              try {
	            	  if (resultSet.next()) {
	            		  Blob blob = resultSet.getBlob(1);
	            		  if (blob.length() > 0) {
	            			  in = blob.getBinaryStream();
	            			  ObjectInputStream ois = new ObjectInputStream(in);
	            			  try {
	            				  cacheObject = ois.readObject();
	            			  } catch (ClassNotFoundException e) {
	            				  e.printStackTrace();
	            			  } finally {
	            				  ois.close();
	            			  }
	            		  }
	            	  }
	              } finally {
	                   resultSet.close();
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
    synchronized public void setBlob(final Object cacheObject) throws SQLException, BusinessException {
		Transaction tr = new Transaction() {
			public Object doExecute() throws SQLException, BusinessException {
				createOrUpdateDeviceCache();
				updateCacheContent();
				return null;
			}

            /**
             * Select the EISDEVICECACHE from the DB, if it doens't exist, create(an empty) one so later you can do an update
             * @throws SQLException
             */
			private void createOrUpdateDeviceCache() throws SQLException {
				SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ?");
				builder.bindInt(rtuid);
				PreparedStatement stmnt = builder.getStatement(Environment.getDefault().getConnection());
				try {
					ResultSet rs = stmnt.executeQuery();
					if (!rs.next()) {
						builder = new SqlBuilder("insert into eisdevicecache (rtuid, content, mod_date) values (?,empty_blob(),sysdate)");
						builder.bindInt(rtuid);
						PreparedStatement insertStmnt = builder.getStatement(Environment.getDefault().getConnection());
						try {
							insertStmnt.executeUpdate();
						}
						finally {
							insertStmnt.close();
						}
					}
				} finally {
					stmnt.close();
				}
			}

            /**
             * Select the EISDEVICECACHE from the DB and update the content 
             * @throws SQLException
             */
			private void updateCacheContent() throws SQLException {
				SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ? for update");
				builder.bindInt(rtuid);
				PreparedStatement stmnt = builder.getStatement(Environment.getDefault().getConnection());
				try {
					ResultSet rs = stmnt.executeQuery();
					if (!rs.next()) {
						throw new SQLException("Record not found");
					}
					try {
						java.sql.Blob blob = (java.sql.Blob) rs.getBlob(1);
						ObjectOutputStream out = new ObjectOutputStream(blob.setBinaryStream(0L));
						out.writeObject(cacheObject);
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						rs.close();
					}
				} finally {
					stmnt.close();
				}
			}
		};
		try {
			MeteringWarehouse.getCurrent().execute(tr);
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException("Failed to execute the stopCacheMechanism." + e);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Failed to execute the stopCacheMechanism." + e);
		}
	}

}
