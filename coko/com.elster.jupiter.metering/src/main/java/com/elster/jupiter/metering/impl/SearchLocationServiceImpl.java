package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.location.SearchLocationService;
import com.elster.jupiter.util.sql.SqlBuilder;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component(name = "com.elster.jupiter.search.location", service = {SearchLocationService.class}, property = "name=" + SearchService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class SearchLocationServiceImpl implements SearchLocationService {

    private final Map<String, String> templateMap = templateMap();
    private volatile DataModel dataModel;
    private volatile MeteringService meteringService;
    private String[] templateMembers;

    @Inject
    public SearchLocationServiceImpl() {
        super();
    }

    protected Map<String, String> templateMap() {

        return Collections.unmodifiableMap(new HashMap<String, String>() {
            {
                put("#ccod", "countryCode");
                put("#cnam", "countryName");
                put("#adma", "administrativeArea");
                put("#loc", "locality");
                put("#subloc", "subLocality");
                put("#styp", "streetType");
                put("#snam", "streetName");
                put("#snum", "streetNumber");
                put("#etyp", "establishmentType");
                put("#enam", "establishmentName");
                put("#enum", "establishmentNumber");
                put("#addtl", "addressDetail");
                put("#zip", "zipCode");
                //   put("#locale", "locale");

            }
        });
    }

    @Activate
    public void activate() {
        String locationTemplate = getAddressTemplate();
        if (locationTemplate != null) {

            locationTemplate = locationTemplate.replace("\\r", "").replace("\\n", "")
                    .replace("\r", "").replace("\n", "")
                    .replace("\r\n", "").replace("\n\r", "");
            templateMembers = locationTemplate.split(",");
        }
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        if ((ormService.getDataModel("ORM").get() != null)) {
            this.dataModel = ormService.getDataModel("ORM").get();
        }
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
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
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    private String getQueryClause(String inputLocation) {
        if (inputLocation == null) {
            inputLocation = "";
        }

        String[] mapInputLocation = inputLocation.split("\\s+|,\\s*|;\\s*|\\.\\s*");

        ArrayList<String> caseClauses = new ArrayList<>();
        String selectClause = "MTR_LOCATIONMEMBER";
        Integer i = 0;
        for (int j = 0; j < mapInputLocation.length; j++) {
            for (int k = 0; k < templateMembers.length; k++) {
                String templateMember = templateMap.get(templateMembers[k]);

                if (templateMember != null) {
                    String whenClause = "";
                    whenClause += String.format(" WHEN UPPER%s = UPPER('%s') THEN %s ", templateMember, mapInputLocation[j], i * 3 + 3);
                    whenClause += String.format(" WHEN UPPER%s LIKE UPPER('%s')||'%%' THEN %s ", templateMember, mapInputLocation[j], i * 3 + 2);
                    whenClause += String.format(" WHEN UPPER%s LIKE '%%'|| UPPER('%s') ||'%%' THEN %s ", templateMember, mapInputLocation[j], i * 3 + 1);
                    caseClauses.add(String.format(" CASE %s ELSE 0 END DESC ", whenClause));
                    i++;
                }
            }

            selectClause = String.format(" SELECT * FROM (%s) WHERE (%s) LIKE '%%' || UPPER('%s') || '%%' ORDER BY %s ",
                    selectClause,
                    templateMap.entrySet().stream().map(entry -> "UPPER" + entry.getValue()).collect(Collectors.joining(" ||' '|| ")),
                    mapInputLocation[j],
                    caseClauses.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }

        selectClause = "SELECT max(locationid) locationid,  COUNTRYNAME," +
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
                "                ZIPCODE," +
                "                rownum rn" +
                " FROM (" + selectClause + ")" +
                " WHERE rownum <=5 " +
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
                "                ZIPCODE, " +
                "                rownum" +
                " ORDER BY rn";

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