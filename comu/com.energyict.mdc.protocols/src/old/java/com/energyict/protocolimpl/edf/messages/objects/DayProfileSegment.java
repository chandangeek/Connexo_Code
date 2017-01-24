package com.energyict.protocolimpl.edf.messages.objects;

import com.energyict.mdc.common.ApplicationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DayProfileSegment extends ComplexCosemObject {

	protected final static String ELEMENTNAME = "dayProfileSegment";
	protected final static String STARTNAME = "startTime";
	protected final static String ACTIONNAME = "action";

	private OctetString startTime = new OctetString();
	private ActionItem action = new ActionItem();

	public DayProfileSegment() {
		super();
	}

	public DayProfileSegment(String startTime,
			String scriptLogicalName, int scriptSelector) {
		super();
		this.startTime = new OctetString(startTime);
		this.action = new ActionItem(scriptLogicalName,scriptSelector);
	}

	public DayProfileSegment(byte[] startTime,
			byte[] scriptLogicalName, int scriptSelector) {
		super();
		this.startTime = new OctetString(startTime);
		this.action = new ActionItem(scriptLogicalName,scriptSelector);
	}

	public DayProfileSegment(Element element) {
		super(element);
		NodeList starts = element.getElementsByTagName(STARTNAME);
		if (starts.getLength() != 0){
			startTime = new OctetString(starts.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create DayProfileSegment");
		}
		NodeList scripts = element.getElementsByTagName(ACTIONNAME);
		if (scripts.getLength() != 0){
			action = new ActionItem((Element) scripts.item(0));
		} else {
			throw new ApplicationException("Cannot create DayProfileSegment");
		}

	}

	public String getStartTime() {
		return startTime.convertOctetStringToString();
	}

	public void setStartTime(String startTime) {
		this.startTime = new OctetString(startTime);
	}

	public byte[] getStartTimeOctets() {
		return startTime.getOctets();
	}

	public void setStartTimeOctects(byte[] startTime) {
		this.startTime = new OctetString(startTime);
	}

	public ActionItem getAction() {
		return action;
	}

	public void setAction(ActionItem action) {
		this.action = action;
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		Element startElement = document.createElement(STARTNAME);
		startElement.appendChild(document.createTextNode(startTime.convertOctetStringToString()));
		root.appendChild(startElement);
		Element scriptElement = document.createElement(ACTIONNAME);
		scriptElement.appendChild(action.generateXMLElement(document));
		root.appendChild(scriptElement);
		return root;
	}

}
