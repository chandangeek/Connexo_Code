package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 10/08/11
 * Time: 13:18
 */
public class SpecialDataModeConfiguration extends AbstractActarisObject {

    public SpecialDataModeConfiguration(ObjectFactory of) {
        super(of);
    }

    @Override
    protected void parse(Element element) {
        //Only ack or nack is sent back
    }

    private int billingInterval;
    private int billingMaxNumberOfRecords;
    private int billingEnable;
    private int loadProfileInterval;
    private int loadProfileMaxNumberOfRecords;
    private int loadProfileEnable;
    private int durationInDays;
    private Date activationDate = null;

    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }

    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    public void setLoadProfileMaxNumberOfRecords(int loadProfileMaxNumberOfRecords) {
        this.loadProfileMaxNumberOfRecords = loadProfileMaxNumberOfRecords;
    }

    public void setLoadProfileEnable(int loadProfileEnable) {
        this.loadProfileEnable = loadProfileEnable;
    }

    public void setLoadProfileInterval(int loadProfileInterval) {
        this.loadProfileInterval = loadProfileInterval;
    }

    public void setBillingEnable(int billingEnable) {
        this.billingEnable = billingEnable;
    }

    public void setBillingInterval(int billingInterval) {
        this.billingInterval = billingInterval;
    }

    public void setBillingMaxNumberOfRecords(int billingMaxNumberOfRecords) {
        this.billingMaxNumberOfRecords = billingMaxNumberOfRecords;
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

        Element sdm = doc.createElement(XMLTags.SPECIALDATAMODE);
        cf.appendChild(sdm);

        Element sBilling = doc.createElement(XMLTags.SPECIAL_BILLING);
        sdm.appendChild(sBilling);
        Element enableElement = doc.createElement(XMLTags.BILLENABLE);
        enableElement.setTextContent(Integer.toString(billingEnable));
        sBilling.appendChild(enableElement);
        Element intervalElement = doc.createElement(XMLTags.BILLINT);
        intervalElement.setTextContent(Integer.toString(billingInterval, 16));
        sBilling.appendChild(intervalElement);
        Element maxNumberElement = doc.createElement(XMLTags.BILLNUMB);
        maxNumberElement.setTextContent(Integer.toString(billingMaxNumberOfRecords, 16));
        sBilling.appendChild(maxNumberElement);


        Element sLP = doc.createElement(XMLTags.SPECIAL_LP);
        sdm.appendChild(sLP);
        enableElement = doc.createElement(XMLTags.LPENABLE);
        enableElement.setTextContent(Integer.toString(loadProfileEnable));
        sLP.appendChild(enableElement);
        intervalElement = doc.createElement(XMLTags.LPINTERVAL);
        intervalElement.setTextContent(Integer.toString(loadProfileInterval, 16));
        sLP.appendChild(intervalElement);
        maxNumberElement = doc.createElement(XMLTags.LPMAXNUMBER);
        maxNumberElement.setTextContent(Integer.toString(loadProfileMaxNumberOfRecords, 16));
        sLP.appendChild(maxNumberElement);

        Element duration = doc.createElement(XMLTags.SDM_DURATION);
        duration.setTextContent(Integer.toString(durationInDays, 16));
        sdm.appendChild(duration);

        Element activation = doc.createElement(XMLTags.SDM_ACTIV_DATE);
        activation.setTextContent(getHexDate(activationDate));
        sdm.appendChild(activation);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }
}