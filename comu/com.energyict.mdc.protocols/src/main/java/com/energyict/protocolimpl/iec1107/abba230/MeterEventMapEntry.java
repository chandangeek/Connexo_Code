package com.energyict.protocolimpl.iec1107.abba230;

public class MeterEventMapEntry {


	int statusId; // 0..20
	int bitId; // 0..7
	int meterEventCode;
	String description;

	public MeterEventMapEntry(int statusId, int bitId, int meterEventCode, String description) {
		// TODO Auto-generated constructor stub
		this.statusId=statusId;
		this.bitId=bitId;
		this.meterEventCode=meterEventCode;
		this.description=description;
	}

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterEventMapEntry:\n");
        strBuff.append("   bitId="+getBitId()+"\n");
        strBuff.append("   meterEventCode="+getMeterEventCode()+"\n");
        strBuff.append("   statusId="+getStatusId()+"\n");
        strBuff.append("   description="+getDescription()+"\n");
        return strBuff.toString();
    }

	public int getStatusId() {
		return statusId;
	}

	public int getBitId() {
		return bitId;
	}

	public int getMeterEventCode() {
		return meterEventCode;
	}

	public String getDescription() {
		return description;
	}

}
