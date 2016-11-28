package com.energyict.protocolimpl.edf.messages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MessageDiscoverMeters extends MessageContent {

    protected static final String ELEMENTNAME = "discoverMeters";
    protected static final String SCRIPTIDELEMENTNAME = "scriptID";

    public static final int READMETERLIST = 0;
    public static final int RESETANDREDISCOVER = 1;
    public static final int RESET = 2;
    public static final int DISCOVER = 3;

    private int scriptId;

    public MessageDiscoverMeters() {
        super();
    }

    public MessageDiscoverMeters(int scriptId) {
        super();
        this.scriptId = scriptId;
    }

    public MessageDiscoverMeters(Element element) {
        super(element);
        NodeList scriptIds = element.getElementsByTagName(SCRIPTIDELEMENTNAME);
        if (scriptIds.getLength() != 0) {
            scriptId = Integer.parseInt(scriptIds.item(0).getFirstChild().getNodeValue());
        } else {
            throw new IllegalArgumentException("Cannot create MessageWriteRegister");
        }
    }

    public boolean isScriptIdREADMETERLIST() {
        return getScriptId() == READMETERLIST;
    }

    public boolean isScriptIdRESETANDREDISCOVER() {
        return getScriptId() == RESETANDREDISCOVER;
    }

    public boolean isScriptIdRESET() {
        return getScriptId() == RESET;
    }

    public boolean isScriptIdDISCOVER() {
        return getScriptId() == DISCOVER;
    }

    public int getScriptId() {
        return scriptId;
    }

    public void setScriptId(int scriptId) {
        this.scriptId = scriptId;
    }

    public Element generateXMLElement(Document document) {
        Element root = document.createElement(ELEMENTNAME);
        root.setAttribute(ORDINALATTRIBUTENAME, "" + getOrdinal());
        Element scriptIdNode = document.createElement(SCRIPTIDELEMENTNAME);
        scriptIdNode.appendChild(document.createTextNode("" + scriptId));
        root.appendChild(scriptIdNode);
        return root;
    }

}
