package com.energyict.protocolimpl.din19244.poreg2.request;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.*;

import java.io.IOException;
import java.util.Date;

/**
 * Class to set the device clock
 *
 * Copyrights EnergyICT
 * Date: 9-mei-2011
 * Time: 16:00:34
 */
public class SetTime extends AbstractRequest {

    public SetTime(Poreg poreg) {
        super(poreg);
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        int response = data[0] & 0xFF;
        if (response != getExpectedResponseType()) {
            throw new IOException("Error setting the time, request returned " + Response.getDescription(response));
        }
    }

    private Date time = new Date();

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    protected int getResponseASDU() {
        return ASDU.SetTimeResponse.getId();
    }

    @Override
    protected byte[] getAdditionalBytes() {
        return DinTimeParser.getBytes(time);
    }

    @Override
    protected int getExpectedResponseType() {
        return Response.ACK.getId();
    }

    @Override
    protected byte[] getRequestASDU() {
        return ASDU.SetTime.getIdBytes();
    }

    protected byte[] getWriteASDU() {
        return ASDU.SetTime.getIdBytes();
    }
}