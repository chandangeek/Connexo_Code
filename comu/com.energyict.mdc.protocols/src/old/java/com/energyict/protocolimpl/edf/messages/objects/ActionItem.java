package com.energyict.protocolimpl.edf.messages.objects;

import com.energyict.mdc.common.ApplicationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ActionItem extends ComplexCosemObject{

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

	public ActionItem(Element element) {
		super(element);
		NodeList names = element.getElementsByTagName(NAMEELEMENTNAME);
		if (names.getLength() != 0){
			logicalName = new OctetString(names.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create ActionItem");
		}
		NodeList selectors = element.getElementsByTagName(SELECTORELEMENTNAME);
		if (names.getLength() != 0){
			selector = Integer.parseInt(selectors.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create ActionItem");
		}
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

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		Element nameElement = document.createElement(NAMEELEMENTNAME);
		nameElement.appendChild(document.createTextNode(logicalName.convertOctetStringToString()));
		root.appendChild(nameElement);
		Element selectorElement = document.createElement(SELECTORELEMENTNAME);
		selectorElement.appendChild(document.createTextNode(""+selector));
		root.appendChild(selectorElement);
		return root;
	}


}
