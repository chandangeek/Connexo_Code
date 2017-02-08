/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.time.Instant;

public interface LocationMember {

    long getLocationId();

    String getLocale();

    String getCountryCode();

    String getCountryName();

    String getAdministrativeArea();

    String getLocality();

    String getSubLocality();

    String getStreetType();

    String getStreetName();

    String getStreetNumber();

    String getEstablishmentType();

    String getEstablishmentName();

    String getEstablishmentNumber();

    String getAddressDetail();

    String getZipCode();

    boolean isDefaultLocation();

    Instant getCreateTime();

    Instant getModTime();

    void remove();
}
