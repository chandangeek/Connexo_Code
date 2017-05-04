package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.mdc.protocol.api.device.data.MeterReadingData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author gna
 *         <p/>
 *         This class parses billing data (billing point = 0, 1, 2, ...) for:
 *         Active & reactive energy (import & export), total or rate 1/2/3/4
 */
public class BillingData extends AbstractActarisObject {

    private String registerList = null;
    private Date from = null;
    private List<Date> billingPointDates = new ArrayList<Date>();

    private int enabled = -1;
    private int interval = -1;
    private int numOfRecs = -1;
    private MeterReadingData mrd = new MeterReadingData();
    private boolean isComplexBillingData = false;

    public BillingData(ObjectFactory of) {
        super(of);
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    /**
     * Request all billing data
     */
    public String prepareXML() {
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

        if (getFrom() == null) {
            Element bd = doc.createElement(XMLTags.REQALLBD);
            md.appendChild(bd);
        } else {
            Element bd = doc.createElement(XMLTags.REQBDRANGE);
            bd.setTextContent(getHexDate(from) + getHexDate(new Date()));
            md.appendChild(bd);
        }

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    public void parse(Element mdElement) {
        registerList = mdElement.getAttribute(XMLTags.BDATTR);
        isComplexBillingData = false;
        if (registerList == null || "".equals(registerList)) {
            registerList = mdElement.getAttribute(XMLTags.BDATTR2);
            isComplexBillingData = true;
        }

        NodeList list = mdElement.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);

            if (element.getNodeName().equalsIgnoreCase(XMLTags.REGDATA)) {
                setRegisterData(element.getTextContent());
            }
        }
    }

    public MeterReadingData getMrd() {
        setBillingPoints();
        return mrd;
    }

    private void setBillingPoints() {
        Collections.sort(billingPointDates);
        MeterReadingData result = new MeterReadingData();

        for (RegisterValue registerValue : mrd.getRegisterValues()) {
            int billingPoint = billingPointDates.size() - billingPointDates.indexOf(registerValue.getToTime()) - 1;
            ObisCode obisCode = ProtocolTools.setObisCodeField(registerValue.getObisCode(), 5, (byte) billingPoint);
            result.add(new RegisterValue(obisCode, registerValue.getQuantity(), registerValue.getEventTime(), registerValue.getToTime()));
        }
        mrd = result;
    }

    private void setRegisterData(String textContent) {

        int offset = 0;
        byte[] decoded = new Base64EncoderDecoder().decode(textContent);
        long timeStamp = (long) (getNumberFromB64(decoded, offset, 4));
        Date date = getObjectFactory().convertMeterDateToSystemDate(timeStamp);
        if (!billingPointDates.contains(date)) {
            billingPointDates.add(date);
        }
        offset += 4;

        if (registerList != null && !isComplexBillingData) {
            Quantity quantity;
            if (registerList.contains("T")) {           //Total import
                quantity = new Quantity(getNumberFromB64(decoded, offset, 4), getUnit());
                offset += 4;
                mrd.add(new RegisterValue(ObisCode.fromString("1.0.1.8.0.0"), quantity, new Date(), date));
            }

            if (registerList.contains("R")) {           //Total export
                quantity = new Quantity(getNumberFromB64(decoded, offset, 4), getUnit());
                offset += 4;
                mrd.add(new RegisterValue(ObisCode.fromString("1.0.2.8.0.0"), quantity, new Date(), date));
            }

            if (registerList.contains("1")) {
                quantity = new Quantity(getNumberFromB64(decoded, offset, 4), getUnit());
                offset += 4;
                mrd.add(new RegisterValue(ObisCode.fromString("1.0.1.8.1.0"), quantity, new Date(), date));
            }

            if (registerList.contains("2")) {
                quantity = new Quantity(getNumberFromB64(decoded, offset, 4), getUnit());
                offset += 4;
                mrd.add(new RegisterValue(ObisCode.fromString("1.0.1.8.2.0"), quantity, new Date(), date));
            }

            if (registerList.contains("3")) {
                quantity = new Quantity(getNumberFromB64(decoded, offset, 4), getUnit());
                offset += 4;
                mrd.add(new RegisterValue(ObisCode.fromString("1.0.1.8.3.0"), quantity, new Date(), date));
            }

            if (registerList.contains("4")) {
                quantity = new Quantity(getNumberFromB64(decoded, offset, 4), getUnit());
                offset += 4;
                mrd.add(new RegisterValue(ObisCode.fromString("1.0.1.8.4.0"), quantity, new Date(), date));
            }
        }

        if (registerList != null && isComplexBillingData) {
            String[] registers = registerList.split(";");
            for (String register : registers) {
                ObisCode obisCode;
                Unit unit;

                if (register.contains("AI")) {
                    obisCode = ObisCode.fromString("1.0.1.8.0.0");
                    unit = getUnit();
                } else if (register.contains("AE")) {
                    obisCode = ObisCode.fromString("1.0.2.8.0.0");
                    unit = getUnit();
                } else if (register.contains("RI")) {
                    obisCode = ObisCode.fromString("1.0.3.8.0.0");
                    unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 0);
                } else if (register.contains("RE")) {
                    obisCode = ObisCode.fromString("1.0.4.8.0.0");
                    unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 0);
                } else {
                    continue;
                    //TODO proper no such register exception thingy
                }

                if (register.split(":")[1].contains("T")) {
                    obisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 0);
                    addRegisterValue(offset, decoded, date, obisCode, unit);
                    offset += 4;
                }

                if (register.split(":")[1].contains("1")) {
                    obisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 1);
                    addRegisterValue(offset, decoded, date, obisCode, unit);
                    offset += 4;
                }

                if (register.split(":")[1].contains("2")) {
                    obisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 2);
                    addRegisterValue(offset, decoded, date, obisCode, unit);
                    offset += 4;
                }

                if (register.split(":")[1].contains("3")) {
                    obisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 3);
                    addRegisterValue(offset, decoded, date, obisCode, unit);
                    offset += 4;
                }

                if (register.split(":")[1].contains("4")) {
                    obisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 4);
                    addRegisterValue(offset, decoded, date, obisCode, unit);
                    offset += 4;
                }
            }
        }
    }

    private Unit getUnit() {
        return Unit.get(BaseUnit.WATTHOUR, 0);
    }

    private void addRegisterValue(int offset, byte[] decoded, Date date, ObisCode obisCode, Unit unit) {
        Quantity quantity = new Quantity(getNumberFromB64(decoded, offset, 4), unit);
        mrd.add(new RegisterValue(obisCode, quantity, new Date(), date));
    }


    public int getEnabled() {
        return enabled;
    }

    protected void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public int getInterval() {
        return interval;
    }

    protected void setInterval(int interval) {
        this.interval = interval;
    }

    protected int getNumOfRecs() {
        return numOfRecs;
    }

    protected void setNumOfRecs(int numOfRecs) {
        this.numOfRecs = numOfRecs;
    }
}