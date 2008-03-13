package com.energyict.protocolimpl.dlms;

import com.energyict.cbo.*;
import com.energyict.cpo.*;
import com.energyict.dlms.DLMSUtils;

import java.util.*;
import java.sql.*;
import java.io.*;
import com.energyict.dlms.UniversalObject;

/**
 * @version : 1.0
 * @author  : Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * This class retrieves and updates the EISDLMSCACHE table. It is used by the meterprotocol to cache up
 * the short name references to cosem objects in DLMS when using short name referencing.
 * <BR>
 * <B>Changes :</B><BR>
 *      KV 30102002 : Initial version 1.0<BR>
 */
public class RtuDLMSCache
{
    // Table fields
    private int rtuid;
    private int logicaldevice;
    private String longname;
    private int shortname;
    private int classid;
    private int version;
    private int associationlevel;
    private String objectdescription;
    
    private int iNROfObjects;
    
    /** Creates a new instance of RtuDLMSCache */
    public RtuDLMSCache(int rtuid)
    {
        this.rtuid=rtuid;
        logicaldevice=0;
        longname=null;
        shortname=0;
        classid=0;
        version=0;
        associationlevel=0;
        objectdescription=null;
        iNROfObjects=0;
    }
    
    synchronized public void saveObjectList(UniversalObject[] universalObject) throws SQLException {
       for (int i=0;i<universalObject.length;i++) {
          longname = universalObject[i].getLN(); 
          shortname = universalObject[i].getBaseName();
          classid = universalObject[i].getClassID();
          version = universalObject[i].getVersion();
          objectdescription = DLMSUtils.getInfoLN(universalObject[i].getLNArray());
         
          try {
             doInsert();
          }
          catch(SQLException e) {
             if ("23000".equals(e.getSQLState())) {
                doUpdate();
             }
             else throw e;
          }
         
       } // for (int i=0;i<universalObject.length;i++)
       
    } // synchronized public void saveObjectList(UniversalObject[] universalObject)
    
    public UniversalObject[] getObjectList () throws SQLException
    {
       Statement statement = null;
       ResultSet resultSet = null;
       UniversalObject universalObject = null;
       List UniversalObjectList = new ArrayList();
       Connection connection = getDefaultConnection();
       
       try
       {
          statement = connection.createStatement();
          String sqlString = "select logicaldevice,longname,shortname,classid,version,associationlevel,objectdescription from eisdlmscache where rtuid = " + rtuid;
          resultSet = statement.executeQuery(sqlString);
          if (!resultSet.next()) return null;
          iNROfObjects=0;
          do
          {
              iNROfObjects++;
              
              // Retrieve field values from ctable
              logicaldevice = resultSet.getInt(1);
              longname = resultSet.getString(2);
              shortname = resultSet.getInt(3);
              classid = resultSet.getInt(4);
              version = resultSet.getInt(5);
              associationlevel = resultSet.getInt(6);
              objectdescription = resultSet.getString(7);

              // Build universalobjectlist entry
              universalObject = new UniversalObject(UniversalObject.getSNObjectListEntrySize());
              universalObject.setBaseName(shortname);
              universalObject.setClassID(classid);
              universalObject.setVersion(version);
              universalObject.setLN(longname);
              UniversalObjectList.add(universalObject);
              
          } while (resultSet.next());
       }
       finally
       {
          if (resultSet != null) resultSet.close();
          if (statement != null) statement.close();
       }
      
       UniversalObject[] uarray = new UniversalObject[UniversalObjectList.size()];
       for (int i = 0;i<UniversalObjectList.size();i++) uarray[i] = (UniversalObject)UniversalObjectList.get(i);

       return uarray;
       
    } // public UniversalObject[] getObjectList (Connection connection, int iRtuID) throws SQLException

    public void clearCache() throws SQLException
    {
       Statement statement = null;
       Connection connection = getDefaultConnection();
       
       try
       {
          statement = connection.createStatement();
          String sqlString = "delete from eisdlmscache where rtuid = " + rtuid;
          statement.executeQuery(sqlString);
       }
       finally
       {
          if (statement != null) statement.close();
       }
       
    } // public void clearCache()
    
    private void doInsert() throws SQLException {
       Connection connection = getDefaultConnection();
       Statement statement = connection.createStatement();  
       
       try {
           StringBuffer buffer = new StringBuffer("insert into eisdlmscache");
           buffer.append(" (RTUID, LOGICALDEVICE, LONGNAME, SHORTNAME, CLASSID, VERSION, ASSOCIATIONLEVEL, OBJECTDESCRIPTION) values(");
           buffer.append(rtuid);
           buffer.append(",");
           buffer.append(0);
           buffer.append(",");
           Utils.appendQuoted(buffer,longname);
           buffer.append(",");
           buffer.append(shortname);
           buffer.append(",");
           buffer.append(classid);
           buffer.append(",");
           buffer.append(version);
           buffer.append(",");
           buffer.append(0);
           buffer.append(",");
           Utils.appendQuoted(buffer,objectdescription);
           buffer.append(")");
           statement.executeUpdate (buffer.toString());
       }
       catch(SQLException e) {
          throw e;   
       }
       finally {
          if (statement != null) statement.close();
       }
    } // private doInsert() throws SQLException
    
    private void doUpdate() throws SQLException
    {
       PreparedStatement statement = null;
       Connection connection = getDefaultConnection();
       try
       {
          String sqlString =
                  "update eisdlmscache SET shortname=?,classid=?,version=?,associationlevel=?,objectdescription=? where rtuid = " + rtuid +" and longname = '"+longname+"'";
          statement = connection.prepareStatement(sqlString);

          // longname and logicaldevice are part of the key. They'return inserted with doInsert.
          // We should not write them with an update, otherwise we get the constrain error!
          statement.setInt(1,shortname);
          statement.setInt(2,classid);
          statement.setInt(3,version);
          statement.setInt(4,0);
          statement.setString(5,objectdescription);
          
          statement.execute();
       }
       catch(SQLException e) {
          throw e;   
       }
       finally
       {
          if (statement != null) statement.close();
       }
      
    } // private void doUpdate() throws SQLException
    
    private Connection getDefaultConnection() {
       return Environment.getDefault().getConnection();
    }

} // public class RtuDLMSCache
