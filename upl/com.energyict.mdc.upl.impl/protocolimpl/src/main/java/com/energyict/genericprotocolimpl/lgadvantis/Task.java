package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.cbo.TimePeriod;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.OldDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edf.messages.MessageContent;
import com.energyict.protocolimpl.edf.messages.objects.ActivityCalendar;
import com.energyict.xml.xmlhelper.DomHelper;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.*;

public class Task {

	private String target;

	private int sInterval;
	private ProfileData profileData;
	private List registerValues;

	private ActivityCalendar activityCalendar;

	private List actionMap;
	private List rtuMessageLinks;

	private boolean badData = false;

	Task( ) {

		profileData      = new ProfileData(  );

		registerValues   = new ArrayList();
		activityCalendar = new ActivityCalendar();

		actionMap        = new ArrayList();
		rtuMessageLinks  = new ArrayList();

	}

	Task(Device meter) {
		this( );
		this.target = meter.getSerialNumber();

		profileData.setChannelInfos( Constant.toChannelInfos(meter) );

	}

	public String getTarget() {
		return target;
	}

	DirectPrimitive add( DirectPrimitive directPrimitive ) {

		actionMap.add(directPrimitive);
		return directPrimitive;

	}

	Collection getRtuMessageLinks( ){
		return rtuMessageLinks;
	}

	boolean isBadData() {
		return badData;
	}

	void setBadData(boolean badData) {
		this.badData = badData;
	}

	RtuMessageLink addMessageLink( MessageContent mc, OldDeviceMessage message ) {

		RtuMessageLink link = new RtuMessageLink(mc).setRtuMessage(message);
		rtuMessageLinks.add(link);

		return link;

	}

	RtuMessageLink addMessageLink( 
			MessageContent mc, OldDeviceMessage message, DirectAction action ) {

		return addMessageLink(mc, message).add(action);

	}

	RtuMessageLink addMessageLink( 
			MessageContent mc, OldDeviceMessage message, List actionsToAdd ){

		return addMessageLink(mc, message).addAll(actionsToAdd);

	}

	String toDirectAction( ) throws IOException {

		DomHelper dh = new DomHelper(  XmlTag.RELEVE_CPL );
		dh.getRootElement().setAttribute(XmlTag.VERSION, "1.0");

		Element rq = dh.addElement( XmlTag.TELEREL_RQ );
		rq.setAttribute(XmlTag.IDENT, "040000000000");

		if( actionMap.size() > 0 ){

			Element wr = dh.addElement(rq, XmlTag.WRITE_RQ); 
			Element ec = dh.addElement(wr, XmlTag.EXP_CMD);

			Iterator i = actionMap.iterator();
			while( i.hasNext() ) {
				DirectPrimitive directPrimitive = (DirectPrimitive)i.next();
				directPrimitive.toXmlElement(dh, ec);
			}

		} else {
			return null;
		}

		return dh.toXmlString();

	}

	List addActionFor( Cosem cosem, boolean write ) {
		return cosem.addMeTo(this, write);
	}

	List addActionFor(CosemList list, boolean write) {
		DirectAccess da = new DirectAccess(list, target);
		
		List result = new ArrayList( );
		result.add( add( da ) );

		return result;	
		
	}
	
	List addActionFor(CosemObject object, boolean write ){
		List result = new ArrayList( );
		if (write){
			// still divide in attributes and methods because there is no multiple write
			
			Iterator i = object.getAttributes().iterator();
			while( i.hasNext() ) {

				CosemAttribute attribute = (CosemAttribute)i.next();
				if(  write && attribute.isWriteable() ) {
					result.addAll( addActionFor(attribute, write) );
				}

			}

			i = object.getMethods().iterator();
			while( i.hasNext() ) {

				CosemMethod method = (CosemMethod)i.next();
				result.addAll( addActionFor( method ) );

			}

		} else {
			// allow objects to be scheduled for multiple reads
			DirectAccess da = new DirectAccess(object, target);
			da.setWrite(write);
			result.add( add( da ) );
		}

		return result;	

	}   

	List addActionFor(CosemAttribute attribute, boolean write ){
		return addActionFor(attribute, write, null);
	}

	List addActionFor(CosemMethod method, boolean write){
		return addActionFor( method, null );
	}

	List addActionFor(CosemAttribute attribute, boolean write, TimePeriod period ){
		DirectAction da = new DirectAction(attribute, target);
		da.setWrite(write);

		if( period != null )
			da.setTimePeriod( period );

		List result = new ArrayList( );
		result.add( add( da ) );

		return result;
	}

	List addActionFor(CosemMethod method, TimePeriod period){
		DirectAction da = new DirectAction(method, target);
		da.setWrite(true);

		if( period != null )
			da.setTimePeriod(period);

		List result = new ArrayList( );
		result.add( add( da ) );

		return result;
	}

	List addReadActionFor( Cosem cosem ) {
		return addActionFor( cosem, false );
	}

	List addWriteActionFor( Cosem cosem ){
		return addActionFor( cosem, true );
	}

	List addActionFor(CosemMethod method){
		return addActionFor( method, null );
	}

	void setResult(String xml ) throws IOException {

		DirectPrimitiveSaxHandler.parse( xml, this);

		Iterator i = actionMap.iterator();

		while( i.hasNext() ) {

			DirectPrimitive primitive = (DirectPrimitive)i.next();
			Cosem cosem = primitive.getCosem();

			if( primitive.isOk() ) {
				if (primitive.getAbstractDataType() != null){
					cosem.parse(primitive.getAbstractDataType(), this);
				} else {
					if (primitive.getBinaryData() != null){
						cosem.parse(primitive.getBinaryData(), this);
					}
				}
			} else { 
				System.out.println( primitive + " NOT parseable " );
			}
		}
	}

	public Iterator getActionMapIterator(){
		return actionMap.iterator();
	}
	
	public DirectAction getDirectAction( int id, String type ) {
		Iterator i = actionMap.iterator();
		while( i.hasNext() ) {
			DirectAction directaction = (DirectAction)i.next();

			if( directaction.getShortName() == id && 
					directaction.getType().equals(type) )

				return directaction;

		}
		return null;
	}

	public DirectAction get( ObisCode searchedObisCode ){

		Iterator i = actionMap.iterator();
		while( i.hasNext() ) {

			DirectAction da = (DirectAction) i.next();    
			ObisCode cosemObis = da.getCosem().getObisCode();

			if( cosemObis.equals( searchedObisCode ) ) 
				return da;

		}

		return null;

	}

	public void setInterval( int interval ) {
		this.sInterval = interval;
	}

	public int getInterval( ) {
		return sInterval;
	}

	public ProfileData getProfileData( ) {
		return profileData;
	}

	public ActivityCalendar getActivityCalendar( ){
		return activityCalendar;
	}

	public Task addRegisterValue( RegisterValue registerValue ) {
		registerValues.add(registerValue);
		return this;
	}

	public List findRegisterValues( ObisCode obisCode, boolean checkF ) {

		if( obisCode == null ) return null;

		Iterator i = registerValues.iterator();
		List result = new ArrayList();

		while( i.hasNext() ) {

			RegisterValue rv = (RegisterValue)i.next();
			ObisCode rvOc = rv.getObisCode();

			boolean bingo = true;

			bingo &= ( rvOc.getA() == obisCode.getA() );
			bingo &= ( rvOc.getB() == obisCode.getB() );
			bingo &= ( rvOc.getC() == obisCode.getC() );
			bingo &= ( rvOc.getD() == obisCode.getD() );
			bingo &= ( rvOc.getE() == obisCode.getE() );

			if( checkF )
				bingo &= rvOc.getF() == obisCode.getF();

			if( bingo )
				result.add( rv );

		}

		return result;

	}


	public String toString( ) {

		StringBuffer result = new StringBuffer();

		result.append( "Task [" ).append( "\n" );

		Iterator i = (Iterator) rtuMessageLinks.iterator();
		while( i.hasNext( ) )
			result.append( "" + i.next() + "\n" );

		result.append( "\n]\n");

		return result.toString();

	}




}
