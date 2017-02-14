/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ValidationRuleInfos {

    public int total;
    public List<ValidationRuleInfo> rules = new ArrayList<ValidationRuleInfo>();

    public ValidationRuleInfos() {
    }
}
