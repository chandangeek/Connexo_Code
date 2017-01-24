package com.energyict.protocolimpl.edf.messages.objects;

public class MovingPeakScript {
	private int scriptId = 0;
	private int serviceId = 0;
	private int classId = 0;
	private OctetString logicalName = new OctetString();
	private int index = 0;

	public MovingPeakScript() {
		super();
	}

	public MovingPeakScript(int scriptId, int serviceId, int classId,
			String logicalName, int index) {
		super();
		this.scriptId = scriptId;
		this.serviceId = serviceId;
		this.classId = classId;
		this.logicalName = new OctetString(logicalName);
		this.index = index;
	}
	
        public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("MovingPeakScript:\n");
            strBuff.append("   classId="+getClassId()+"\n");
            strBuff.append("   index="+getIndex()+"\n");
            strBuff.append("   logicalName="+getLogicalName()+"\n");
            for (int i=0;i<getLogicalNameOctets().length;i++) {
                strBuff.append("       logicalNameOctets["+i+"]="+getLogicalNameOctets()[i]+"\n");
            }
            strBuff.append("   scriptId="+getScriptId()+"\n");
            strBuff.append("   serviceId="+getServiceId()+"\n");
            return strBuff.toString();
        }       
        
	public int getScriptId() {
		return scriptId;
	}
	public void setScriptId(int scriptId) {
		this.scriptId = scriptId;
	}
	public int getServiceId() {
		return serviceId;
	}
	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
	public int getClassId() {
		return classId;
	}
	public void setClassId(int classId) {
		this.classId = classId;
	}
	public String getLogicalName() {
		return logicalName.convertOctetStringToString();
	}
	public void setLogicalName(String logicalName) {
		this.logicalName = new OctetString(logicalName);
	}
	public byte[] getLogicalNameOctets() {
		return logicalName.getOctets();
	}
	public void setLogicalNameOctets(byte[] logicalName) {
		this.logicalName = new OctetString(logicalName);
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
}
