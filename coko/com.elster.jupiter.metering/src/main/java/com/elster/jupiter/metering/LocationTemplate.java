package com.elster.jupiter.metering;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

public interface LocationTemplate {

    long getId();

    void remove();

    void parseTemplate(String template, String mandatoryFields);

    String getTemplateFields();

    Map<String, Integer> getRankings();

    String getMandatoryFields();

    List<String> getTemplateElementsNames();

    List<String> getMandatoryFieldsNames();

    List<TemplateField> getTemplateMembers();

    void setTemplateMembers(List<TemplateField> templateMembers);

    interface TemplateField {
        int getRanking();

        void setRanking(int ranking);

        boolean isMandatory();

        void setMandatory(boolean mandatory);

        String getName();

        void setName(String name);

        String getAbbreviation();

        void setAbbreviation(String abbreviation);
    }

}
