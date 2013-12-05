package com.energyict.protocolimpl.edf.messages;

import com.energyict.mdc.common.ApplicationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MessagePostXmlFile extends MessageContent {

	protected final static String ELEMENTNAME = "postXmlFile";
	protected final static String FILENAMEELEMENTNAME = "filename";


	private String filename;

	public MessagePostXmlFile() {
		super();
		filename = "";
	}

	public MessagePostXmlFile(String filename) {
		super();
		this.filename = filename;
	}

	public MessagePostXmlFile(Element element) {
		super(element);
		NodeList filenames = element.getElementsByTagName(FILENAMEELEMENTNAME);
		if (filenames.getLength() != 0){
			filename = filenames.item(0).getFirstChild().getNodeValue();
		} else {
			throw new ApplicationException("Cannot create MessageWriteRegister");
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		root.setAttribute(ORDINALATTRIBUTENAME,""+getOrdinal());
		Element scriptIdNode = document.createElement(FILENAMEELEMENTNAME);
		scriptIdNode.appendChild(document.createTextNode(filename));
		root.appendChild(scriptIdNode);
		return root;
	}

}
