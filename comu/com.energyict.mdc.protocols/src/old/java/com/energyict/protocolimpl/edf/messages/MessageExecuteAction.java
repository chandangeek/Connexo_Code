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

public class MessageExecuteAction extends MessageContent {

	protected final static String ELEMENTNAME = "onDemandExecuteAction";
	protected final static String OBISCODEELEMENTNAME = "obisCode";
	protected final static String METHODELEMENTNAME = "MethodId";
	protected final static String METHODDATAELEMENTNAME = "MethodData";

	private String obisCode;
	private int methodId;
	private Object methodData;

	public MessageExecuteAction() {
		super();
	}

	public MessageExecuteAction(String obisCode, int methodId, Object methodData) {
		super();
		this.obisCode = obisCode;
		this.methodId = methodId;
		this.methodData = methodData;
	}

	public MessageExecuteAction(Element element){
		super(element);
		NodeList obisCodes = element.getElementsByTagName(OBISCODEELEMENTNAME);
		if (obisCodes.getLength() != 0){
			obisCode = obisCodes.item(0).getFirstChild().getNodeValue();
		} else {
			throw new ApplicationException("Cannot create MessageExecuteAction");
		}
		NodeList MethodIds = element.getElementsByTagName(METHODELEMENTNAME);
		if (MethodIds.getLength() != 0){
			String methodIdString = MethodIds.item(0).getFirstChild().getNodeValue();
			methodId = Integer.parseInt(methodIdString);
		} else {
			throw new ApplicationException("Cannot create MessageExecuteAction");
		}
		NodeList data = element.getElementsByTagName(METHODDATAELEMENTNAME);
		if (data.getLength() != 0){
			Element dataElement = (Element) data.item(0);
			if (dataElement.getFirstChild().hasChildNodes()){
				methodData = ComplexCosemObjectFactory.createCosemObject((Element)dataElement.getFirstChild());
			} else {
				methodData = dataElement.getFirstChild().getNodeValue();
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

	public int getMethodId() {
		return methodId;
	}

	public void setMethodId(int methodId) {
		this.methodId = methodId;
	}

	public Object getMethodData() {
		return methodData;
	}

	public void setMethodData(Object methodData) {
		this.methodData = methodData;
	}

	public Element generateXMLElement(Document document){
			Element root = document.createElement(ELEMENTNAME);
			root.setAttribute(ORDINALATTRIBUTENAME,""+getOrdinal());
			Element obisCodeNode = document.createElement(OBISCODEELEMENTNAME);
			obisCodeNode.appendChild(document.createTextNode(obisCode));
			root.appendChild(obisCodeNode);
			Element methodIdNode = document.createElement(METHODELEMENTNAME);
			methodIdNode.appendChild(document.createTextNode(""+methodId));
			root.appendChild(methodIdNode);
			Element methodDataNode = document.createElement(METHODDATAELEMENTNAME);
			if (methodData instanceof ComplexCosemObject){
				ComplexCosemObject complexValue = (ComplexCosemObject) methodData;
				methodDataNode.appendChild(complexValue.generateXMLElement(document));
			} else {
				methodDataNode.appendChild(document.createTextNode(methodData.toString()));
			}
			root.appendChild(methodDataNode);
			return root;
	}

}
