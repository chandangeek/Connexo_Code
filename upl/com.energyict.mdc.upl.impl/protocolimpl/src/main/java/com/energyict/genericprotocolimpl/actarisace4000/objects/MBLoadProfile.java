/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.axis.encoding.Base64;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.actarisace4000.ActarisACE4000;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.core.DataRecord;
import com.energyict.protocolimpl.mbus.core.ValueInformationfieldCoding;
/**
 * @author gna
 *
 */
public class MBLoadProfile extends AbstractActarisObject {
	
	private int DEBUG = 1;
	
	private String reqString = null;
	private int trackingID;
	
	private int lpInterval;
	private ProfileData profileData;
	
	/**
	 * @param of
	 */
	public MBLoadProfile(ObjectFactory of) {
		super(of);
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.actarisace4000.objects.AbstractActarisObject#getReqString()
	 */
	protected String getReqString() {
		return reqString;
	}
	
	private void setReqString(String reqString){
		this.reqString = reqString;
	}
	
	/**
	 * Request all loadProfileIntervals
	 */
	protected void prepareXML(){
		
		Document doc = createDomDocument();
		
		Element root = doc.createElement(XMLTags.mPull);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.meterData);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.serialNumber);
		s.setTextContent(getObjectFactory().getAace().getNecessarySerialnumber());
		md.appendChild(s);
		Element t = doc.createElement(XMLTags.tracker);
		t.setTextContent(String.valueOf(trackingID));
		md.appendChild(t);
		
		Element lp = doc.createElement(XMLTags.reqMBAllData);
		md.appendChild(lp);
		
		String msg = convertDocumentToString(doc);
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}
	/**
	 * Request the loadProfile from the requested date
	 * @param from
	 * TODO the form is not correct yet! will not work!
	 */
	protected void prepareXML(Date from){
		
		//TODO
		
		Document doc = createDomDocument();
		
		Element root = doc.createElement(XMLTags.mPull);
		doc.appendChild(root);
		Element md = doc.createElement(XMLTags.meterData);
		root.appendChild(md);
		Element s = doc.createElement(XMLTags.serialNumber);
		s.setTextContent(getObjectFactory().getAace().getNecessarySerialnumber());
		md.appendChild(s);
		Element t = doc.createElement(XMLTags.tracker);
		t.setTextContent(String.valueOf(trackingID));
		md.appendChild(t);
		
		Element lp = doc.createElement(XMLTags.reqMBrange);
		lp.setTextContent(Long.toHexString(from.getTime()/1000)+Long.toHexString(System.currentTimeMillis()/1000));
		md.appendChild(lp);
		
		String msg = convertDocumentToString(doc);
		setReqString(msg.substring(msg.indexOf("?>")+2));
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.actarisace4000.objects.AbstractActarisObject#getTrackingID()
	 */
	protected int getTrackingID() {
		return trackingID;
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.actarisace4000.objects.AbstractActarisObject#setTrackingID(int)
	 */
	protected void setTrackingID(int trackingID) {
		this.trackingID = trackingID;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String str = "Q/H/f3hWNBJ3BAsESgAAAAx4AAAAAAQGGSgAAAwWhyMAAAstIgAACzsAGAAKWmcBCl5WAQthCQEABG0PAs0CAicGAAn9DhAJ/Q8YDwAI";
		
		ActarisACE4000 aace = new ActarisACE4000();
		ObjectFactory of = new ObjectFactory(aace);
		MBLoadProfile mblp = new MBLoadProfile(of);
		try {
			mblp.setMBLoadProfile(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void setElement(Element mdElement) throws DOMException, IOException, SQLException, BusinessException {
		NodeList list = mdElement.getChildNodes();
		
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			if(element.getNodeName().equalsIgnoreCase(XMLTags.mbusRaw)){
				setMBLoadProfile(element.getTextContent());
			}
		}
	}
	
	private void setMBLoadProfile(String data) throws IOException{
		int offset = 0;
		byte[] decoded = Base64.decode(data);
		
		long timeStamp = (long)(getNumberFromB64(decoded, offset, 4))*1000;
		if(DEBUG >= 1)System.out.println(new Date(timeStamp));
		offset+=4;
		
		//TODO get the timeZone of the meter, if it is null, get a default one
//		CIField72h ciField72h = new CIField72h(getObjectFactory().getAace().getMeter().getDeviceTimeZone());
		CIField72h ciField72h = new CIField72h(TimeZone.getDefault());
		byte[] rawFrame = new byte[decoded.length-offset];
		System.arraycopy(decoded, offset, rawFrame, 0, rawFrame.length);
		try {
			ciField72h.parse(rawFrame);
			List dataRecords = ciField72h.getDataRecords();
			if(DEBUG >= 1){
				Iterator it = dataRecords.iterator();
				while(it.hasNext()){
					DataRecord dRecord = (DataRecord)it.next();
					System.out.println(dRecord);
				}
			}
			
			//TODO verify allot of stuff, just added some data
			if(true){
				DataRecord record;
				ValueInformationfieldCoding vInfo;
				
				IntervalData id = null;
				int value;
				
				// TODO add the proper channel, you can get the unit from the mbus frame
				if(getProfileData().getChannelInfos().size() == 0)
					getProfileData().addChannel(getDefaultChannelInfo());
				
				for(Object dataRecord : dataRecords){
					record = (DataRecord) dataRecord;
					
					if(record.getQuantity() != null){
						id = new IntervalData(new Date(timeStamp), 0);
						id.addValue(record.getQuantity().getAmount());
						getProfileData().addInterval(id);
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to parse the RawMBusFrame." + e.getMessage());
		}
	}

	public int getLpInterval() {
		return lpInterval;
	}

	private void setLpInterval(int lpInterval) {
		this.lpInterval = lpInterval;
	}

	public ProfileData getProfileData(){
		if(profileData == null)
			profileData = new ProfileData();
		return profileData;
	}
	
	private ChannelInfo getDefaultChannelInfo(){
		return new ChannelInfo(0, 0, "Actaris ACE4000 MBUS Channel 1", Unit.get(BaseUnit.CUBICMETER));
	}
}
