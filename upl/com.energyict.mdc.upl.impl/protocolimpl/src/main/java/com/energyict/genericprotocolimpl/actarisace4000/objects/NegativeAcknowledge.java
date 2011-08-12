package com.energyict.genericprotocolimpl.actarisace4000.objects;

import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author khe
 */
public class NegativeAcknowledge extends AbstractActarisObject {

    public NegativeAcknowledge(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected void parse(Element element) {

        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Element subElement = (Element) list.item(i);
            if (subElement.getNodeName().equalsIgnoreCase(XMLTags.TRACKER)) {
                getObjectFactory().setTrackingID(Integer.parseInt(subElement.getTextContent(), 16));
            }
            if (subElement.getNodeName().equalsIgnoreCase(XMLTags.REASON)) {
                reason = Integer.parseInt(subElement.getTextContent(), 16);
            }
        }
    }

    public String prepareXML() {
        return "";      //We don't send NACK's
    }
}