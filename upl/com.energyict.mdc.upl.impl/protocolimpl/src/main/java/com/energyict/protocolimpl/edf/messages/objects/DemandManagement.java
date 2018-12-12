package com.energyict.protocolimpl.edf.messages.objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DemandManagement extends ComplexCosemObject {

    protected final static String ELEMENTNAME = "demandManagement";
    protected final static String MAXATTRIBUTE = "maxloadThreshold";
    protected final static String SUBSCRIBEDATTRIBUTE = "subscribedThreshold";
    protected final static String CLASSATTRIBUTE = "monitoredClass";
    protected final static String CLASSNAMEATTRIBUTE = "monitoredName";
    protected final static String ATTRIBUTEATTRIBUTE = "monitoredAttribute";
    protected final static String UPACTIONNAME = "upAction";
    protected final static String DOWNACTIONNAME = "downAction";

    private int maxloadThreshold;
    private int subscribedThreshold;
    private int monitoredClass;
    private OctetString monitoredName = new OctetString();
    private int monitoredAttribute;
    private ActionItem up = new ActionItem();
    private ActionItem down = new ActionItem();

    public DemandManagement() {
        super();
    }

    public DemandManagement(int maxloadThreshold, int subscribedThreshold) {
        super();
        this.maxloadThreshold = maxloadThreshold;
        this.subscribedThreshold = subscribedThreshold;
    }

    public DemandManagement(Element element) {
        super(element);
        maxloadThreshold = Integer.parseInt(element.getAttribute(MAXATTRIBUTE));
        subscribedThreshold = Integer.parseInt(element.getAttribute(SUBSCRIBEDATTRIBUTE));
        monitoredClass = Integer.parseInt(element.getAttribute(CLASSATTRIBUTE));
        monitoredName = new OctetString(element.getAttribute(CLASSNAMEATTRIBUTE));
        monitoredAttribute = Integer.parseInt(element.getAttribute(ATTRIBUTEATTRIBUTE));
        NodeList upNames = element.getElementsByTagName(UPACTIONNAME);
        if (upNames.getLength() != 0) {
            up = new ActionItem((Element) upNames.item(0));
        } else {
            throw new IllegalArgumentException("Cannot create DemandManagement");
        }
        NodeList downNames = element.getElementsByTagName(DOWNACTIONNAME);
        if (downNames.getLength() != 0) {
            down = new ActionItem((Element) downNames.item(0));
        } else {
            throw new IllegalArgumentException("Cannot create DemandManagement");
        }
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandManagement:\n");
        strBuff.append("   down=" + getDown() + "\n");
        strBuff.append("   maxloadThreshold=" + getMaxloadThreshold() + "\n");
        strBuff.append("   monitoredAttribute=" + getMonitoredAttribute() + "\n");
        strBuff.append("   monitoredClass=" + getMonitoredClass() + "\n");
        if (monitoredName != null) {
            strBuff.append("   monitoredName=" + getMonitoredName() + "\n");
            for (int i = 0; i < getMonitoredNameOctets().length; i++) {
                strBuff.append("       monitoredNameOctets[" + i + "]=" + getMonitoredNameOctets()[i] + "\n");
            }
        }
        strBuff.append("   subscribedThreshold=" + getSubscribedThreshold() + "\n");
        strBuff.append("   up=" + getUp() + "\n");
        return strBuff.toString();
    }

    public int getMaxloadThreshold() {
        return maxloadThreshold;
    }

    public void setMaxloadThreshold(int maxloadThreshold) {
        this.maxloadThreshold = maxloadThreshold;
    }

    public int getSubscribedThreshold() {
        return subscribedThreshold;
    }

    public void setSubscribedThreshold(int subscribedThreshold) {
        this.subscribedThreshold = subscribedThreshold;
    }

    public int getMonitoredClass() {
        return monitoredClass;
    }

    public void setMonitoredClass(int monitoredClass) {
        this.monitoredClass = monitoredClass;
    }

    public String getMonitoredName() {
        return monitoredName.convertOctetStringToString();
    }

    public void setMonitoredName(String monitoredName) {
        this.monitoredName = new OctetString(monitoredName);
    }

    public byte[] getMonitoredNameOctets() {
        return monitoredName.getOctets();
    }

    public void setMonitoredNameOctets(byte[] monitoredName) {
        this.monitoredName = new OctetString(monitoredName);
    }

    public int getMonitoredAttribute() {
        return monitoredAttribute;
    }

    public void setMonitoredAttribute(int monitoredAttribute) {
        this.monitoredAttribute = monitoredAttribute;
    }

    public ActionItem getUp() {
        return up;
    }

    public void setUp(ActionItem up) {
        this.up = up;
    }

    public ActionItem getDown() {
        return down;
    }

    public void setDown(ActionItem down) {
        this.down = down;
    }

    public Element generateXMLElement(Document document) {
        Element root = document.createElement(ELEMENTNAME);
        root.setAttribute(MAXATTRIBUTE, "" + maxloadThreshold);
        root.setAttribute(SUBSCRIBEDATTRIBUTE, "" + subscribedThreshold);
        root.setAttribute(CLASSATTRIBUTE, "" + monitoredClass);
        if (monitoredName != null) {
            root.setAttribute(CLASSNAMEATTRIBUTE, "" + monitoredName.convertOctetStringToString());
        }
        root.setAttribute(ATTRIBUTEATTRIBUTE, "" + monitoredAttribute);

        if (up != null) {
            Element upAction = document.createElement(UPACTIONNAME);
            upAction.appendChild(up.generateXMLElement(document));
            root.appendChild(upAction);
        }

        if (down != null) {
            Element downAction = document.createElement(DOWNACTIONNAME);
            downAction.appendChild(down.generateXMLElement(document));
            root.appendChild(downAction);
        }
        return root;
    }

}
