package com.elster.jupiter.metering;

public interface LocationMember {

    Location getLocation();
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
    void remove();
}
