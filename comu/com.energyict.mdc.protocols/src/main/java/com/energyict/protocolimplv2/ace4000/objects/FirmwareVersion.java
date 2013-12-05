package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author gna
 *
 */
public class FirmwareVersion extends AbstractActarisObject{

	private String metrologyFirmwareVersion = null;
	private String auxiliaryFirmwareVersion = null;

	public FirmwareVersion(ObjectFactory of) {
		super(of);
	}

	protected String prepareXML(){
		Document doc = createDomDocument();

		Element root = doc.createElement(XMLTags.MPULL);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.METERDATA);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.SERIALNUMBER);
		s.setTextContent(getObjectFactory().getAce4000().getSerialNumber());
		md.appendChild(s);
		Element t = doc.createElement(XMLTags.TRACKER);
		t.setTextContent(Integer.toString(getTrackingID(), 16));
		md.appendChild(t);

		Element cf = doc.createElement(XMLTags.REQFIRMWARE);
		md.appendChild(cf);

		String msg = convertDocumentToString(doc);

		return (msg.substring(msg.indexOf("?>")+2));
	}

	protected void parse(Element mdElement) {
		NodeList list = mdElement.getChildNodes();

		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);

			if(element.getNodeName().equalsIgnoreCase(XMLTags.AUXFIRMVERS)) {
				setAuxiliaryFirmwareVersion(element.getTextContent());
			} else if(element.getNodeName().equalsIgnoreCase(XMLTags.METFIRMVERS)) {
				setMetrologyFirmwareVersion(element.getTextContent());
			}

		}
	}

	public String getAuxiliaryFirmwareVersion() {
		return auxiliaryFirmwareVersion;
	}

	private void setAuxiliaryFirmwareVersion(String auxiliaryFirmwareVersion) {
		this.auxiliaryFirmwareVersion = auxiliaryFirmwareVersion;
	}

	public String getMetrologyFirmwareVersion() {
		return metrologyFirmwareVersion;
	}

	private void setMetrologyFirmwareVersion(String metrologyFirmwareVersion) {
		this.metrologyFirmwareVersion = metrologyFirmwareVersion;
	}
}