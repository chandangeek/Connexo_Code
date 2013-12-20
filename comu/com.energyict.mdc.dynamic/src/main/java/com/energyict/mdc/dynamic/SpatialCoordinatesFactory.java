package com.energyict.mdc.dynamic;


import com.energyict.mdc.common.coordinates.DegreesWorldCoordinate;
import com.energyict.mdc.common.coordinates.SpatialCoordinates;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.Environment;
import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:59)
 */
public class SpatialCoordinatesFactory extends AbstractValueFactory<SpatialCoordinates> {

    private static final String LATITUDE_LONGITUDE_SEPARATOR = ":";

    @Override
    public Class<SpatialCoordinates> getValueType () {
        return SpatialCoordinates.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return SpatialCoordinates.SQL_TYPE_NAME;
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.STRUCT;
    }

    public String getStructType() {
        return SpatialCoordinates.SQL_TYPE_NAME;
    }

    @Override
    public SpatialCoordinates valueFromDatabase (Object object) throws SQLException {
        if (object == null) {
            return null;
        }
        else {
            try {
                oracle.sql.STRUCT struct = (oracle.sql.STRUCT) object;
                Object[] attributes = struct.getAttributes();
                oracle.sql.STRUCT point = (oracle.sql.STRUCT) attributes[2];
                Object[] pointAttributes = point.getAttributes();
                BigDecimal latitude = (BigDecimal) pointAttributes[0];
                BigDecimal longitude = (BigDecimal) pointAttributes[1];
                return new SpatialCoordinates(new DegreesWorldCoordinate(latitude), new DegreesWorldCoordinate(longitude));
            }
            catch (SQLException e) {
                throw new ApplicationException(e);
            }
        }
    }

    @Override
    public Object valueToDatabase (SpatialCoordinates object) {
        if (object == null) {
            return null;
        }
        else {
            try {
                OracleConnection conn = (OracleConnection) Environment.DEFAULT.get().getUnwrappedConnection();
                oracle.sql.NUMBER SDO_GTYPE = new oracle.sql.NUMBER(2001);  //point = 2001
                oracle.sql.NUMBER SDO_SRID = new oracle.sql.NUMBER(8307);   //datum = 8307

                oracle.sql.StructDescriptor pointDescriptor = oracle.sql.StructDescriptor.createDescriptor("MDSYS.SDO_POINT_TYPE", conn);
                oracle.sql.NUMBER x = new oracle.sql.NUMBER(object.getLatitude().getValue());  // latitude
                oracle.sql.NUMBER y = new oracle.sql.NUMBER(object.getLongitude().getValue());    //longitude
                oracle.sql.NUMBER z = null;
                Object[] pointAttributes = {x, y, z};
                oracle.sql.STRUCT SDO_POINT = new STRUCT(pointDescriptor, conn, pointAttributes);

                oracle.sql.ARRAY SDO_ELEM_INFO = null;
                oracle.sql.ARRAY SDO_ORDINATES = null;

                oracle.sql.StructDescriptor geometryDescriptor = oracle.sql.StructDescriptor.createDescriptor("MDSYS.SDO_GEOMETRY", conn);
                Object[] geometryAttributes = {SDO_GTYPE, SDO_SRID, SDO_POINT, SDO_ELEM_INFO, SDO_ORDINATES};
                oracle.sql.STRUCT SDO_GEOMETRY = new STRUCT(geometryDescriptor, conn, geometryAttributes);
                return SDO_GEOMETRY;
            }
            catch (SQLException e) {
                throw new ApplicationException(e);
            }
        }
    }

    @Override
    public boolean requiresIndex () {
        return true;
    }

    @Override
    public String getIndexType () {
        return "MDSYS.SPATIAL_INDEX";
    }

    @Override
    public SpatialCoordinates fromStringValue (String stringValue) {
        if (stringValue == null || stringValue.isEmpty() || !stringValue.contains(LATITUDE_LONGITUDE_SEPARATOR)) {
            return null;
        }
        String[] parts = stringValue.split(LATITUDE_LONGITUDE_SEPARATOR);
        if (parts.length != 2) {
            return null;
        }
        DegreesWorldCoordinate latitude = new DegreesWorldCoordinate(new BigDecimal(parts[0]));
        DegreesWorldCoordinate longitude = new DegreesWorldCoordinate(new BigDecimal(parts[1]));
        return new SpatialCoordinates(latitude, longitude);
    }

    @Override
    public String toStringValue (SpatialCoordinates object) {
        if (object == null) {
            return "";
        }
        // string representation: <latitude as BigDecimal>:<longitude as BigDecimal>
        DegreesWorldCoordinate latitude = object.getLatitude();
        DegreesWorldCoordinate longitude = object.getLongitude();
        return latitude.getValue() + LATITUDE_LONGITUDE_SEPARATOR + longitude.getValue();
    }

}