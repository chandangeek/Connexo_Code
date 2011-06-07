package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ASDU;
import com.energyict.protocolimpl.din19244.poreg2.core.Response;
import com.energyict.protocolimpl.din19244.poreg2.request.AbstractRequest;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Parent class containing methods common for all registers, e.g. the doRequest()
 *
 * Copyrights EnergyICT
 * Date: 20-apr-2011
 * Time: 14:10:38
 */
abstract public class AbstractRegister extends AbstractRequest {

    private int registerAddress;
    private int fieldAddress;
    private int numberOfRegisters;
    private int numberOfFields;

    private int receivedRegisterAddress;
    private int receivedFieldAddress;
    private int receivedNumberOfRegisters;
    private int previouslyReceivedNumberOfRegisters = 0;
    private int totalReceivedNumberOfRegisters = 0;
    private int receivedNumberOfFields;

    /**
     * Constructor common for all registers.
     * @param poreg the protocol instance
     * @param registerAddress register row address to start with in the register group
     * @param fieldAddress column address to start with
     * @param numberOfRegisters the number of registers (starting from registerAddress)
     * @param numberOfFields the number of fields (starting from fieldAddress)
     */
    public AbstractRegister(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg);
        this.registerAddress = registerAddress;
        this.fieldAddress = fieldAddress;
        this.numberOfRegisters = numberOfRegisters;
        this.numberOfFields = numberOfFields;
    }

    public int getReceivedNumberOfFields() {
        return receivedNumberOfFields;
    }

    public int getReceivedFieldAddress() {
        return receivedFieldAddress;
    }

    public int getFieldAddress() {
        return fieldAddress;
    }

    public int getRegisterAddress() {
        return registerAddress;
    }

    public int getReceivedRegisterAddress() {
        return receivedRegisterAddress;
    }

    public int getReceivedNumberOfRegisters() {
        return receivedNumberOfRegisters;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public int getNumberOfRegisters() {
        return numberOfRegisters;
    }

    public int getTotalReceivedNumberOfRegisters() {
        return totalReceivedNumberOfRegisters;
    }

    /**
     * Requests the registers contents based on the number of fields and registers defined in the constructor.
     * Parses the received data into relevant content.
     * @throws java.io.IOException in case of timeout
     */
    @Override
    public void doRequest() throws IOException {
        byte[] result = new byte[0];
        byte[] response = poreg.getConnection().doRequest(getRequestASDU(), getAdditionalBytes(), getExpectedResponseType(), getResponseASDU());

        while (true) {
            validateAdditionalBytes(response);
            response = ProtocolTools.getSubArray(response, getLengthOfReceivedAdditionalBytes());
            result = ProtocolTools.concatByteArrays(result, response);
            if (isCompleted() || isEndOfTable()) {
                break;
            }
            previouslyReceivedNumberOfRegisters = getReceivedNumberOfRegisters();
            response = poreg.getConnection().doContinue(getExpectedResponseType(), getResponseASDU());
        }

        //Parse the rest
        parse(result);
    }

    private boolean isEndOfTable() {
        return (getReceivedNumberOfRegisters() < previouslyReceivedNumberOfRegisters);
    }

    private boolean isCompleted() {
        int receivedRegisters = getReceivedRegisterAddress() + getReceivedNumberOfRegisters();
        int receivedFields = getReceivedFieldAddress() + getReceivedNumberOfFields();
        int expectedRegisters = getRegisterAddress() + getNumberOfRegisters();
        int expectedFields = getFieldAddress() + getNumberOfFields();

        return (expectedRegisters == receivedRegisters) && (receivedFields == expectedFields);
    }

    /**
     * Checks the received additional bytes.
     * They should contain the same values as the info given in the constructor.
     * @param response the received bytes
     * @return the remaining bytes (without the additional data)
     * @throws java.io.IOException
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