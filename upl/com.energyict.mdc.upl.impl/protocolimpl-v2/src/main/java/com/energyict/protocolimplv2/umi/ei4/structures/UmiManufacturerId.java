package com.energyict.protocolimplv2.umi.ei4.structures;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.nio.charset.StandardCharsets;

public class UmiManufacturerId extends LittleEndianData {
    public static final int SIZE = 23;
    public static final UmiCode MANUFACTURER_ID_UMI_CODE = new UmiCode("umi.1.1.0.0");

    /**
     * The Model Type is 8 alpha-numeric (ASCII coded) characters maximum.
     * Model types of less than 8 characters should be padded with spaces (ASCII - 0x20).
     * This forms part of the manufacturer's identification number.
     * It is also used as the identification part of the Identification Message on the optical Interface.
     * See EN62056 for further details. Characters '/' and '!' are not permitted.
     * '\' can only be used as an escape sequence and should not be used in the Model Type. Default value = 'EISGMv0'.
     */
    private String modelType;        // 8

    /**
     * The three characters used as the XXX field in the Identification Message on the optical interface (ASCII coded).
     * This would usually be the three characters, 'E', 'L', 'S'. The characters should be upper-case 7-bit only
     * and are assigned to a manufacturer by the FLAG association. This forms part of the manufacturer's identification number.
     * Default value = 'ELS'
     */
    private String manufacturerId;  // 3

    /**
     * The Factory ID is 4 numeric characters in the range 0 to 9999 and indicates the location of manufacture (ASCII coded).
     * For example, Lotte Osnabr�ck is 0025, {0x30, 0x30, 0x32, 0x35}. This forms part of the manufactorer's identification number.
     * Defailt value 0025.
     */
    private String factoryId;        // 4

    /**
     * The serial number is 8 decimal numbers maximum (ASCII coded). Serial numbers of less then 8 characters should be padded with spaces (0x20).
     * This forms part of the manufacturer's identification number. Default value = 00000000
     */
    private String serialNumber;     // 8

    public UmiManufacturerId(byte[] raw) {
        super(raw, SIZE, false);
        byte[] modelTypeBytes = new byte[8];
        getRawBuffer().get(modelTypeBytes);
        this.modelType = String.copyValueOf(UmiHelper.convertBytesToChars(modelTypeBytes)).trim();
        byte[] manufacturerIdBytes = new byte[3];
        getRawBuffer().get(manufacturerIdBytes);
        this.manufacturerId = String.valueOf(UmiHelper.convertBytesToChars(manufacturerIdBytes)).trim();

        byte[] factoryIdBytes = new byte[4];
        getRawBuffer().get(factoryIdBytes);
        this.factoryId = String.valueOf(UmiHelper.convertBytesToChars(factoryIdBytes)).trim();
        byte[] serialNumberBytes = new byte[8];
        getRawBuffer().get(serialNumberBytes);
        this.serialNumber = String.valueOf(UmiHelper.convertBytesToChars(serialNumberBytes)).trim();
    }

    /**
     * Constructor for testing purposes
     */
    public UmiManufacturerId(String modelType, String manufacturerId, String factoryId, String serialNumber) {
        super(SIZE);
        this.modelType = modelType;
        this.manufacturerId = manufacturerId;
        this.factoryId = factoryId;
        this.serialNumber = serialNumber;


        getRawBuffer().put(modelType.getBytes(StandardCharsets.US_ASCII))
                .put(manufacturerId.getBytes(StandardCharsets.US_ASCII))
                .put(factoryId.getBytes(StandardCharsets.US_ASCII))
                .put(serialNumber.getBytes(StandardCharsets.US_ASCII));
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }
}
