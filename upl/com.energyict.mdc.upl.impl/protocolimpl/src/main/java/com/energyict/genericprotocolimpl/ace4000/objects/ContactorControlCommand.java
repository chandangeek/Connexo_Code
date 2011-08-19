package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.genericprotocolimpl.ace4000.objects.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 10/08/11
 * Time: 13:18
 */
public class ContactorControlCommand extends AbstractActarisObject {

    public ContactorControlCommand(ObjectFactory of) {
        super(of);
    }

    @Override
    protected void parse(Element element) throws Exception {
        //Only ack or nack is sent back
    }

    private int command = 0;
    private Date date = null;

    public void setCommand(int command) {
        this.command = command;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
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

        Element cmdElement = doc.createElement(XMLTags.CONTACTORCMD);
        cmdElement.setTextContent(String.valueOf(command));
        if (date != null) {
            cmdElement.setAttribute(XMLTags.TIME_ATTR, getHexDate(date));
        }
        md.appendChild(cmdElement);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }
}