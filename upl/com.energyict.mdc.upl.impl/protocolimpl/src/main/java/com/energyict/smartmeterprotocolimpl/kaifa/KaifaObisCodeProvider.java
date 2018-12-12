package com.energyict.smartmeterprotocolimpl.kaifa;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.generic.CommonObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.MultipleClientRelatedObisCodes;


public class KaifaObisCodeProvider implements CommonObisCodeProvider {

    public static final ObisCode ClockObisCode = ObisCode.fromString("0.0.1.0.0.255");
    public static final ObisCode ClockSynchronizationObisCode = ObisCode.fromString("0.0.96.2.12.255");
    public static final ObisCode SerialNumberObisCode = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode LoadProfileP1 = ObisCode.fromString("0.0.99.1.0.255");
    public static final ObisCode LoadProfileP2 = ObisCode.fromString("0.0.99.1.1.255");
    public static final ObisCode LoadProfileP3 = ObisCode.fromString("0.0.99.1.2.255");
    public static final ObisCode LoadProfileStatusP1 = ObisCode.fromString("1.0.96.10.1.255");
    public static final ObisCode LoadProfileStatusP2 = ObisCode.fromString("1.0.96.10.2.255");

    /**
     * @return the {@link #ClockObisCode} for the {@link com.energyict.dlms.cosem.Clock}
     */
    public ObisCode getClockObisCode() {
        return ClockObisCode;
    }
    
    /**
     * @return the obisCode for the <i>default</i> {@link com.energyict.dlms.cosem.ProfileGeneric}
     */
    public ObisCode getDefaultLoadProfileObisCode() {
        return LoadProfileP1;
    }

    public ObisCode getDailyLoadProfileObisCode() {
        return null;
    }

    /**
     * @return the {@link #ClockSynchronizationObisCode} for the ClockSynchronization
     */
    public ObisCode getClockSynchronization() {
        return ClockSynchronizationObisCode;
    }

    /**
     * @return the {@link #SerialNumberObisCode} for the SerialNumber
     */
    public ObisCode getSerialNumberObisCode() {
        return SerialNumberObisCode;
    }

    /**
     * We return the Association according to the Management Client (clientId = 1)
     *
     * @return the obisCode for the AssociationLn object
     */
    public ObisCode getAssociationLnObisCode() {
        return getAssociationLnObisCode(MultipleClientRelatedObisCodes.MANAGEMENT_CLIENT.getClientId());
    }

    public ObisCode getFirmwareVersion() {
        return null;
    }

    /**
     * @param clientId the used clientId for this association
     * @return the {@link com.energyict.smartmeterprotocolimpl.eict.ukhub.common.MultipleClientRelatedObisCodes#frameCounterObisCode}
     */
    public ObisCode getFrameCounterObisCode(int clientId){
        return MultipleClientRelatedObisCodes.frameCounterForClient(clientId);
    }

    /**
     * @param clientId the used clientId for this association
     * @return the {@link com.energyict.smartmeterprotocolimpl.eict.ukhub.common.MultipleClientRelatedObisCodes#associationObisCode} for the {@link com.energyict.dlms.cosem.AssociationLN} object
     */
    public ObisCode getAssociationLnObisCode(int clientId) {
        return MultipleClientRelatedObisCodes.associationLNForClient(clientId);
    }


}