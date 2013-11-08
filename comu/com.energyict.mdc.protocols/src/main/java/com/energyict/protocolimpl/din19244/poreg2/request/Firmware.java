package com.energyict.protocolimpl.din19244.poreg2.request;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ASDU;
import com.energyict.protocolimpl.din19244.poreg2.core.Response;

/**
 * Class to read out the firmware version.
 *
 * Copyrights EnergyICT
 * Date: 21-apr-2011
 * Time: 22:01:39
 */
public class Firmware extends AbstractRequest {

    public Firmware(Poreg poreg) {
        super(poreg);
    }

    private String firmware;

    public String getFirmware() {
        return firmware;
    }

    @Override
    public void parse(byte[] data) {
        this.firmware = "version " + (data[5] & 0xFF) / 100 + "." + (data[0] & 0xFF);
    }

    @Override
    protected int getResponseASDU() {
        return ASDU.FirmwareResponse.getId();
    }

    @Override
    protected int getExpectedResponseType() {
        return Response.USERDATA.getId();
    }

    @Override
    protected byte[] getRequestASDU() {
        return ASDU.Firmware.getIdBytes();
    }
}
