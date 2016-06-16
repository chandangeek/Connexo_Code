package com.elster.jupiter.util.geo;

import oracle.sql.NUMBER;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.Arrays;

public class SpatialCoordinatesFactory {

    public String getDbType() {
        return SpatialCoordinates.SQL_TYPE_NAME;
    }

    public SpatialCoordinates valueFromDb(Object object) {
        try {
            if (object == null || ((Struct) object).getAttributes()!=null && ((Struct) object).getAttributes().length == 5
                    && ((Struct) object).getAttributes()[2]==null) {
                return null;
            } else {
                try {
                    Struct struct = (Struct) object;
                    Object[] attributes = struct.getAttributes();
                    Struct point = (Struct) attributes[2];
                    Object[] pointAttributes = point.getAttributes();
                    BigDecimal latitude = (BigDecimal) pointAttributes[0];
                    BigDecimal longitude = (BigDecimal) pointAttributes[1];
                    BigDecimal elevation = (BigDecimal) pointAttributes[2];
                    return new SpatialCoordinates(new Latitude(latitude), new Longitude(longitude), new Elevation(elevation));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Object valueToDb(SpatialCoordinates object, Connection conn) {
        if (object == null) {
            try {
                Struct SDO_GEOMETRY = conn.createStruct("MDSYS.SDO_GEOMETRY", null);
                return SDO_GEOMETRY;
            } catch (SQLException e) {
                throw new RuntimeException(e);//ApplicationException(e);
            }

        } else {
            try {

                NUMBER SDO_GTYPE = new NUMBER(2001);  //point = 2001
                NUMBER SDO_SRID = new NUMBER(8307);   //datum = 8307
                NUMBER x = new NUMBER((object.getLatitude()).getValue());  // latitude
                NUMBER y = new NUMBER((object.getLongitude()).getValue());    //longitude
                NUMBER z = new NUMBER((object.getElevation()).getValue());  // elevation
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

    private String SEPARATOR = ":";


    public SpatialCoordinates fromStringValue(String stringValue) {
        if (stringValue == null || stringValue.length() == 0 || stringValue.indexOf(SEPARATOR) == -1) {
            return null;
        }
        String[] parts = stringValue.split(SEPARATOR);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Incorrectly formatted coordinates.Please check format and range.");
        }

        if (Arrays.asList(parts)
                .stream()
                .anyMatch(element -> element.split(",").length > 2
                        || element.split(".").length > 2)) {
            throw new IllegalArgumentException("Incorrectly formatted coordinates.Please check format and range.");
        }

        BigDecimal numericLatitude = new BigDecimal(parts[0].contains(",") ? String.valueOf(parts[0].replace(",", ".")) : parts[0]);
        BigDecimal numericLongitude = new BigDecimal(parts[1].contains(",") ? String.valueOf(parts[1].replace(",", ".")) : parts[1]);
        BigDecimal numericElevation = new BigDecimal(0);
        if (parts.length == 3 && !parts[2].equals("null")) {
            numericElevation = new BigDecimal(parts[2]);
        }
        if (numericLatitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || numericLatitude.compareTo(BigDecimal.valueOf(90)) > 0
                || numericLongitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || numericLongitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("Incorrectly formatted coordinates.Please check format and range.");
        }


        Latitude latitude = new Latitude(numericLatitude);
        Longitude longitude = new Longitude(numericLongitude);
        Elevation elevation = new Elevation(numericElevation);
        return new SpatialCoordinates(latitude, longitude, elevation);
    }


    public String toStringValue(SpatialCoordinates object) {
        if (object == null) {
            return "";
        }
        // string representation: <latitude as BigDecimal>:<longitude as BigDecimal>
        SpatialCoordinates spatialCoordinates = object;
        Latitude latitude = spatialCoordinates.getLatitude();
        Longitude longitude = spatialCoordinates.getLongitude();
        Elevation elevation = spatialCoordinates.getElevation();
        return latitude.getValue() + SEPARATOR + longitude.getValue() + SEPARATOR + elevation.getValue();
    }

}
