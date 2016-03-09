package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.LocationMember;

public class LocationMemberInfo {
    private long id;
    private String countryCode;
    private String countryName;
    private String administrativeArea;
    private String locality;
    private String subLocality;
    private String streetType;
    private String streetName;
    private String streetNumber;
    private String establishmentType;
    private String establishmentName;
    private String establishmentNumber;
    private String addressDetail;
    private String zipCode;
    private boolean defaultLocation;
    private String locale;

    public LocationMemberInfo() {
    }

    public LocationMemberInfo(LocationMember locationMember) {
        id = locationMember.getLocationId();
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
