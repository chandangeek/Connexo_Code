package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.cbo.*;
import com.energyict.cpo.Transaction;
import com.energyict.genericprotocolimpl.actarisplcc3g.MessagePair;
import com.energyict.genericprotocolimpl.lgadvantis.collector.Collector;
import com.energyict.genericprotocolimpl.lgadvantis.encoder.Encoder;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edf.messages.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

/**
 *  
 * 
 * EDF maquette specific code.  
 * 
 * PowerQuality objects are not returned correctly yet by Actaris and Iskra 
 * meters.
 * 
 * To mimic correct behaviour during the Maquette 'default' values will be 
 * returned.  These are hard coded constants.
 * 
 * Code related to this will be marked EdfMq (Edf Maquette)
 *  
 *
 */

class ReadMeterTransaction implements Transaction {

	private Concentrator concentrator;
	private CommunicationProfile communicationProfile;

	private List meterList;
	private Device meter;

	private Date newLastReading = null;
	private Date newLastLogbook = null;

	ReadMeterTransaction(Concentrator protocol, Device concentrator, EeItem meter)
	throws BusinessException {

		this.concentrator = protocol;
		this.communicationProfile = protocol.getCommunicationProfile();

		this.meterList = find(meter);

	}

	private List find(EeItem meter) throws BusinessException {

		String serial = meter.getIdent();
		DeviceFactory rtuF = MeteringWarehouse.getCurrent().getDeviceFactory();

		return rtuF.findBySerialNumber(serial);

	}

	public Object doExecute() throws BusinessException, SQLException {

		Iterator i = meterList.iterator();

		while( i.hasNext() ) {

			this.meter = (Device)i.next();

			debug( "ReadMeter: " + meter.getFullName() );

			Task task = new Task( meter );

			boolean profile     = communicationProfile.getReadDemandValues();
			boolean registers   = communicationProfile.getReadMeterReadings();
			boolean writeClock  = communicationProfile.getWriteClock();

			if (profile || registers)
				addScheduledRead(task, profile, registers, writeClock);

			if (communicationProfile.getSendRtuMessage())
				addMessages(task);

			try {

				debug( "Posting : " + task );
				String result = post( task.toDirectAction() );
				debug( "Processing : "+ task );
				if (result != null){ // no actions to process
					task.setResult( result );
					debug( "" + task );
					store(task);
				}
				debug( "Processing : "+ task );				

			} catch (IOException e) {
				e.printStackTrace();
				throw new BusinessException( e );
			} 

			debug( "ReadMeter: " + meter.getFullName() + " DONE \n\n" );

		}

		return null;

	}

	/** Scheduled reads add DirectActions for reading 
	 *      - Profile
	 *      - Registers
	 */
	private void addScheduledRead(
			Task task, 
			boolean readProfile, boolean readRegisters, boolean writeTime ) 
	throws BusinessException, SQLException {

		if( readRegisters ) {

			Iterator i = getCosemFactory().allAttributes().iterator();
			while( i.hasNext() ) {
				CosemAttribute attribute = (CosemAttribute)i.next();
				task.add( new DirectAction( attribute, meter.getSerialNumber() ) );
			}

		}

		if( readProfile ) {

			addMessage(new MessageReadBillingValues( ), null, task);
			addMessage(new MessageReadLogBook( ), null, task);

		}


	}

	/* add a Task with collection of directations */
	private void addMessages(Task task) throws BusinessException, SQLException {
		List messagePairs = new ArrayList();

		for (Iterator it = meter.getPendingMessages().iterator(); it.hasNext();){
			RtuMessage msg = (RtuMessage) it.next();
			messagePairs.add(new MessagePair(msg,MessageContentFactory.createMessageContent(msg.getContents())));
		}

		Collections.sort(messagePairs);

		for (Iterator jt = messagePairs.iterator();jt.hasNext();){
			MessagePair msgPair = (MessagePair) jt.next();
			RtuMessage msg = msgPair.getRtuMessage();
			String content = msg.getContents();
			MessageContent mc = MessageContentFactory.createMessageContent(content);
			addMessage(mc, msg, task);

		}


	}

	private boolean isDefinedOnRtu( Cosem cosem ) {
		return meter.getRegister( cosem.getObisCode() ) != null;
	}

	private void warn(String obisCode) {
		debug( obisCode + " not supported by protocol.");
	}

	/** Set periods on a list of directactions ( S-: ) */
	private List set( List directActions, TimePeriod timePeriod ) {

		Iterator i = directActions.iterator();
		while (i.hasNext()) {
			DirectAction action = (DirectAction) i.next();
			action.setTimePeriod(timePeriod);
		}

		return directActions;

	}

	/** Add a SINGLE directaction. */
	private void addMessage( MessageContent mc, RtuMessage rtuMessage, Task task ) 
	throws BusinessException, SQLException {

		CosemFactory rf = getCosemFactory();

		if (mc instanceof MessageReadIndexes) {

			// special provisions for industrial meter
			if  (meter.getSerialNumber().substring(4, 6).equals("10")){
				try {
					concentrator.post(ConstructXmlFiles.getIndustrialRead(meter.getSerialNumber()));
					rtuMessage.confirm();
				} catch (IOException e) {
					e.printStackTrace();
					throw new BusinessException(e);
				}
			} else {

				RtuMessageLink link = task.addMessageLink(mc, rtuMessage );

				if( isDefinedOnRtu(rf.getActiveEnergySumm() ) ) 
					link.addAll( 
							task.addReadActionFor( rf.getActiveEnergySumm() ) );

				if( isDefinedOnRtu(rf.getActiveEnergyTou1() ) ) 
					link.addAll( 
							task.addReadActionFor( rf.getActiveEnergyTou1() ) );

				if( isDefinedOnRtu(rf.getActiveEnergyTou2() ) ) 
					link.addAll( 
							task.addReadActionFor( rf.getActiveEnergyTou2() ) );

				if( isDefinedOnRtu(rf.getActiveEnergyTou3() ) ) 
					link.addAll( 
							task.addReadActionFor( rf.getActiveEnergyTou3() ) );

				if( isDefinedOnRtu(rf.getActiveEnergyTou4() ) ) 
					link.addAll( 
							task.addReadActionFor( rf.getActiveEnergyTou4() ) );

				if( isDefinedOnRtu(rf.getActiveEnergyTou5() ) ) 
					link.addAll( 
							task.addReadActionFor( rf.getActiveEnergyTou5() ) );

				if( isDefinedOnRtu(rf.getActiveEnergyTou6() ) ) 
					link.addAll( 
							task.addReadActionFor( rf.getActiveEnergyTou6() ) );
			}
			return;

		}

		if (mc instanceof MessageReadBillingValues) {

			MessageReadBillingValues mrp = (MessageReadBillingValues) mc;

			Date from   = (mrp.getFrom()!=null) ? mrp.getFrom() : getLastReading();
			Date to     = (mrp.getTo()!=null)   ? mrp.getTo()   : new Date();

			if (mrp.getFrom()==null && mrp.getTo()==null) {
				newLastReading = to;
			} else {
				newLastReading = null;
			}

			TimePeriod period = new TimePeriod( from, to );

			CosemAttribute cPeriod     = rf.getCapturePeriod();
			CosemAttribute billing     = rf.getDaylyEnergyValueProfileBuffer();

			RtuMessageLink link = task.addMessageLink(mc, rtuMessage );

			link.addAll( task.addReadActionFor(cPeriod) );
			link.addAll( set( task.addReadActionFor(billing), period ) );

			return;

		}

		if (mc instanceof MessageReadLoadProfiles) {

			MessageReadLoadProfiles mrp = (MessageReadLoadProfiles) mc;

			Date from   = (mrp.getFrom()!=null) ? mrp.getFrom() : getLastReading();
			Date to     = (mrp.getTo()!=null)   ? mrp.getTo()   : new Date();

			if (mrp.getFrom()==null && mrp.getTo()==null) {
				newLastReading = to;
			} else {
				newLastReading = null;
			}

			TimePeriod period = new TimePeriod( from, to );

			CosemAttribute loadProfile = rf.getLoadProfileBuffer();
			CosemAttribute cPeriod     = rf.getCapturePeriod();

			RtuMessageLink link = task.addMessageLink(mc, rtuMessage );

			link.addAll( task.addReadActionFor(cPeriod) );
			link.addAll( set( task.addReadActionFor(loadProfile), period ) );

			return;

		}

		if (mc instanceof MessageReadLogBook) {

			MessageReadLogBook mrl = (MessageReadLogBook) mc;

			Date from   = (mrl.getFrom()!=null) ? mrl.getFrom() : getLastLogbook();
			Date to     = (mrl.getTo()!=null)   ? mrl.getTo()   : new Date();

			if (mrl.getFrom()==null && mrl.getTo()==null) {
				newLastLogbook = to;
			} else {
				newLastLogbook = null;
			}

			TimePeriod period = new TimePeriod( from, to );

			CosemAttribute logBook = rf.getLogbookBuffer();
			RtuMessageLink link = task.addMessageLink(mc, rtuMessage );
			link.addAll( set( task.addReadActionFor(logBook ), period ) );

			return;

		}

		if (mc instanceof MessageReadRegister) {

			MessageReadRegister mrr = (MessageReadRegister) mc;
			Cosem r = getCosemFactory().findByObisCode( mrr.getObisCode() );


			if( isDefinedOnRtu( r ) ) {

				RtuMessageLink link = task.addMessageLink(mc, rtuMessage );
				link.addAll( task.addReadActionFor( r ) );

			} else {

				warn( mrr.getObisCode() );
				rtuMessage.setFailed();

			}

			return;

		}

		if (mc instanceof MessageReadRegisterList) {

			MessageReadRegisterList mrrl = (MessageReadRegisterList) mc;
			List obisCodes = mrrl.getObisCodes();
			CosemList cosemList = new CosemList();
			for (Iterator it = obisCodes.iterator(); it.hasNext(); ){
				String obisCode = (String) it.next();
				Cosem r = getCosemFactory().findByObisCode(obisCode);
				if( isDefinedOnRtu( r ) ) {
					cosemList.add(r);
				} else {
					warn( obisCode );
					rtuMessage.setFailed();
					return;
				}
			}
			RtuMessageLink link = task.addMessageLink(mc, rtuMessage );
			link.addAll( task.addReadActionFor( cosemList ) );
			return;
		}

		if (mc instanceof MessageWriteRegister) {
			MessageWriteRegister mwr = (MessageWriteRegister) mc;

			// special provisions for industrial meter
			String industrialTag = meter.getSerialNumber().substring(4, 6);
			if  (industrialTag.equals("10")){
				try {
					concentrator.post(ConstructXmlFiles.getIndustrialWrite(meter.getSerialNumber(), mwr.getValue().toString()));
					rtuMessage.confirm();
				} catch (IOException e) {
					e.printStackTrace();
					throw new BusinessException(e);
				}
			} else {

				Cosem r = getCosemFactory().findByObisCode( mwr.getObisCode() );

				RtuMessageLink link = task.addMessageLink(mc, rtuMessage );
				List list = task.addWriteActionFor( r );

				Iterator i = list.iterator();

				while (i.hasNext()) {
					DirectAction action = (DirectAction) i.next();

					Encoder encoder = action.getCosem().getEncoder();
					if( encoder != null )
						action.setAbstractDataType( encoder.encode( mwr.getValue() ) );
					System.out.println( ""+ action + " " + action.getAbstractDataType() );
				}

				link.addAll( list);
			}
			return;

		}

		if (mc instanceof MessageExecuteAction) {

			MessageExecuteAction mea = (MessageExecuteAction) mc;

			Cosem r = getCosemFactory().findByObisCode(mea.getObisCode() );

			RtuMessageLink link = task.addMessageLink(mc, rtuMessage );
			List list = task.addWriteActionFor( r );
			DirectAction action = (DirectAction)list.get(0);

			Encoder encoder = action.getCosem().getEncoder();
			if( encoder != null )
				action.setAbstractDataType( encoder.encode( mea.getMethodData() ) );

			link.addAll( list);

			return;

		}

	}

	private Date getLastReading() {

		if (meter.getLastReading() != null) {
			return meter.getLastReading();
		} else {
			return yesterday();
		}

	}

	private Date getLastLogbook() {

		if (meter.getLastLogbook() != null) {
			return meter.getLastLogbook();
		} else {
			return yesterday();
		}

	}


	private void store( Task task ) throws BusinessException, SQLException {

		Iterator i = task.getRtuMessageLinks().iterator();
		while( i.hasNext() ) {

			RtuMessageLink link = (RtuMessageLink) i.next();
			storeMessage(task, link);

		}

	}


	private void storeMessage(Task task, RtuMessageLink link) 
	throws BusinessException, SQLException {

		/** EdfMq */
		if( isPowerQualityConfig() ) {

			Iterator i = link.getDirectactions().iterator();
			while (i.hasNext()) {

				DirectAction directAction = (DirectAction) i.next();
				int shortName = directAction.getCosem().getShortName();

				if( useDefaultValue(meter, shortName) ) {

					store(  getDefaultValue(shortName) );

					link.confirm();
					debug( "stored (dv) " + link );
					return;
				}
			}
		}

		if( link.allCplStatusOk() && !task.isBadData() ) {

			Iterator i = link.getDirectactions().iterator();
			while (i.hasNext()) {

				DirectAction directAction = (DirectAction) i.next();

				Collector c = directAction.getCosem().getCollector();
				store( c.getAll(task, link) );

			}

			if (link.getMessageContent() instanceof MessageReadLoadProfiles){
				updateLastReadingIfRequired();
			}
			if (link.getMessageContent() instanceof MessageReadLogBook){
				updateLastLogbookIfRequired();
			}

			link.confirm();
			debug( "stored " + link );

		} else {

			link.setFailed();
			debug( "NOT STORED (failed or bad data) " + link );
		}

	}

	/** EdfMq */
	private RegisterValue getDefaultValue( int shortName ) {

		Cosem c = getCosemFactory().getThresholdForSagAttribute();
		if( shortName == c.getShortName() ) {

			ObisCode oc = c.getObisCode();
			Quantity q = new Quantity( new BigDecimal( 207 ), Unit.getUndefined() );

			return new RegisterValue( oc, q );
		}

		c = getCosemFactory().getThresholdForSwellAttribute();
		if( shortName == c.getShortName() ) {

			ObisCode oc = c.getObisCode();
			Quantity q = new Quantity( new BigDecimal( 257 ), Unit.getUndefined() );

			return new RegisterValue( oc, q );
		}

		c = getCosemFactory().getTimeIntegralForSagMeasurementAttribute();
		if( shortName == c.getShortName() ) {

			ObisCode oc = c.getObisCode();
			Quantity q = new Quantity( new BigDecimal( 1 ), Unit.getUndefined() );

			return new RegisterValue( oc, q );
		}

		c = getCosemFactory().getTimeThresholdForLongPowerFailureAttribute();
		if( shortName == c.getShortName() ) {

			ObisCode oc = c.getObisCode();
			Quantity q = new Quantity( new BigDecimal( 180 ), Unit.getUndefined() );

			return new RegisterValue( oc, q );
		}

		c = getCosemFactory().getTimeIntegralForInstantaneousDemandAttribute();
		if( shortName == c.getShortName() ) {

			ObisCode oc = c.getObisCode();
			Quantity q = new Quantity( new BigDecimal( 1 ), Unit.getUndefined() );

			return new RegisterValue( oc, q );
		}

		c = getCosemFactory().getTimeIntegralForSwellMeasurementAttribute();
		if( shortName == c.getShortName() ) {

			ObisCode oc = c.getObisCode();
			Quantity q = new Quantity( new BigDecimal( 1 ), Unit.getUndefined() );

			return new RegisterValue( oc, q );
		}

		return null;

	}

	/** EdfMq */
	private boolean useDefaultValue( Device meter, int shortName ) {

		if( isPowerQualityConfig() ) {

			Cosem c = getCosemFactory().getThresholdForSagAttribute();
			if( shortName == c.getShortName() ) {
				return true;
			}

			c = getCosemFactory().getThresholdForSwellAttribute();
			if( shortName == c.getShortName() ) {
				return true;
			}

			c = getCosemFactory().getTimeIntegralForSagMeasurementAttribute();
			if( shortName == c.getShortName() ) {
				return true;
			}

			c = getCosemFactory().getTimeThresholdForLongPowerFailureAttribute();
			if( shortName == c.getShortName() ) {
				return true;
			}

			c = getCosemFactory().getTimeIntegralForInstantaneousDemandAttribute();
			if( shortName == c.getShortName() ) {
				return true;
			}

			c = getCosemFactory().getTimeIntegralForSwellMeasurementAttribute();
			if( shortName == c.getShortName() ) {
				return true;
			}
		}

		return false;

	}

	private boolean isPowerQualityConfig( ) {
		String pk = Constant.PK_POWER_QUALITY_CONFIG;
		return "1".equals( meter.getProperties().getProperty(pk) );
	}

	private Date yesterday() {

		long yesterday = System.currentTimeMillis();
		yesterday -= 86400;
		yesterday -= (yesterday % 86400);

		return new Date(yesterday);

	}

	private CosemFactory getCosemFactory() {
		return concentrator.getCosemFactory();
	}

	private void debug(String msg) {
		concentrator.getLogger().info(msg);
		System.out.println(msg);
	}

	private String post(String msg) throws IOException {

		debug("SENT MSG: " + msg);

		String result = concentrator.post(msg);

		debug("RCVD MSG: " + result);

		return result;

	}

	private void store( ReadResult result ) 
	throws SQLException, BusinessException {

		Iterator i = result.getRegisterValues().iterator();
		while (i.hasNext()) {
			store( (RegisterValue) i.next() );
		}

		if( result.getProfileData() != null ) {
			meter.store( result.getProfileData() );
		}

	}

	private void store( RegisterValue registerValue ) throws SQLException, BusinessException {
		RtuRegister rr = meter.getRegister(registerValue.getObisCode());
		if (rr != null) rr.store(registerValue);

	}

	private void updateLastReadingIfRequired() throws SQLException, BusinessException {
		if (newLastReading != null) {
			meter.updateLastReading(newLastReading);
		}
	}

	private void updateLastLogbookIfRequired() throws SQLException, BusinessException {
		if (newLastLogbook != null) {
			meter.updateLastLogbook(newLastLogbook);
		}
	}


}
