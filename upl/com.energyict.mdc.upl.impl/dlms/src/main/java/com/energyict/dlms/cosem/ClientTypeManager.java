package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.attributes.ClientTypeManagerAttributes;
import com.energyict.dlms.cosem.methods.ClientTypeManagerMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2015 - 13:50
 */
public class ClientTypeManager extends AbstractCosemObject {

    private static final ObisCode DEFAULT_OBISCODE = ObisCode.fromString("0.0.128.0.16.255");

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public ClientTypeManager(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return DEFAULT_OBISCODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.CLIENT_TYPE_MANAGER.getClassId();
    }

    /**
     * The set of known client types for this concentrator.
     */
    public Array readClients() throws IOException {
        return readDataType(ClientTypeManagerAttributes.CLIENT, Array.class);
    }

    /**
     * Adds a client type to the set of known client types for this concentrator.
     */
    public void addClientType(Structure clientType) throws IOException {
        methodInvoke(ClientTypeManagerMethods.ADD_CLIENT_TYPE, clientType);
    }

    /**
     * Removes a client type from the set of known client types for this concentrator.
     */
    public void removeClientType(long id) throws IOException {
        methodInvoke(ClientTypeManagerMethods.REMOVE_CLIENT_TYPE, new Unsigned32(id));
    }

    /**
     * Updates the client type structure with the same ID as passed in the structure.
     */
    public void updateClientType(Structure clientType) throws IOException {
        methodInvoke(ClientTypeManagerMethods.UPDATE_CLIENT_TYPE, clientType);
    }
}