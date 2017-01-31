/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.LocationMember;

public class LocationMemberInfo {
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

    public LocationMemberInfo(){
    }

    public LocationMemberInfo(LocationMember locationMember){
        this.countryCode = locationMember.getCountryCode();
        this.countryName = locationMember.getCountryName();
        this.administrativeArea = locationMember.getAdministrativeArea();
        this.locality = locationMember.getAdministrativeArea();
        this.subLocality = locationMember.getSubLocality();
        this.streetName = locationMember.getStreetName();
        this.streetNumber = locationMember.getStreetNumber();
        this.streetType = locationMember.getStreetType();
        this.establishmentName = locationMember.getEstablishmentName();
        this.establishmentType = locationMember.getEstablishmentType();
        this.establishmentNumber = locationMember.getEstablishmentNumber();
        this.addressDetail = locationMember.getAddressDetail();
        this.zipCode = locationMember.getZipCode();
        this.defaultLocation = locationMember.isDefaultLocation();
        this.locale = locationMember.getLocale();
    }
}
