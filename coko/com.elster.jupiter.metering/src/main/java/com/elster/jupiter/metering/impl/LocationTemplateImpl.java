/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


public final class LocationTemplateImpl implements LocationTemplate {

    static final ImmutableList<String> ALLOWED_LOCATION_TEMPLATE_ELEMENTS =
            ImmutableList.of("#ccod", "#cnam", "#adma", "#loc", "#subloc",
                    "#styp", "#snam", "#snum", "#etyp", "#enam", "#enum", "#addtl", "#zip", "#locale");

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private String templateFields;
    private String mandatoryFields;
    private List<String> splitLineElements = new ArrayList<>();
    private final DataModel dataModel;
    private List<TemplateField> templateMembers = new ArrayList<>();
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    enum LocationTemplateElements {
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

        public String getElementAbbreviation() {
            return elementAbbreviation;
        }

        private final String elementAbbreviation;

        LocationTemplateElements(String elementCode) {
            this.elementAbbreviation = elementCode;
        }

        @Override
        public String toString() {
            String elementName = name().toLowerCase();
            if (elementName.contains("_")) {
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
        if (locationTemplate != null) {
            this.templateFields = locationTemplate.trim();
            Arrays.asList(this.templateFields.split(",")).stream().filter(f ->
                    f.startsWith("\\n") || f.startsWith("\\r")
                            || f.startsWith("\n") || f.startsWith("\r")).forEach(e ->
                    splitLineElements.add(normalize(e)));
            String[] templateElements = normalize(this.templateFields).split(",");

            if (Arrays.asList(templateElements).containsAll(ALLOWED_LOCATION_TEMPLATE_ELEMENTS)) {
                AtomicInteger index = new AtomicInteger(-1);
                Arrays.asList(templateElements).stream().forEach(t -> templateMembers.forEach(tm -> {
                    if (tm.getAbbreviation().equalsIgnoreCase(t)) {
                        tm.setRanking(index.incrementAndGet());
                    }
                }));
                if (mandatoryFields != null && !mandatoryFields.isEmpty()
                        && Arrays.asList(templateElements).containsAll(Arrays.asList(mandatoryFields.trim().split(",")))) {
                    this.mandatoryFields = mandatoryFields.trim();
                    String[] mandatoryFieldElements = this.mandatoryFields.split(",");

                    Arrays.asList(mandatoryFieldElements).stream().forEach(t -> templateMembers.forEach(tm -> {
                        if (tm.getAbbreviation().equalsIgnoreCase(t)) {
                            tm.setMandatory(true);
                        }
                    }));

                }
            } else {
                throw new IllegalArgumentException("Bad Location Template");
            }
        } else {
            throw new IllegalArgumentException("Bad Location Template");
        }
    }

    @Inject
    public LocationTemplateImpl(DataModel dataModel) {
        this.dataModel = dataModel;

    }

    LocationTemplateImpl init(String locationTemplate, String mandatoryFields) {
        this.templateFields = locationTemplate;
        this.mandatoryFields = mandatoryFields;
        Stream.of(LocationTemplateElements.values()).forEach(t ->
                this.templateMembers.add(new TemplateFieldImpl(t.elementAbbreviation, t.toString(), 0, false)));
        return this;
    }

    public static LocationTemplateImpl from(DataModel dataModel, String locationTemplate, String mandatoryFields) {
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
                list.add(LocationTemplateElements.fromAbbreviation(normalize(e)).toString()));
        return list;
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

    private String normalize(String input) {
        return input.replace("\\r", "").replace("\\n", "")
                .replace("\r", "").replace("\n", "")
                .replace("\r\n", "").replace("\n\r", "");
    }

    @Override
    public List<TemplateField> getTemplateMembers() {
        return Collections.unmodifiableList(this.templateMembers);
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

    @Override
    public List<String> getSplitLineElements() {
        return Collections.unmodifiableList(this.splitLineElements);
    }

    static final class TemplateFieldImpl implements TemplateField, Comparable<TemplateFieldImpl> {
        int ranking;
        boolean mandatory;
        String name;
        String abbreviation;

        TemplateFieldImpl(String abbreviation, String name, int ranking, boolean mandatory) {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TemplateFieldImpl that = (TemplateFieldImpl) o;

            if (ranking != that.ranking) {
                return false;
            }
            if (mandatory != that.mandatory) {
                return false;
            }
            if (name != null ? !name.equals(that.name) : that.name != null) {
                return false;
            }
            return abbreviation != null ? abbreviation.equals(that.abbreviation) : that.abbreviation == null;

        }

        @Override
        public int hashCode() {
            int result = ranking;
            result = 31 * result + (mandatory ? 1 : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (abbreviation != null ? abbreviation.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "TemplateFieldImpl{" +
                    "ranking=" + ranking +
                    ", mandatory=" + mandatory +
                    ", name='" + name + '\'' +
                    ", abbreviation='" + abbreviation + '\'' +
                    '}';
        }

        @Override
        public int compareTo(TemplateFieldImpl templateField) {
            int lastCmp = name.compareTo(templateField.name);
            return (lastCmp != 0 ? lastCmp : name.compareTo(templateField.name));
        }
    }

}
