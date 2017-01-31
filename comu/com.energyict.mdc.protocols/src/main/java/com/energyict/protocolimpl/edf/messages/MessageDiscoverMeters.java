/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edf.messages;

import com.energyict.mdc.common.ApplicationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MessageDiscoverMeters extends MessageContent {

	protected final static String ELEMENTNAME = "discoverMeters";
	protected final static String SCRIPTIDELEMENTNAME = "scriptID";

	public final static int READMETERLIST = 0;
	public final static int RESETANDREDISCOVER = 1;
    public final static int RESET = 2;
	public final static int DISCOVER = 3;

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
		if (scriptIds.getLength() != 0){
			scriptId = Integer.parseInt(scriptIds.item(0).getFirstChild().getNodeValue());
		} else {
			throw new ApplicationException("Cannot create MessageWriteRegister");
		}
	}

        public boolean isScriptIdREADMETERLIST() {
            return getScriptId()==READMETERLIST;
        }
        public boolean isScriptIdRESETANDREDISCOVER() {
            return getScriptId()==RESETANDREDISCOVER;
        }
        public boolean isScriptIdRESET() {
            return getScriptId()==RESET;
        }
        public boolean isScriptIdDISCOVER() {
            return getScriptId()==DISCOVER;
        }


	public int getScriptId() {
		return scriptId;
	}

	public void setScriptId(int scriptId) {
		this.scriptId = scriptId;
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		root.setAttribute(ORDINALATTRIBUTENAME,""+getOrdinal());
		Element scriptIdNode = document.createElement(SCRIPTIDELEMENTNAME);
		scriptIdNode.appendChild(document.createTextNode(""+scriptId));
		root.appendChild(scriptIdNode);
		return root;
	}

}
