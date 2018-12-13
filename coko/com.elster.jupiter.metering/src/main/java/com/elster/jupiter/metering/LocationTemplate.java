/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface LocationTemplate {

    long getId();

    void remove();

    void parseTemplate(String template, String mandatoryFields);

    String getTemplateFields();

    String getMandatoryFields();

    List<String> getTemplateElementsNames();

    List<String> getMandatoryFieldsNames();

    List<TemplateField> getTemplateMembers();

    List<String> getSplitLineElements();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

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
