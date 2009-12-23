/**
 *
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;

/**
 * @author gna
 *
 */
public class FirmwareVersion extends AbstractActarisObject{

	private String metrologyFirmwareVersion = null;
	private String auxiliaryFirmwareVersion = null;
	private String reqString = null;
	private int trackingID;

	/**
	 * empty constructor
	 */
	public FirmwareVersion() {
		this(null);
	}

	public FirmwareVersion(ObjectFactory of) {
		super(of);
	}

	protected void prepareXML(){
		Document doc = createDomDocument();

		Element root = doc.createElement(XMLTags.MPULL);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.METERDATA);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.SERIALNUMBER);
		s.setTextContent(getObjectFactory().getAace().getNecessarySerialnumber());
		md.appendChild(s);
		Element t = doc.createElement(XMLTags.TRACKER);
		t.setTextContent(String.valueOf(trackingID));
		md.appendChild(t);

		Element cf = doc.createElement(XMLTags.REQFIRMWARE);
		md.appendChild(cf);

		String msg = convertDocumentToString(doc);

		setReqString(msg.substring(msg.indexOf("?>")+2));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	protected int getTrackingID() {
		return trackingID;
	}

	protected void setTrackingID(int trackingID) {
		this.trackingID = trackingID;
	}

	protected void setElement(Element mdElement) {
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

	protected String getReqString() {
		return reqString;
	}

	private void setReqString(String reqString) {
		this.reqString = reqString;
	}

}
