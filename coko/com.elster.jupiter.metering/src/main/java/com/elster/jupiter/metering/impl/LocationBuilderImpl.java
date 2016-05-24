package com.elster.jupiter.metering.impl;


import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.orm.DataModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationBuilderImpl implements LocationBuilder {


    private String name;
    private List<LocationMemberBuilder> members = new ArrayList<>();


    private final DataModel dataModel;

   LocationBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public Location create() {
        LocationImpl location = LocationImpl.from(dataModel, name);
        location.doSave();
        for (LocationMemberBuilder member : members) {
            member.createMember(location);
        }
        return location;
    }

    @Override
    public LocationBuilder named(String name) {
        this.name = name;
        return this;
    }



    public Optional<LocationMemberBuilder> getMember(String locale) {
        return members.stream().filter(location -> location.getLocale().equalsIgnoreCase(locale))
                .findFirst();
    }


    @Override
    public LocationMemberBuilder member() {
        return new LocationMemberBuilderImpl();
    }

    private class LocationMemberBuilderImpl implements LocationMemberBuilder {

        private String name;
        private long locationId;
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


        @Override
        public LocationMemberBuilder setLocationId(long locationId) {
            this.locationId = locationId;
            return this;
        }

        @Override
        public LocationMemberBuilder setCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        @Override
        public LocationMemberBuilder setCountryName(String countryName) {
            this.countryName = countryName;
            return this;
        }

        @Override
        public LocationMemberBuilder setAdministrativeArea(String administrativeArea) {
            this.administrativeArea=administrativeArea;
            return this;
        }

        @Override
        public LocationMemberBuilder setLocality(String locality) {
            this.locality = locality;
            return this;
        }

        @Override
        public LocationMemberBuilder setSubLocality(String subLocality) {
            this.subLocality = subLocality;
            return this;
        }

        @Override
        public LocationMemberBuilder setStreetType(String streetType) {
            this.streetType = streetType;
            return this;
        }

        @Override
        public LocationMemberBuilder setStreetName(String streetName) {
            this.streetName = streetName;
            return this;
        }

        @Override
        public LocationMemberBuilder setStreetNumber(String streetNumber) {
            this.streetNumber = streetNumber;
            return this;
        }

        @Override
        public LocationMemberBuilder setEstablishmentType(String establishmentType) {
            this.establishmentType = establishmentType;
            return this;
        }

        @Override
        public LocationMemberBuilder setEstablishmentName(String establishmentName) {
            this.establishmentName = establishmentName;
            return this;
        }

        @Override
        public LocationMemberBuilder setEstablishmentNumber(String establishmentNumber) {
            this.establishmentNumber = establishmentNumber;
            return this;
        }

        @Override
        public LocationMemberBuilder setAddressDetail(String addressDetail) {
            this.addressDetail = addressDetail;
            return this;
        }

        @Override
        public LocationMemberBuilder setZipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        @Override
        public LocationMemberBuilder isDaultLocation(boolean defaultLocation) {
            this.defaultLocation = defaultLocation;
            return this;
        }

        @Override
        public LocationMemberBuilder setLocale(String locale) {
            this.locale = locale;
            return this;
        }

        @Override
        public LocationMemberBuilder named(String name) {
            this.name = name;
            return this;
        }

        @Override
        public LocationBuilder add() {
            LocationBuilderImpl.this.members.add(this);
            return LocationBuilderImpl.this;
        }


        @Override
        public String getLocale(){
            return this.locale;
        }

        @Override
        public LocationMember createMember(Location location) {
            return location.setMember(countryCode, countryName,
                    administrativeArea,locality,subLocality,streetType,streetName,streetNumber,establishmentType,
                    establishmentName,establishmentNumber,addressDetail,zipCode,defaultLocation,locale);
        }
    }
}

