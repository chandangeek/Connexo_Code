package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ASDU;
import com.energyict.protocolimpl.din19244.poreg2.core.Response;
import com.energyict.protocolimpl.din19244.poreg2.request.AbstractRequest;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Parent class containing methods common for all registers, e.g. the doRequest()
 * Copyrights EnergyICT
 * Date: 20-apr-2011
 * Time: 14:10:38
 */
abstract public class AbstractRegister extends AbstractRequest {

    /**
     * Constructor common for all registers.
     *
     * @param poreg             the protocol instance
     * @param registerAddress   register row address to start with in the register group
     * @param fieldAddress      column address to start with
     * @param numberOfRegisters the number of registers (starting from registerAddress)
     * @param numberOfFields    the number of fields (starting from fieldAddress)
     */
    public AbstractRegister(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg);
        this.registerAddress = registerAddress;
        this.fieldAddress = fieldAddress;
        this.numberOfRegisters = numberOfRegisters;
        this.numberOfFields = numberOfFields;
    }

    /**
     * Requests the registers contents based on the number of fields and registers defined in the constructor.
     * Parses the received data into relevant content.
     * Retry the full request when a corrupt frame (ProtocolConnectionException) is received.
     *
     * @throws IOException in case of timeout
     */
    @Override
    public void doRequest() throws IOException {
        corruptFrame = true;
        int count = 0;
        while (corruptFrame) {
            doTheRequest();      //First attempt

            if (corruptFrame) {
                poreg.getLogger().warning("Received corrupted frame while requesting register data (GID = " + getRegisterGroupID() + ")");
                poreg.getLogger().warning("Cause: " + corruptCause);

                count++;     //Retry counter
                if (count > poreg.getConnection().getRetries()) {  //Stop retrying after X retries
                    String msg = "Still received a corrupt frame (" + "after " + poreg.getConnection().getRetries() + " retries) while trying to request register data (GID = " + getRegisterGroupID() + "). Aborting.";
                    poreg.getLogger().severe(msg);
                    throw new IOException(msg);
                }
                poreg.getLogger().warning("Resending request for register data (GID = " + getRegisterGroupID() + ") (retry " + count + "/" + poreg.getConnection().getRetries() + ")");
            }
        }
    }

    /**
     * Checks the received additional bytes.
     * They should contain the same values as the info given in the constructor.
     *
     * @param response the received bytes
     * @return the remaining bytes (without the additional data)
     * @throws IOException
     */
    @Override
    protected byte[] validateAdditionalBytes(byte[] response) throws IOException {
        int receivedGID = response[0] & 0xFF;
        if (receivedGID != getRegisterGroupID()) {
            throw new IOException("Error receiving register data. Expected GID: " + getRegisterGroupID() + ", received " + receivedGID);
        }

        receivedRegisterAddress = response[1] & 0xFF;
        receivedFieldAddress = response[2] & 0xFF;
        receivedNumberOfRegisters = response[3] & 0xFF;
        receivedNumberOfFields = response[4] & 0xFF;
        totalReceivedNumberOfRegisters += receivedNumberOfRegisters;
        return ProtocolTools.getSubArray(response, getLengthOfReceivedAdditionalBytes());
    }

    protected abstract int getRegisterGroupID();

    /**
     * Puts the constructor info into a byte array
     *
     * @return byte array
     */
    @Override
    public byte[] getAdditionalBytes() {
        byte[] request = new byte[5];
        request[0] = (byte) getRegisterGroupID();
        request[1] = (byte) registerAddress;
        request[2] = (byte) fieldAddress;
        request[3] = (byte) numberOfRegisters;
        request[4] = (byte) numberOfFields;
        return request;
    }

    protected int getLengthOfReceivedAdditionalBytes() {
        return 5;
    }

    protected int getResponseASDU() {
        return ASDU.RegisterResponse.getId();
    }

    public int getExpectedResponseType() {
        return Response.USERDATA.getId();
    }

    public byte[] getRequestASDU() {
        return ASDU.ReadRegister.getIdBytes();
    }
}