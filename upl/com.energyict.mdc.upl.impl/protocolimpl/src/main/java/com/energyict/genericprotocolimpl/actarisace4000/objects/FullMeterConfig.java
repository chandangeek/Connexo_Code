package com.energyict.genericprotocolimpl.actarisace4000.objects;

import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import org.w3c.dom.*;

import java.io.IOException;


/**
 * @author gna
 */
public class FullMeterConfig extends AbstractActarisObject {

    public FullMeterConfig(ObjectFactory of) {
        super(of);
    }

    protected String prepareXML() {
        Document doc = createDomDocument();

        Element root = doc.createElement(XMLTags.MPULL);
        doc.appendChild(root);
        Element md = doc.createElement(XMLTags.METERDATA);
        root.appendChild(md);
        Element s = doc.createElement(XMLTags.SERIALNUMBER);
        s.setTextContent(getObjectFactory().getAce4000().getMasterSerialNumber());
        md.appendChild(s);
        Element t = doc.createElement(XMLTags.TRACKER);
        t.setTextContent(Integer.toString(getTrackingID(), 16));
        md.appendChild(t);

        Element cf = doc.createElement(XMLTags.FULLCONFIG);
        md.appendChild(cf);

        String msg = convertDocumentToString(doc);

        return (msg.substring(msg.indexOf("?>") + 2));
    }

    protected void parse(Element mdElement) throws IOException {
        NodeList list = mdElement.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);

            // TODO handle the other config parameters
            if (element.getNodeName().equalsIgnoreCase(XMLTags.BILLINGCONF)) {
                getObjectFactory().getBillingConfig().parse(element);
            } else if (element.getNodeName().equalsIgnoreCase(XMLTags.EVENT)) {
                getObjectFactory().getEventData().parse(element);
            }





        }
    }
}
