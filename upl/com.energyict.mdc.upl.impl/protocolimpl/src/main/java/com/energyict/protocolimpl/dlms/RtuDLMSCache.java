package com.energyict.protocolimpl.dlms;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.UniversalObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Koenraad Vanderschaeve
 *         <p>
 *         <B>Description :</B><BR>
 *         This class retrieves and updates the EISDLMSCACHE table. It is used by the meterprotocol to cache up
 *         the short name references to cosem objects in DLMS when using short name referencing.
 *         <BR>
 *         <B>Changes :</B><BR>
 *         KV 30102002 : Initial version 1.0<BR>
 * @version : 1.0
 */
public class RtuDLMSCache {
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

    /**
     * Creates a new instance of RtuDLMSCache
     */
    public RtuDLMSCache(int rtuid) {
        this.rtuid = rtuid;
        logicaldevice = 0;
        longname = null;
        shortname = 0;
        classid = 0;
        version = 0;
        associationlevel = 0;
        objectdescription = null;
        iNROfObjects = 0;
    }

    public synchronized void saveObjectList(UniversalObject[] universalObject, Connection connection) throws SQLException {
        for (int i = 0; i < universalObject.length; i++) {
            longname = universalObject[i].getLN();
            shortname = universalObject[i].getBaseName();
            classid = universalObject[i].getClassID();
            version = universalObject[i].getVersion();
            objectdescription = DLMSUtils.getInfoLN(universalObject[i].getLNArray());
            if (objectdescription == "") {
                objectdescription = "Unknown";
            }

            try {
                doInsert(connection);
            } catch (SQLException e) {
                if ("23000".equals(e.getSQLState())) {
                    doUpdate(connection);
                } else {
                    throw e;
                }
            }

        }
    }

    public UniversalObject[] getObjectList(Connection connection) throws SQLException {
        List<UniversalObject> objects = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT logicaldevice, longname, shortname, classid, version, associationlevel, objectdescription FROM eisdlmscache WHERE rtuid = ?")) {
            statement.setInt(1, rtuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                iNROfObjects = 0;
                do {
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
                    UniversalObject universalObject = new UniversalObject(UniversalObject.getSNObjectListEntrySize());
                    universalObject.setBaseName(shortname);
                    universalObject.setClassID(classid);
                    universalObject.setVersion(version);
                    universalObject.setLN(longname);
                    objects.add(universalObject);

                } while (resultSet.next());
            }
        }
        return objects.toArray(new UniversalObject[objects.size()]);
    }

    private void doInsert(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO eisdlmscache (RTUID, LOGICALDEVICE, LONGNAME, SHORTNAME, CLASSID, VERSION, ASSOCIATIONLEVEL, OBJECTDESCRIPTION) VALUES(?,0,?,?,?,?,0,?)")) {
            statement.setInt(1, rtuid);
            statement.setString(2, longname);
            statement.setInt(3, shortname);
            statement.setInt(4, classid);
            statement.setInt(5, version);
            statement.setString(6, objectdescription);
            statement.executeUpdate();
        }
    }

    private void doUpdate(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE eisdlmscache SET shortname=?,classid=?,version=?,associationlevel=?,objectdescription=? WHERE rtuid = ? AND longname = ?")) {
            statement.setInt(1, shortname);
            statement.setInt(2, classid);
            statement.setInt(3, version);
            statement.setInt(4, 0);
            statement.setString(5, objectdescription);
            statement.setInt(6, rtuid);
            statement.setString(7, longname);
            statement.executeUpdate();
        }
    }

}