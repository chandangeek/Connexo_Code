/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.eict.ukhub.common;

import com.energyict.mdc.common.ObisCode;

import com.energyict.smartmeterprotocolimpl.elster.apollo.AS300ObisCodeProvider;

/**
 * Enumeration containing the possible {@link com.energyict.dlms.cosem.AssociationLN} ObisCodes and FrameCounter ObisCodes.
 * Each clientID has a different AssociationObject and FrameCounter object.
 */
public enum MultipleClientRelatedObisCodes {

    /**
     * The current_client
     */
    MANUFACTURING_CLIENT(0, AS300ObisCodeProvider.AssociationLnManufactureClient, AS300ObisCodeProvider.FrameCounterManufactureClient),
    /**
    * The public client has limited read access
    */
    PUBLIC_CLIENT(16, AS300ObisCodeProvider.AssociationLnPublicClient, AS300ObisCodeProvider.FrameCounterPublicClient),
    /**
    * The reading client has reading access
    */
    DATACOLLECTION_CLIENT(32, AS300ObisCodeProvider.AssociationLnDataCollectionClient, AS300ObisCodeProvider.FrameCounterDataCollectionClient),
    /**
    * The reading client has reading access
    */
    EXT_DATACOLLECTION_CLIENT(48, AS300ObisCodeProvider.AssociationLnExtDataCollectionClient, AS300ObisCodeProvider.FrameCounterExtDataCollectionClient),
    /**
     * The management client has all access
     */
    MANAGEMENT_CLIENT(64, AS300ObisCodeProvider.AssociationLnManagementClient, AS300ObisCodeProvider.FrameCounterManagementClient),
    /**
     * The firmware client has access to firmware related objects (ex. FirmwareUpgrade)
     */
    FIRMWARE_CLIENT(80, AS300ObisCodeProvider.AssociationLnFirmwareClient, AS300ObisCodeProvider.FrameCounterFirmwareClient);

    /**
     * The clientId for this association
     */
    private final int clientId;
    /**
     * The obiscode for the {@link com.energyict.dlms.cosem.AssociationLN} object
     */
    private final ObisCode associationObisCode;

    private final ObisCode frameCounterObisCode;


    /**
     * Private constructor
     *
     * @param clientId                      the ID of the client
     * @param associationLnManagementClient the ObisCode of this clients' associationLN object
     * @param frameCounterObisCode
     */
    MultipleClientRelatedObisCodes(int clientId, ObisCode associationLnManagementClient, final ObisCode frameCounterObisCode) {
        this.clientId = clientId;
        this.associationObisCode = associationLnManagementClient;
        this.frameCounterObisCode = frameCounterObisCode;
    }

    /**
     * Return the obisCode of the {@link com.energyict.dlms.cosem.AssociationLN} object for the given {@link #clientId}
     *
     * @param clientId the ID of the client
     * @return the requested ObisCode
     */
    public static ObisCode associationLNForClient(int clientId) {
        for (MultipleClientRelatedObisCodes multipleClientRelatedObisCodes : values()) {
            if(multipleClientRelatedObisCodes.getClientId() == clientId){
                return multipleClientRelatedObisCodes.getAssociationLnObisCode();
            }
        }
        return null;
    }

    public static ObisCode frameCounterForClient(int clientId){
        for (MultipleClientRelatedObisCodes multipleClientRelatedObisCodes : values()) {
            if(multipleClientRelatedObisCodes.getClientId() == clientId){
                return multipleClientRelatedObisCodes.getFrameCounterObisCode();
            }
        }
        return null;
    }

    public ObisCode getAssociationLnObisCode() {
        return this.associationObisCode;
    }

    public ObisCode getFrameCounterObisCode(){
        return this.frameCounterObisCode;
    }

    public int getClientId() {
        return this.clientId;
    }

}
