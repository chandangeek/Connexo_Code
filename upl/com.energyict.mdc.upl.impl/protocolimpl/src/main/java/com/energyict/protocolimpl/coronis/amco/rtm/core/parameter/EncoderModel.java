package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7-apr-2011
 * Time: 16:45:00
 */
public class EncoderModel extends AbstractParameter {

    public EncoderModel(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    private int port = 1;

    public void setPort(int port) {
        this.port = port;
    }

    int encoderManufacturer;
    int encoderModel;

    @Override
    ParameterId getParameterId() {
        if (port == 1) {
            return ParameterId.EncoderModelOnPortA;
        } else {
            return ParameterId.EncoderModelOnPortB;
        }
    }

    @Override
    public void parse(byte[] data) throws IOException {
        encoderManufacturer = data[0] & 0xFF;
        encoderModel = data[1] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Cannot set this parameter");
    }

    public String getEncoderManufacturerDescription() {
        switch (encoderManufacturer) {
            case 1:
                return "Elster AMCO";
            case 2:
                return "Sensus, Hersey, Invensys, Badger";
            case 3:
                return "Neptune";
            default:
                return "";
        }
    }

    public String getEncoderModelDescription() throws IOException {
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
                return "First byte of the serial number: " + encoderModel;
        }
    }
}