/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class EstimationRuleSetInfo {

    public long id;
	public String name;
	public String description;
    public long numberOfInactiveRules;
    public long numberOfRules;
    public List<EstimationRuleInfo> rules;
    public long version;
    public Boolean isInUse;

}
