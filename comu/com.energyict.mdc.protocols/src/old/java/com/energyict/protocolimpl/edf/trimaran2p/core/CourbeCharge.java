/*
 * CourbeCharge.java
 *
 * Created on 2 maart 2007, 9:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran2p.core;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class CourbeCharge {

    final int DEBUG=0;

    private TrimaranObjectFactory trimaranObjectFactory;
    private List elements;
    private List meterEvents = new ArrayList();
    private Date now;
    private int elementCount = 0;
    private boolean boundryExceed = false;
    private boolean dontAdd = false;
    private boolean reactive = true;
    private boolean corrupted = false;
    private Iterator elementIterator;

    final int ELEMENT_BEGIN=-1;
    final int ELEMENT_PUISSANCE=0;
    final int ELEMENT_PUISSANCE_TRONQUE=1;
    final int ELEMENT_DATATION_HEURE=2;
    final int ELEMENT_DATATION_DATE=3;
    final int ELEMENT_DATATION_MINUTE_SECONDE=4;
    final int ELEMENT_DATATION_POSTE=5;
    final int STATE_PUISSANCE=0;
    final int STATE_OLD_TIME=1;
    final int STATE_NEW_TIME=2;

    private ProfileData profileData=null;

    /** Creates a new instance of CourbeCharge */
    public CourbeCharge(TrimaranObjectFactory trimaranObjectFactory) {
        this.setTrimaranObjectFactory(trimaranObjectFactory);
    }

    public String toString() {
         StringBuffer strBuff = new StringBuffer();
         for(int i=0;i<getElements().size();i++) {
             strBuff.append("value "+i+" = 0x"+Integer.toHexString(((Integer)getElements().get(i)).intValue()));
             if (i<(getElements().size()-1))
                 strBuff.append(", ");
         }
         return strBuff.toString();
    }

    private int getProfileInterval() throws IOException {
       if (getTrimaranObjectFactory() == null)
           return 600;
       else
           return getTrimaranObjectFactory().getTrimaran().getProfileInterval();
    }

    private TimeZone getTimeZone() {
       if (getTrimaranObjectFactory() == null)
           return TimeZone.getTimeZone("ECT");
       else
           return getTrimaranObjectFactory().getTrimaran().getTimeZone();
    }

    protected void initCollectionsTEC() throws IOException {
        setElements(new ArrayList());
        setProfileData(new ProfileData());
        getProfileData().addChannel((new ChannelInfo(0,"Trimaran 2P TEC P+ channel", Unit.get("kW"), 0, 0, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        getProfileData().addChannel((new ChannelInfo(1,"Trimaran 2P TEC P- channel", Unit.get("kW"), 0, 1, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        if(getTrimaranObjectFactory().readParameters().isCcReact()){
        	 getProfileData().addChannel((new ChannelInfo(2,"Trimaran 2P TEC Q1(Q+) channel", Unit.get("kvar"), 0, 2, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        	 getProfileData().addChannel((new ChannelInfo(3,"Trimaran 2P TEC Q3(Q-) channel", Unit.get("kvar"), 0, 3, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        	 getProfileData().addChannel((new ChannelInfo(4,"Trimaran 2P TEC Q2(Q+) channel", Unit.get("kvar"), 0, 4, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        	 getProfileData().addChannel((new ChannelInfo(5,"Trimaran 2P TEC Q4(Q-) channel", Unit.get("kvar"), 0, 5, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        }
    }

    protected void initCollectionsTEP() throws IOException {
        setElements(new ArrayList());
        setProfileData(new ProfileData());
        getProfileData().addChannel((new ChannelInfo(0,"Trimaran 2P TEP Q channel", Unit.get("kvar"), 0, 0, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        getProfileData().addChannel((new ChannelInfo(1,"Trimaran 2P TEP P channel", Unit.get("kW"), 0, 1, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
    }

    private void waitUntilCopied() throws IOException { // max 20 sec
        int retry=0;
        while(retry++<15) {
            try {
                Thread.sleep(1000);
                AccessPartiel ap = getTrimaranObjectFactory().readAccessPartiel();
                if (DEBUG>=2) System.out.println("GN_DEBUG> "+ap);
                if (ap.getNomAccess() == 0){
                	return;
                }
            }
            catch(InterruptedException e) {
                // absorb
            }
        }
        throw new IOException("CourbeCharge, Error! Already waiting 20 sec for copy of the load profile data!");
    }

    public void collect(Date from, Date to) throws IOException {
    	if(to == null)
	    	now = new Date();
    	else
    		now = to;
    	Date collectTime = from;
        if( getTrimaranObjectFactory().getTrimaran().isTECMeter())
        	initCollectionsTEC();	// only the tec version has this on
        else if( getTrimaranObjectFactory().getTrimaran().isTEPMeter())
        	initCollectionsTEP();	// only the tep version has this on
        do {
        	if (DEBUG>=1) System.out.println("GN_DEBUG> Retreive data from: " + collectTime);
            retrieve(collectTime);
            collectTime = getProfileData().getIntervalData(getProfileData().getIntervalDatas().size()-1).getEndTime();
        }while(Math.abs(collectTime.getTime()/1000-now.getTime()/1000) > getTrimaranObjectFactory().getTrimaran().getProfileInterval());

        // if the connection of data takes more then the profileinterval, a duplicate interval will occur
        aggregateAndRemoveDuplicates();

        if (DEBUG >= 1) System.out.println(getProfileData());
    } //  public void collect(Date from) throws IOException


    protected void aggregateAndRemoveDuplicates() {
        IntervalData ivd2Check=null;
        Iterator it = getProfileData().getIntervalDatas().iterator();
        while(it.hasNext()) {
            IntervalData ivd = (IntervalData)it.next();
            if (ivd2Check !=null) {
                if (ivd.getEndTime().compareTo(ivd2Check.getEndTime())==0) {
                    if ((ivd.getEiStatus() != 0) || (ivd2Check.getEiStatus() != 0)) {
                        List intervalValues = ivd2Check.getIntervalValues();
                        IntervalValue iv = (IntervalValue)intervalValues.get(0);
                        Integer i = new Integer(ivd.get(0).intValue()+ivd2Check.get(0).intValue());
                        iv.setNumber(i);
                    }
                    it.remove(); // remove ivd
                }
            }
            ivd2Check = ivd;
        }
    }
    private int fileCounter = 0;
    private void retrieve(Date from) throws IOException {
        getTrimaranObjectFactory().writeAccessPartiel(from);
        waitUntilCopied();
        int[] values = getTrimaranObjectFactory().getCourbeChargePartielle().getValues();
//        int[] values = getTrimaranObjectFactory().getCourbeChargePartielle2().getValues();

//    	System.out.println("GN_DEBUG> write to file");
//    	File file = new File("c://TEST_FILES/201Profile_" + fileCounter+".bin");
//    	fileCounter++;
//    	FileOutputStream fos = new FileOutputStream(file);
//    	ObjectOutputStream oos = new ObjectOutputStream(fos);
//    	oos.writeObject(values);
//    	oos.close();
//    	fos.close();

        addValues(values);
        doParse();
    }

    private void retrieve() throws IOException {
//        getTrimaranObjectFactory().writeAccessPartiel(elementId);
        getTrimaranObjectFactory().writeAccessPartiel(now);
        waitUntilCopied();
        int[] values = getTrimaranObjectFactory().getCourbeChargePartielle().getValues();

//    	System.out.println("GN_DEBUG> write to file");
//    	File file = new File("c://TEST_FILES/080307000201Profile.bin");
//    	FileOutputStream fos = new FileOutputStream(file);
//    	ObjectOutputStream oos = new ObjectOutputStream(fos);
//    	oos.writeObject(values);
//    	oos.close();
//    	fos.close();

        addValues(values);
        doParse();
    } // public void collect(int range) throws IOException


    protected void addValues(int[] values) throws IOException {
        List temp = new ArrayList();
        for (int i = 0; i< values.length; i++) {
            temp.add(new Integer(values[i]));
        }
        setElements(new ArrayList());
        getElements().addAll(0, temp);

    } // private void addValues(int[] values) throws IOException

    public void doParse() throws IOException {
        int previousElement=ELEMENT_BEGIN;
        int currentElement=ELEMENT_BEGIN;
        int type=0;
        int state=STATE_PUISSANCE;
        int tariff=0;
        int elementOffset=0;
        int i=0;
        reactive = true;

        Calendar calSetClock=null;
        Calendar cal = null;
        IntervalData intervalData=null;
        List intervalDatas=new ArrayList();
        meterEvents = new ArrayList();

        if (DEBUG>=2)
        	System.out.println("GN_DEBUG> load profile up to now="+now);

        setElementIterator(getElements().iterator());

        while(getElementIterator().hasNext()) {
            int val = ((Integer)getElementIterator().next()).intValue();
            if (DEBUG>=2){
            	i++;
            	if(val == 49151)
            		System.out.println("STOP");
            }

            if ((val & 0x8000) == 0) {
                if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", val="+val);
                val = getValue(val);
                if (cal != null) {
                    cal.add(Calendar.SECOND,getProfileInterval());
                    currentElement=ELEMENT_PUISSANCE;
                    state = STATE_PUISSANCE;
                    // bit 14..0 Valeur de la puissance sans coupure

                    if (now.after(cal.getTime())) {
                        intervalData = new IntervalData(new Date(cal.getTime().getTime()),0,0,tariff);
                        intervalData.addValue(new Integer(val));
                        addRestOfIntervals(intervalData);
                        if(corrupted){
                        	corrupted = false;
                        	intervalData.addEiStatus(IntervalStateBits.CORRUPTED);
                        }
                        if(!dontAdd){
                        	intervalDatas.add(intervalData);
                        }
                        else
                        	dontAdd = false;
                    }
                }
                type = 0;
            }
            else if ((val & 0xC000) == 0x8000) {
                // bit 13..0 Valeur de la puissance avec coupure (é tronquée )
                val = getValue(val);
                if (DEBUG>=2) System.out.println("KV_DEBUG> "+i+", shortlong, val="+val);
                if (cal != null) {
                	if(type == 0) // only add when no event has occured
                		cal.add(Calendar.SECOND,getProfileInterval());
                	else{
                		if(intervalDatas.size()!=0){
                			if(!boundryExceed){
                				cal.setTime(((IntervalData)intervalDatas.get(intervalDatas.size()-1)).getEndTime());
                			} else {
                				cal.add(Calendar.SECOND, getProfileInterval());
                				boundryExceed = false;
                			}
                		}
                	}

                    currentElement=ELEMENT_PUISSANCE_TRONQUE;
                    state = STATE_PUISSANCE;

                    if (now.after(cal.getTime())) {
                        intervalData = new IntervalData(new Date(cal.getTime().getTime()),0,0,tariff);
                		intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
                		if (type == 6) intervalData.addEiStatus(IntervalStateBits.POWERUP);
                		intervalData.addValue(new Integer(val));
                		addRestOfIntervals(intervalData);
                        if(corrupted){
                        	corrupted = false;
                        	intervalData.addEiStatus(IntervalStateBits.CORRUPTED);
                        }
//                		meterEvents.add(new MeterEvent(cal.getTime(),MeterEvent.POWERDOWN));
                        if(!dontAdd){
                        	intervalDatas.add(intervalData);
                        }
                        else
                        	dontAdd = false;
                        type = 0;
                    }
                }
            }
            // ************************************************************************************************************************
            // ************************************************ ELEMENT_DATATION_DATE ************************************************
            // ************************************************************************************************************************
            else if ((val & 0xE000) == 0xC000) {

                currentElement=ELEMENT_DATATION_DATE;
                // element date
                // bit 12..9 chiffre des unités de l'année bit 8..5 mois bit 4..0 jour
                int year = (val & 0x1E00) >> 9;
                int month = (val & 0x01E0) >> 5;
                int day = (val & 0x001F);

                if ((elementOffset>0) && (cal == null)) {
                    if (DEBUG>=2) System.out.println("KV_DEBUG> set calendar date");
                }

                cal = ProtocolUtils.getCleanCalendar(getTimeZone());
                cal.set(Calendar.YEAR,year > 50?1990+year:2000+year);
                cal.set(Calendar.MONTH,month-1);
                cal.set(Calendar.DAY_OF_MONTH,day);

                if (DEBUG>=2) System.out.println("GN_DEBUG> *********** "+i+", cal="+cal.getTime());

            }
            // ************************************************************************************************************************
            // ************************************************ ELEMENT_DATATION_HEURE ************************************************
            // ************************************************************************************************************************
            else if ((val & 0xF000) == 0xE000) {
                currentElement=ELEMENT_DATATION_HEURE;
                // element heure
                // bit 11..9 type 8..4 heure bit 3..0 minutes en multiples de Tc
                type = (val & 0x0E00) >> 9;
                int hour = (val & 0x01F0) >> 4;
//              // La minute doit été multiplié avec 5mn pour le TEC et 10mn pour le TEP!!
                int minutes = 0;
                if(getTrimaranObjectFactory().getTrimaran().isTECMeter())
                	minutes = (val & 0x000F)*5;
                else if(getTrimaranObjectFactory().getTrimaran().isTEPMeter())
                	minutes = (val & 0x000F)*10;
                if (cal != null) {
                    cal.set(Calendar.HOUR_OF_DAY,hour);
                    cal.set(Calendar.MINUTE,minutes);
                }

                if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", type=0x"+Integer.toHexString(type)+", cal="+(cal!=null?""+cal.getTime():"no start calendar"));

                if (type == 0) { // every hour
                    // heure ronde ou changement de jour tarifaire ; dans le cas d'une heure ronde seule, l'élément-date n'est pas
                    // inséré ; ce type de marquage n'est fait que s'il n'y a pas d'autre marquage faire la méme date
                }
                else if (type == 1) { // timeset
                    // remise e l'heure ou changement d'heure legale ; dans ce cas, deux marquages sont effectues, un avec
                    // l'ancienne heure et un avec la nouvelle heure (element-date et element-heure e chaque fois) ; pour chacun
                    // un enregistrement complementaire est effectue pour donner la valeur des minutes et des secondes de la date
                    // marquee (element-minute/seconde)
                }
                else if (type == 2) {
                    // suppressions des puissances réactives
                }
                else if (type == 3) {
                    // introductions des puissances réactives
                }
                else if (type == 4) {
                    // changements de valeur de paramètres (TC, TT, KJ, KF, KPr, RL, XL, Kep)

                	// possible to uncomment these again... see with testing
//                	intervalData.addEiStatus(IntervalStateBits.CONFIGURATIONCHANGE);
//                	meterEvents.add(new MeterEvent(cal.getTime(),MeterEvent.CONFIGURATIONCHANGE));
                }
                else if (type == 5) {
                    // changement de valeur de Tc(élément date et heure)
                }
                else if (type == 6) {

                        if (now.after(cal.getTime())) {
                        	val = ((Integer)getElementIterator().next()).intValue();
                        	val = getValue(val);
                            intervalData = new IntervalData(new Date(cal.getTime().getTime()),0,0,tariff);
                    		intervalData.addEiStatus(IntervalStateBits.POWERUP);
                    		meterEvents.add(new MeterEvent(cal.getTime(),MeterEvent.POWERUP));
                            intervalData.addValue(new Integer(val));
                            addRestOfIntervals(intervalData);
                            if(corrupted){
                            	corrupted = false;
                            	intervalData.addEiStatus(IntervalStateBits.CORRUPTED);
                            }
                            if(!dontAdd){
                            	intervalDatas.add(intervalData);
                            }
                            else
                            	dontAdd = false;
                        }

                    // retour de lealimentation reseau apres une coupure ; si la duree de la coupure excede la reserve de marche, la
                    // date enregistree correspond au 1er Janvier 1992, et l'heure enregistree est 00h00
                }
                else if (type == 7) {
                    // multi-marquage. Dans ce cas, un enregistrement complementaire est effectue pour preciser les marquages.
                    // Le multi-marquage ne concerne pas le marquage de e remise e l'heure e ou de e changement d'heure
                    // legale e qui est effectue independamment du reste.
                }
            }
            // ************************************************************************************************************************
            // ****************************** ELEMENT_DATATION_MINUTE_SECONDE or ELEMENT_DATATION_POSTE *******************************
            // ************************************************************************************************************************
            else if ((val & 0xF000) == 0xF000) {
                if (previousElement == ELEMENT_DATATION_HEURE) {
                    if (type == 0) {
                        // heure ronde ou changement de jour tarifaire ; dans le cas d'une heure ronde seule, l'element-date n'est pas
                        // insere ; ce type de marquage n'est fait que s'il n'y a pas d'autre marquage e faire e la meme date ;
                    }
                    else if (type == 1) {

                        currentElement=ELEMENT_DATATION_MINUTE_SECONDE;

                        if ((previousElement == ELEMENT_DATATION_HEURE) && (state == STATE_PUISSANCE)) {
                             currentElement=ELEMENT_DATATION_MINUTE_SECONDE;
                             int minute = (val & 0x0FC0)>>6;
                             int seconde = (val & 0x003F);
                             calSetClock = (Calendar)cal.clone();
                             calSetClock.set(Calendar.MINUTE,minute);
                             calSetClock.set(Calendar.SECOND,seconde);
                             meterEvents.add(new MeterEvent(calSetClock.getTime(),MeterEvent.SETCLOCK_BEFORE));
                             state = STATE_OLD_TIME;
                             if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", minute="+minute+", seconde="+seconde);
                        }
                        else if ((previousElement == ELEMENT_DATATION_HEURE) && (state == STATE_OLD_TIME)) {
                             currentElement=ELEMENT_DATATION_MINUTE_SECONDE;
                             int minute = (val & 0x0FC0)>>6;
                             int seconde = (val & 0x003F);
                             calSetClock = (Calendar)cal.clone();
                             calSetClock.set(Calendar.MINUTE,minute);
                             calSetClock.set(Calendar.SECOND,seconde);
                             meterEvents.add(new MeterEvent(calSetClock.getTime(),MeterEvent.SETCLOCK_AFTER));
                             state = STATE_NEW_TIME;
                             if(!calSetClock.getTime().before(((IntervalData)intervalDatas.get(intervalDatas.size()-1)).getEndTime()))
                            	 boundryExceed = true;
                             if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", minute="+minute+", seconde="+seconde);
                        }
                        else if ((previousElement == ELEMENT_DATATION_MINUTE_SECONDE) && (state == STATE_NEW_TIME)) {
                             currentElement=ELEMENT_DATATION_POSTE;
                             int mode = (val & 0x0040) >> 6;
                             int config = (val & 0x0E00) >> 9;
                             int marquage = (val & 0x003F);
                             state = STATE_PUISSANCE;
                             if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", Mode="+mode+", config="+config+", marquage="+marquage);
                             tariff=val&0xFFF;
                        }
                    }
                    else if (type == 2) {
                        currentElement=ELEMENT_DATATION_POSTE;
                        state = STATE_PUISSANCE;
                        multiMarquage(val, cal);

                        if(getTrimaranObjectFactory().getTrimaran().isTEPMeter()){
                        	//*******************************
                        	// Only with TEP meter
                        	//*******************************
                        	if (now.after(cal.getTime())){
                        		intervalData = directionChange(cal, tariff);
                                if(corrupted){
                                	corrupted = false;
                                	intervalData.addEiStatus(IntervalStateBits.CORRUPTED);
                                }
                        	}
                        	if(!dontAdd)
                        		intervalDatas.add(intervalData);
                        	else
                        		dontAdd = false;
                        	type = 0;
                        }
                    }
                    else if (type == 3) {
                        currentElement=ELEMENT_DATATION_POSTE;
                        state = STATE_PUISSANCE;
                        multiMarquage(val, cal);

                        if(getTrimaranObjectFactory().getTrimaran().isTEPMeter()){
                        	//*******************************
                        	// Only with TEP meter
                        	//*******************************
                        	if (now.after(cal.getTime())){
                        		intervalData = directionChange(cal, tariff);
                                if(corrupted){
                                	corrupted = false;
                                	intervalData.addEiStatus(IntervalStateBits.CORRUPTED);
                                }
                        	}
                        	if(!dontAdd)
                        		intervalDatas.add(intervalData);
                        	else
                        		dontAdd = false;
                        	type = 0;
                        }

                    }
                    else if (type == 4) {
                        currentElement=ELEMENT_DATATION_POSTE;
                        state = STATE_PUISSANCE;
                        multiMarquage(val, cal);

                        if(getTrimaranObjectFactory().getTrimaran().isTEPMeter()){
                        	//*******************************
                        	// Only with TEP meter
                        	//*******************************
                        	if (now.after(cal.getTime())){
                        		intervalData = directionChange(cal, tariff);
                                if(corrupted){
                                	corrupted = false;
                                	intervalData.addEiStatus(IntervalStateBits.CORRUPTED);
                                }
                        	}
                        	if(!dontAdd)
                        		intervalDatas.add(intervalData);
                        	else
                        		dontAdd = false;
                        	type = 0;
                        }
                    }
                    else if (type == 5) {
                        currentElement=ELEMENT_DATATION_POSTE;
                        state = STATE_PUISSANCE;
                        multiMarquage(val, cal);
                    }
                    else if (type == 6) {
                        currentElement=ELEMENT_DATATION_POSTE;
                        state = STATE_PUISSANCE;
                        multiMarquage(val, cal);
                    }
                    else if (type == 7) {
                        state = STATE_PUISSANCE;
                        multiMarquage(val, cal);
                    }
                    else {
                       throw new IOException("Courbecharge, parse(), invalid element 0x"+Integer.toHexString(val)+", type="+type+", currentElement="+currentElement+", previousElement="+previousElement);
                    }
                } // if (previousElement == ELEMENT_DATATION_HEURE)
            } // else if ((val & 0xF000) == 0xF000)
            else {
               throw new IOException("Courbecharge, parse(), invalid element 0x"+Integer.toHexString(val)+", currentElement="+currentElement+", previousElement="+previousElement);
            }
            previousElement = currentElement;

            if (cal==null) {
                elementOffset++;
                getElementIterator().remove();
            }

        } // while(count<getValues().length)

        meterEvents.addAll(getProfileData().getMeterEvents());				// first get the events and add them to the local events
        getProfileData().setMeterEvents(meterEvents);						// then add the events back to the profileData
        if (DEBUG >= 2) System.out.println(getProfileData().getMeterEvents());
        getProfileData().getIntervalDatas().addAll(mergeDuplicateIntervals(intervalDatas));
        getProfileData().sort();
        if (DEBUG >= 2) System.out.println(getProfileData());

    } // public void doParse() throws IOException

    private IntervalData directionChange(Calendar cal, int tariff) throws IOException{
    	IntervalData[] intervals = {null, null};
    	IntervalData intervalData = null;
		intervals[0] = new IntervalData(new Date(cal.getTime().getTime()),0,0,tariff);
		intervals[1] = new IntervalData(new Date(cal.getTime().getTime()),0,0,tariff);

//		int val = (((Integer)getElementIterator().next()).intValue());
		int val = getValue(((Integer)getElementIterator().next()).intValue());
		intervals[0].addValue(new Integer(val));
		intervals[0] = addRestOfIntervals(intervals[0]);
//		val = (((Integer)getElementIterator().next()).intValue());
		val = getValue(((Integer)getElementIterator().next()).intValue());
		intervals[1].addValue(new Integer(val));
		intervals[1] = addRestOfIntervals(intervals[1]);

		intervalData = new IntervalData(new Date(cal.getTime().getTime()),0,0,tariff);
		intervalData.addEiStatus(IntervalStateBits.OTHER);
		Iterator it0 = ((IntervalData)intervals[0]).getValuesIterator();
		Iterator it1 = ((IntervalData)intervals[1]).getValuesIterator();
		while(it0.hasNext()){
			intervalData.addValue(new Integer(((IntervalValue)it0.next()).getNumber().intValue() + ((IntervalValue)it1.next()).getNumber().intValue()));
		}
    	return intervalData;
    }

    private IntervalData addRestOfIntervals(IntervalData intervalData) throws IOException{
    	for(int i = 0; i < getTrimaranObjectFactory().getTrimaran().getNumberOfChannels() -1; i++){
    		if(getElementIterator().hasNext()){
				int val = getValue(((Integer)getElementIterator().next()).intValue());
				intervalData.addValue(new Integer(val));
    		}
    		else
    			dontAdd = true;
    	}
    	return intervalData;
    }

    private void multiMarquage(int value, Calendar cal){
        int mode = (value & 0x0040) >> 6;
        int config = (value & 0x0E00) >> 9;
        int marquage = (value & 0x003F);
        switch(marquage){
        case 1: meterEvents.add(new MeterEvent(cal.getTime(), MeterEvent.OTHER, "Changement de sens de transit ou sens indéterminé"));break;
        case 2: meterEvents.add(new MeterEvent(cal.getTime(), MeterEvent.CONFIGURATIONCHANGE));break;
        case 8: meterEvents.add(new MeterEvent(cal.getTime(), MeterEvent.CONFIGURATIONCHANGE, "Changement de temp d'intégration"));break;
        case 16: meterEvents.add(new MeterEvent(cal.getTime(), MeterEvent.POWERUP));break;
        case 32: meterEvents.add(new MeterEvent(cal.getTime(), MeterEvent.OTHER, "Changement de mode"));break;
        }
    }

    private List mergeDuplicateIntervals(List intervalDatas) {

    	List mergedIntervals = new ArrayList();
    	Iterator it = intervalDatas.iterator();
    	IntervalData intervalData = new IntervalData();
    	IntervalData newIntervalData = new IntervalData();
    	IntervalData previousIntervalData = null;
    	while(it.hasNext()){
    		intervalData = (IntervalData)it.next();
    		if(previousIntervalData == null)
    			newIntervalData = intervalData;
    		else{
    			if(previousIntervalData.getEndTime().getTime() == intervalData.getEndTime().getTime()){
    				newIntervalData = new IntervalData(previousIntervalData.getEndTime());
    				newIntervalData.addEiStatus(previousIntervalData.getEiStatus()|intervalData.getEiStatus());
    				Iterator itValuesP = previousIntervalData.getValuesIterator();
    				Iterator itValuesC = intervalData.getValuesIterator();
    				while(itValuesP.hasNext()){
    					newIntervalData.addValue(new Integer(((IntervalValue)itValuesP.next()).getNumber().intValue() + ((IntervalValue)itValuesC.next()).getNumber().intValue()));
    				}
    			}
    			else{
    				newIntervalData = intervalData;
    				mergedIntervals.add(previousIntervalData);
    			}
    		}

    		previousIntervalData = newIntervalData;
    	}
    	mergedIntervals.add(previousIntervalData);
    	return mergedIntervals;
	}

	public List getElements() {
        return elements;
    }

    public void setElements(List elements) {
        this.elements = elements;
    }

    public TrimaranObjectFactory getTrimaranObjectFactory() {
        return trimaranObjectFactory;
    }

    public void setTrimaranObjectFactory(TrimaranObjectFactory trimaranObjectFactory) {
        this.trimaranObjectFactory = trimaranObjectFactory;
    }

    public int getValue(int val){
    	int tronquee = 0xC000;
    	int pasTronquee = 0x8000;

    	if(getTrimaranObjectFactory().getTrimaran().isTEPMeter()){	// for the TEP meter
    		if(reactive){	// only the reactive part has possible diff. values
    			reactive = false;
    			if((val&pasTronquee) == 0){		// case no gap
    				if((val&0x4000) == 1){			// check the sign bit
    					val = val-0x8000;		// two's complement ...
    				}
    			}
    			else if((val&tronquee) == 0x8000){	// case we do have a gap
    				val &= 0x3FFF;					// trim the value
    				if((val&0x2000) == 1){				// check the sign bit
    					val = val - 0x4000;			// two's complement ...
    				}
    			}
    		}
    		else{
    			reactive = true;	// the Active value stays the same for the TEP meter
    		}
    	}
    	else if(getTrimaranObjectFactory().getTrimaran().isTECMeter()){ 	// for the TEC meter
    		if((val&pasTronquee) == 0){		// case no gap
    			if(val == (0x8000-1)){
    				val = 0; corrupted = true;
    			}
    		}
    		else if((val&tronquee) == 0x8000){
    			val &= 0x3FFF;
    			if(val == (0x4000-1)){
    				val = 0; corrupted = true;
    			}
    		}
    	}
    	return val;
    }

    public ProfileData getProfileData() {
        return profileData;
    }

    public void setProfileData(ProfileData profileData) {
        this.profileData = profileData;
    }

	/**
	 * @return the elementCount
	 */
	public int getElementCount() {
		return elementCount;
	}

	/**
	 * @param elementCount the elementCount to set
	 */
	public void setElementCount(int elementCount) {
		this.elementCount = elementCount;
	}

	/**
	 * @return the now date
	 */
	protected Date getNow() {
		return now;
	}

	/**
	 * @param set now to a specific date
	 */
	protected void setNow(Date now) {
		this.now = now;
	}

	private List getMeterEvents() {
		return meterEvents;
	}

	private void setMeterEvents(List meterEvents) {
		this.meterEvents = meterEvents;
	}

	private Iterator getElementIterator() {
		return elementIterator;
	}

	private void setElementIterator(Iterator elementIterator) {
		this.elementIterator = elementIterator;
	}


}
