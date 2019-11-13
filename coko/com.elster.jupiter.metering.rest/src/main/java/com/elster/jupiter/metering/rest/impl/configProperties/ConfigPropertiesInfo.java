/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl.configProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class ConfigPropertiesInfo {

    public String id;
    public String type;
    public List<ConfigPropertiesPropertiesInfo> properties;
}
