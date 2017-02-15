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
public class SyncTime extends AbstractActarisObject {

	private long meterTime;
	private long receiveTime;

    /**
     * @param of
     */
    public SyncTime(ObjectFactory of) {
        super(of);
    }

    public long getMeterTime() {
        return meterTime;
    }

    public void setMeterTime(long meterTime) {
        this.meterTime = meterTime;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

	/**
	 * Sync the meter time to the system time
	 */
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

		Element ft = doc.createElement(XMLTags.SYNCTIME);
		md.appendChild(ft);
		Element t1 = doc.createElement(XMLTags.TIME1);
		t1.setTextContent(Long.toHexString(getMeterTime() / 1000));
		ft.appendChild(t1);
		Element t2 = doc.createElement(XMLTags.TIME2);
		t2.setTextContent(Long.toHexString(getReceiveTime()/1000));
		ft.appendChild(t2);
		Element t3 = doc.createElement(XMLTags.TIME3);
		t3.setTextContent(Long.toHexString(getObjectFactory().getCurrentMeterTime().getTime() / 1000));    //System time, in the meter time zone!
		ft.appendChild(t3);

		String msg = convertDocumentToString(doc);
		return (msg.substring(msg.indexOf("?>")+2));
	}

	protected void parse(Element mdElement) {
        //Results are parsed in the DateTime class
	}
}