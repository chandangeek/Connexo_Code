package com.energyict.protocolimpl.edf.messages;

import com.energyict.mdc.common.ApplicationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MessagePerformanceTest extends MessageContent {

	protected final static String ELEMENTNAME = "performanceTest";
	protected final static String SCRIPTIDELEMENTNAME = "scriptID";

	private int scriptId;

	public MessagePerformanceTest() {
		super();
		this.scriptId = 0;
	}

	public MessagePerformanceTest(int scriptId) {
		super();
		this.scriptId = scriptId;
	}

	public MessagePerformanceTest(Element element) {
		super(element);
		NodeList scriptIds = element.getElementsByTagName(SCRIPTIDELEMENTNAME);
		if (scriptIds.getLength() != 0){
			scriptId = Integer.parseInt(scriptIds.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create MessageWriteRegister");
		}
	}

	public int getScriptId() {
		return scriptId;
	}

	public void setScriptId(int scriptId) {
		this.scriptId = scriptId;
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		root.setAttribute(ORDINALATTRIBUTENAME,""+getOrdinal());
		Element scriptIdNode = document.createElement(SCRIPTIDELEMENTNAME);
		scriptIdNode.appendChild(document.createTextNode(""+scriptId));
		root.appendChild(scriptIdNode);
		return root;
	}

}
