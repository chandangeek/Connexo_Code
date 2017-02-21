/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface LocationBuilder {


    Location create();

    LocationBuilder named(String name);

    LocationMemberBuilder member();

    Optional<LocationMemberBuilder> getMemberBuilder(String locale);

    /**
     * Intermediate builder for members
     */
    interface LocationMemberBuilder {

        LocationMemberBuilder setLocationId(long locationId);
        LocationMemberBuilder setCountryCode(String countryCode);
        LocationMemberBuilder setCountryName(String countryName);
        LocationMemberBuilder setAdministrativeArea(String administrativeArea);
        LocationMemberBuilder setLocality(String locality);
        LocationMemberBuilder setSubLocality(String subLocality);
        LocationMemberBuilder setStreetType(String streetType);
        LocationMemberBuilder setStreetName(String streetName);
        LocationMemberBuilder setStreetNumber(String streetNumber);
        LocationMemberBuilder setEstablishmentType(String establishmentType);
        LocationMemberBuilder setEstablishmentName(String establishmentName);
        LocationMemberBuilder setEstablishmentNumber(String establishmentNumber);
        LocationMemberBuilder setAddressDetail(String addressDetail);
        LocationMemberBuilder setZipCode(String zipCode);
        LocationMemberBuilder isDaultLocation(boolean defaultLocation);
        LocationMemberBuilder setLocale(String locale);
        String getLocale();
        LocationMemberBuilder named(String name);
        LocationBuilder add();
    }
}
