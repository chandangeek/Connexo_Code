/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.ValidationRuleSetVersion;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class ValidationRuleSetVersionInfos{

    public int total;
    public List<ValidationRuleSetVersionInfo> versions = new ArrayList<>();

    public ValidationRuleSetVersionInfos() {
    }

    public ValidationRuleSetVersionInfos(Iterable<? extends ValidationRuleSetVersion> sets) {
        addAll(sets);
    }

    public ValidationRuleSetVersionInfo add(ValidationRuleSetVersion ruleSet) {
        ValidationRuleSetVersionInfo result = new ValidationRuleSetVersionInfo(ruleSet);
        versions.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends ValidationRuleSetVersion> sets) {
        for (ValidationRuleSetVersion each : sets) {
            add(each);
        }
    }
}


