/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edf.messages;

import com.energyict.mdc.common.ApplicationException;

import com.energyict.protocolimpl.edf.messages.objects.ComplexCosemObject;
import com.energyict.protocolimpl.edf.messages.objects.ComplexCosemObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MessageWriteRegister extends MessageContent{

	protected final static String ELEMENTNAME = "onDemandWriteRegister";
	protected final static String OBISCODEELEMENTNAME = "obisCode";
	protected final static String VALUEELEMENTNAME = "value";

	private String obisCode;
	private Object value;

	public MessageWriteRegister() {
		super();
	}

	public MessageWriteRegister(String obisCode, Object value) {
		super();
		this.obisCode = obisCode;
		this.value = value;
	}

	public MessageWriteRegister(Element element){
		super(element);
		NodeList obisCodes = element.getElementsByTagName(OBISCODEELEMENTNAME);
		if (obisCodes.getLength() != 0){
			obisCode = obisCodes.item(0).getFirstChild().getNodeValue();
		} else {
			throw new ApplicationException("Cannot create MessageWriteRegister");
		}
		NodeList values = element.getElementsByTagName(VALUEELEMENTNAME);
		if (values.getLength() != 0){
			Element valueElement = (Element) values.item(0);
			if (valueElement.getFirstChild() != null) {
				if (valueElement.getFirstChild().hasChildNodes()){
					value = ComplexCosemObjectFactory.createCosemObject((Element)valueElement.getFirstChild());
				} else {
					value = valueElement.getFirstChild().getNodeValue();
				}
			} else {
				value = null;
			}
		} else {
			throw new ApplicationException("Cannot create MessageWriteRegister");
		}
	}

	public String getObisCode() {
		return obisCode;
	}

	public void setObisCode(String obisCode) {
		this.obisCode = obisCode;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Element generateXMLElement(Document document){
			Element root = document.createElement(ELEMENTNAME);
			root.setAttribute(ORDINALATTRIBUTENAME,""+getOrdinal());
			Element obisCodeNode = document.createElement(OBISCODEELEMENTNAME);
			obisCodeNode.appendChild(document.createTextNode(obisCode));
			root.appendChild(obisCodeNode);
			Element valueNode = document.createElement(VALUEELEMENTNAME);
			if (value != null) {
				if (value instanceof ComplexCosemObject){
					ComplexCosemObject complexValue = (ComplexCosemObject) value;
					valueNode.appendChild(complexValue.generateXMLElement(document));
				} else {
					valueNode.appendChild(document.createTextNode(value.toString()));
				}
			}
			root.appendChild(valueNode);
			return root;
	}
}
