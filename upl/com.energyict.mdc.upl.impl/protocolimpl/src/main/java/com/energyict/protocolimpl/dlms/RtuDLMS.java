package com.energyict.protocolimpl.dlms;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NotFoundException;
import com.energyict.cpo.Environment;
import com.energyict.cpo.Transaction;
import com.energyict.dlms.UniversalObject;
import com.energyict.mdw.core.MeteringWarehouse;

import java.sql.*;

public class RtuDLMS {
    
    int confprogchange;
    int iCount;
    int rtuid;
    
    /** Creates a new instance of RtuDLMS */
    public RtuDLMS(int rtuid) {
        confprogchange=-1;
        this.rtuid = rtuid;
    }
    
    public int getConfProgChange() throws SQLException, BusinessException
    {
       PreparedStatement statement = null;
       ResultSet resultSet = null;
       Connection connection = getDefaultConnection();
       
       try
       {
          statement = connection.prepareStatement("select confprogchange from eisdlms where rtuid = ?");
          statement.setInt(1,rtuid);
          resultSet = statement.executeQuery();
          if (!resultSet.next()) throw new NotFoundException("ERROR: No rtu record found!");
          iCount=0;
          do
          {
              iCount++;
              // Retrieve field values from ctable
              confprogchange = resultSet.getInt(1);
          } while (resultSet.next());
          
          if (iCount >1) throw new BusinessException("ERROR: NR of records found > 1!");
       }
       finally
       {
          if (resultSet != null) resultSet.close();
          if (statement != null) statement.close();
       }
       
       return confprogchange;
       
    } // public int getConfProgChange(int rtuid)
    
    synchronized public void setConfProgChange(int confprogchange) throws SQLException
    {
       try
       {
           doInsert(confprogchange);
       }
       catch(SQLException e)
       {
          if ("23000".equals(e.getSQLState())) 
          {
             doUpdate(confprogchange);
          }
          else throw e;   
       }
       
    } // synchronized public void setConfProgChange(int confprogchange) throws SQLException

    private void doUpdate(int confprogchange) throws SQLException
    {
       Connection connection = getDefaultConnection(); 
       PreparedStatement statement = null;
       
       try { 
           String sqlString = "update eisdlms SET confprogchange=? where rtuid = ? ";
           statement = connection.prepareStatement(sqlString);
           statement.setInt(1,confprogchange);
           statement.setInt(2,rtuid);
           statement.executeUpdate();
       }
       catch(SQLException e) {
          throw e;   
       }
       finally {
          if (statement != null) statement.close();
       }
       
    }
    
    void doInsert(int confprogchange) throws SQLException
    {
       Connection connection = getDefaultConnection();
       PreparedStatement statement = connection.prepareStatement(
    		   "insert into eisdlms (RTUID, CONFPROGCHANGE) values(?,?)");  
       
       try {
    	   statement.setInt(1,rtuid);
    	   statement.setInt(2, confprogchange);
           statement.executeUpdate();
       } finally {
    	   statement.close();
       }
    }
    
    private Connection getDefaultConnection() {
       return Environment.getDefault().getConnection();
    }


    public void saveObjectList(final int confProgChange, final UniversalObject[] universalObject) throws BusinessException, SQLException    {
        Transaction tr = new Transaction() {
            public Object doExecute() throws SQLException, BusinessException {

                RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
                rtuCache.saveObjectList(universalObject);
                RtuDLMS.this.setConfProgChange(confProgChange);

                return null;
            }
        };
        MeteringWarehouse.getCurrent().execute(tr);
    }
} // public class RtuDLMS
