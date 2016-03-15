package com.elster.jupiter.metering;

import java.util.Map;

public interface LocationTemplate {

    long getId();
    void remove();
    void parseTemplate(String template, String mandatoryFields);
    String getLocationTemplate();
    Map<String, Integer> getRankings();
    String getMandatoryFields();
    String[] getTemplateElements();


}
