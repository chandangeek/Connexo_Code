package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 10/08/11
 * Time: 13:18
 */
public class ConsumptionLimitationConfiguration extends AbstractActarisObject {

    public ConsumptionLimitationConfiguration(ObjectFactory of) {
        super(of);
    }

    @Override
    protected void parse(Element element) {
        //Only ack or nack is sent back
    }

    private Date date = null;
    private int numberOfSubIntervals;
    private int subIntervalDuration;
    private int ovlRate;
    private int thresholdTolerance;
    private int thresholdSelection;
    private List<String> switchingMomentsDP0;
    private List<Integer> thresholdsDP0;
    private List<Integer> unitsDP0;
    private List<String> actionsDP0;
    private List<String> switchingMomentsDP1;
    private List<Integer> thresholdsDP1;
    private List<Integer> unitsDP1;
    private List<String> actionsDP1;
    private List<Integer> weekProfile;

    public void setNumberOfSubIntervals(int numberOfSubIntervals) {
        this.numberOfSubIntervals = numberOfSubIntervals;
    }

    public void setSubIntervalDuration(int subIntervalDuration) {
        this.subIntervalDuration = subIntervalDuration;
    }

    public void setActionsDP0(List<String> actionsDP0) {
        this.actionsDP0 = actionsDP0;
    }

    public void setActionsDP1(List<String> actionsDP1) {
        this.actionsDP1 = actionsDP1;
    }

    public void setOvlRate(int ovlRate) {
        this.ovlRate = ovlRate;
    }

    public void setSwitchingMomentsDP0(List<String> switchingMomentsDP0) {
        this.switchingMomentsDP0 = switchingMomentsDP0;
    }

    public void setSwitchingMomentsDP1(List<String> switchingMomentsDP1) {
        this.switchingMomentsDP1 = switchingMomentsDP1;
    }

    public void setThresholdsDP0(List<Integer> thresholdsDP0) {
        this.thresholdsDP0 = thresholdsDP0;
    }

    public void setThresholdsDP1(List<Integer> thresholdsDP1) {
        this.thresholdsDP1 = thresholdsDP1;
    }

    public void setThresholdSelection(int thresholdSelection) {
        this.thresholdSelection = thresholdSelection;
    }

    public void setThresholdTolerance(int thresholdTolerance) {
        this.thresholdTolerance = thresholdTolerance;
    }

    public void setUnitsDP0(List<Integer> unitsDP0) {
        this.unitsDP0 = unitsDP0;
    }

    public void setUnitsDP1(List<Integer> unitsDP1) {
        this.unitsDP1 = unitsDP1;
    }

    public void setWeekProfile(List<Integer> weekProfile) {
        this.weekProfile = weekProfile;
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
        Element consumptionLimitElement = doc.createElement(XMLTags.CONSLIMITCONFIG);
        if (date != null) {
            consumptionLimitElement.setAttribute(XMLTags.TIME_ATTR, getHexDate(date));
        }
        cf.appendChild(consumptionLimitElement);

        Element numberOfIntervalsElement = doc.createElement(XMLTags.SUBINTERVALS);
        numberOfIntervalsElement.setTextContent(pad(Integer.toString(numberOfSubIntervals)) + pad(Integer.toString(subIntervalDuration)));
        consumptionLimitElement.appendChild(numberOfIntervalsElement);

        Element el = doc.createElement(XMLTags.CONSRATE);
        el.setTextContent(Integer.toString(ovlRate));
        consumptionLimitElement.appendChild(el);
        el = doc.createElement(XMLTags.CONSTOLERANCE);
        el.setTextContent(pad(Integer.toString(thresholdTolerance, 16)));
        consumptionLimitElement.appendChild(el);
        el = doc.createElement(XMLTags.CONSTHRESHOLD);
        el.setTextContent(pad(Integer.toString(thresholdSelection)));
        consumptionLimitElement.appendChild(el);
        el = doc.createElement(XMLTags.CONS_DP0);
        el.setTextContent(calcDP0());
        consumptionLimitElement.appendChild(el);
        el = doc.createElement(XMLTags.CONS_DP1);
        el.setTextContent(calcDP1());
        consumptionLimitElement.appendChild(el);
        el = doc.createElement(XMLTags.CONS_WEEKPROFILE);
        el.setTextContent(calcWP());
        consumptionLimitElement.appendChild(el);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    private String pad(String text, int length) {
        while (text.length() < length) {
            text = "0" + text;
        }
        return text;
    }

    private String calcDP1() {
        String result = "";
        for (int i = 0; i < 8; i++) {
            result += switchingMomentsDP1.get(i);
            String threshold = pad(Integer.toString(thresholdsDP1.get(i), 16), 8);
            int first = Integer.parseInt(threshold.substring(0, 2), 16) & 0xFF;
            first = (first | (0x80 * unitsDP1.get(i))) & 0xFF;   //Set bit indicating the unit
            result += pad(Integer.toString(first, 16)) + threshold.substring(2, 8);
            result += actionsDP1.get(i);
        }
        return result;
    }

    private String calcDP0() {
        String result = "";
        for (int i = 0; i < 8; i++) {
            result += switchingMomentsDP0.get(i);
            String threshold = pad(Integer.toString(thresholdsDP0.get(i), 16), 8);
            int first = Integer.parseInt(threshold.substring(0, 2), 16) & 0xFF;
            first = (first | (0x80 * unitsDP0.get(i))) & 0xFF;   //Set bit indicating the unit
            result += pad(Integer.toString(first, 16)) + threshold.substring(2, 8);
            result += actionsDP0.get(i);
        }
        return result;
    }

    private String calcWP() {
        String result = "";
        for (Integer integer : weekProfile) {
            result += pad(Integer.toString(integer));
        }
        return result;
    }

    private String pad(String text) {
        if (text.length() == 1) {
            text = "0" + text;
        }
        return text;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}