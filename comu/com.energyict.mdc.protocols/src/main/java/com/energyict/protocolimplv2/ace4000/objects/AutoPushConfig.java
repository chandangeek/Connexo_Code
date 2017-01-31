/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author gna
 *
 */
public class AutoPushConfig extends AbstractActarisObject {

    private int enableState;
    private int open;
    private int close;
    private boolean random;
    private int retryWindowPercentage;

    public AutoPushConfig(ObjectFactory of) {
		super(of);
	}

    public void setClose(int close) {
        this.close = close;
    }

    public void setEnableState(int enableState) {
        this.enableState = enableState;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    public void setRetryWindowPercentage(int retryWindowPercentage) {
        this.retryWindowPercentage = retryWindowPercentage;
    }

    protected String prepareXML(){
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
		Element ps = doc.createElement(XMLTags.PUSHSCHEDULE);
		cf.appendChild(ps);
		Element enable = doc.createElement(XMLTags.ENABLESTATE);
		enable.setTextContent(Integer.toString(enableState, 16));
		ps.appendChild(enable);
		Element tmo = doc.createElement(XMLTags.TIMEOPEN);
		tmo.setTextContent(Integer.toString(open, 16));
		ps.appendChild(tmo);
		Element tmc = doc.createElement(XMLTags.TIMECLOSE);
		tmc.setTextContent(Integer.toString(close, 16));
		ps.appendChild(tmc);
		if(random){
			Element rand = doc.createElement(XMLTags.RANDOM);
			ps.appendChild(rand);
		} else {
            Element rwin = doc.createElement(XMLTags.RWIN);
            rwin.setTextContent(Integer.toString(retryWindowPercentage, 16));
            ps.appendChild(rwin);
        }

		String msg = convertDocumentToString(doc);

		return (msg.substring(msg.indexOf("?>")+2));
	}

	protected void parse(Element element) {
        //No parsing
	}
}