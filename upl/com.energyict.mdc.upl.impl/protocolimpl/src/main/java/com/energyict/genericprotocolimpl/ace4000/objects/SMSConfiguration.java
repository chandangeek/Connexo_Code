package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.genericprotocolimpl.ace4000.objects.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Copyrights EnergyICT
 * Date: 29/08/11
 * Time: 13:36
 */
public class SMSConfiguration extends AbstractActarisObject {

    private int days;
    private int maximum;
    private String telephoneNumber;
    private String msgCenterNumber;

    public SMSConfiguration(ObjectFactory of) {
        super(of);
    }

    public void setDays(int days) {
        this.days = days;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public void setMsgCenterNumber(String msgCenterNumber) {
        this.msgCenterNumber = msgCenterNumber;
    }

    @Override
    protected void parse(Element element) throws Exception {
        //Only CAK is sent back
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

        Element cf = doc.createElement(XMLTags.CONFIGURATION);
        md.appendChild(cf);

        Element numberOfDaysElement = doc.createElement("SMS");
        numberOfDaysElement.setTextContent(Integer.toString(days));
        cf.appendChild(numberOfDaysElement);

        Element maxNumberOfSMSElement = doc.createElement("SMSDT");
        maxNumberOfSMSElement.setTextContent(Integer.toString(maximum));
        cf.appendChild(maxNumberOfSMSElement);

        Element telElement = doc.createElement("Tel");
        telElement.setTextContent(telephoneNumber);
        cf.appendChild(telElement);

        Element centerElement = doc.createElement("MCN");
        centerElement.setTextContent(msgCenterNumber);
        cf.appendChild(centerElement);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }
}
