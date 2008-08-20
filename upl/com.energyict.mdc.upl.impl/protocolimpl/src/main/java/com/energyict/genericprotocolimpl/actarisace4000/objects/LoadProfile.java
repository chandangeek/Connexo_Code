/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.apache.axis.encoding.Base64;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.actarisace4000.ActarisACE4000;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
/**
 * @author gna
 *
 */
public class LoadProfile extends AbstractActarisObject {
	
	private int DEBUG = 1;
	
	private String reqString = null;
	private int trackingID;
	
	private int lpInterval;
	private ProfileData profileData;
	
	/**
	 * @param of
	 */
	public LoadProfile(ObjectFactory of) {
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
		
		Element lp = doc.createElement(XMLTags.reqLPAll);
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
		
		Element lp = doc.createElement(XMLTags.reqLP);
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
//		int s = 1212105600;
//		byte[] data = new byte[50];
//		String str = Integer.toHexString(s);
//		System.out.println(str);
		
		String mbus = "SJFxDxYFEAdnBAEDKQAAAA14ETYxNTAwMTcwQUcwMTAxMjAwDBMyBgAA";
		
		String str = "SI0TCA8cAAAD1UEAAQAAA9VBAAEAAAPVQQABAAAD1UEAAQAAA9VBAAEAAAPVQQABAAAD1UEAAQAAA9VBAAEAAAPVQQABAAAD1UEAAQAAA9VBAAEAAAPVQQABAAAD1UEAAQAAA9VBAAEAAAPVQQABAAAD1UEAAQAAA9VBAAEAAAPVQQABAAAD1UEAAQAAA9VBAAEAAAPVQQABAAAD1UEAAQAAA9VBAAEAAAPVQQACAAAD1UEAAgAAA9VBAAIAAAPVQQECAAAD1UEBAg==</LPA><LPA>SI11tA8BAAAD1UEBAg==</LPA><LPA>SI14/A8IAAAD1UEBAgAAA9VBAQIAAAPVQQACAAAD1UEAAgAAA9VBAAIAAAPVQQACAAAD1UEAAgAAA9VBAAI=</LPA><LPA>SI2V0A8BAAAD1UEBAg==</LPA><LPA>SI2YoA8CAAAD1UEAAgAAA9VBAAI=";
		String str2 = "QOfxQA8GAAAowwAAAAEBAAQAAwEADgAFAQAVAAsC+1sAAAIAVAARAg==";
		String str3 = "QOfxQA8GAAAOWWABAQAAKMcAAEwEAACjVAAUBAAAo6gALAgabI0UAAAIAASOZABEC";
		String str4 = "SKFmKA8OAAAD4kEAAgAAA+NBAAIAAAPkQQACAAAD5UEAAgAAA+ZBAAIAAAPnQQACAAAD6EEAAgAAA+lBAAIAAAPqQQACAAAD60EAAgAAA+xBAAIAAAPtQQACAAAD7kEAAgAAA+9BAAI=";
		
		ActarisACE4000 aace = new ActarisACE4000();
		ObjectFactory of = new ObjectFactory(aace);
		LoadProfile lp = new LoadProfile(of);
		try {
			lp.setABSLoadProfile(str4);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (BusinessException e) {
			e.printStackTrace();
		}
//		lp.setLoadProfile(str);
	}

	protected void setElement(Element mdElement) throws DOMException, IOException, SQLException, BusinessException {
		if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.loadPr)){
			setLoadProfile(mdElement.getTextContent());
		}
		else if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.loadPrAbs)){
			setABSLoadProfile(mdElement.getTextContent());
		}
	}
	
	private void setLoadProfile(String data){
		int offset = 0;
		int length = 0;
		byte[] decoded = Base64.decode(data);
		if(DEBUG >= 1)System.out.println(new String(decoded));
		
		long timeStamp = (long)(getNumberFromB64(decoded, offset, 4))*1000;
		if(DEBUG >= 1)System.out.println(new Date(timeStamp));
		offset+=4;
		
		setLpInterval((getNumberFromB64(decoded, offset, 1))*60);
		offset+=1;
		if(DEBUG >= 1)System.out.println("Interval = " + getLpInterval());
		length = getNumberFromB64(decoded, offset, 1);
		offset+=1;
		if(DEBUG >= 1)System.out.println("IntervalCount = " + length);
		int first = getNumberFromB64(decoded, offset, 4);
		offset+=4;
		if(DEBUG >= 1)System.out.println("First: " + first);
		for(int i = 0; i < length; i++){
			System.out.println("Value " + i + ": " +getNumberFromB64(decoded, offset, 2));
			offset+=2;
			System.out.println("Alarm " + i + ": " +getNumberFromB64(decoded, offset, 2));
			offset+=2;
			System.out.println("Active Rate " + i + ": " +getNumberFromB64(decoded, offset, 1));
			offset+=1;
		}
	}
	
	private void setABSLoadProfile(String data) throws IOException, SQLException, BusinessException{
		int offset = 0;
		int length = 0;
		Calendar intervalCalendar = Calendar.getInstance();
		byte[] decoded = Base64.decode(data);
		if(DEBUG >= 1)System.out.println(new String(decoded));
		
		long timeStamp = (long)(getNumberFromB64(decoded, offset, 4))*1000;
		intervalCalendar.setTimeInMillis(timeStamp);
		if(DEBUG >= 1)System.out.println(new Date(timeStamp));
		offset+=4;
		
		setLpInterval((getNumberFromB64(decoded, offset, 1))*60);
		offset+=1;
		if(DEBUG >= 1)System.out.println("Interval = " + getLpInterval());
		
		length = getNumberFromB64(decoded, offset, 1);
		offset+=1;
		if(DEBUG >= 1)System.out.println("IntervalCount = " + length);
		
		// check if the interval matches the interval configured on the meter
		if(getObjectFactory().getAace().getMeterProfileInterval() == getLpInterval()){
			IntervalData id = null;
			int value, alarm, tariff;
			
			// just add one channel
			if(getProfileData().getChannelInfos().size() == 0)
				getProfileData().addChannel(getDefaultChannelInfo());
			
			for(int i = 0; i < length; i++){
				value = getNumberFromB64(decoded, offset, 4); offset+=4;
				alarm = getNumberFromB64(decoded, offset, 2); offset+=2;
				tariff = getNumberFromB64(decoded, offset, 1); offset+=1;
				id = new IntervalData(intervalCalendar.getTime(), 0, 0, tariff);
				id.addValue(value);
				intervalCalendar.add(Calendar.SECOND, getLpInterval());
				getProfileData().addInterval(id);
			}
		}
		else{
			throw new ApplicationException("Actaris ACE4000, pushedLoadProfile, interval did not match, EIServer: "
					+getObjectFactory().getAace().getMeterProfileInterval() + "s , Meter: " + getLpInterval() + "s");
		}
			
		if(DEBUG >=1)System.out.println(getProfileData());
		
		if(getTrackingID() != -1)
			getObjectFactory().sendAcknowledge(getTrackingID());
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
		return new ChannelInfo(0, "Actaris ACE4000 Channel 1", Unit.get(BaseUnit.WATTHOUR));
	}
}
