package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.genericprotocolimpl.ace4000.objects.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Date;

/**
 * @author gna
 */
public class ForceTime extends AbstractActarisObject {

    public ForceTime(ObjectFactory of) {
        super(of);
    }

    /**
     * Force the Meter time to the System time
     */
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

        String newDate = getHexDate(new Date());

        Element ft = doc.createElement(XMLTags.FORCETIME);
        md.appendChild(ft);
        Element t1 = doc.createElement(XMLTags.TIME1);
        t1.setTextContent("00000000");
        ft.appendChild(t1);
        Element t2 = doc.createElement(XMLTags.TIME2);
        t2.setTextContent(newDate);
        ft.appendChild(t2);
        Element t3 = doc.createElement(XMLTags.TIME3);
        t3.setTextContent(newDate);
        ft.appendChild(t3);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    protected void parse(Element mdElement) throws IOException {
        //This results in <DT> tags, which are parsed in the DateTime class
    }
}