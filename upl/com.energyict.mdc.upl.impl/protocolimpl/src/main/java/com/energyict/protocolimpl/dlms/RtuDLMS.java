package com.energyict.protocolimpl.dlms;

import com.energyict.cpo.*;
import com.energyict.cbo.*;
import java.util.*;
import java.sql.*;
import java.io.*;

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
       Statement statement = null;
       ResultSet resultSet = null;
       Connection connection = getDefaultConnection();
       
       try
       {
          statement = connection.createStatement();
          String sqlString = "select confprogchange from eisdlms where rtuid = " + rtuid;
          resultSet = statement.executeQuery(sqlString);
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
           String sqlString = "update eisdlms SET confprogchange=? where rtuid = " + rtuid;
           statement = connection.prepareStatement(sqlString);
           statement.setInt(1,confprogchange);
           statement.execute();
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
       Statement statement = connection.createStatement();  
       
       try {
           StringBuffer buffer = new StringBuffer("insert into eisdlms");
           buffer.append(" (RTUID, CONFPROGCHANGE) values(");
           buffer.append(rtuid);
           buffer.append(",");
           buffer.append(confprogchange);
           buffer.append(")");
           statement.executeUpdate (buffer.toString());
       }
       catch(SQLException e) {
          throw e;   
       }
       finally {
          if (statement != null) statement.close();
       }
    }
    
    private Connection getDefaultConnection() {
       return Environment.getDefault().getConnection();
    }

} // public class RtuDLMS
