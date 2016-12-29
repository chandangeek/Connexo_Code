package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 8-apr-2011
 * Time: 9:40:18
 */
public class EncoderModelDetection extends AbstractRadioCommand {

    protected EncoderModelDetection(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    int statusOrNumberOfWheelsForEncoderA;
    int encoderManufacturerA;
    int encoderModelA;
    int statusOrNumberOfWheelsForEncoderB;
    int encoderManufacturerB;
    int encoderModelB;

    @Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        statusOrNumberOfWheelsForEncoderA = data[offset++] & 0xFF;
        encoderManufacturerA = data[offset++] & 0xFF;
        encoderModelA = data[offset++] & 0xFF;
        statusOrNumberOfWheelsForEncoderB = data[offset++] & 0xFF;
        encoderManufacturerB = data[offset++] & 0xFF;
        encoderModelB = data[offset] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.EncoderModelDetection;
    }

    public String getEncoderManufacturerDescriptionA() {
        return getEncoderManufacturerDescription(encoderManufacturerA);
    }

    public String getEncoderManufacturerDescriptionB() {
        return getEncoderManufacturerDescription(encoderManufacturerB);
    }

    public String getEncoderModelDescriptionA() {
        return getEncoderModelDescription(encoderManufacturerA, encoderModelA);
    }

    public String getEncoderModelDescriptionB() {
        return getEncoderModelDescription(encoderManufacturerB, encoderModelB);
    }


    public String getEncoderManufacturerDescription(int encoderManufacturer) {
        switch (encoderManufacturer) {
            case 1:
                return "Elster AMCO";
            case 2:
                return "Sensus, Hersey, Invensys, Badger";
            case 3:
                return "Neptune";
            default:
                return "(No encoder)";
        }
    }

    public String getEncoderModelDescription(int encoderManufacturer, int encoderModel) {
        if (encoderManufacturer == 0xFF) {
            return "";
        }

        switch (encoderManufacturer) {
            case 1:
                switch (encoderModel & 0xF0) {                 //Only first nibble contains relevant data
                    case 0x00:
                        return "Scan coder";
                    case 0x10:
                        return "Dual scan";
                    case 0x20:
                        return "Multi scan";
                    case 0x30:
                        return "Q100";
                    case 0x40:
                        return "Scan counter";
                    case 0x50:
                        return "Aqua master";
                    case 0x60:
                        return "Invision 11C";
                    case 0x70:
                        return "Invision 21C";
                }
            default:
                return "Manufacturer ID: " + encoderModel;
        }
    }
}