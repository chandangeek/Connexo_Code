package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.NbiotPushSetupAttributes;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class NbiotPushSetup extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.12.255");

    /**
     * Creates a new instance of PushSetupConfig
     */
    public NbiotPushSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP.getClassId();
    }

    public Array readPushObjectList() throws IOException {
        return (Array)readDataType(NbiotPushSetupAttributes.PUSH_OBJECT_LIST);
    }

    public void writePushObjectList(Array pushObjectList) throws IOException {
        try{
            write(NbiotPushSetupAttributes.PUSH_OBJECT_LIST, pushObjectList.getBEREncodedByteArray());
        }catch (IOException e){
            throw new NestedIOException(e, "Could not write the object list. " + e.getMessage());
        }
    }

    public AbstractDataType readDestinationAndMethod() throws IOException {
        return readDataType(NbiotPushSetupAttributes.SEND_DESTINATION_AND_METHOD);
    }

    public void writeSendDestinationAndMethod(int transportType, String destinationAddress, int messageType) throws IOException {
        Structure config = new Structure();
        config.addDataType(new TypeEnum(transportType));
        config.addDataType(new OctetString(destinationAddress.getBytes()));
        config.addDataType(new TypeEnum(messageType));
        write(NbiotPushSetupAttributes.SEND_DESTINATION_AND_METHOD, config.getBEREncodedByteArray());
    }

    public Array readCommunicationWindow() throws IOException {
        return (Array)readDataType(NbiotPushSetupAttributes.COMMUNICATION_WINDOW);
    }

    public void writeCommunicationWindow(Array callingWindow) throws IOException {
        try{
            write(NbiotPushSetupAttributes.COMMUNICATION_WINDOW, callingWindow.getBEREncodedByteArray());
        }catch (IOException e){
            throw new NestedIOException(e, "Could not write the communication window. " + e.getMessage());
        }
    }

    public Unsigned16 readRandomizationStartInterval() throws IOException {
        return (Unsigned16)readDataType(NbiotPushSetupAttributes.RANDOMIZATION_START_INTERVAL);
    }

    public void writeRandomizationStartInterval(Unsigned16 randomizationStartInterval) throws IOException {
        try{
            write(NbiotPushSetupAttributes.RANDOMIZATION_START_INTERVAL, randomizationStartInterval.getBEREncodedByteArray());
        }catch (IOException e){
            throw new NestedIOException(e, "Could not write the randomization of the start interval. " + e.getMessage());
        }
    }

    public Unsigned8 readNumberOfRetries() throws IOException {
        return (Unsigned8)readDataType(NbiotPushSetupAttributes.NUMBER_OF_RETRIES);
    }

    public void writeNumberOfRetries(Unsigned8 numberOfRetries) throws IOException {
        try{
            write(NbiotPushSetupAttributes.NUMBER_OF_RETRIES, numberOfRetries.getBEREncodedByteArray());
        }catch (IOException e){
            throw new NestedIOException(e, "Could not write the number of retries. " + e.getMessage());
        }
    }

    public Unsigned16 readRepetitionDelay() throws IOException {
        return (Unsigned16)readDataType(NbiotPushSetupAttributes.REPETITION_DELAY);
    }

    public void writeRepetitionDelay(Unsigned16 repetitionDelay) throws IOException {
        try{
            write(NbiotPushSetupAttributes.REPETITION_DELAY, repetitionDelay.getBEREncodedByteArray());
        }catch (IOException e){
            throw new NestedIOException(e, "Could not write the repetition delay. " + e.getMessage());
        }
    }
}