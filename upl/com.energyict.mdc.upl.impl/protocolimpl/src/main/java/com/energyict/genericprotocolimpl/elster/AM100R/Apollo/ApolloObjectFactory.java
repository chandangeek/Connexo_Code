package com.energyict.genericprotocolimpl.elster.AM100R.Apollo;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;

import java.util.HashMap;
import java.util.Map;

/**
 * The ObjectFactory provides and serves all objects required for the protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 26-nov-2010
 * Time: 11:28:56
 */
public class ApolloObjectFactory {

    /**
     * The used {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ObisCodeProvider}
     */
    private ObisCodeProvider obisCodeProvider;
    /**
     * A map containing all objects that have been read for the given {@link #meterProtocol}
     */
    private Map<ObisCode, AbstractCosemObject> objectMap = new HashMap<ObisCode, AbstractCosemObject>();
    /**
     * The {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloMeter} that uses this objectFactory
     */
    private final ProtocolLink meterProtocol;

    public ApolloObjectFactory(ProtocolLink meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Getter for the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ObisCodeProvider}
     *
     * @return the {@link #obisCodeProvider}
     */
    public ObisCodeProvider getObisCodeProvider() {
        if (this.obisCodeProvider == null) {
            this.obisCodeProvider = new ObisCodeProvider();
        }
        return this.obisCodeProvider;
    }

    /**
     * Getter for the DLMS {@link com.energyict.dlms.cosem.Clock}
     *
     * @return the DLMS clock
     */
    public Clock getClock() {
        if (!objectMap.containsKey(getObisCodeProvider().getClockObisCode())) {
            Clock clock = new Clock(this.meterProtocol, new ObjectReference(getObisCodeProvider().getClockObisCode().getLN()));
            objectMap.put(getObisCodeProvider().getClockObisCode(), clock);
        }
        return (Clock) objectMap.get(getObisCodeProvider().getClockObisCode());
    }

    /**
     * Getter for a requested {@link com.energyict.dlms.cosem.Data} object
     *
     * @param obisCode the {@link com.energyict.obis.ObisCode} of the object
     * @return the requested {@link com.energyict.dlms.cosem.Data} object
     */
    public Data getData(ObisCode obisCode) {
        if (!objectMap.containsKey(obisCode)) {
            Data data = new Data(this.meterProtocol, new ObjectReference(obisCode.getLN()));
            objectMap.put(obisCode, data);
        }
        return (Data) objectMap.get(obisCode);
    }

    /**
     * Getter for the requested {@link com.energyict.dlms.cosem.Register} object
     *
     * @param obisCode the {@link com.energyict.obis.ObisCode} of the object
     * @return the requested {@link com.energyict.dlms.cosem.Register} object
     */
    public Register getRegister(ObisCode obisCode) {
        if (!objectMap.containsKey(obisCode)) {
            Register register = new Register(this.meterProtocol, new ObjectReference(obisCode.getLN()));
            objectMap.put(obisCode, register);
        }
        return (Register) objectMap.get(obisCode);
    }

    /**
     * Getter for the requested {@link com.energyict.dlms.cosem.DemandRegister} object
     *
     * @param obisCode the {@link com.energyict.obis.ObisCode} of the object
     * @return the requested {@link com.energyict.dlms.cosem.DemandRegister} object
     */
    public DemandRegister getDemandRegister(ObisCode obisCode) {
        if (!objectMap.containsKey(obisCode)) {
            DemandRegister register = new DemandRegister(this.meterProtocol, new ObjectReference(obisCode.getLN()));
            objectMap.put(obisCode, register);
        }
        return (DemandRegister) objectMap.get(obisCode);
    }

    /**
     * Getter for the requested {@link com.energyict.dlms.cosem.ExtendedRegister} object
     *
     * @param obisCode the {@link com.energyict.obis.ObisCode} of the object
     * @return the requested {@link com.energyict.dlms.cosem.ExtendedRegister} object
     */
    public ExtendedRegister getExtendedRegister(ObisCode obisCode) {
        if (!objectMap.containsKey(obisCode)) {
            ExtendedRegister register = new ExtendedRegister(this.meterProtocol, new ObjectReference(obisCode.getLN()));
            objectMap.put(obisCode, register);
        }
        return (ExtendedRegister) objectMap.get(obisCode);
    }

    /**
     * Getter for a {@link com.energyict.dlms.cosem.ProfileGeneric} object
     *
     * @param obisCode the obisCode of the object
     * @return the requested object
     */
    public ProfileGeneric getGenericProfileObject(ObisCode obisCode) {
        if (!objectMap.containsKey(obisCode)) {
            ProfileGeneric profile = new ProfileGeneric(this.meterProtocol, new ObjectReference(obisCode.getLN()));
            objectMap.put(obisCode, profile);
        }
        return (ProfileGeneric) objectMap.get(obisCode);
    }

    /**
     * Getter for a generic {@link com.energyict.dlms.cosem.CosemObject}
     *
     * @param obisCode the {@link com.energyict.obis.ObisCode} of the object
     * @param classId  the classId of the object (currently 1, 3, 4 and 5 are supported)
     * @return the requested {@link com.energyict.dlms.cosem.CosemObject}
     * @throws ProtocolException if a non-implemented object is requested
     */
    public CosemObject getCosemObject(ObisCode obisCode, int classId) throws ProtocolException {
        if (!objectMap.containsKey(obisCode)) {
            if (classId == DLMSClassId.REGISTER.getClassId()) {
                return getRegister(obisCode);
            } else if (classId == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                return getExtendedRegister(obisCode);
            } else if (classId == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                return getDemandRegister(obisCode);
            } else if (classId == DLMSClassId.DATA.getClassId()) {
                return getData(obisCode);
            }
        }
        throw new ProtocolException("Can not convert to an AbstractCosemObject...");
    }

    /**
     * Getter for the SerialNumber of the device
     *
     * @return a {@link com.energyict.dlms.cosem.Data} object which represents the serialNumber
     */
    public Data getSerialNumber() {
        return getData(getObisCodeProvider().getSerialNumberObisCode());
    }

    /**
     * Getter for the default {@link com.energyict.dlms.cosem.ProfileGeneric} object
     *
     * @return the default GenericProfile object
     */
    public ProfileGeneric getDefaultProfile() {
        return getGenericProfileObject(getObisCodeProvider().getDefaultLoadProfileObisCode());
    }

    /**
     * Getter for the requested {@link com.energyict.dlms.cosem.AssociationLN} object
     *
     * @param clientId the clientMacAddress of this current association
     * @return the requested {@link com.energyict.dlms.cosem.AssociationLN}
     */
    public AssociationLN getAssociationLnObject(int clientId) {
        if (!objectMap.containsKey(getObisCodeProvider().getAssociationLnObisCode(clientId))) {
            AssociationLN aln = new AssociationLN(this.meterProtocol, new ObjectReference(getObisCodeProvider().getAssociationLnObisCode(clientId).getLN()));
            objectMap.put(getObisCodeProvider().getAssociationLnObisCode(clientId), aln);
        }
        return (AssociationLN) objectMap.get(getObisCodeProvider().getAssociationLnObisCode(clientId));
    }

    /**
     * Getter for the FirmwareVersion {@link com.energyict.dlms.cosem.Data} object
     *
     * @return the firmware version object
     */
    public Data getFirmwareVersion() {
        return getData(getObisCodeProvider().getFirmwareVersion());
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.ProfileGeneric} object which contains the instant Energy Values
     *
     * @return the instant energy value profile object
     */
    public ProfileGeneric getInstantaneousEnergyProfile() {
        return getGenericProfileObject(getObisCodeProvider().getInstantaneousEnergyValueObisCode());
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.ProfileGeneric} object which contains the Standard Events
     *
     * @return the standard event profile object
     */
    public ProfileGeneric getStandardEventLog() {
        return getGenericProfileObject(getObisCodeProvider().getStandardEventLogObisCode());
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.ProfileGeneric} object which contains the Fraud Detection event log
     *
     * @return the fraud detection event profile object
     */
    public ProfileGeneric getFraudDetectionEventLog() {
        return getGenericProfileObject(getObisCodeProvider().getFraudDetectionEventLogObisCode());
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.ProfileGeneric} object which contains the PowerQuality event log
     *
     * @return the power quality event profile object
     */
    public ProfileGeneric getPowerQualityEventLog() {
        return getGenericProfileObject(getObisCodeProvider().getPowerQualityEventLogObisCode());
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.ProfileGeneric} object which contains the DemandManagement event log
     *
     * @return the demand management event profile object
     */
    public ProfileGeneric getDemandManagementEventLog() {
        return getGenericProfileObject(getObisCodeProvider().getDemandManagementEventLog());
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.ProfileGeneric} object which contains the Common event log
     *
     * @return the common event profile object
     */
    public ProfileGeneric getCommonEventLog() {
        return getGenericProfileObject(getObisCodeProvider().getCommonEventLog());
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.ProfileGeneric} object which contains the PowerContract event log
     *
     * @return the power contract event profile object
     */
    public ProfileGeneric getPowerContractEventLog() {
        return getGenericProfileObject(getObisCodeProvider().getPowerContractEventLog());
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.ProfileGeneric} object which contains the Firmware event log
     *
     * @return the firmware event profile object
     */
    public ProfileGeneric getFirmwareEventLog() {
        return getGenericProfileObject(getObisCodeProvider().getFirmwareEventLog());
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.ProfileGeneric} object which contains the Object Synchronization event log
     *
     * @return the object synchronization event profile object
     */
    public ProfileGeneric getObjectSynchronizationEventLog() {
        return getGenericProfileObject(getObisCodeProvider().getObjectSynchronizationEventLog());
    }
}
