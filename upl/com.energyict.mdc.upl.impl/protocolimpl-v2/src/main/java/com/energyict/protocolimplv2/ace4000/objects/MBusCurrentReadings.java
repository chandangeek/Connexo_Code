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
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @author gna & khe
 */
public class MBusCurrentReadings extends AbstractActarisObject {

    private Date timeStamp = null;

    public MBusCurrentReadings(ObjectFactory of) {
        super(of);
    }

    protected void parse(Element mdElement) {
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
        s.setTextContent(getObjectFactory().getAce4000().getConfiguredSerialNumber());
        md.appendChild(s);
        Element t = doc.createElement(XMLTags.TRACKER);
        t.setTextContent(Integer.toString(getTrackingID(), 16));
        md.appendChild(t);

        Element lp = doc.createElement(XMLTags.REQMBUSCR);
        md.appendChild(lp);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    private void setMBReadingData(String textContent) {
        int offset = 0;
        byte[] decoded = new Base64EncoderDecoder().decode(textContent);
        long timeStamp = (long) (getNumberFromB64(decoded, offset, 4));
        setTimeStamp(getObjectFactory().convertMeterDateToSystemDate(timeStamp));
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
                            ObisCode obisCode = ObisCode.fromString(obisCodeCreator.toString());

                            RegisterValue value = new RegisterValue(obisCode, record.getQuantity(), getTimeStamp(), getTimeStamp());
                            mrd.add(value);
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw DataParseException.ioException(e);
        }
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    protected void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}