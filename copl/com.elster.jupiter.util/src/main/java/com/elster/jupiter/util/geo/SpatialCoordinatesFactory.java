package com.elster.jupiter.util.geo;

import java.sql.*;

import java.math.BigDecimal;
import oracle.jdbc.OracleConnection;
import oracle.sql.NUMBER;

public class SpatialCoordinatesFactory{ //extends AbstractValueFactory<SpatialCoordinates> {

    public String getDbType() {
        return SpatialCoordinates.SQL_TYPE_NAME;
    }

    public SpatialCoordinates valueFromDb(Object object ) {
        if (object == null) {
            return null;
        } else {
            try {
                Struct struct = (Struct) object;
                Object[] attributes = struct.getAttributes();
                Struct point = (Struct) attributes[2];
                Object[] pointAttributes = point.getAttributes();
                BigDecimal latitude = (BigDecimal) pointAttributes[0];
                BigDecimal longitude = (BigDecimal) pointAttributes[1];
                return new SpatialCoordinates(new DegreesWorldCoordinate(latitude), new DegreesWorldCoordinate(longitude));
            } catch (SQLException e) {
                throw new RuntimeException(e); //new ApplicationException(e);
            }
        }
    }

    public Object valueToDb(SpatialCoordinates object, Connection conn) {
        if (object == null) {
            return null;
        } else {
            try {

                NUMBER SDO_GTYPE = new NUMBER(2001);  //point = 2001
                NUMBER SDO_SRID = new NUMBER(8307);   //datum = 8307
                NUMBER x = new NUMBER(((DegreesWorldCoordinate) object.getLatitude()).getValue());  // latitude
                NUMBER y = new NUMBER(((DegreesWorldCoordinate) object.getLongitude()).getValue());    //longitude
                NUMBER z = null;
                Object[] pointAttributes = {x, y, z};
                Struct SDO_POINT = conn.createStruct("MDSYS.SDO_POINT_TYPE", pointAttributes);

                Array SDO_ELEM_INFO = null;
                Array SDO_ORDINATES = null;

                Object[] geometryAttributes = {SDO_GTYPE, SDO_SRID, SDO_POINT, SDO_ELEM_INFO, SDO_ORDINATES};
                Struct SDO_GEOMETRY = conn.createStruct("MDSYS.SDO_GEOMETRY", geometryAttributes);
                return SDO_GEOMETRY;
            } catch (SQLException e) {
                throw new RuntimeException(e);//ApplicationException(e);
            }
        }
    }


    public Class<SpatialCoordinates> getValueType() {
        return SpatialCoordinates.class;
    }

    public int getJdbcType() {
        return java.sql.Types.STRUCT;
    }


    public String getStructType() {
        return SpatialCoordinates.SQL_TYPE_NAME;
    }

    public boolean requiresIndex() {
        return true;
    }

    public String getIndexType() {
        return "MDSYS.SPATIAL_INDEX";
    }

    private String LATITUDE_LONGITUDE_SEPARATOR = ":";

    //@Override
    public SpatialCoordinates fromStringValue(String stringValue) {
        if (stringValue==null || stringValue.length()==0 || stringValue.indexOf(LATITUDE_LONGITUDE_SEPARATOR)==-1) {
            return null;
        }
        String[] parts = stringValue.split(LATITUDE_LONGITUDE_SEPARATOR);
        if (parts.length!=2) {
            return null;
        }
        DegreesWorldCoordinate latitude = new DegreesWorldCoordinate( new BigDecimal(parts[0]) );
        DegreesWorldCoordinate longitude = new DegreesWorldCoordinate( new BigDecimal(parts[1]) );
        return new SpatialCoordinates(latitude, longitude);
    }

    //@Override
    public String toStringValue(SpatialCoordinates object) {
        if (object==null) {
            return "";
        }
        // string representation: <latitude as BigDecimal>:<longitude as BigDecimal>
        SpatialCoordinates spatialCoordinates = object;
        DegreesWorldCoordinate latitude = spatialCoordinates.getLatitude();
        DegreesWorldCoordinate longitude = spatialCoordinates.getLongitude();
        return latitude.getValue() + LATITUDE_LONGITUDE_SEPARATOR + longitude.getValue();
    }

}
