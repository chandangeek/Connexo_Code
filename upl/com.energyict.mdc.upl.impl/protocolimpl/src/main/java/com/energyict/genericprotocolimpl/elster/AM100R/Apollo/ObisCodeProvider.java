package com.energyict.genericprotocolimpl.elster.AM100R.Apollo;

import com.energyict.genericprotocolimpl.common.CommonObisCodeProvider;
import com.energyict.obis.ObisCode;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Straighforward summary of the possible Obiscodes of the Apollo meter
 * </p>
 * <p>
 * Copyrights EnergyICT<br/>
 * Date: 23-nov-2010<br/>
 * Time: 16:32:14<br/>
 * </p>
 */
public class ObisCodeProvider implements CommonObisCodeProvider {

    public static final ObisCode clockObisCode = ObisCode.fromString("0.0.1.0.0.255");
    public static final ObisCode clockSynchronizationObisCode = ObisCode.fromString("0.0.96.2.12.255");
    public static final ObisCode serialNumberObisCode = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode loadProfileP1 = ObisCode.fromString("1.0.99.1.0.255");
    public static final ObisCode loadProfileP2 = ObisCode.fromString("1.0.99.2.0.255");
    public static final ObisCode loadProfileMonthly = ObisCode.fromString("0.0.98.1.1.255");
    public static final ObisCode associationLnCurrentClient = ObisCode.fromString("0.0.40.0.0.255");
    public static final ObisCode associationLnPublicClient = ObisCode.fromString("0.0.40.0.1.255");
    public static final ObisCode associationLnReadingClient = ObisCode.fromString("0.0.40.0.2.255");
    public static final ObisCode associationLnManagementClient = ObisCode.fromString("0.0.40.0.3.255");
    public static final ObisCode associationLnFirmwareClient = ObisCode.fromString("0.0.40.0.4.255");
    public static final ObisCode firmwareVersionObisCode = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode instantaneousEnergyValuesObisCode = ObisCode.fromString("0.0.21.0.6.255");


    /**
     * @return the {@link #clockObisCode} for the {@link com.energyict.dlms.cosem.Clock}
     */
    public ObisCode getClockObisCode() {
        return clockObisCode;
    }

    /**
     * @return the obisCode for the <i>default</i> {@link com.energyict.dlms.cosem.ProfileGeneric}
     */
    public ObisCode getDefaultLoadProfileObisCode() {
//        return loadProfileP1;
        return loadProfileMonthly;
    }

    /**
     * @return the {@link #clockSynchronizationObisCode} for the ClockSynchronization
     */
    public ObisCode getClockSynchronization() {
        return clockSynchronizationObisCode;
    }

    /**
     * @return the {@link #serialNumberObisCode} for the SerialNumber
     */
    public ObisCode getSerialNumberObisCode() {
        return serialNumberObisCode;
    }

    /**
     * We return the Association according to the Management Client (clientId = 1)
     *
     * @return the obisCode for the AssociationLn object
     */
    public ObisCode getAssociationLnObisCode() {
        return getAssociationLnObisCode(1);
    }

    /**
     * @param clientId the used clientId for this association
     * @return the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ObisCodeProvider.AssociationLnObisCodes#obiscode} for the {@link com.energyict.dlms.cosem.AssociationLN} object
     */
    public ObisCode getAssociationLnObisCode(int clientId) {
        return AssociationLnObisCodes.forClient(clientId);
    }

    /**
     * @return the {@link #firmwareVersionObisCode} of this Firmware Version
     */
    public ObisCode getFirmwareVersion() {
        return firmwareVersionObisCode;
    }

    /**
     * @return the {@link #instantaneousEnergyValuesObisCode} of the Instantaneous Values Profile
     */
    public ObisCode getInstantaneousEnergyValueObisCode(){
        return instantaneousEnergyValuesObisCode;
    }

    /**
     * Enumeration containing the possible {@link com.energyict.dlms.cosem.AssociationLN} Obiscodes.
     * Each clientID has a different AssociationObject.
     */
    enum AssociationLnObisCodes {

        /**
         * The current_client
         */
        CURRENT_CLIENT(0, associationLnCurrentClient),
        /**
         * The management client has all access
         */
        MANAGEMENT_CLIENT(1, associationLnManagementClient),
        /**
         * The public client has limited read access
         */
        PUBLIC_CLIENT(16, associationLnPublicClient),
        /**
         * The reading client has reading access
         */
        READING_CLIENT(2, associationLnReadingClient),
        /**
         * The firmware client has access to firmware related objects (ex. FirmwareUpgrade)
         */
        FIRMARE_CLIENT(3, associationLnFirmwareClient);

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
        private AssociationLnObisCodes(int clientId, ObisCode associationLnManagementClient) {
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
    }
}
