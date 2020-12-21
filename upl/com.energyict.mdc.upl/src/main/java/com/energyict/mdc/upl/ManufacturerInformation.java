package com.energyict.mdc.upl;

/**
 * Placeholder implementation
 */
public class ManufacturerInformation {

    private final Manufacturer manufacturer;

    public ManufacturerInformation() {
        this.manufacturer = Manufacturer.Eict;
    }

    public ManufacturerInformation(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

}