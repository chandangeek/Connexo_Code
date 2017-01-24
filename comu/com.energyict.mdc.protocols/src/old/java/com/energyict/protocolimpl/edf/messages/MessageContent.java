package com.energyict.protocolimpl.edf.messages;

import com.energyict.mdc.common.ApplicationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public abstract class MessageContent {

	protected final static String ORDINALATTRIBUTENAME = "ordinal";

	private int ordinal;

	public MessageContent() {
		super();
	}

	public MessageContent(int ordinal) {
		super();
		this.ordinal = ordinal;
	}

	public MessageContent(Element element){
		Integer ordinalInteger = new Integer(element.getAttribute(ORDINALATTRIBUTENAME));
		ordinal = ordinalInteger.intValue();
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	public String xmlEncode(){
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			document.appendChild(generateXMLElement(document));
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new StringWriter());
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
			return result.getWriter().toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ApplicationException("Could not encode the document in xml");
		}
	}

	public abstract Element generateXMLElement(Document document);

}
