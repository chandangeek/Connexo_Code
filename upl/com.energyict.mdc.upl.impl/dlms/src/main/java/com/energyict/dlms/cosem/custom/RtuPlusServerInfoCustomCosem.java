package com.energyict.dlms.cosem.custom;

import java.io.IOException;
import java.util.regex.*;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.client.CompoundDataBuilderConnection;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

public class RtuPlusServerInfoCustomCosem extends Data {

	AbstractDataType dataType=null;
	String softwareVersion=null;
	String coreVersion=null;
	
	private final String DEFAULT_SOFTWARE_VERSION="1.3.1";
	private final String DEFAULT_CORE_VERSION="8.3.5";
	
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RtuPlusServerInfoCustomCosem:\n");
		strBuff.append("   softwareVersion="+getSoftwareVersion()+"\n");
        strBuff.append("   coreVersion="+getCoreVersion()+"\n");
        return strBuff.toString();
    }	
	
	static final byte[] LN=new byte[]{0,0,96,51,0,0};
	
	public RtuPlusServerInfoCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
	}
	
	public RtuPlusServerInfoCustomCosem() {
		super(new CompoundDataBuilderConnection(),new ObjectReference(LN));
		softwareVersion=DEFAULT_SOFTWARE_VERSION;
		coreVersion=DEFAULT_CORE_VERSION;
	}
	
	public RtuPlusServerInfoCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public void setFields(String softwareVersion, String coreVersion) throws IOException {
		Structure structure = new Structure();
		structure.addDataType(OctetString.fromString(softwareVersion));
		structure.addDataType(OctetString.fromString(coreVersion));
		setValueAttr(structure);
		this.softwareVersion = softwareVersion;
		this.coreVersion = coreVersion;
    }
    
    
    public String getSoftwareVersion() {
    	if (softwareVersion==null) {
	    	if (getValueAttr().getStructure().nrOfDataTypes()>=1)
	    		softwareVersion = getValueAttr().getStructure().getDataType(0).isOctetString()?dataType.getStructure().getDataType(0).getOctetString().stringValue():null;
	   		else
	   			softwareVersion = null;
    	}
    	
    	return softwareVersion;
    }
    
    public String getCoreVersion() {
    	if (coreVersion==null) {
	    	if (getValueAttr().getStructure().nrOfDataTypes()>=2)
	    		coreVersion = getValueAttr().getStructure().getDataType(1).isOctetString()?dataType.getStructure().getDataType(1).getOctetString().stringValue():null;
	   		else
	   			coreVersion = null;
    	}
    	return coreVersion;
    }
    
    public AbstractDataType getValueAttr() {
    	return dataType;
    }
    
    public void setValueAttr(AbstractDataType val) throws IOException {
    	dataType = val;
    	super.setValueAttr(dataType);
    }
    
    public boolean isVersion131() {
    	return (getIntSoftwareVersion() == Integer.parseInt(DEFAULT_SOFTWARE_VERSION.replace(".","")));
    }
    
    private int getIntSoftwareVersion() {
        String patternStr = "[0-9].[0-9].[0-9]";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(getSoftwareVersion());
        boolean matchFound = matcher.find();    // true

        if (!matchFound)
        	return Integer.parseInt(DEFAULT_SOFTWARE_VERSION.replace(".",""));
        
        // Retrieve matching string
        String newVersion = matcher.group().replace(".","");         
        return Integer.parseInt(newVersion);
    	
    }
    
    public boolean isHigherThenVersion131() {
        return (getIntSoftwareVersion() > Integer.parseInt(DEFAULT_SOFTWARE_VERSION.replace(".","")));
    }
    
//    static public void main(String[] args) {
//    	RtuPlusServerInfoCustomCosem o = new RtuPlusServerInfoCustomCosem();
//        try {
//			o.setFields("1.3.0","");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        System.out.println(o.isHigherThenVersion131());
//        System.out.println(o.isVersion131());
//    }
}
