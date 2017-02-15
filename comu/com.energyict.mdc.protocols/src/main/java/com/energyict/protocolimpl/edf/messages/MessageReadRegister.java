/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edf.messages;

import com.energyict.mdc.common.ApplicationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MessageReadRegister extends MessageContent {

	protected final static String ELEMENTNAME = "onDemandReadRegister";
	protected final static String OBISCODEELEMENTNAME = "obisCode";

	private String obisCode;

	public MessageReadRegister() {
		super();
	}

	public MessageReadRegister(String obisCode) {
		super();
		this.obisCode = obisCode;
	}

	public MessageReadRegister(Element element) {
		super(element);
		NodeList obisCodes = element.getElementsByTagName(OBISCODEELEMENTNAME);
		if (obisCodes.getLength() != 0){
			obisCode = obisCodes.item(0).getFirstChild().getNodeValue();
		} else {
			throw new ApplicationException("Cannot create MessageReadRegister");
		}
	}

	public String getObisCode() {
		return obisCode;
	}

	public void setObisCode(String obisCode) {
		this.obisCode = obisCode;
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		root.setAttribute(ORDINALATTRIBUTENAME,""+getOrdinal());
		Element obisCodeNode = document.createElement(OBISCODEELEMENTNAME);
		obisCodeNode.appendChild(document.createTextNode(obisCode));
		root.appendChild(obisCodeNode);
		return root;
	}

}
