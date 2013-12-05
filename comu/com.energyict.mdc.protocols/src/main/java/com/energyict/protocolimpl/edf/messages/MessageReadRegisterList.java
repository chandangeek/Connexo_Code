package com.energyict.protocolimpl.edf.messages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MessageReadRegisterList extends MessageContent {

	protected final static String ELEMENTNAME = "onDemandReadRegisterList";
	protected final static String OBISCODEELEMENTNAME = "obisCode";

	private List obisCodes = new ArrayList();

	public MessageReadRegisterList() {
		super();
	}

	public MessageReadRegisterList(List obisCodes) {
		super();
		this.obisCodes = obisCodes;
	}

	public MessageReadRegisterList(Element element) {
		super(element);
		NodeList obisCodeElements = element.getElementsByTagName(OBISCODEELEMENTNAME);
		for (int i=0; i<obisCodeElements.getLength(); i++){
			obisCodes.add(obisCodeElements.item(i).getFirstChild().getNodeValue());
		}
	}

	public List getObisCodes() {
		return obisCodes;
	}

	public void setObisCode(List obisCodes) {
		this.obisCodes = obisCodes;
	}

	public void addObisCode(String obisCode) {
		obisCodes.add(obisCode);
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		root.setAttribute(ORDINALATTRIBUTENAME,""+getOrdinal());
		for (Iterator it = obisCodes.iterator(); it.hasNext(); ){
			String obisCode = (String) it.next();
			Element obisCodeNode = document.createElement(OBISCODEELEMENTNAME);
			obisCodeNode.appendChild(document.createTextNode(obisCode));
			root.appendChild(obisCodeNode);
		}
		return root;
	}

}
