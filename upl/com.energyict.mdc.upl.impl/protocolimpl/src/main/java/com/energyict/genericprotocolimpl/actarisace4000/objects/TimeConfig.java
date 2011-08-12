package com.energyict.genericprotocolimpl.actarisace4000.objects;

import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * @author gna
 *
 */
public class TimeConfig extends AbstractActarisObject {

	public TimeConfig(ObjectFactory of) {
		super(of);
	}

    private int diff;
    private int trip;
    private int retry;

    /**
     * @return Maximum time difference allowed for clock synchronization in seconds
     */
    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    /**
     * @return Number of clock sync retries allowed
     */
    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    /**
     * @return Maximum SNTP message trip allowed in seconds
     */
    public int getTrip() {
        return trip;
    }

    public void setTrip(int trip) {
        this.trip = trip;
    }

	protected String prepareXML(){
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

		Element cf = doc.createElement(XMLTags.CONFIGURATION);
		md.appendChild(cf);
		Element ps = doc.createElement(XMLTags.TIMESYNC);
		cf.appendChild(ps);
		Element tDiff = doc.createElement(XMLTags.DIFF);
		tDiff.setTextContent(Integer.toString(diff, 16));
		ps.appendChild(tDiff);
		Element tTrip = doc.createElement(XMLTags.TRIPP);
		tTrip.setTextContent(Integer.toString(trip, 16));
		ps.appendChild(tTrip);
		Element tRetry = doc.createElement(XMLTags.RETRY);
		tRetry.setTextContent(Integer.toString(retry, 16));
		ps.appendChild(tRetry);

		String msg = convertDocumentToString(doc);
		return (msg.substring(msg.indexOf("?>")+2));
	}


	protected void parse(Element mdElement) throws IOException{
	}
}