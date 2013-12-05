package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 18/07/11
 * Time: 15:43
 */
public class EventData extends AbstractActarisObject {

    private List<MeterEvent> meterEvents = null;
    private Map<String, String> eventConfig;

    @Override
    protected void parse(Element element) {
        meterEvents = new ArrayList<MeterEvent>();
        NodeList list = element.getChildNodes();
        try {
            if (list.getLength() > 0) {
                for (int i = 0; i < list.getLength(); i++) {
                    Element eventElement = (Element) list.item(i);
                    String nodeName = eventElement.getNodeName();
                    if (nodeName.equalsIgnoreCase(XMLTags.EVENTDATA)) {
                        byte[] textContent = new Base64EncoderDecoder().decode(eventElement.getTextContent());
                        long date = ProtocolTools.getUnsignedIntFromBytes(textContent, 0, 4);
                        Date timeStamp = getObjectFactory().convertMeterDateToSystemDate(date);
                        int eventId = textContent[4] & 0xFF;
                        meterEvents.add(new MeterEvent(timeStamp, getEiCode(eventId), eventId, getDescription(eventId) + getOptionalValue(textContent, eventId)));
                    }
                }
                meterEvents = shiftDates(meterEvents);
            }
        } catch (ClassCastException e) {          //No event data was sent, only event config.
            if ((list.getLength() == 1) && (element.getTextContent() != null) && (!"".equals(element.getTextContent()))) {

                eventConfig = new HashMap<String, String>();
                int offset = 0;
                String data = element.getTextContent();
                while (offset < (data.length() - 2)) {
                    eventConfig.put(data.substring(offset, offset + 2), data.substring(offset + 2, offset + 3));
                    offset += 3;
                }
            }
        }
    }

    public List<MeterEvent> getMeterEvents() {   //Null if no response was received
        return meterEvents;
    }

    /**
     * Check the timestamps for the meter events, add a second to make the stamps different (if necessary)
     *
     * @param meterEvents the events
     * @return the fixed events
     */
    private List<MeterEvent> shiftDates(List<MeterEvent> meterEvents) {
        if (meterEvents.size() < 2) {
            return meterEvents;
        }
        List<MeterEvent> newMeterEvents = new ArrayList<MeterEvent>();
        newMeterEvents.add(meterEvents.get(0)); //add the first event
        MeterEvent newMeterEvent;

        for (int i = 1; i < meterEvents.size(); i++) {
            MeterEvent previousMeterEvent = meterEvents.get(i - 1);
            MeterEvent currentMeterEvent = meterEvents.get(i);
            if (currentMeterEvent.getTime().equals(previousMeterEvent.getTime())) {
                Date newDate = new Date(newMeterEvents.get(i - 1).getTime().getTime() + 1000); //add one second to make the timestamps different
                newMeterEvent = new MeterEvent(newDate, currentMeterEvent.getEiCode(), currentMeterEvent.getProtocolCode(), currentMeterEvent.getMessage());
            } else {
                newMeterEvent = currentMeterEvent;
            }
            newMeterEvents.add(i, newMeterEvent);
        }
        return newMeterEvents;
    }

    public Map<String, String> getEventConfig() {
        return eventConfig;
    }

    private String getDescription(int eventId) {
        switch (eventId) {
            case 0x00:
                return "Power fail";
            case 0x40:
                return "Power up";
            case 0x03:
                return "V850 watchdog reset";
            case 0x04:
                return "TC65 watchdog reset";
            case 0x05:
                return "Meter does not have valid time";
            case 0x06:
                return "Meter communication timeout";
            case 0x08:
                return "Phase fail";
            case 0x48:
                return "Phase fail restore";
            case 0x09:
                return "Over voltage";
            case 0x49:
                return "Over voltage restore";
            case 0x0A:
                return "3-phase power restore";
            case 0x0B:
                return "Reverse energy detected";
            case 0x4B:
                return "Reverse energy disappeared";
            case 0x0C:
                return "Force time update";
            case 0x0D:
                return "No MBus slave response";
            case 0x0E:
                return "Meter cover opened";
            case 0x0F:
                return "Magnetic field detected";
            case 0x8F:
                return "Invalid calibration data";
            case 0x10:
                return "Digital input state change";
            case 0x11:
                return "Configuration changed";
            case 0x12:
                return "PDM SMS sent";
            case 0x13:
                return "Energy being consumed while contactor is open";
            case 0x14:
                return "Power restored event";
            case 0x15:
                return "Button enabled";
            case 0x16:
                return "Contactor opened";
            case 0x17:
                return "Contactor closed";
            case 0x18:
                return "Contactor state change failed";
            case 0x28:
                return "SMS threshold exceeded";
            case 0x2A:
                return "Unexpected MBus slave replacement";
            case 0x7F:
                return "Invalid XML received";
            case 0x19:
                return "Consumption limitation start";
            case 0x1A:
                return "Consumption limitation end";
            case 0x1B:
                return "Consumption limitation user override";
            case 0x1C:
                return "Emergency consumption limitation mode start";
            case 0x1D:
                return "Emergency consumption limitation mode end";
            case 0x1E:
                return "Emergency consumption limitation user override";
            case 0x20:
                return "Gas valve open";
            case 0x21:
                return "Gas valve close";
            case 0x22:
                return "Day profile threshold set";
            case 0x23:
                return "Maximum threshold set";
            case 0x24:
                return "Emergency consumption limitation period started";
            case 0x25:
                return "Emergency consumption limitation period aborted";
            case 0x1F:
                return "OTA error";
            case 0x26:
                return "Relay open";
            case 0x27:
                return "Relay close";
            case 0x29:
                return "Time sync error";
            case 0x30:
                return "Long message activation error";
            case 0x31:
                return "Threshold out of range";
            case 0x32:
                return "Unsuccessful sign-on attempt";
            case 0x33:
                return "OTA success";
        }
        return "Unknown event code";

    }

    private String getOptionalValue(byte[] textContent, int eventId) {
        if (textContent.length <= 5) {
            return "";
        }
        String result = ", ";

        textContent = ProtocolTools.getSubArray(textContent, 5);
        if (eventId == 0x40) {
            return result + "duration: " + getInt(textContent, 0, 4) + " seconds, phase: " + (textContent[4] & 0xFF);
        } else if (eventId == 0x08 || eventId == 0x09) {
            return result + "event counter: " + getInt(textContent, 0, 2) + ", phase: " + (textContent[2] & 0xFF);
        } else if (eventId == 0x0C) {
            return result + "event counter: " + getInt(textContent, 0, 2) + ", time difference: " + getInt(textContent, 2, 4);
        } else if (eventId == 0x0D) {
            return result + "event counter: " + getInt(textContent, 0, 2) + ", Slave meter serial number: " + ProtocolTools.getHexStringFromBytes(ProtocolTools.getSubArray(textContent, 2), "");
        } else if (eventId == 0x48) {
            return result + "phase: " + (textContent[0] & 0xFF);
        } else if (eventId == 0x10) {
            return result + "digital state: " + (textContent[0] & 0xFF);
        } else if (eventId == 0x28) {
            return result + "daily threshold: " + getInt(textContent, 0, 2) + ", daily count: " + getInt(textContent, 2, 2);
        } else if (eventId == 0x1C) {
            return result + "contactor state: " + (textContent[0] & 0xFF) + ", demand at last disconnection: " + getInt(textContent, 1, 4);
        } else {
            return result + "Event counter: " + getInt(textContent);
        }
    }

    private int getInt(byte[] byteArray, int from, int length) {
        return ProtocolTools.getUnsignedIntFromBytes(byteArray, from, length);
    }

    private int getInt(byte[] byteArray) {
        return ProtocolTools.getUnsignedIntFromBytes(byteArray);
    }

    private int getEiCode(int eventId) {
        switch (eventId) {
            case 0x00:
                return MeterEvent.POWERDOWN;
            case 0x40:
                return MeterEvent.POWERUP;
            case 0x03:
                return MeterEvent.WATCHDOGRESET;
            case 0x04:
                return MeterEvent.WATCHDOGRESET;
            case 0x05:
                return MeterEvent.CLOCK_INVALID;
            case 0x06:
                return MeterEvent.OTHER;
            case 0x08:
                return MeterEvent.PHASE_FAILURE;
            case 0x48:
                return MeterEvent.OTHER;
            case 0x09:
                return MeterEvent.VOLTAGE_SWELL;
            case 0x49:
                return MeterEvent.OTHER;
            case 0x0A:
                return MeterEvent.OTHER;
            case 0x0B:
                return MeterEvent.REVERSE_RUN;
            case 0x4B:
                return MeterEvent.OTHER;
            case 0x0C:
                return MeterEvent.SETCLOCK;
            case 0x0D:
                return MeterEvent.COMMUNICATION_ERROR_MBUS;
            case 0x0E:
                return MeterEvent.COVER_OPENED;
            case 0x0F:
                return MeterEvent.STRONG_DC_FIELD_DETECTED;
            case 0x8F:
                return MeterEvent.OTHER;
            case 0x10:
                return MeterEvent.OTHER;
            case 0x11:
                return MeterEvent.CONFIGURATIONCHANGE;
            case 0x12:
                return MeterEvent.OTHER;
            case 0x13:
                return MeterEvent.OTHER;
            case 0x14:
                return MeterEvent.POWERUP;
            case 0x15:
                return MeterEvent.OTHER;
            case 0x16:
                return MeterEvent.MANUAL_DISCONNECTION;
            case 0x17:
                return MeterEvent.MANUAL_CONNECTION;
            case 0x18:
                return MeterEvent.OTHER;
            case 0x28:
                return MeterEvent.OTHER;
            case 0x2A:
                return MeterEvent.OTHER;
            case 0x7F:
                return MeterEvent.OTHER;
            case 0x19:
                return MeterEvent.LIMITER_THRESHOLD_EXCEEDED;
            case 0x1A:
                return MeterEvent.LIMITER_THRESHOLD_OK;
            case 0x1B:
                return MeterEvent.LIMITER_THRESHOLD_CHANGED;
            case 0x1C:
                return MeterEvent.LIMITER_THRESHOLD_EXCEEDED;
            case 0x1D:
                return MeterEvent.LIMITER_THRESHOLD_OK;
            case 0x1E:
                return MeterEvent.LIMITER_THRESHOLD_CHANGED;
            case 0x20:
                return MeterEvent.OTHER;
            case 0x21:
                return MeterEvent.OTHER;
            case 0x22:
                return MeterEvent.OTHER;
            case 0x23:
                return MeterEvent.LIMITER_THRESHOLD_CHANGED;
            case 0x24:
                return MeterEvent.OTHER;
            case 0x25:
                return MeterEvent.OTHER;
            case 0x1F:
                return MeterEvent.OTHER;
            case 0x26:
                return MeterEvent.MANUAL_DISCONNECTION;
            case 0x27:
                return MeterEvent.MANUAL_CONNECTION;
            case 0x29:
                return MeterEvent.OTHER;
            case 0x30:
                return MeterEvent.OTHER;
            case 0x31:
                return MeterEvent.OTHER;
            case 0x32:
                return MeterEvent.OTHER;
            case 0x33:
                return MeterEvent.OTHER;
        }
        return MeterEvent.OTHER;
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
        Element lp = doc.createElement(XMLTags.EVENTREQUEST);
        md.appendChild(lp);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    public EventData(ObjectFactory objectFactory) {
        super(objectFactory);
    }
}
