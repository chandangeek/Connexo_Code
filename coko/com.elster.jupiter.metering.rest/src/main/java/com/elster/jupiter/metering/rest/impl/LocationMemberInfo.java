/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.LocationMember;

public class LocationMemberInfo {
    public long locationId;
    public String countryCode;
    public String countryName;
    public String administrativeArea;
    public String locality;
    public String subLocality;
    public String streetType;
    public String streetName;
    public String streetNumber;
    public String establishmentType;
    public String establishmentName;
    public String establishmentNumber;
    public String addressDetail;
    public String zipCode;
    public boolean defaultLocation;
    public String locale;

    public LocationMemberInfo() {
    }

    public LocationMemberInfo(LocationMember locationMember) {
        locationId = locationMember.getLocationId();
        countryCode = locationMember.getCountryCode();
        countryName = locationMember.getCountryName();
        administrativeArea = locationMember.getAdministrativeArea();
        locality = locationMember.getLocality();
        subLocality = locationMember.getSubLocality();
        streetType = locationMember.getStreetType();
        streetName = locationMember.getStreetName();
        streetNumber = locationMember.getStreetNumber();
        establishmentType = locationMember.getEstablishmentType();
        establishmentName = locationMember.getEstablishmentName();
        establishmentNumber = locationMember.getEstablishmentNumber();
        addressDetail = locationMember.getAddressDetail();
        zipCode = locationMember.getZipCode();
        defaultLocation = locationMember.isDefaultLocation();
        locale = locationMember.getLocale();
    }
}
