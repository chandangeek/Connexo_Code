/*
 * CosemObjectFactory.java
 *
 * Created on 18 augustus 2004, 9:26
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.*;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class CosemObjectFactory implements DLMSCOSEMGlobals {

    private ProtocolLink protocolLink;
    private StoredValues storedValues=null; // cached
    private LoadProfile loadProfile=null; // cached
    private SAPAssignment sapAssignment = null; // cached
    private boolean useGetWithList;

    /** Creates a new instance of CosemObjectFactory */
    public CosemObjectFactory(ProtocolLink protocolLink) {
        this(protocolLink, false);
    }

    public CosemObjectFactory(ProtocolLink protocolLink, boolean useGetWithList) {
        this.protocolLink=protocolLink;
        this.useGetWithList = useGetWithList;
    }

    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    public SAPAssignment getSAPAssignment() {
        if (sapAssignment == null) {
            sapAssignment = new SAPAssignment(protocolLink);
        }
        return sapAssignment;
    }

    public StoredValues getStoredValues() throws IOException {
        if (storedValues==null) {
            storedValues = protocolLink.getStoredValues();
            storedValues.retrieve();
        }
        return storedValues;
    }

    public LoadProfile getLoadProfile() throws IOException {
        if (loadProfile==null) {
            loadProfile = new LoadProfile(this);
            loadProfile.retrieve();
        }
        return loadProfile;
    }

    public Clock getClock() throws IOException {
        return new Clock(protocolLink,getObjectReference(CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
    }

    public Clock getClock(ObisCode obisCode) throws IOException {
        return new Clock(protocolLink,getObjectReference(obisCode));
    }

    public GenericRead getGenericRead(int baseObject, int snAttr) {
        return new GenericRead(protocolLink,new ObjectReference(baseObject),snAttr);
    }

    public GenericRead getGenericRead(UniversalObject uo) throws IOException {
        return getGenericRead(uo.getObisCode(),uo.getValueAttributeOffset(),uo.getClassID());
    }

    public GenericRead getGenericRead(DLMSAttribute attribute) throws IOException {
        return getGenericRead(attribute.getObisCode(), attribute.getAttribute(), attribute.getClassId());
    }

    public GenericRead getGenericRead(ObisCode obisCode, int snAttr) throws IOException {
        return getGenericRead(obisCode,snAttr,-1);
    }

    public GenericRead getGenericRead(ObisCode obisCode, int snAttr, int classId) throws IOException {
        return new GenericRead(protocolLink,getObjectReference(obisCode,classId),snAttr);
    }

    public SMTPSetup getSMTPSetup(ObisCode obisCode) throws IOException {
        return new SMTPSetup(protocolLink, getObjectReference(obisCode));
    }
    public ActivityCalendar getActivityCalendar(ObisCode obisCode) throws IOException {
        return new ActivityCalendar(protocolLink, getObjectReference(obisCode));
    }
    public SpecialDaysTable getSpecialDaysTable(ObisCode obisCode) throws IOException {
        return new SpecialDaysTable(protocolLink, getObjectReference(obisCode));
    }
    public ScriptTable getScriptTable(ObisCode obisCode) throws IOException {
        return new ScriptTable(protocolLink, getObjectReference(obisCode));
    }

    public ScriptTable getGlobalMeterResetScriptTable() throws IOException {
    	return new ScriptTable(protocolLink, ScriptTable.LN_GLOBAL_METER_RESET);
    }
    public ScriptTable getTarifficationScriptTable() throws IOException {
    	return new ScriptTable(protocolLink, ScriptTable.LN_TARIFFICATION_SCRIPT_TABLE);
    }
    public ScriptTable getDisconnectControlScriptTable() throws IOException {
    	return new ScriptTable(protocolLink, ScriptTable.LN_DISCONNECT_CONTROL);
    }
    public ScriptTable getImageActivationScriptTable() throws IOException {
    	return new ScriptTable(protocolLink, ScriptTable.LN_IMAGE_ACTIVATION);
    }

    public RegisterMonitor getRegisterMonitor(ObisCode obisCode) throws IOException {
        return new RegisterMonitor(protocolLink, getObjectReference(obisCode));
    }

    public SingleActionSchedule getSingleActionSchedule(ObisCode obisCode) throws IOException{
    	return new SingleActionSchedule(protocolLink, getObjectReference(obisCode));
    }

    public void writeObject(ObisCode obisCode, int classId, int attrId, byte[] data) throws IOException {
        GenericWrite gw = new GenericWrite(protocolLink,new ObjectReference(obisCode.getLN(),classId), attrId);
        gw.write(data);
    }

    public GenericWrite getGenericWrite(int baseObject,int attr) {
        return new GenericWrite(protocolLink,new ObjectReference(baseObject),attr);
    }
    public GenericWrite getGenericWrite(UniversalObject uo) throws IOException {
        return getGenericWrite(uo.getObisCode(),uo.getValueAttributeOffset(),uo.getClassID());
    }
    public GenericWrite getGenericWrite(ObisCode obisCode,int attr) throws IOException {
        return getGenericWrite(obisCode,attr,-1);
    }
    public GenericWrite getGenericWrite(ObisCode obisCode,int attr, int classId) throws IOException {
        return new GenericWrite(protocolLink,getObjectReference(obisCode,classId),attr);
    }

    public GenericInvoke getGenericInvoke(int baseObject, int method){
    	return new GenericInvoke(protocolLink, new ObjectReference(baseObject), method);
    }
    public GenericInvoke getGenericInvoke(ObisCode obisCode, int classId, int method) throws IOException{
    	return new GenericInvoke(protocolLink, getObjectReference(obisCode, classId), method);
    }

    public ProfileGeneric getProfileGeneric(ObisCode obisCode) throws IOException {
        return new ProfileGeneric(protocolLink,getObjectReference(obisCode));
    }

    public ProfileGeneric getProfileGeneric(int shortNameReference) throws IOException {
        return new ProfileGeneric(protocolLink,new ObjectReference(shortNameReference));
    }

    public Register getRegister(int baseObject) {
        return new Register(protocolLink,new ObjectReference(baseObject));
    }
    public Register getRegister(ObisCode obisCode) throws IOException {
        return new Register(protocolLink,getObjectReference(obisCode));
    }

    public ExtendedRegister getExtendedRegister(int baseObject) {
        return new ExtendedRegister(protocolLink,new ObjectReference(baseObject));
    }

    public ExtendedRegister getExtendedRegister(ObisCode obisCode) throws IOException {
        return new ExtendedRegister(protocolLink,getObjectReference(obisCode));
    }

    public Data getData(ObisCode obisCode) throws IOException {
        return new Data(protocolLink,getObjectReference(obisCode));
    }

    public Data getData(int baseObject) throws IOException {
        return new Data(protocolLink,new ObjectReference(baseObject));
    }

    public DemandRegister getDemandRegister(ObisCode obisCode) throws IOException {
        return new DemandRegister(protocolLink,getObjectReference(obisCode));
    }

    public AssociationLN getAssociationLN() {
        return new AssociationLN(protocolLink,new ObjectReference(ASSOC_LN_OBJECT_LN));
    }
    public AssociationSN getAssociationSN() {
        return new AssociationSN(protocolLink,new ObjectReference(ASSOC_SN_OBJECT));
    }

    public IPv4Setup getIPv4Setup() throws IOException {
    	return new IPv4Setup(protocolLink, getObjectReference(IPV4_SETUP, protocolLink.getMeterConfig().getIPv4SetupSN()));
    }

    public IPv4Setup getIPv4Setup(ObisCode obisCode) throws IOException {
    	return new IPv4Setup(protocolLink, getObjectReference(obisCode));
    }

    public P3ImageTransfer getP3ImageTransfer() throws IOException {
    	return new P3ImageTransfer(protocolLink, getObjectReference(P3IMAGE_TRANSFER, protocolLink.getMeterConfig().getP3ImageTransferSN()));
    }

    public P3ImageTransfer getP3ImageTransfer(ObisCode obisCode) throws IOException{
    	return new P3ImageTransfer(protocolLink, getObjectReference(obisCode));
    }

    public Disconnector getDisconnector() throws IOException {
    	return new Disconnector(protocolLink, getObjectReference(DISCONNECTOR, protocolLink.getMeterConfig().getDisconnectorSN()));
    }

    public Disconnector getDisconnector(ObisCode obisCode) throws IOException{
    	return new Disconnector(protocolLink, getObjectReference(obisCode));
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.MBusClient} object according to BlueBook version 9 or below
     *
     * @param obisCode the obisCode of the Object
     * @return a newly created MbusClient object
     * @throws IOException if the {@link com.energyict.dlms.ProtocolLink#getReference()} != (ProtocolLink#LN_REFERENCE || ProtocolLink#SN_REFERENCE)
     * @deprecated use {@link #getMbusClient(com.energyict.obis.ObisCode, int)} instead
     */
	public MBusClient getMbusClient(ObisCode obisCode) throws IOException{
        return getMbusClient(obisCode, MbusClientAttributes.VERSION9);
	}

    /**
     * Getter for the {@link com.energyict.dlms.cosem.MBusClient} object (valid for any DLMS version)
     *
     * @param obisCode the obisCode of the Object
     * @param version  the version of the object (see {@link com.energyict.dlms.cosem.attributes.MbusClientAttributes#version} for more details)
     * @return a newly created MbusClient object
     * @throws IOException if the {@link com.energyict.dlms.ProtocolLink#getReference()} != (ProtocolLink#LN_REFERENCE || ProtocolLink#SN_REFERENCE)
     */
    public MBusClient getMbusClient(ObisCode obisCode, int version) throws IOException {
        return new MBusClient(protocolLink, getObjectReference(obisCode), version);
    }

	public Limiter getLimiter() throws IOException{
		return new Limiter(protocolLink, getObjectReference(LIMITER, protocolLink.getMeterConfig().getLimiterSN()));
	}

	public PPPSetup getPPPSetup() throws IOException{
		return new PPPSetup(protocolLink, getObjectReference(PPPSETUP, protocolLink.getMeterConfig().getPPPSetupSN()));
	}

	public GPRSModemSetup getGPRSModemSetup() throws IOException{
		return new GPRSModemSetup(protocolLink, getObjectReference(GPRSMODEMSETUP, protocolLink.getMeterConfig().getGPRSModemSetupSN()));
	}

	public SFSKPhyMacSetup getSFSKPhyMacSetup() throws IOException {
		return getSFSKPhyMacSetup(SFSKPhyMacSetup.getObisCode());
	}

	public SFSKPhyMacSetup getSFSKPhyMacSetup(ObisCode obisCode) throws IOException {
		return new SFSKPhyMacSetup(protocolLink, getObjectReference(obisCode));
	}

	/**
	 * Getter for the ShortName ImageTransfer Object
	 */
	public SFSKPhyMacSetup getSFSKPhyMacSetupSN() throws IOException {
	    return new SFSKPhyMacSetup(protocolLink, new ObjectReference(protocolLink.getMeterConfig().getSFSKPhyMacSetupSN()));
	}

	public SFSKMacCounters getSFSKMacCounters() throws IOException {
		return getSFSKMacCounters(SFSKMacCounters.getObisCode());
	}

	public SFSKMacCounters getSFSKMacCounters(ObisCode obisCode) throws IOException {
		return new SFSKMacCounters(protocolLink, getObjectReference(obisCode));
	}

	public SFSKIec61334LLCSetup getSFSKIec61334LLCSetup() throws IOException {
		return getSFSKIec61334LLCSetup(SFSKIec61334LLCSetup.getObisCode());
	}

	public SFSKIec61334LLCSetup getSFSKIec61334LLCSetup(ObisCode obisCode) throws IOException {
		return new SFSKIec61334LLCSetup(protocolLink, getObjectReference(obisCode));
	}

	public SFSKActiveInitiator getSFSKActiveInitiator() throws IOException {
		return getSFSKActiveInitiator(SFSKActiveInitiator.getObisCode());
	}

	public SFSKActiveInitiator getSFSKActiveInitiator(ObisCode obisCode) throws IOException {
		return new SFSKActiveInitiator(protocolLink, getObjectReference(obisCode));
	}

	public SFSKSyncTimeouts getSFSKSyncTimeouts() throws IOException {
		return getSFSKSyncTimeouts(SFSKSyncTimeouts.getObisCode());
	}

	public SFSKSyncTimeouts getSFSKSyncTimeouts(ObisCode obisCode) throws IOException {
		return new SFSKSyncTimeouts(protocolLink, getObjectReference(obisCode));
	}

	public MBusSlavePortSetup getMBusSlavePortSetup() throws IOException {
		return getMBusSlavePortSetup(MBusSlavePortSetup.getObisCode());
	}

	public MBusSlavePortSetup getMBusSlavePortSetup(ObisCode obisCode) throws IOException {
		return new MBusSlavePortSetup(protocolLink, getObjectReference(obisCode));
	}

	public TCPUDPSetup getTCPUDPSetup() throws IOException{
		return  new TCPUDPSetup(protocolLink);
	}

	public AutoConnect getAutoConnect() throws IOException {
		return new AutoConnect(protocolLink);
	}

	/**
	 * Getter for the LongName ImageTransfer Object
	 */
	public ImageTransfer getImageTransfer() throws IOException {
		return new ImageTransfer(protocolLink);
	}
	/**
	 * Getter for the ShortName ImageTransfer Object
	 */
	public ImageTransfer getImageTransferSN() throws IOException {
	    return new ImageTransfer(protocolLink, new ObjectReference(protocolLink.getMeterConfig().getImageTransferSN()));
	}

	/**
	 * Getter for the ImageTransfer Object with a given obisCode.
	 * If it is the default you need, then it is advised to use {@link #getImageTransfer()}
	 * or {@link #getImageTransferSN()}
	 */
	public ImageTransfer getImageTransfer(ObisCode obisCode) throws IOException {
		return new ImageTransfer(protocolLink, getObjectReference(obisCode));
	}

	public SecuritySetup getSecuritySetup() throws IOException {
		return new SecuritySetup(protocolLink);
	}
    
    public ZigbeeHanManagement getZigbeeHanManagement() throws IOException {
        return new ZigbeeHanManagement(protocolLink);
    }

    public ZigBeeSETCControl getZigBeeSETCControl() throws IOException {
        return new ZigBeeSETCControl(protocolLink);
    }

    public CosemObject getCosemObject(ObisCode obisCode) throws IOException {
        if (obisCode.getF() != 255) {
            return getStoredValues().getHistoricalValue(obisCode);
        }
        else {
            if (protocolLink.getMeterConfig().getClassId(obisCode) == Register.CLASSID) {
				return new Register(protocolLink,getObjectReference(obisCode));
			} else if (protocolLink.getMeterConfig().getClassId(obisCode) == ExtendedRegister.CLASSID) {
				return new ExtendedRegister(protocolLink,getObjectReference(obisCode));
			} else if (protocolLink.getMeterConfig().getClassId(obisCode) == DLMSClassId.DEMAND_REGISTER.getClassId()) {
				return new DemandRegister(protocolLink,getObjectReference(obisCode));
			} else if (protocolLink.getMeterConfig().getClassId(obisCode) == Data.CLASSID) {
				return new Data(protocolLink,getObjectReference(obisCode));
			} else if (protocolLink.getMeterConfig().getClassId(obisCode) == DLMSClassId.PROFILE_GENERIC.getClassId()) {
				return new ProfileGeneric(protocolLink,getObjectReference(obisCode));
			} else {
				throw new IOException("CosemObjectFactory, getCosemObject, invalid classId "+protocolLink.getMeterConfig().getClassId(obisCode)+" for obisCode "+obisCode.toString()) ;
			}
        }
    }

    /**
     * Create a cosemObject based on the given parameters. Currently Data, Register, ExtendedRegister and DemandRegister are implemented
     *
     * @param oc      the obisCode for the object
     * @param classId the classId for the object
     * @return the newly constructed object
     * @throws IOException if the classId is not supported for this method
     */
    public CosemObject getCosemObjectFromObisAndClassId(ObisCode oc, int classId) throws IOException {
        switch (classId) {
            case 1:
                return new Data(protocolLink, getObjectReference(oc));
            case 3:
                return new Register(protocolLink, getObjectReference(oc));
            case 4:
                return new ExtendedRegister(protocolLink, getObjectReference(oc));
            case 5:
                return new DemandRegister(protocolLink, getObjectReference(oc));
            default:
                throw new IOException("CosemObjectFactory, getCosemObject, invalid classId " + classId + " for obisCode " + oc);
        }
    }

    public ComposedCosemObject getComposedCosemObject(DLMSAttribute... dlmsAttributes) {
        return new ComposedCosemObject(protocolLink, isUseGetWithList(), dlmsAttributes);
    }

    public ComposedCosemObject getComposedCosemObject(List<DLMSAttribute> dlmsAttributes) {
        return new ComposedCosemObject(protocolLink, isUseGetWithList(), dlmsAttributes);
    }

    //*****************************************************************************************
    public ObjectReference getObjectReference(ObisCode obisCode) throws IOException {
        return getObjectReference(obisCode,-1);
    }
    public ObjectReference getObjectReference(ObisCode obisCode, int classId) throws IOException {
        if (protocolLink.getReference() == ProtocolLink.LN_REFERENCE) {
			return new ObjectReference(obisCode.getLN(),classId);
		} else if (protocolLink.getReference() == ProtocolLink.SN_REFERENCE) {
			return new ObjectReference(protocolLink.getMeterConfig().getSN(obisCode));
		}
        throw new IOException("CosemObjectFactory, getObjectReference, invalid reference type "+protocolLink.getReference());
    }
    public ObjectReference getObjectReference(byte[] ln,int sn) throws IOException {
        return getObjectReference(ln,-1,sn);
    }

	public ObjectReference getObjectReference(byte[] ln, int classId, int sn) throws IOException {
		if (protocolLink.getReference() == ProtocolLink.LN_REFERENCE) {
			return new ObjectReference(ln, classId);
		} else if (protocolLink.getReference() == ProtocolLink.SN_REFERENCE) {
			return new ObjectReference(sn);
		}
		throw new IOException("CosemObjectFactory, getObjectReference, invalid reference type " + protocolLink.getReference());
	}

    public boolean isUseGetWithList() {
        return useGetWithList;
    }

}
