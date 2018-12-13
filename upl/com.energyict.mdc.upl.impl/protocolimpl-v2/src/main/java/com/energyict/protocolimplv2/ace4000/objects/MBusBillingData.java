package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.core.DataRecord;
import com.energyict.protocolimpl.mbus.core.ObisCodeCreator;
import com.energyict.protocolimpl.mbus.core.ValueInformationfieldCoding;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author gna
 */
public class MBusBillingData extends AbstractActarisObject {

    private Date from;
    private Map<String, List<Date>> billingPointDates = new HashMap<String, List<Date>>();
    private Map<String, MeterReadingData> mrdPerSlave = new HashMap<String, MeterReadingData>();

    public MeterReadingData getMrdPerSlave(String serialNumber) {
        setBillingPoints();
        return mrdPerSlave.get(serialNumber);
    }

    private void setBillingPoints() {
        for (String serialNumber : getObjectFactory().getAllSlaveSerialNumbers()) {
            List<Date> billingPointDatesPerSerialNumber = billingPointDates.get(serialNumber);
            Collections.sort(billingPointDatesPerSerialNumber);
            MeterReadingData result = new MeterReadingData();

            for (RegisterValue registerValue : mrd.getRegisterValues()) {
                if (serialNumber.equals(registerValue.getText())) {
                    int billingPoint = billingPointDatesPerSerialNumber.size() - billingPointDatesPerSerialNumber.indexOf(registerValue.getToTime()) - 1;
                    ObisCode obisCode = ProtocolTools.setObisCodeField(registerValue.getObisCode(), 5, (byte) billingPoint);
                    result.add(new RegisterValue(obisCode, registerValue.getQuantity(), registerValue.getEventTime(), registerValue.getToTime()));
                }
            }
            mrdPerSlave.put(serialNumber, result);
        }
    }

    public MeterReadingData getMrd() {
        return mrd;
    }

    public MBusBillingData(ObjectFactory of) {
        super(of);
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    /**
     * Request all MBus billing data
     */
    protected String prepareXML() {

        Document doc = createDomDocument();

        Element root = doc.createElement(XMLTags.MPULL);
        doc.appendChild(root);
        Element md = doc.createElement(XMLTags.METERDATA);
        root.appendChild(md);
        Element s = doc.createElement(XMLTags.SERIALNUMBER);
        s.setTextContent(getObjectFactory().getAce4000().getConfiguredSerialNumber());
        md.appendChild(s);
        Element t = doc.createElement(XMLTags.TRACKER);
        t.setTextContent(Integer.toString(getTrackingID(), 16));
        md.appendChild(t);

        if (getFrom() == null) {
            Element lp = doc.createElement(XMLTags.REQMBALLDATA);
            md.appendChild(lp);
        } else {
            Element lp = doc.createElement(XMLTags.REQMBRANGE);
            lp.setTextContent(getHexDate(from) + getHexDate(new Date()));
            md.appendChild(lp);
        }

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    protected void parse(Element mdElement) {
        NodeList list = mdElement.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);
            if (element.getNodeName().equalsIgnoreCase(XMLTags.MBUSRAW)) {
                setMBLoadProfile(element.getTextContent());
            }
        }
    }

    private void setMBLoadProfile(String data) {
        int offset = 0;
        byte[] decoded = new Base64EncoderDecoder().decode(data);
        Date timeStamp = getObjectFactory().convertMeterDateToSystemDate((long) (getNumberFromB64(decoded, offset, 4)));

        List<Date> dates = billingPointDates.get(getObjectFactory().getCurrentSlaveSerialNumber());
        if (dates == null) {
            dates = new ArrayList<Date>();
            dates.add(timeStamp);
        } else {
            if (!dates.contains(timeStamp)) {
                dates.add(timeStamp);
            }
        }
        billingPointDates.put(getObjectFactory().getCurrentSlaveSerialNumber(), dates);

        offset += 4;

        //Raw MBus frame
        CIField72h ciField72h = new CIField72h(getObjectFactory().getAce4000().getTimeZone());
        byte[] rawFrame = ProtocolTools.getSubArray(decoded, 4);
        try {
            ciField72h.parse(rawFrame);
            List dataRecords = ciField72h.getDataRecords();
            DataRecord record;
            ValueInformationfieldCoding valueInfo;

            for (Object dataRecord : dataRecords) {
                record = (DataRecord) dataRecord;
                if (record.getQuantity() != null) {
                    valueInfo = record.getDataRecordHeader().getValueInformationBlock().getValueInformationfieldCoding();
                    ObisCodeCreator obisCodeCreator = valueInfo.getObisCodeCreator();
                    if (valueInfo.isTypeUnit()) {
                        if (obisCodeCreator == null) {
                            getObjectFactory().log(Level.WARNING, "No obis code mapped for received quantity: " + record.getQuantity() + ", register value will not be stored");
                        } else {
                            if (obisCodeCreator.getA() == -1) {
                                obisCodeCreator.setA(ciField72h.getDeviceType().getObisA());
                            }
                            obisCodeCreator.setB(0);
                            obisCodeCreator.setF(0);
                            ObisCode obisCode = ObisCode.fromString(obisCodeCreator.toString());

                            RegisterValue value = new RegisterValue(obisCode, record.getQuantity(), timeStamp, timeStamp);
                            mrd.add(value);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

}