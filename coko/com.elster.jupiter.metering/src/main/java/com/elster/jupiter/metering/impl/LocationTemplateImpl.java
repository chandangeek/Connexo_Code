package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.impl.config.Installer;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


public final class LocationTemplateImpl implements LocationTemplate {

    private long id;
    private String templateFields;
    private String mandatoryFields;
    private Map<String, Integer> rankings;
    private final DataModel dataModel;
    private List<TemplateField> templateMembers = new ArrayList<>();
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;
    public static final ImmutableList<String> ALLOWED_LOCATION_TEMPLATE_ELEMENTS =
            ImmutableList.of("#ccod", "#cnam", "#adma", "#loc", "#subloc",
                    "#styp", "#snam", "#snum", "#etyp", "#enam", "#enum", "#addtl", "#zip", "#locale");


    private enum LocationTemplateElements {
        COUNTRY_CODE("#ccod"),
        COUNTRY_NAME("#cnam"),
        ADMINISTRATIVE_AREA("#adma"),
        LOCALITY("#loc"),
        SUB_LOCALITY("#subloc"),
        STREET_TYPE("#styp"),
        STREET_NAME("#snam"),
        STREET_NUMBER("#snum"),
        ESTABLISHMENT_TYPE("#etyp"),
        ESTABLISHMENT_NAME("#enam"),
        ESTABLISHMENT_NUMBER("#enum"),
        ADDRESS_DETAIL("#addtl"),
        ZIP_CODE("#zip"),
        LOCALE("#locale");

        private final String elementAbbreviation;

        LocationTemplateElements(String elementCode) {
            this.elementAbbreviation = elementCode;
        }

        @Override
        public String toString() {
            String elementName = name().toLowerCase();
            if (elementName.indexOf("_") != -1) {
                elementName = elementName.substring(0, elementName.indexOf("_"))
                        + elementName.substring(elementName.indexOf("_") + 1).substring(0, 1).toUpperCase()
                        + elementName.substring(elementName.indexOf("_") + 1).substring(1);
            }
            return elementName;
        }

        private static final Map<String, LocationTemplateElements> stringToEnum
                = new HashMap<>();

        static {
            Stream.of(values()).forEach(e -> stringToEnum.put(e.elementAbbreviation, e));
        }

        public static LocationTemplateElements fromAbbreviation(String elementCode) {
            return stringToEnum.get(elementCode);
        }
    }


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
                        rankings.put(LocationTemplateElements.fromAbbreviation(t).toString(), index.incrementAndGet()));
                this.templateFields = locationTemplate.trim();
                this.mandatoryFields = mandatoryFields.trim();

                AtomicInteger index2 = new AtomicInteger(-1);
                Arrays.asList(templateElements).stream().forEach(t -> {
                    templateMembers.forEach(tm -> {
                        if (tm.getAbbreviation().equalsIgnoreCase(t)) {
                            tm.setRanking(index2.incrementAndGet());
                        }
                    });
                });

                Arrays.asList(mandatoryFieldElements).stream().forEach(t -> {
                    templateMembers.forEach(tm -> {
                        if (tm.getAbbreviation().equalsIgnoreCase(t)) {
                            tm.setMandatory(true);
                        }
                    });
                });

            } else {
                throw new IllegalArgumentException("Bad Location Template");
            }
        } else {
            throw new IllegalArgumentException("Bad Location Template");
        }
    }

    @Inject
    LocationTemplateImpl(DataModel dataModel) {
        this.dataModel = dataModel;

    }

    LocationTemplateImpl init(String locationTemplate, String mandatoryFields) {
        this.templateFields = locationTemplate;
        this.mandatoryFields = mandatoryFields;
        Stream.of(LocationTemplateElements.values()).forEach(t ->
                this.templateMembers.add(new TemplateFieldImpl(t.elementAbbreviation, t.toString(), 0, false)));
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
        Arrays.asList(templateFields.split(",")).stream().forEach(e ->
                list.add(LocationTemplateElements.fromAbbreviation(e).toString()));
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
        Arrays.asList(mandatoryFields.split(",")).stream().forEach(m ->
                list.add(LocationTemplateElements.fromAbbreviation(m).toString()));
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

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    private static final class TemplateFieldImpl implements TemplateField {


        int ranking;
        boolean mandatory;
        String name;
        String abbreviation;

        public TemplateFieldImpl(String abbreviation, String name, int ranking, boolean mandatory) {
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
