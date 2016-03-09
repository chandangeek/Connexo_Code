package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import javax.inject.Inject;
import java.time.Instant;


public class LocationMemberImpl implements LocationMember {
    private Reference<Location> location = ValueReference.absent();

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
    //private String coordLat;
    //private String coordLong;
    private boolean defaultLocation;
    private String locale;
    private final DataModel dataModel;
    private Instant createDate;
    private Instant modDate;

    @Inject
    LocationMemberImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    LocationMemberImpl init(String countryCode,
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
                     // String coordLat,
                     // String coordLong,
                      boolean defaultLocation,
                      String locale) {

        this.countryCode = countryCode;
        this.countryName = countryName;
        this.administrativeArea = administrativeArea;
        this.locality = locality;
        this.subLocality = subLocality;
        this.streetType = streetType;
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.establishmentType = establishmentType;
        this.establishmentName = establishmentName;
        this.establishmentNumber = establishmentNumber;
        this.addressDetail = addressDetail;
        this.zipCode = zipCode;
        //this.coordLat = coordLat;
        //this.coordLong = coordLong;
        this.defaultLocation = defaultLocation;
        this.locale = locale;
        return this;
    }

    static LocationMemberImpl from(DataModel dataModel,
                             String countryCode,
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
                             String coordLat,
                             String coordLong,
                             boolean defaultLocation,
                             String locale) {
        return dataModel.getInstance(LocationMemberImpl.class).init(countryCode, countryName, administrativeArea,locality,subLocality,
                streetType,streetName,streetNumber,establishmentType,establishmentName,establishmentNumber,addressDetail,zipCode,
               // coordLat,coordLong,
                defaultLocation,locale);
    }


    public long getLocationId() {
        return locationId;
    }

    @Override
    public String getCountryCode() {
        return countryCode;
    }

    @Override
    public String getCountryName() {
        return countryName;
    }

    @Override
    public String getAdministrativeArea() {
        return administrativeArea;
    }

    @Override
    public String getLocality() {
        return locality;
    }

    @Override
    public String getSubLocality() {
        return subLocality;
    }

    @Override
    public String getStreetType() {
        return streetType;
    }

    @Override
    public String getStreetName() {
        return streetName;
    }

    @Override
    public String getStreetNumber() {
        return streetNumber;
    }

    @Override
    public String getEstablishmentType() {
        return establishmentType;
    }

    @Override
    public String getEstablishmentName() {
        return establishmentName;
    }

    @Override
    public String getEstablishmentNumber() {
        return establishmentNumber;
    }

    @Override
    public String getAddressDetail() {
        return addressDetail;
    }

    @Override
    public String getZipCode() {
        return zipCode;
    }

   // public String getCoordLat() { return coordLat; }

    //public String getCoordLong() { return coordLong;}
    @Override
    public boolean isDefaultLocation() {
        return defaultLocation;
    }

    @Override
    public String getLocale() {
        return locale;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public Location getLocation() {
        return location.get();
    }


}
