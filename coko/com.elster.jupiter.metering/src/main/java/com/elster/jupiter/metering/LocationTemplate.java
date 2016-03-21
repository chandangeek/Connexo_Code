package com.elster.jupiter.metering;

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


}
