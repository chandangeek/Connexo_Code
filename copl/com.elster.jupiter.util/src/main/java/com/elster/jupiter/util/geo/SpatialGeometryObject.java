package com.elster.jupiter.util.geo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SpatialGeometryObject {

    int sdoGtype;
    int sdoSrid;
    SdoPoint sdoPoint;
    List<BigDecimal> sdoElemInfo;
    List<BigDecimal> sdoOrdinates;

    public SpatialGeometryObject(int sdoGtype, int sdoSrid, SdoPoint sdoPoint, List<BigDecimal> sdoElemInfo, List<BigDecimal> sdoOrdinates) {
        this.sdoGtype = sdoGtype;
        this.sdoSrid = sdoSrid;
        this.sdoPoint = sdoPoint;
        this.sdoElemInfo = sdoElemInfo;
        this.sdoOrdinates = sdoOrdinates;
    }


    private static class SdoPoint {
        Latitude x;
        Longitude y;
        Elevation z;

        public SdoPoint(Latitude x, Longitude y, Elevation z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return "SDO_POINT( " + x + ", " + y + ", " + z + " )";
        }
    }

    public int getSdoGtype() {
        return sdoGtype;
    }

    public int getSdoSrid() {
        return sdoSrid;
    }

    public SdoPoint getSdoPoint() {
        return sdoPoint;
    }

    public List<BigDecimal> getSdoElemInfo() {
        return sdoElemInfo;
    }

    public List<BigDecimal> getSdoOrdinates() {
        return sdoOrdinates;
    }

    public void setSdoGtype(int sdoGtype) {
        this.sdoGtype = sdoGtype;
    }

    public void setSdoSrid(int sdoSrid) {
        this.sdoSrid = sdoSrid;
    }

    public void setSdoPoint(Latitude x, Longitude y, Elevation z) {
        this.sdoPoint = new SdoPoint(x, y, z);
    }

    public void setSdoElemInfo(List<BigDecimal> sdoElemInfo) {
        this.sdoElemInfo = sdoElemInfo;
    }

    public void setSdoOrdinates(List<BigDecimal> sdoOrdinates) {
        this.sdoOrdinates = sdoOrdinates;
    }

    public static SpatialGeometryObject fromString(String serialized) {
        int sdoGtype = 0;
        int sdoSrid = 0;
        SdoPoint sdoPoint = null;
        List<BigDecimal> sdoElemInfo = null;
        List<BigDecimal> sdoOrdinates = null;
        try {
            String sdoGeometrySection = serialized == null ? null : serialized.substring(serialized.indexOf("(") + 1, serialized.lastIndexOf(")"));
            if (sdoGeometrySection != null && !sdoGeometrySection.isEmpty()) {
                String sdoGtypeParam = sdoGeometrySection.substring(0, sdoGeometrySection.indexOf(","));
                sdoGtype = (sdoGtypeParam == null || sdoGtypeParam.isEmpty()) ? 0 : Integer.parseInt(sdoGtypeParam.trim());
                String sdoSridParam = sdoGeometrySection.substring(sdoGeometrySection.indexOf(",") + 2, sdoGeometrySection.indexOf("SDO_POINT") - 2);
                sdoSrid = (sdoSridParam == null || sdoSridParam.isEmpty()) ? 0 : Integer.parseInt(sdoSridParam.trim());
                String sdoPointSection = sdoGeometrySection.substring(sdoGeometrySection.indexOf("SDO_POINT"), sdoGeometrySection.indexOf("SDO_ELEM_INFO_ARRAY") - 2);
                if (sdoPointSection != null && !sdoPointSection.isEmpty()) {
                    String coordinatesParam = sdoPointSection.substring(sdoPointSection.indexOf("(") + 1, sdoPointSection.lastIndexOf(")"));
                    List<String> coordinates = Arrays.asList((coordinatesParam == null || coordinatesParam.isEmpty()) ? new String[]{} : coordinatesParam.trim().split(", "));
                    List<BigDecimal> numericCoordinates = coordinates.isEmpty() ? new LinkedList<>() : coordinates.stream()
                            .map(BigDecimal::new)
                            .collect(Collectors.toList());
                    if (!numericCoordinates.isEmpty()) {
                        Latitude latitude = new Latitude(numericCoordinates.get(0));
                        Longitude longitude = new Longitude(numericCoordinates.get(1));
                        Elevation elevation = new Elevation(numericCoordinates.get(2));
                        sdoPoint = new SdoPoint(latitude, longitude, elevation);
                    }
                }
                String sdoElemInfoSection = sdoGeometrySection.substring(sdoGeometrySection.indexOf("SDO_ELEM_INFO_ARRAY"), sdoGeometrySection.indexOf("SDO_ORDINATE_ARRAY") - 2).trim();
                if (sdoElemInfoSection != null && !sdoElemInfoSection.isEmpty()) {
                    String sdoElemInfoItemsParam = sdoElemInfoSection.substring(sdoElemInfoSection.indexOf("(") + 1, sdoElemInfoSection.lastIndexOf(")"));
                    List<String> sdoElemInfoItems = Arrays.asList((sdoElemInfoItemsParam != null || sdoElemInfoItemsParam.isEmpty()) ? new String[]{} : sdoElemInfoItemsParam.trim().split(", "));
                    sdoElemInfo = sdoElemInfoItems.isEmpty() ? new LinkedList<>() : sdoElemInfoItems.stream()
                            .map(BigDecimal::new)
                            .collect(Collectors.toList());
                }
                String sdoOrdinatesSection = sdoGeometrySection.substring(sdoGeometrySection.indexOf("SDO_ORDINATE_ARRAY"), sdoGeometrySection.lastIndexOf(")")).trim();
                if (sdoOrdinatesSection != null && !sdoOrdinatesSection.isEmpty()) {
                    String sdoElemInfoItemsParam = sdoOrdinatesSection.substring(sdoOrdinatesSection.indexOf("(") + 1, sdoOrdinatesSection.lastIndexOf(")"));
                    List<String> sdoElemInfoItems = Arrays.asList((sdoElemInfoItemsParam != null || sdoElemInfoItemsParam.isEmpty()) ? new String[]{} : sdoElemInfoItemsParam.trim().split(", "));
                    sdoOrdinates = sdoElemInfoItems.isEmpty() ? new LinkedList<>() : sdoElemInfoItems.stream()
                            .map(BigDecimal::new)
                            .collect(Collectors.toList());
                }
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("The data provided for the Spatial geometry is either incorrect or incomplete");
        }

        if (sdoGtype == 0 || sdoSrid == 0 || sdoPoint == null) {
            throw new IllegalArgumentException("Spatial parameters have not been set correctly");
        }

        return new SpatialGeometryObject(sdoGtype, sdoSrid, sdoPoint, sdoElemInfo, sdoOrdinates);
    }

    @Override
    public String toString() {
        String sdoElemInfoStringified = sdoElemInfo == null ? null : sdoElemInfo.stream().map(Object::toString).collect(Collectors.joining(","));
        String sdoOrdinatesStringified = sdoOrdinates == null ? null : sdoOrdinates.stream().map(Object::toString).collect(Collectors.joining(","));
        return "SDO_GEOMETRY( "
                + sdoGtype
                + ", " + sdoSrid
                + ", " + sdoPoint
                + ", SDO_ELEM_INFO_ARRAY( " + sdoElemInfoStringified + " ),"
                + ", SDO_ORDINATE_ARRAY( " + sdoOrdinatesStringified + " )"
                + " )";
    }

}
