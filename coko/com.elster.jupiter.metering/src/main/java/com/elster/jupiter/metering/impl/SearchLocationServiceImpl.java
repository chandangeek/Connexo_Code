/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.location.SearchLocationService;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.search.location", service = {SearchLocationService.class}, property = "name=" + SearchService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class SearchLocationServiceImpl implements SearchLocationService {

    private final Map<String, String> templateMap = templateMap();
    private volatile DataModel dataModel;
    private String[] templateMembers;

    // For OSGi purposes
    public SearchLocationServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public SearchLocationServiceImpl(MeteringDataModelService meteringDataModelService) {
        this();
        this.setMeteringDataModelService(meteringDataModelService);
    }

    private Map<String, String> templateMap() {
        return ImmutableMap.<String, String>builder()
                .put("#ccod", "countryCode")
                .put("#cnam", "countryName")
                .put("#adma", "administrativeArea")
                .put("#loc", "locality")
                .put("#subloc", "subLocality")
                .put("#styp", "streetType")
                .put("#snam", "streetName")
                .put("#snum", "streetNumber")
                .put("#etyp", "establishmentType")
                .put("#enam", "establishmentName")
                .put("#enum", "establishmentNumber")
                .put("#addtl", "addressDetail")
                .put("#zip", "zipCode")
                .build();
    }

    @Reference
    public void setMeteringDataModelService(MeteringDataModelService meteringDataModelService) {
        this.dataModel = meteringDataModelService.getDataModel();
        this.ensureLocationTemplateInitialized();
    }

    private void ensureLocationTemplateInitialized() {
        String locationTemplate = getAddressTemplate();
        if (locationTemplate != null) {

            locationTemplate = locationTemplate.replace("\\r", "").replace("\\n", "")
                    .replace("\r", "").replace("\n", "")
                    .replace("\r\n", "").replace("\n\r", "");
            templateMembers = locationTemplate.split(",");
        }
    }

    @Override
    public Map<Long, String> findLocations(String inputLocation) {
        Map<Long, String> result = new LinkedHashMap<>();

        if (templateMembers != null) {
            SqlBuilder locationBuilder = new SqlBuilder();
            locationBuilder.append(this.getQueryClause(inputLocation));

            try (Connection connection = dataModel.getConnection(false);
                 PreparedStatement statement = locationBuilder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        List<String> formatedMembers = new ArrayList<>();
                        for (String identifier : templateMembers) {
                            try {
                                if (identifier.compareToIgnoreCase("#locale") == 0) {
                                    continue;
                                }

                                String value = resultSet.getString(templateMap.get(identifier));
                                if (value != null && !value.isEmpty()) {
                                    formatedMembers.add(value);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        result.put(resultSet.getLong("LOCATIONID"), formatedMembers.stream().map(Object::toString).collect(Collectors.joining(", ")));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(String::compareToIgnoreCase))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    private String getQueryClause(String inputLocation) {
        if (inputLocation == null) {
            inputLocation = "";
        }

        String[] mapInputLocations = inputLocation.split("\\s+|,\\s*|;\\s*|\\.\\s*");


        String selectClause = "MTR_LOCATIONMEMBER";
        String orderByClauses = "";
        String maxClauses = "";
        Integer i = 0;
        for (int j = 0; j < mapInputLocations.length; j++) {
            String whenClause = "";
            String mapInputLocation = mapInputLocations[j].replace("'", "''");

            for (int k = 0; k < templateMembers.length; k++) {
                String templateMember = templateMap.get(templateMembers[k]);

                if (templateMember != null) {
                    whenClause += String.format(" WHEN UPPER%s = UPPER('%s') THEN %s ", templateMember, mapInputLocation, i * 3 + 1);
                    whenClause += String.format(" WHEN UPPER%s LIKE UPPER('%s')||'%%' THEN %s ", templateMember, mapInputLocation, i * 3 + 2);
                    whenClause += String.format(" WHEN UPPER%s LIKE '%%'|| UPPER('%s') ||'%%' THEN %s ", templateMember, mapInputLocation, i * 3 + 3);
                    i++;
                }
            }

            selectClause = String.format(" SELECT MTR_LOCATIONMEMBER.*,  ( CASE %s ELSE 0 END ) as rank%s FROM (%s) MTR_LOCATIONMEMBER WHERE (%s) LIKE '%%' || UPPER('%s') || '%%' ",
                    whenClause, j,
                    selectClause,
                    templateMap.entrySet().stream().map(entry -> "UPPER" + entry.getValue()).collect(Collectors.joining(" ||' '|| ")),
                    mapInputLocation);

            orderByClauses += (orderByClauses.length() == 0) ? " ORDER BY " : ", ";

            orderByClauses += String.format(" rank%s ASC", j);
            maxClauses += String.format(" max(rank%s) rank%s, ", j, j);
        }

        selectClause = "SELECT * from " +
                "   (SELECT max(locationid) locationid, " + maxClauses + " COUNTRYNAME," +
                "                COUNTRYCODE," +
                "                ADMINISTRATIVEAREA," +
                "                LOCALITY," +
                "                SUBLOCALITY," +
                "                STREETTYPE," +
                "                STREETNAME," +
                "                STREETNUMBER," +
                "                ESTABLISHMENTTYPE," +
                "                ESTABLISHMENTNAME," +
                "                ESTABLISHMENTNUMBER," +
                "                ADDRESSDETAIL," +
                "                ZIPCODE" +
                " FROM (" + selectClause + ")" +
                " GROUP BY COUNTRYNAME," +
                "               COUNTRYCODE," +
                "                ADMINISTRATIVEAREA," +
                "                LOCALITY," +
                "                SUBLOCALITY," +
                "                STREETTYPE," +
                "                STREETNAME," +
                "                STREETNUMBER," +
                "                ESTABLISHMENTTYPE," +
                "                ESTABLISHMENTNAME," +
                "                ESTABLISHMENTNUMBER," +
                "                ADDRESSDETAIL," +
                "                ZIPCODE" + orderByClauses +
                "   ) WHERE rownum <=5 ";

        return selectClause;
    }

    private String getAddressTemplate() {
        String template = "";
        SqlBuilder locationBuilder = new SqlBuilder();

        locationBuilder.append(" select LOCATIONTEMPLATE, MANDATORYFIELDS ");
        locationBuilder.append(" from MTR_LOCATION_TEMPLATE ");

        try (Connection connection = dataModel.getConnection(false);
             PreparedStatement statement = locationBuilder.prepare(connection)) {
             try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    template = resultSet.getString("LOCATIONTEMPLATE");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return template;
    }
}