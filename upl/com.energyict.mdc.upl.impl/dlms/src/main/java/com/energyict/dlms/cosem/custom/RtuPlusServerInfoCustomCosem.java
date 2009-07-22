package com.energyict.dlms.cosem.custom;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.client.CompoundDataBuilderConnection;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.obis.ObisCode;

public class RtuPlusServerInfoCustomCosem extends Data {
	
	private AbstractDataType dataType=null;
	private String softwareVersion=null;
	private String coreVersion=null;
	
	/** Indicates whether the RTU+Server has an RF interface. */
	private Boolean eictRF;
	
	/** Indicates whether the RTU+Server has a Wavenis interface. */
	private Boolean wavenis;
	
	/** Indicates whether the RTU+Server has a PLC interface. */
	private Boolean plc;
	
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

    /**
     * Set the fields of the Cosem.
     * 
     * @param 	softwareVersion			The RTU+Server version.
     * @param 	coreVersion				The MDW version.
     * @param 	wavenisInterface		<code>true</code> if the RTU+Server has a Wavenis interface, <code>false</code> if not.
     * @param 	eictRFInterface			<code>true</code> if the RTU+Server has an EICT RF interface, <code>false</code> if not.
     * @param 	plcInterface			<code>true</code> if the RTU+Server has a PLC interface, <code>false</code> if not.
     * 
     * @throws 	IOException
     */
    public void setFields(String softwareVersion, String coreVersion, final boolean wavenisInterface, final boolean eictRFInterface, final boolean plcInterface) throws IOException {
		Structure structure = new Structure();
		structure.addDataType(OctetString.fromString(softwareVersion));
		structure.addDataType(OctetString.fromString(coreVersion));
		
		// Add indicators of southbound communication types.
		structure.addDataType(new BooleanObject(wavenisInterface));
		structure.addDataType(new BooleanObject(eictRFInterface));
		structure.addDataType(new BooleanObject(plcInterface));
		
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
    
    /**
     * Indicates whether the given RTU+Server has a Wavenis interface.
     * 
     * @return	<code>true</code> if the RTU+Server has a wavenis interface, <code>false</code> if it doesn't.
     */
    public final boolean hasWavenisInterface() {
    	if (this.wavenis != null) {
    		return this.wavenis.booleanValue();
    	} else {
    		if (getValueAttr().getStructure().nrOfDataTypes() >= 3) {
    			final BooleanObject dlmsBoolean = (BooleanObject)this.getValueAttr().getStructure().getDataType(2);
    			this.wavenis = Boolean.valueOf(dlmsBoolean.getState());
    			
    			return this.wavenis.booleanValue();
    		}
    		
    		return false;
    	}
    }
    
    /**
     * Indicates whether the given RTU+Server has an EICT RF interface.
     * 
     * @return	<code>true</code> if it does, <code>false</code> if it doesn't.
     */
    public final boolean hasEictRFInterface() {
    	if (this.eictRF != null) {
    		return this.eictRF.booleanValue();
    	} else {
    		if (getValueAttr().getStructure().nrOfDataTypes() >= 3) {
    			final BooleanObject dlmsBoolean = (BooleanObject)this.getValueAttr().getStructure().getDataType(3);
    			this.eictRF = Boolean.valueOf(dlmsBoolean.getState());
    			
    			return this.eictRF.booleanValue();
    		}
    		
    		return false;
    	}
    }
    
    /**
     * Indicates whether the given RTU+Server has a PLC interface.
     * 
     * @return	<code>true</code> if it does, <code>false</code> if it doesn't.
     */
    public final boolean hasPLCInterface() {
    	if (this.plc != null) {
    		return this.plc.booleanValue();
    	} else {
    		if (getValueAttr().getStructure().nrOfDataTypes() >= 3) {
    			final BooleanObject dlmsBoolean = (BooleanObject)this.getValueAttr().getStructure().getDataType(4);
    			this.plc = Boolean.valueOf(dlmsBoolean.getState());
    			
    			return this.plc.booleanValue();
    		}
    		
    		return false;
    	}
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
