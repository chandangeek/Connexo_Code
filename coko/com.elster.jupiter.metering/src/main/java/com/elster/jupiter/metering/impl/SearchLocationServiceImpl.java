package com.elster.jupiter.metering.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.location.SearchLocationService;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.search.location", service = {SearchLocationService.class}, property = "name=" + SearchService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class SearchLocationServiceImpl implements SearchLocationService {

    private final Map<String, String> templateMap = templateMap();
    private volatile DataModel dataModel;
    private String locationTemplate;

    // For OSGi purposes
    public SearchLocationServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public SearchLocationServiceImpl(OrmService ormService) {
        this();
        this.setOrmService(ormService);
        this.activate();
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
                .put("#locale", "locale")
                .build();
    }

    @Activate
    public void activate() {
        if (this.dataModel != null) {
            this.ensureLocationTemplateInitialized();
        }
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        Optional<DataModel> ormDataModel = ormService.getDataModel("ORM");
        if (ormDataModel.isPresent()) {
            this.dataModel = ormDataModel.get();
            this.ensureLocationTemplateInitialized();
        }
    }

    private void ensureLocationTemplateInitialized() {
        if (this.locationTemplate == null) {
            this.locationTemplate = getAddressTemplate();
        }
    }

    @Override
    public Map<Long, String> findLocations(String locationPart) {
        Map<Long, String> result = new HashMap<>();

        if (locationTemplate != null) {

            locationTemplate = locationTemplate.replace("\\r", "").replace("\\n", "")
                    .replace("\r", "").replace("\n", "")
                    .replace("\r\n", "").replace("\n\r", "");
            String[] templateMembers = locationTemplate.split(",");

            SqlBuilder locationBuilder = new SqlBuilder();
            locationBuilder.append("select * from (");
            locationBuilder.append(" select min(LOCATIONID) LOCATIONID, COUNTRYCODE, COUNTRYNAME, ADMINISTRATIVEAREA, LOCALITY,");
            locationBuilder.append("    SUBLOCALITY, STREETTYPE, STREETNAME, STREETNUMBER, ");
            locationBuilder.append("    ESTABLISHMENTTYPE, ESTABLISHMENTNAME, ESTABLISHMENTNUMBER, ADDRESSDETAIL, ZIPCODE ");
            locationBuilder.append(" from MTR_LOCATIONMEMBER ");
            locationBuilder.append(this.getWhereClause(locationPart));
            locationBuilder.append(" group by COUNTRYCODE, COUNTRYNAME, ADMINISTRATIVEAREA, LOCALITY, SUBLOCALITY, STREETTYPE, ");
            locationBuilder.append("    STREETNAME, STREETNUMBER, ESTABLISHMENTTYPE, ESTABLISHMENTNAME, ESTABLISHMENTNUMBER, ");
            locationBuilder.append("    ADDRESSDETAIL, ZIPCODE");
            locationBuilder.append(") WHERE ROWNUM <=5");

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

    private String getWhereClause(String locationPart) {
        if ((locationPart == null) || (locationPart.isEmpty())) {
            return "";
        }

        String[] mapLocationPart = locationPart.split(",");
        String[] mapTemplate = locationTemplate.split(",");
        List<String> resultClause = new ArrayList<>();

        //mapTemplate = Arrays.copyOfRange(mapTemplate, 1, mapTemplate.length);

        for (int i = 0; i < mapLocationPart.length; i++) {
            String part = mapLocationPart[i].trim();
            String item = mapTemplate[i];

            if (item.startsWith("#")) {
                item = item.substring(1);
            }
            if (item.endsWith("#")) {
                item = item.substring(0, item.length() - 1);
            }

            if ((i == mapLocationPart.length - 1) && (!locationPart.endsWith(","))) {
                if (!part.isEmpty()) {
                    resultClause.add(String.format("upper%s LIKE UPPER('%%%s%%')", templateMap.get("#" + item), part));
                }
            } else {
                if (!part.isEmpty()) {
                    resultClause.add(String.format("upper%s = UPPER('%s')", templateMap.get("#" + item), part));
                }
            }
        }

        String result = resultClause.stream().map(Object::toString).collect(Collectors.joining(" AND "));
        if (!result.isEmpty()) {
            return String.format(" WHERE %s", result);
        }
        return "";
    }

    private String getAddressTemplate() {
        String template = "";
        String mandatoryFields = "";
        SqlBuilder locationBuilder = new SqlBuilder();

        locationBuilder.append("select LOCATIONTEMPLATE, MANDATORYFIELDS ");
        locationBuilder.append(" from MTR_LOCATION_TEMPLATE ");

        try (Connection connection = dataModel.getConnection(false);
             PreparedStatement statement = locationBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    template = resultSet.getString("LOCATIONTEMPLATE");
                    mandatoryFields = resultSet.getString("MANDATORYFIELDS");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return template;
    }
}