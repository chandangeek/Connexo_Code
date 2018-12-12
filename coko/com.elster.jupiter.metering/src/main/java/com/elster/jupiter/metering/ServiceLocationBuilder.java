/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;

public interface ServiceLocationBuilder {
    
    ServiceLocation create();
    
    ServiceLocationBuilder setAlias(String alias);
    ServiceLocationBuilder setDescription(String description);
    ServiceLocationBuilder setMRID(String mRID);
    ServiceLocationBuilder setName(String name);
    ServiceLocationBuilder setDirection(String direction);
    ServiceLocationBuilder setElectronicAddress(ElectronicAddress electronicAddress);
    ServiceLocationBuilder setGeoInfoReference(String geoInfoReference);
    ServiceLocationBuilder setMainAddress(StreetAddress mainAddress);
    ServiceLocationBuilder setPhone1(TelephoneNumber phone1);
    ServiceLocationBuilder setPhone2(TelephoneNumber phone2);
    ServiceLocationBuilder setSecondaryAddress(StreetAddress secondaryAddress);
    ServiceLocationBuilder setStatus(Status status);
    ServiceLocationBuilder setType(String type);
    ServiceLocationBuilder setAccessMethod(String accessMethod);
    ServiceLocationBuilder setNeedsInspection(boolean needsInspection);
    ServiceLocationBuilder setSiteAccessProblem(String siteAccessProblem);

}
