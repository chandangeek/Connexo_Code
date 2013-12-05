package com.energyict.protocolimpl.edf.messages.objects;

import com.energyict.mdc.common.ApplicationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MeterIdentification extends ComplexCosemObject {

	protected final static String ELEMENTNAME = "meter";
	protected final static String IDNAME = "id";

	private OctetString id = new OctetString();

	public MeterIdentification() {
		super();
	}

	public MeterIdentification(String id) {
		super();
		this.id = new OctetString(id);
	}

	public MeterIdentification(byte[] id) {
		super();
		this.id = new OctetString(id);
	}

	public MeterIdentification(Element element) {
		super(element);
		NodeList names = element.getElementsByTagName(IDNAME);
		if (names.getLength() != 0){
			Node firstChild = (Node) names.item(0).getFirstChild();
			if (firstChild != null){
				id = new OctetString(names.item(0).getFirstChild().getNodeValue());
			} else {
				id = new OctetString();
			}
		} else {
			throw new ApplicationException("Cannot create meterIdentification");
		}
	}

        public String toString() {
            // Generated code by ToStringBuilder
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("MeterIdentification:\n");
            strBuff.append("   id="+getId()+"\n");
            for (int i=0;i<getIdOctets().length;i++) {
                strBuff.append("       idOctets["+i+"]="+getIdOctets()[i]+"\n");
            }
            return strBuff.toString();
        }

	public String getId() {
		return id.convertOctetStringToString();
	}

	public void setId(String id) {
		this.id = new OctetString(id);
	}

	public byte[] getIdOctets() {
		return id.getOctets();
	}

	public void setId(byte[] id) {
		this.id = new OctetString(id);
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		Element nameElement = document.createElement(IDNAME);
		nameElement.appendChild(document.createTextNode(id.convertOctetStringToString()));
		root.appendChild(nameElement);
		return root;
	}

}
