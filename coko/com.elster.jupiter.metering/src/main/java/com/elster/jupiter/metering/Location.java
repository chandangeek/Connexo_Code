package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface Location {

    long getId();
    List<? extends LocationMember> getMembers();
    Optional<LocationMember> getMember(String locale);
    void remove();
    LocationMember setMember(String countryCode,
              String countryName,
              String administrativeArea,
              String locality,
              String subLocality,
              String streetType,
              String streetName,
              String streetNumber,
              String establishmentType,
              String establishmentName,
              String establishmentNumber,
              String addressDetail,
              String zipCode,
              boolean defaultLocation,
              String locale);


}
