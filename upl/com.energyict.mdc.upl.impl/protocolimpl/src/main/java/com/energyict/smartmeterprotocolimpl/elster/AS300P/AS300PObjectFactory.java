package com.energyict.smartmeterprotocolimpl.elster.AS300P;

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
public class AS300PObjectFactory {

    /**
     * The used {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ObisCodeProvider}
     */
    private AS300PObisCodeProvider obisCodeProvider;
    /**
     * A map containing all objects that have been read for the given {@link #meterProtocol}
     */
    private Map<ObisCode, AbstractCosemObject> objectMap = new HashMap<ObisCode, AbstractCosemObject>();
    /**
     * The {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloMeter} that uses this objectFactory
     */
    private final ProtocolLink meterProtocol;

    public AS300PObjectFactory(ProtocolLink meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Getter for the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ObisCodeProvider}
     *
     * @return the {@link #obisCodeProvider}
     */
    public AS300PObisCodeProvider getObisCodeProvider() {
        if (this.obisCodeProvider == null) {
            this.obisCodeProvider = new AS300PObisCodeProvider();
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
     * @throws com.energyict.protocol.ProtocolException if a non-implemented object is requested
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
     * Getter for the Daily {@link com.energyict.dlms.cosem.ProfileGeneric} object
     *
     * @return the daily GenericProfile object
     */
    public ProfileGeneric getDailyProfile() {
        return getGenericProfileObject(getObisCodeProvider().getDailyLoadProfileObisCode());
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

    public Data getActiveFirmwareIdACOR() {
        return getData(getObisCodeProvider().getActiveLongFirmwareIdentifierACOR());
    }

    public Data getActiveFirmwareIdMCOR() {
        return getData(getObisCodeProvider().getActiveLongFirmwareIdentifierMCOR());
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
     * Getter for the {@link com.energyict.dlms.cosem.ActivityCalendar} object according to contract 1
     *
     * @return the requested ActivityCalendar
     */
    public ActivityCalendar getActivityCalendar() {
        if (!objectMap.containsKey(getObisCodeProvider().getActivityCalendarObisCode())) {
            ActivityCalendar activityCalendar = new ActivityCalendar(this.meterProtocol, new ObjectReference(getObisCodeProvider().getActivityCalendarObisCode().getLN()));
            objectMap.put(getObisCodeProvider().getActivityCalendarObisCode(), activityCalendar);
        }
        return (ActivityCalendar) objectMap.get(getObisCodeProvider().getActivityCalendarObisCode());
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.SpecialDaysTable} object according to contract 1
     *
     * @return the requested SpecialDaysTable
     */
    public SpecialDaysTable getSpecialDayTable() {
        if (!objectMap.containsKey(getObisCodeProvider().getActiveSpecialDayObisCode())) {
            SpecialDaysTable specialDayTable = new SpecialDaysTable(this.meterProtocol, new ObjectReference(getObisCodeProvider().getActiveSpecialDayObisCode().getLN()));
            this.objectMap.put(getObisCodeProvider().getActiveSpecialDayObisCode(), specialDayTable);
        }
        return (SpecialDaysTable) objectMap.get(getObisCodeProvider().getActiveSpecialDayObisCode());
    }

    /**
     * Getter for the <i>Passive</i> {@link com.energyict.dlms.cosem.SpecialDaysTable} according to contract 1
     *
     * @return the requested SpecialDaysTable
     */
    public SpecialDaysTable getPassiveSpecialDaysTable() {
        if (!objectMap.containsKey(getObisCodeProvider().getPassiveSpecialDayObisCode())) {
            SpecialDaysTable specialDayTable = new SpecialDaysTable(this.meterProtocol, new ObjectReference(getObisCodeProvider().getPassiveSpecialDayObisCode().getLN()));
            this.objectMap.put(getObisCodeProvider().getPassiveSpecialDayObisCode(), specialDayTable);
        }
        return (SpecialDaysTable) objectMap.get(getObisCodeProvider().getPassiveSpecialDayObisCode());
    }

    /**
     * Get the passive script table, used for TOU switches
     *
     * @return the ScriptTable linked to the passive TOU
     */
    public ScriptTable getScriptTablePassive() {
        if (!objectMap.containsKey(getObisCodeProvider().getScriptTablePassiveObisCode())) {
            ObisCode obisCode = getObisCodeProvider().getScriptTablePassiveObisCode();
            this.objectMap.put(obisCode, new ScriptTable(this.meterProtocol, new ObjectReference(obisCode.getLN())));
        }
        return (ScriptTable) objectMap.get(getObisCodeProvider().getScriptTablePassiveObisCode());
    }

    /**
     * Get the active script table, used for TOU switches
     *
     * @return the ScriptTable linked to the TOU
     */
    public ScriptTable getScriptTable() {
        if (!objectMap.containsKey(getObisCodeProvider().getScriptTableObisCode())) {
            ObisCode obisCode = getObisCodeProvider().getScriptTableObisCode();
            this.objectMap.put(obisCode, new ScriptTable(this.meterProtocol, new ObjectReference(obisCode.getLN())));
        }
        return (ScriptTable) objectMap.get(getObisCodeProvider().getScriptTableObisCode());
    }

    public ProfileGeneric getStandardEventLog() {
        if (!objectMap.containsKey(getObisCodeProvider().getStandardEventLogObisCode())) {
            ObisCode obisCode = getObisCodeProvider().getStandardEventLogObisCode();
            this.objectMap.put(obisCode, new ProfileGeneric(this.meterProtocol, new ObjectReference(obisCode.getLN())));
        }
        return (ProfileGeneric) objectMap.get(getObisCodeProvider().getStandardEventLogObisCode());
    }

    public ProfileGeneric getPowerQualityEventLog() {
        if (!objectMap.containsKey(getObisCodeProvider().getPowerQualityEventLogObisCode())) {
            ObisCode obisCode = getObisCodeProvider().getPowerQualityEventLogObisCode();
            this.objectMap.put(obisCode, new ProfileGeneric(this.meterProtocol, new ObjectReference(obisCode.getLN())));
        }
        return (ProfileGeneric) objectMap.get(getObisCodeProvider().getPowerQualityEventLogObisCode());
    }

    public ProfileGeneric getPowerFailureEventLog() {
        if (!objectMap.containsKey(getObisCodeProvider().getPowerFailureEventLogObisCode())) {
            ObisCode obisCode = getObisCodeProvider().getPowerFailureEventLogObisCode();
            this.objectMap.put(obisCode, new ProfileGeneric(this.meterProtocol, new ObjectReference(obisCode.getLN())));
        }
        return (ProfileGeneric) objectMap.get(getObisCodeProvider().getPowerFailureEventLogObisCode());
    }

    public ProfileGeneric getFraudDetectionEventLog() {
        if (!objectMap.containsKey(getObisCodeProvider().getFraudDetectionEventLogObisCode())) {
            ObisCode obisCode = getObisCodeProvider().getFraudDetectionEventLogObisCode();
            this.objectMap.put(obisCode, new ProfileGeneric(this.meterProtocol, new ObjectReference(obisCode.getLN())));
        }
        return (ProfileGeneric) objectMap.get(getObisCodeProvider().getFraudDetectionEventLogObisCode());
    }

    public ProfileGeneric getCommonEventLog() {
        if (!objectMap.containsKey(getObisCodeProvider().getCommonEventLogObisCode())) {
            ObisCode obisCode = getObisCodeProvider().getCommonEventLogObisCode();
            this.objectMap.put(obisCode, new ProfileGeneric(this.meterProtocol, new ObjectReference(obisCode.getLN())));
        }
        return (ProfileGeneric) objectMap.get(getObisCodeProvider().getCommonEventLogObisCode());
    }

    public ProfileGeneric getFirmwareEventLog() {
        if (!objectMap.containsKey(getObisCodeProvider().getFirmwareEventLogObisCode())) {
            ObisCode obisCode = getObisCodeProvider().getFirmwareEventLogObisCode();
            this.objectMap.put(obisCode, new ProfileGeneric(this.meterProtocol, new ObjectReference(obisCode.getLN())));
        }
        return (ProfileGeneric) objectMap.get(getObisCodeProvider().getFirmwareEventLogObisCode());
    }

    public ProfileGeneric getDisconnectControlLog() {
        if (!objectMap.containsKey(getObisCodeProvider().getDisconnectControlLogObisCode())) {
            ObisCode obisCode = getObisCodeProvider().getDisconnectControlLogObisCode();
            this.objectMap.put(obisCode, new ProfileGeneric(this.meterProtocol, new ObjectReference(obisCode.getLN())));
        }
        return (ProfileGeneric) objectMap.get(getObisCodeProvider().getDisconnectControlLogObisCode());
    }
}
