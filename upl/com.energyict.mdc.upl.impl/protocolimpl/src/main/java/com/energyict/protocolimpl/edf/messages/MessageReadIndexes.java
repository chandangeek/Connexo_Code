package com.energyict.protocolimpl.edf.messages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MessageReadIndexes extends MessageContent {

	protected final static String ELEMENTNAME = "onDemandReadIndexes";
	
	public MessageReadIndexes() {
		super();
	}

	public MessageReadIndexes(Element element) {
		super(element);
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		root.setAttribute(ORDINALATTRIBUTENAME,""+getOrdinal());
		return root;
	}

}
