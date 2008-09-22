/*
 * CosemObjectFactory.java
 *
 * Created on 18 augustus 2004, 9:26
 */

package com.energyict.dlms.cosem;

import java.io.*;
import java.util.*;

import com.energyict.protocolimpl.dlms.*;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
/**
 *
 * @author  Koen
 */
public class CosemObjectFactory implements DLMSCOSEMGlobals {
    
    ProtocolLink protocolLink;
    StoredValues storedValues=null; // cached
    LoadProfile loadProfile=null; // cached
    SAPAssignment sapAssignment = null; // cached
    
    /** Creates a new instance of CosemObjectFactory */
    public CosemObjectFactory(ProtocolLink protocolLink) {
        this.protocolLink=protocolLink;
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
    private GenericWrite getGenericWrite(ObisCode obisCode,int attr, int classId) throws IOException {
        return new GenericWrite(protocolLink,getObjectReference(obisCode,classId),attr);
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

    public CosemObject getCosemObject(ObisCode obisCode) throws IOException {
        if (obisCode.getF() != 255) {
            return getStoredValues().getHistoricalValue(obisCode);
        }
        else { 


            if (protocolLink.getMeterConfig().getClassId(obisCode) == Register.CLASSID)
               return new Register(protocolLink,getObjectReference(obisCode));
            else if (protocolLink.getMeterConfig().getClassId(obisCode) == ExtendedRegister.CLASSID)
               return new ExtendedRegister(protocolLink,getObjectReference(obisCode));
            else if (protocolLink.getMeterConfig().getClassId(obisCode) == DemandRegister.CLASSID)
               return new DemandRegister(protocolLink,getObjectReference(obisCode));
            else if (protocolLink.getMeterConfig().getClassId(obisCode) == Data.CLASSID)
               return new Data(protocolLink,getObjectReference(obisCode));
            else if (protocolLink.getMeterConfig().getClassId(obisCode) == ProfileGeneric.CLASSID)
               return new ProfileGeneric(protocolLink,getObjectReference(obisCode));
            else
               throw new IOException("CosemObjectFactory, getCosemObject, invalid classId "+protocolLink.getMeterConfig().getClassId(obisCode)+" for obisCode "+obisCode.toString()) ;
        }
            
    }

    //*****************************************************************************************
    public ObjectReference getObjectReference(ObisCode obisCode) throws IOException {
        return getObjectReference(obisCode,-1);
    }
    public ObjectReference getObjectReference(ObisCode obisCode, int classId) throws IOException {
        if (protocolLink.getReference() == ProtocolLink.LN_REFERENCE)
            return new ObjectReference(obisCode.getLN(),classId);
        else if (protocolLink.getReference() == ProtocolLink.SN_REFERENCE)
            return new ObjectReference(protocolLink.getMeterConfig().getSN(obisCode));
        throw new IOException("CosemObjectFactory, getObjectReference, invalid reference type "+protocolLink.getReference());
    }
    public ObjectReference getObjectReference(byte[] ln,int sn) throws IOException {
        return getObjectReference(ln,-1,sn);
    }
    public ObjectReference getObjectReference(byte[] ln,int classId,int sn) throws IOException {
        if (protocolLink.getReference() == ProtocolLink.LN_REFERENCE)
            return new ObjectReference(ln,classId);
        else if (protocolLink.getReference() == ProtocolLink.SN_REFERENCE)
            return new ObjectReference(sn);
        throw new IOException("CosemObjectFactory, getObjectReference, invalid reference type "+protocolLink.getReference());
    }
    
    
}
