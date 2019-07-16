/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import java.time.Instant;
import java.util.List;

public class LicenseInfo extends LicenseShortInfo {

    public Instant validfrom;
    public int graceperiod;
    public String type;
    public String description;
    public List<LicenseProperty> content;

}
