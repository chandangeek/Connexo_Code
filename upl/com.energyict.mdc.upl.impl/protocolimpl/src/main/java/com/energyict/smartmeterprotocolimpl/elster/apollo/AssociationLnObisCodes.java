package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.obis.ObisCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration containing the possible {@link com.energyict.dlms.cosem.AssociationLN} Obiscodes.
 * Each clientID has a different AssociationObject.
 */
enum AssociationLnObisCodes {

    /**
     * The current_client
     */
    CURRENT_CLIENT(0, AS300ObisCodeProvider.AssociationLnCurrentClient),
    /**
    * The public client has limited read access
    */
    PUBLIC_CLIENT(16, AS300ObisCodeProvider.AssociationLnPublicClient),
    /**
    * The reading client has reading access
    */
    DATACOLLECTION_CLIENT(32, AS300ObisCodeProvider.AssociationLnDataCollectionClient),
    /**
    * The reading client has reading access
    */
    EXT_DATACOLLECTION_CLIENT(48, AS300ObisCodeProvider.AssociationLnExtDataCollectionClient),
    /**
     * The management client has all access
     */
    MANAGEMENT_CLIENT(64, AS300ObisCodeProvider.AssociationLnManagementClient),
    /**
     * The firmware client has access to firmware related objects (ex. FirmwareUpgrade)
     */
    FIRMWARE_CLIENT(80, AS300ObisCodeProvider.AssociationLnFirmwareClient);

    /**
     * The clientId for this association
     */
    private final int clientId;
    /**
     * The obiscode for the {@link com.energyict.dlms.cosem.AssociationLN} object
     */
    private final ObisCode obiscode;
    /**
     * Map containig all the possible instances
     */
    private static Map<Integer, ObisCode> instances;

    /**
     * Private constructor
     *
     * @param clientId                      the ID of the client
     * @param associationLnManagementClient the ObisCode of this clients' associationLN object
     */
    AssociationLnObisCodes(int clientId, ObisCode associationLnManagementClient) {
        this.clientId = clientId;
        this.obiscode = associationLnManagementClient;
        getInstances().put(this.clientId, this.obiscode);
    }

    /**
     * Return the obisCode of the {@link com.energyict.dlms.cosem.AssociationLN} object for the given {@link #clientId}
     *
     * @param clientId the ID of the client
     * @return the requested ObisCode
     */
    public static ObisCode forClient(int clientId) {
        ObisCode oc = getInstances().get(clientId);
        return oc != null ? oc : null;
    }

    /**
     * @return an instance of all the clients
     */
    private static Map<Integer, ObisCode> getInstances() {
        if (instances == null) {
            instances = new HashMap<Integer, ObisCode>(5);
        }
        return instances;
    }

    public int getClientId() {
        return this.clientId;
    }
}
