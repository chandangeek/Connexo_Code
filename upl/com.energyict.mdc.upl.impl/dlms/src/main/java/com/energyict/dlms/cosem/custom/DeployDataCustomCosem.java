package com.energyict.dlms.cosem.custom;

import java.io.IOException;
import java.util.*;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.dlms.client.CompoundDataBuilderConnection;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.discover.DiscoverResult;

public class DeployDataCustomCosem extends Data {

	AbstractDataType dataType=null;

    private List<DiscoverResult> discoverResults=null;
    private String rtuPlusServerSerialNumber=null;
    private String rtuPlusServerDeviceType=null;
	
	// in case of message null, only a integer8 (byte) is send in the data value
	// in case of message, a struct is coded in the data value containing a integer8 (byte) and a octetstring with the message
	
	static final byte[] LN=new byte[]{0,0,96,111,0,0};
	
	public DeployDataCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
	}
	
	public DeployDataCustomCosem() {
		super(new CompoundDataBuilderConnection(),new ObjectReference(LN));
	}
	
	public DeployDataCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public void setFields(String rtuPlusServerSerialNumber,String rtuPlusServerDeviceType,List discoverResults) throws IOException {
    	if (dataType==null) {
			Structure structure = new Structure();
			
			structure.addDataType(AXDRString.encode(rtuPlusServerSerialNumber));
			structure.addDataType(AXDRString.encode(rtuPlusServerDeviceType));
			
			Array drArray = new Array();
			Iterator<DiscoverResult> it = discoverResults.iterator();
			while(it.hasNext()) {
				DiscoverResult dr = it.next();
				Structure drStructure = new Structure();
				drStructure.addDataType(new Integer8(dr.getProtocol()));
				drStructure.addDataType(AXDRBoolean.encode(dr.isDiscovered()));
				drStructure.addDataType(AXDRString.encode(dr.getResult()));
				drStructure.addDataType(AXDRString.encode(dr.getProtocolName()));
				drStructure.addDataType(new Integer32(dr.getAddress()));
				drStructure.addDataType(AXDRString.encode(dr.getDeviceTypeName()));
				drStructure.addDataType(AXDRString.encode(dr.getShortDeviceTypeName()));
				drStructure.addDataType(AXDRString.encode(dr.getNetworkId()));
				drStructure.addDataType(AXDRString.encode(dr.getSerialNumber()));
				drStructure.addDataType(AXDRString.encode(dr.getDeviceName()));
				drArray.addDataType(drStructure);
			}
			setValueAttr(drArray);
    	}
    }    
    
    public String getRtuPlusServerSerialNumber() throws IOException {
    	if (rtuPlusServerSerialNumber == null)
    		rtuPlusServerSerialNumber = getValueAttr().getStructure().getDataType(0).getOctetString().stringValue();
    	return rtuPlusServerSerialNumber;
    }
    
    public String getRtuPlusServerDeviceType() throws IOException {
    	if (rtuPlusServerDeviceType == null)
    		rtuPlusServerDeviceType = getValueAttr().getStructure().getDataType(1).getOctetString().stringValue();
    	return rtuPlusServerDeviceType;
    }
    
    public String rtuPlusServerName() throws IOException {
        return getRtuPlusServerDeviceType()+" "+getRtuPlusServerSerialNumber();
    }    
    
    public List<DiscoverResult> getDiscoverResults() throws IOException {
    	if (discoverResults==null) {
    		discoverResults = new ArrayList<DiscoverResult>();
	    	Array drArray = getValueAttr().getStructure().getDataType(2).getArray();
	    	for (int i=0;i<drArray.nrOfDataTypes();i++) {
	    		Structure drStructure = drArray.getDataType(i).getStructure();
	    		DiscoverResult dr = new DiscoverResult();
	    		dr.setProtocol(drStructure.getNextDataType().intValue());
	    		dr.setDiscovered(AXDRBoolean.decode(drStructure.getNextDataType()));
	    		dr.setResult(AXDRString.decode(drStructure.getNextDataType()));
	    		dr.setProtocolName(AXDRString.decode(drStructure.getNextDataType()));
	    		dr.setAddress(drStructure.getNextDataType().intValue());
	    		dr.setDeviceTypeName(AXDRString.decode(drStructure.getNextDataType()));
	    		dr.setShortDeviceTypeName(AXDRString.decode(drStructure.getNextDataType()));
	    		dr.setNetworkId(AXDRString.decode(drStructure.getNextDataType()));
	    		dr.setSerialNumber(AXDRString.decode(drStructure.getNextDataType()));
	    		dr.setDeviceName(AXDRString.decode(drStructure.getNextDataType()));
	    		discoverResults.add(dr);
	    	}
    	}
    	return discoverResults;
    }
    
    public AbstractDataType getValueAttr() throws IOException {
    	if (dataType == null)
    		dataType = super.getValueAttr();
    	return dataType;
    }
    
    public void setValueAttr(AbstractDataType val) throws IOException {
    	dataType = val;
    	super.setValueAttr(dataType);
    }
    
}
