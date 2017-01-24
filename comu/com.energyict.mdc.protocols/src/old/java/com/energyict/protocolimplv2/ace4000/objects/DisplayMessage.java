package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Copyrights EnergyICT
 * Date: 10/08/11
 * Time: 13:18
 */
public class DisplayMessage extends AbstractActarisObject {

    public DisplayMessage(ObjectFactory of) {
        super(of);
    }

    @Override
    protected void parse(Element element) {
        //Only ack or nack is sent back
    }

    private int mode = 0;
    private String message = "";

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    protected String prepareXML() {
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

        Element cf = doc.createElement(XMLTags.CONFIGURATION);
        md.appendChild(cf);
        Element messageElement = doc.createElement(XMLTags.MESSAGE);
        cf.appendChild(messageElement);
        Element modeElement = doc.createElement(XMLTags.MODE);
        modeElement.setTextContent(Integer.toString(mode));
        messageElement.appendChild(modeElement);
        if (mode != 0) {
            Element messageContentsElement = doc.createElement(getModeTag());
            messageContentsElement.setTextContent(message);
            messageElement.appendChild(messageContentsElement);
        }

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    private String getModeTag() {
        switch (mode) {
            case 1:
                return XMLTags.SHORTMSG;
            case 2:
                return XMLTags.LONGMSG;
            default:
                return "";
        }
    }
}