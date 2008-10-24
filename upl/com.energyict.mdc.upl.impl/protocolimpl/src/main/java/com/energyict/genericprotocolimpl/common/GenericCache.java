package com.energyict.genericprotocolimpl.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.DatabaseException;
import com.energyict.cpo.Environment;
import com.energyict.cpo.SqlBuilder;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;

/**
 * 
 * @author gna
 *
 */
public class GenericCache {
	
	public static Object startCacheMechanism(Rtu meter) throws FileNotFoundException, IOException {
		Object cacheObject = null;
		SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ? ");
        builder.bindInt(meter.getId());
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
	
	public static void stopCacheMechanism(final Rtu meter, final Object cacheObject) throws BusinessException, SQLException {
		Transaction tr = new Transaction() {
			public Object doExecute() throws SQLException, BusinessException {
				createOrUpdateDeviceCache();
				updateCacheContent();
				return null;
			}

			private void createOrUpdateDeviceCache() throws SQLException {
				SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ?");
				builder.bindInt(meter.getId());
				PreparedStatement stmnt = builder.getStatement(Environment.getDefault().getConnection());		
				try {
					ResultSet rs = stmnt.executeQuery();
					if (!rs.next()) {
						builder = new SqlBuilder("insert into eisdevicecache (rtuid, content, mod_date) values (?,empty_blob(),sysdate)");
						builder.bindInt(meter.getId());
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

			private void updateCacheContent() throws SQLException {
				SqlBuilder builder = new SqlBuilder("select content from eisdevicecache where rtuid = ? for update");
				builder.bindInt(meter.getId());
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
