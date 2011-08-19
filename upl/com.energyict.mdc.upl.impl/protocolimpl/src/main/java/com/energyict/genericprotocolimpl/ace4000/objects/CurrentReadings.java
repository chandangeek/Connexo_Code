package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.cbo.*;
import com.energyict.genericprotocolimpl.ace4000.objects.xml.XMLTags;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.RegisterValue;
import org.apache.axis.encoding.Base64;
import org.w3c.dom.*;

import java.util.Date;

/**
 * @author gna
 */
public class CurrentReadings extends AbstractActarisObject {

    public static final Unit UNIT = Unit.get(BaseUnit.WATTHOUR);

    private String includedRegisters = null;
    private Date timeStamp = null;
    private MeterReadingData mrd = new MeterReadingData();

    public CurrentReadings(ObjectFactory of) {
        super(of);
    }

    protected void parse(Element mdElement) {
        includedRegisters = mdElement.getAttribute(XMLTags.CRATTR);        //e.g.: TR1234

        NodeList list = mdElement.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);
            if (element.getNodeName().equalsIgnoreCase(XMLTags.READINGDATA)) {
                setReadingData(element.getTextContent());
            }
        }
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

        Element lp = doc.createElement(XMLTags.REQCR);
        md.appendChild(lp);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    private void setReadingData(String textContent) {
        int offset = 0;
        byte[] decoded = Base64.decode(textContent);
        long timeStamp = (long) (getNumberFromB64(decoded, offset, 4));
        setTimeStamp(getObjectFactory().convertMeterDateToSystemDate(timeStamp));
        offset += 4;

        if (includedRegisters != null) {
            RegisterValue rv;
            RtuRegister register;
            ObisCode oc;
            if (includedRegisters.contains("T")) {
                oc = ObisCode.fromString("1.0.1.8.0.255");
                addRegister(oc, new Quantity(getNumberFromB64(decoded, offset, 4), UNIT));
                offset += 4;
            }

            if (includedRegisters.contains("R")) {
                oc = ObisCode.fromString("1.0.2.8.0.255");
                addRegister(oc, new Quantity(getNumberFromB64(decoded, offset, 4), UNIT));
                offset += 4;
            }

            if (includedRegisters.contains("1")) {
                oc = ObisCode.fromString("1.0.1.8.1.255");
                addRegister(oc, new Quantity(getNumberFromB64(decoded, offset, 4), UNIT));
                offset += 4;
            }

            if (includedRegisters.contains("2")) {
                oc = ObisCode.fromString("1.0.1.8.2.255");
                addRegister(oc, new Quantity(getNumberFromB64(decoded, offset, 4), UNIT));
                offset += 4;
            }

            if (includedRegisters.contains("3")) {
                oc = ObisCode.fromString("1.0.1.8.3.255");
                addRegister(oc, new Quantity(getNumberFromB64(decoded, offset, 4), UNIT));
                offset += 4;
            }

            if (includedRegisters.contains("4")) {
                oc = ObisCode.fromString("1.0.1.8.4.255");
                addRegister(oc, new Quantity(getNumberFromB64(decoded, offset, 4), UNIT));
                offset += 4;
            }
        }
    }

    private void addRegister(ObisCode oc, Quantity value) {
        RtuRegister register;
        RegisterValue rv;
        register = getObjectFactory().getAce4000().getMasterMeter().getRegister(oc);
        if (register != null) {
            rv = new RegisterValue(oc, value, new Date(), getTimeStamp());
            rv.setRtuRegisterId(register.getId());
            mrd.add(rv);
        }
    }

    public MeterReadingData getMrd() {
        return mrd;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    protected void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}