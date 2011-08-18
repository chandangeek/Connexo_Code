package com.energyict.genericprotocolimpl.actarisace4000.objects;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.mbus.core.*;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.apache.axis.encoding.Base64;
import org.w3c.dom.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @author gna & khe
 */
public class MBusCurrentReadings extends AbstractActarisObject {

    private Date timeStamp = null;
    private String slaveSerialNumber = "";
    private MeterReadingData mrd = new MeterReadingData();


    public MBusCurrentReadings(ObjectFactory of) {
        super(of);
    }

    protected void parse(Element mdElement) throws DOMException, IOException, SQLException, BusinessException {
        NodeList list = mdElement.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);
            if (element.getNodeName().equalsIgnoreCase(XMLTags.MBUSRAW)) {
                setMBReadingData(element.getTextContent());
            }
        }
    }

    public MeterReadingData getMrdPerSlave(String serialNumber) {
        MeterReadingData result = new MeterReadingData();
        for (RegisterValue registerValue : mrd.getRegisterValues()) {
            if (serialNumber.equals(registerValue.getText())) {
                result.add(new RegisterValue(registerValue.getObisCode(), registerValue.getQuantity(), registerValue.getEventTime(), registerValue.getToTime()));
            }
        }
        return result;
    }

    public MeterReadingData getMrd() {
        return mrd;
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

        Element lp = doc.createElement(XMLTags.REQMBUSCR);
        md.appendChild(lp);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    private void setMBReadingData(String textContent) throws IOException, SQLException, BusinessException {
        int offset = 0;
        byte[] decoded = Base64.decode(textContent);
        long timeStamp = (long) (getNumberFromB64(decoded, offset, 4));
        setTimeStamp(getObjectFactory().convertMeterDateToSystemDate(timeStamp));
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
                            ObisCode obisCode = ObisCode.fromString(obisCodeCreator.toString());

                            RegisterValue value = new RegisterValue(obisCode, record.getQuantity(), new Date(), getTimeStamp(), getTimeStamp(), new Date(), 0, slaveSerialNumber);
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

    public Date getTimeStamp() {
        return timeStamp;
    }

    protected void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setSlaveSerialNumber(String pushedSerialNumber) {
        this.slaveSerialNumber = pushedSerialNumber;
    }
}