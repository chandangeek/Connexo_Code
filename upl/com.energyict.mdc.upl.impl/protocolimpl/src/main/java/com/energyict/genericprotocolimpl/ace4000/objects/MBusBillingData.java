package com.energyict.genericprotocolimpl.ace4000.objects;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.ace4000.objects.xml.XMLTags;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.mbus.core.*;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.apache.axis.encoding.Base64;
import org.w3c.dom.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * @author gna
 */
public class MBusBillingData extends AbstractActarisObject {

    private Date from;
    private Map<String, List<Date>> billingPointDates = new HashMap<String, List<Date>>();
    private String slaveSerialNumber = "";
    private MeterReadingData mrd = new MeterReadingData();
    private List<String> slaveSerialNumbers = new ArrayList<String>();
    private Map<String, MeterReadingData> mrdPerSlave = new HashMap<String, MeterReadingData>();

    public MeterReadingData getMrdPerSlave(String serialNumber) {
        setBillingPoints();
        return mrdPerSlave.get(serialNumber);
    }

    private void setBillingPoints() {
        for (String serialNumber : slaveSerialNumbers) {
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
        s.setTextContent(getObjectFactory().getAce4000().getMasterSerialNumber());
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

    protected void parse(Element mdElement) throws DOMException, IOException, SQLException, BusinessException {
        NodeList list = mdElement.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);
            if (element.getNodeName().equalsIgnoreCase(XMLTags.MBUSRAW)) {
                setMBLoadProfile(element.getTextContent());
            }
        }
    }

    private void setMBLoadProfile(String data) throws IOException {
        int offset = 0;
        byte[] decoded = Base64.decode(data);
        Date timeStamp = getObjectFactory().convertMeterDateToSystemDate((long) (getNumberFromB64(decoded, offset, 4)));

        List<Date> dates = billingPointDates.get(slaveSerialNumber);
        if (dates == null) {
            dates = new ArrayList<Date>();
            dates.add(timeStamp);
        } else {
            if (!dates.contains(timeStamp)) {
                dates.add(timeStamp);
            }
        }
        billingPointDates.put(slaveSerialNumber, dates);

        offset += 4;

        //Raw MBus frame
        CIField72h ciField72h = new CIField72h(getObjectFactory().getAce4000().getDeviceTimeZone());
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

                            RegisterValue value = new RegisterValue(obisCode, record.getQuantity(), new Date(), timeStamp, timeStamp, new Date(), 0, slaveSerialNumber);
                            mrd.add(value);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to parse the raw MBus frame." + e.getMessage());
        }
    }

    public void setSlaveSerialNumber(String pushedSerialNumber) {
        this.slaveSerialNumber = pushedSerialNumber;
        if (!slaveSerialNumbers.contains(pushedSerialNumber)) {
            slaveSerialNumbers.add(pushedSerialNumber);
        }
    }
}