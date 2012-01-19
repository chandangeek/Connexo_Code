package com.energyict.dlms;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.aso.ApplicationServiceObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AdaptorConnection implements DLMSConnection {

    ByteArrayOutputStream baos = null;
    private InvokeIdAndPriority invokeIdAndPriority = new InvokeIdAndPriority();

    public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
        if (baos == null) {
            baos = new ByteArrayOutputStream();
        }
        baos.write(byteRequestBuffer, 3, byteRequestBuffer.length - 3); // skip HDLS LLC
        return null;
    }

    public void reset() {
        baos.reset();
    }

    public byte[] getCompoundData() {
        return baos.toByteArray();
    }

    public void connectMAC() throws IOException, DLMSConnectionException {
        // Nothing to do
    }

    public void disconnectMAC() throws IOException, DLMSConnectionException {
        // Nothing to do
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public int getType() {
        return 0;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
        // Nothing to do
    }

    public void setIskraWrapper(int type) {
        // Nothing to do
    }

    public void setSNRMType(int type) {
        // Nothing to do
    }

    public void setInvokeIdAndPriority(InvokeIdAndPriority iiap) {
        this.invokeIdAndPriority = iiap;
    }

    public InvokeIdAndPriority getInvokeIdAndPriority() {
        return this.invokeIdAndPriority;
    }

    public int getMaxRetries() {
        return 0;
    }

    public ApplicationServiceObject getApplicationServiceObject() {
        return null;
    }

}
