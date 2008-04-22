package com.energyict.genericprotocolimpl.common.tou;

import com.energyict.cbo.ApplicationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ActionItem {
	
	protected final static String ELEMENTNAME = "actionItem";
	protected final static String NAMEELEMENTNAME = "name";
	protected final static String SELECTORELEMENTNAME = "selector";
	
	private OctetString logicalName = new OctetString();
	private int selector;

	public ActionItem() {
		super();
	}
	
	public ActionItem(String name, int selector){
		this.logicalName = new OctetString(name);
		this.selector = selector;
	}

	public ActionItem(byte[] name, int selector){
		this.logicalName = new OctetString(name);
		this.selector = selector;
	}

	
        public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("ActionItem:\n");
            if (logicalName != null) {
                strBuff.append("   logicalName="+getLogicalName()+"\n");
                for (int i=0;i<getLogicalNameOctets().length;i++) {
                    strBuff.append("       logicalNameOctets["+i+"]="+getLogicalNameOctets()[i]+"\n");
                }
            }
            strBuff.append("   selector="+getSelector()+"\n");
            return strBuff.toString();
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

	public int getSelector() {
		return selector;
	}

	public void setSelector(int selector) {
		this.selector = selector;
	}
	

}
