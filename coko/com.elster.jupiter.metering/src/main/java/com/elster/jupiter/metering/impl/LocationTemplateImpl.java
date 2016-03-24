package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class LocationTemplateImpl implements LocationTemplate {

    private long id;
    private String templateFields;
    private String mandatoryFields;
    private Map<String, Integer> rankings;
    private final DataModel dataModel;
    private List<TemplateField> templateMembers = new ArrayList<>();


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
                put("#locale", "locale");

            }
        });
    }

    private final Map<String, String> templateMap = templateMap();

    @Override
    public void parseTemplate(String locationTemplate, String mandatoryFields) {
        if (locationTemplate != null && mandatoryFields != null) {
            rankings = new HashMap<>();
            String[] templateElements = locationTemplate.trim().split(",");
            String[] mandatoryFieldElements = mandatoryFields.trim().split(",");
            if (Arrays.asList(templateElements).containsAll(ALLOWED_LOCATION_TEMPLATE_ELEMENTS)
                    && Arrays.asList(templateElements).containsAll(Arrays.asList(mandatoryFields.trim().split(",")))) {
                AtomicInteger index = new AtomicInteger(-1);

                Arrays.asList(templateElements).stream().forEach(t ->
                        rankings.put(templateMap.get(t), index.incrementAndGet()));
                this.templateFields = locationTemplate.trim();
                this.mandatoryFields = mandatoryFields.trim();

                AtomicInteger index2 = new AtomicInteger(-1);
                Arrays.asList(templateElements).stream().forEach(t ->{
                    templateMembers.forEach(tm -> {
                        if(tm.getAbbreviation().equalsIgnoreCase(t)){
                            tm.setRanking(index2.incrementAndGet());
                        }
                    });
                });

                Arrays.asList(mandatoryFieldElements).stream().forEach(t ->{
                    templateMembers.forEach(tm -> {
                        if(tm.getAbbreviation().equalsIgnoreCase(t)){
                            tm.setMandatory(true);
                        }
                    });
                });

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
        this.templateFields = locationTemplate;
        this.mandatoryFields = mandatoryFields;
        templateMap.entrySet().stream().forEach(t ->
                this.templateMembers.add(new TemplateFieldImpl(t.getKey(),t.getValue(),0,false)));
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
    public List<String> getTemplateElementsNames() {
        List<String> list = new ArrayList<>();
        Arrays.asList(templateFields.split(",")).stream().forEach(t ->
                list.add(templateMap.get(t)));
        return list;
    }

    @Override
    public Map<String, Integer> getRankings() {
        return rankings;
    }

    public String getTemplateFields() {
        return templateFields;
    }

    @Override
    public String getMandatoryFields() {
        return mandatoryFields;
    }

    @Override
    public List<String> getMandatoryFieldsNames() {
        List<String> list = new ArrayList<>();
        Arrays.asList(mandatoryFields.split(",")).stream().forEach(t ->
                list.add(templateMap.get(t)));
        return list;
    }

    @Override
    public List<TemplateField> getTemplateMembers() {
        return templateMembers;
    }

    @Override
    public void setTemplateMembers(List<TemplateField> templateMembers) {
        this.templateMembers = templateMembers;
    }

    private static class TemplateFieldImpl implements TemplateField{


        int ranking;
        boolean mandatory;
        String name;
        String abbreviation;

        public TemplateFieldImpl( String abbreviation, String name, int ranking, boolean mandatory) {
            this.abbreviation = abbreviation;
            this.name = name;
            this.ranking = ranking;
            this.mandatory = mandatory;

        }

        @Override
        public int getRanking() {
            return ranking;
        }

        @Override
        public void setRanking(int ranking) {
            this.ranking = ranking;
        }

        @Override
        public boolean isMandatory() {
            return mandatory;
        }

        @Override
        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getAbbreviation() {
            return abbreviation;
        }

        @Override
        public void setAbbreviation(String abbreviation) {
            this.abbreviation = abbreviation;
        }

    }

}
