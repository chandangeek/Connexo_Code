package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;
import javax.inject.Inject;
import java.util.*;



public class LocationTemplateImpl implements LocationTemplate {

    private long id;
    private String locationTemplate;
    private String mandatoryFields;
    private Map<String, Integer> rankings;
    private final DataModel dataModel;
    private final ImmutableList<String> ALLOWED_LOCATION_TEMPLATE_ELEMENTS =
            ImmutableList.of("#ccod", "#cnam", "#adma", "#loc", "#subloc",
                    "#styp", "#snam", "#snum", "#etyp", "#enam", "#enum", "#addtl", "#zip");

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
                put("#enum","establishmentNumber");
                put("#addtl", "addressDetail");
                put("#zip", "zipCode");

            }
        });
    }

    private Map<String, String> templateMap = templateMap();

    @Override
    public void parseTemplate(String templateRaw, String mandatoryFields) {
        int i = 0;
        if (templateRaw != null && mandatoryFields != null) {
            rankings = new HashMap<>();
            locationTemplate = templateRaw;
            String[] templateElements = templateRaw.split(",");
            if (Arrays.asList(templateElements).containsAll(ALLOWED_LOCATION_TEMPLATE_ELEMENTS)) {
                for (String templateElement : templateElements) {
                    rankings.put(templateMap.get(templateElement), i++);
                }
                this.mandatoryFields = mandatoryFields;
            } else {
                throw new IllegalArgumentException("Bad Template");
            }
        } else {
            throw new IllegalArgumentException("Bad Template");
        }
    }

    @Inject
    LocationTemplateImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    LocationTemplateImpl init(String locationTemplate, String mandatoryFields) {
        this.locationTemplate = locationTemplate;
        this.mandatoryFields = mandatoryFields;
        return this;
    }

    static LocationTemplateImpl from(DataModel dataModel, String locationTemplate, String mandatoryFields) {
        return dataModel.getInstance(LocationTemplateImpl.class).init(locationTemplate, mandatoryFields);
    }

    @Override
    public long getId() {
        return id;
    }


    private boolean hasId() {
        return id != 0L;
    }

    void doSave() {
        if (hasId()) {
            dataModel.mapper(LocationTemplate.class).update(this);
            return;
        }
        dataModel.mapper(LocationTemplate.class).persist(this);
    }

    @Override
    public void remove() {
        if (hasId()) {
            dataModel.mapper(LocationTemplate.class).remove(this);
        }
    }

    @Override
    public String[] getTemplateElements() {
        return locationTemplate.split(",");
    }

    @Override
    public Map<String, Integer> getRankings() {
        return rankings;
    }

    @Override
    public String getLocationTemplate() {
        return locationTemplate;
    }

    @Override
    public String getMandatoryFields(){
        return mandatoryFields;
    }
}
