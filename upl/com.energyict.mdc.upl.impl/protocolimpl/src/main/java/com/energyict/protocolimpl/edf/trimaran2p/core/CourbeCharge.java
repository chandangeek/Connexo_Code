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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author Koen
 */
public class CourbeCharge {
    
    final int DEBUG=0;
     
    private TrimaranObjectFactory trimaranObjectFactory;
    private List elements;
    private Date now;
    private int elementCount = 0;
    
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
    
    protected void initCollections() throws IOException {
        setElements(new ArrayList());
        setProfileData(new ProfileData());
        getProfileData().addChannel((new ChannelInfo(0,"Trimaran 2P P+ channel", Unit.get("kW"), 0, 0, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        getProfileData().addChannel((new ChannelInfo(1,"Trimaran 2P P- channel", Unit.get("kW"), 0, 1, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        if(getTrimaranObjectFactory().readParameters().isCcReact()){
        	 getProfileData().addChannel((new ChannelInfo(2,"Trimaran 2P Q1(Q+) channel", Unit.get("kvar"), 0, 2, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        	 getProfileData().addChannel((new ChannelInfo(3,"Trimaran 2P Q3(Q-) channel", Unit.get("kvar"), 0, 3, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        	 getProfileData().addChannel((new ChannelInfo(4,"Trimaran 2P Q2(Q+) channel", Unit.get("kvar"), 0, 4, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        	 getProfileData().addChannel((new ChannelInfo(5,"Trimaran 2P Q4(Q-) channel", Unit.get("kvar"), 0, 5, new BigDecimal(Math.pow(10, (getTrimaranObjectFactory().readParameters().getKep()))))));
        }
    }
    
    private void waitUntilCopied() throws IOException { // max 20 sec
        int retry=0;
        while(retry++<15) {
            try {
                Thread.sleep(1000);
                AccessPartiel ap = getTrimaranObjectFactory().readAccessPartiel();
                if (DEBUG>=2) System.out.println("GN_DEBUG> "+ap);
                if (ap.getNomAccess() == 0)
                    return;
            }
            catch(InterruptedException e) {
                // absorb
            }
        }
        throw new IOException("CourbeCharge, Error! Already waiting 20 sec for copy of the load profile data!");
    }
    
    public void collect(Date from) throws IOException {
//        Date previousEndTime=null;
    	now = new Date();
    	Date collectTime = from;
        initCollections();
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
        int[] values = getTrimaranObjectFactory().getCourbeChargePartielle1().getValues();
        
//    	System.out.println("GN_DEBUG> write to file");
//    	File file = new File("c://TEST_FILES/Object_Values_0406_long" + fileCounter+".bin");
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
//    	File file = new File("c://TEST_FILES/Object_Values_0406.bin");
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
        
        boolean dontAdd = false;
        
        Calendar calSetClock=null;
        Calendar cal = null;
        IntervalData intervalData=null;
        List meterEvents=new ArrayList();
        List intervalDatas=new ArrayList();
        
        if (DEBUG>=2) System.out.println("GN_DEBUG> load profile up to now="+now);
        
        Iterator it = getElements().iterator();
        while(it.hasNext()) {
            int val = ((Integer)it.next()).intValue();
            
            if (DEBUG>=2) i++;
            
            if ((val & 0x8000) == 0) {
                if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", val="+val);
                if (cal != null) {
                    cal.add(Calendar.SECOND,getProfileInterval());
                    currentElement=ELEMENT_PUISSANCE;
                    state = STATE_PUISSANCE;
                    // bit 14..0 Valeur de la puissance sans coupure

                    if (now.after(cal.getTime())) {
                        intervalData = new IntervalData(new Date(cal.getTime().getTime()),0,0,tariff);
                        intervalData.addValue(new Integer(val));
                        for(int j = 0; j < getTrimaranObjectFactory().getTrimaran().getNumberOfChannels() - 1; j++){
//                        for(int j = 0; j < 5; j++){
                        	if(it.hasNext()){
	                        	val = ((Integer)it.next()).intValue();
	                        	 if (DEBUG>=2) i++;
	                        	intervalData.addValue(new Integer(val));
                        	}
                        	else
                        		dontAdd = true;
                        }
                        if(!dontAdd){
                        	intervalDatas.add(intervalData);
                        }
                        else
                        	dontAdd = false;
                    }
                }
            }
            else if ((val & 0xC000) == 0x8000) {
                // bit 13..0 Valeur de la puissance avec coupure (� tronqu�e �)
                val &= 0x3FFF;
                if (DEBUG>=2) System.out.println("KV_DEBUG> "+i+", shortlong, val="+val);
                if (cal != null) {
                    cal.add(Calendar.SECOND,getProfileInterval());
                    currentElement=ELEMENT_PUISSANCE_TRONQUE;
                    state = STATE_PUISSANCE;

                    if (now.after(cal.getTime())) {
                        intervalData = new IntervalData(new Date(cal.getTime().getTime()),0,0,tariff);
                        intervalData.addValue(new Integer(val));
                        intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
                        if (type == 6) intervalData.addEiStatus(IntervalStateBits.POWERUP);;
                        intervalDatas.add(intervalData);
                    }
                }
            }
            else if ((val & 0xE000) == 0xC000) {
                
                currentElement=ELEMENT_DATATION_DATE;
                // element date
                // bit 12..9 chiffre des unit�s de l'ann�e bit 8..5 mois bit 4..0 jour
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
                
            } // else if ((val & 0xE000) == 0xC000)
            // ************************************************************************************************************************ 
            // ************************************************ ELEMENT_DATATION_HEURE ************************************************
            // ************************************************************************************************************************ 
            else if ((val & 0xF000) == 0xE000) {
                currentElement=ELEMENT_DATATION_HEURE;
                // element heure
                // bit 11..9 type 8..4 heure bit 3..0 minutes en multiples de Tc 
                type = (val & 0x0E00) >> 9;
                int hour = (val & 0x01F0) >> 4;
                int minutes = (val & 0x000F)*(getProfileInterval()/60);
                //if (((previousElement == ELEMENT_BEGIN) || (previousElement == ELEMENT_DATATION_DATE)) && (cal != null)) {
                if (cal != null) {
                    cal.set(Calendar.HOUR_OF_DAY,hour);
                    cal.set(Calendar.MINUTE,minutes);
                }
                
                if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", type=0x"+Integer.toHexString(type)+", cal="+(cal!=null?""+cal.getTime():"no start calendar"));
                
                if (type == 0) { // every hour
                    // heure ronde ou changement de jour tarifaire ; dans le cas d'une heure ronde seule, l'�l�ment-date n'est pas
                    // ins�r� ; ce type de marquage n'est fait que s'il n'y a pas d'autre marquage � faire � la m�me date
                }
                else if (type == 1) { // timeset
                    // remise � l'heure ou changement d'heure l�gale ; dans ce cas, deux marquages sont effectu�s, un avec
                    // l'ancienne heure et un avec la nouvelle heure (�l�ment-date et �l�ment-heure � chaque fois) ; pour chacun
                    // un enregistrement compl�mentaire est effectu� pour donner la valeur des minutes et des secondes de la date
                    // marqu�e (�l�ment-minute/seconde)
                }
                else if (type == 2) {
                    // suppressions des puissances réactives
                }
                else if (type == 3) {
                    // introductions des puissances réactives
                }
                else if (type == 4) {
                    // changements de valeur de paramètres (TC, TT, KJ, KF, KPr, RL, XL, Kep)
                }
                else if (type == 5) {
                    // changement de valeur de Tc(élément date et heure)
                }
                else if (type == 6) {
                    
                    intervalData.addEiStatus(IntervalStateBits.POWERDOWN);
                    meterEvents.add(new MeterEvent(cal.getTime(),MeterEvent.POWERUP));
                    // retour de l�alimentation r�seau apr�s une coupure ; si la dur�e de la coupure exc�de la r�serve de marche, la
                    // date enregistr�e correspond au 1er Janvier 1992, et l'heure enregistr�e est 00h00    
                }
                else if (type == 7) {
                    // multi-marquage. Dans ce cas, un enregistrement compl�mentaire est effectu� pour pr�ciser les marquages.
                    // Le multi-marquage ne concerne pas le marquage de � remise � l'heure � ou de � changement d'heure
                    // l�gale � qui est effectu� ind�pendamment du reste.
                }
            }
            // ************************************************************************************************************************ 
            // ****************************** ELEMENT_DATATION_MINUTE_SECONDE or ELEMENT_DATATION_POSTE *******************************
            // ************************************************************************************************************************ 
            else if ((val & 0xF000) == 0xF000) {
                if (previousElement == ELEMENT_DATATION_HEURE) {
                    if (type == 0) {
                        // heure ronde ou changement de jour tarifaire ; dans le cas d'une heure ronde seule, l'�l�ment-date n'est pas
                        // ins�r� ; ce type de marquage n'est fait que s'il n'y a pas d'autre marquage � faire � la m�me date ;
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
                             //getMeterEvents().add(new MeterEvent(calSetClock.getTime(),MeterEvent.OTHER,val&0xFFF));
                        }
                    }
                    else if (type == 2) {
                        currentElement=ELEMENT_DATATION_POSTE;
                        int mode = (val & 0x0040) >> 6;
                        int config = (val & 0x0E00) >> 9;
                        int marquage = (val & 0x003F);
                        state = STATE_PUISSANCE;
                        if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", Mode="+mode+", config="+config+", marquage="+marquage);
                    }
                    else if (type == 3) {
                        currentElement=ELEMENT_DATATION_POSTE;
                        int mode = (val & 0x0040) >> 6;
                        int config = (val & 0x0E00) >> 9;
                        int marquage = (val & 0x003F);
                        state = STATE_PUISSANCE;
                        if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", Mode="+mode+", config="+config+", marquage="+marquage);
                    }
                    else if (type == 4) {
                        currentElement=ELEMENT_DATATION_POSTE;
                        int mode = (val & 0x0040) >> 6;
                        int config = (val & 0x0E00) >> 9;
                        int marquage = (val & 0x003F);
                        state = STATE_PUISSANCE;
                        if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", Mode="+mode+", config="+config+", marquage="+marquage);
                    }
                    else if (type == 5) {
                        currentElement=ELEMENT_DATATION_POSTE;
                        int mode = (val & 0x0040) >> 6;
                        int config = (val & 0x0E00) >> 9;
                        int marquage = (val & 0x003F);
                        state = STATE_PUISSANCE;
                        if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", Mode="+mode+", config="+config+", marquage="+marquage);
                    }
                    else if (type == 6) {
                        currentElement=ELEMENT_DATATION_POSTE;
                        int mode = (val & 0x0040) >> 6;
                        int config = (val & 0x0E00) >> 9;
                        int marquage = (val & 0x003F);
                        state = STATE_PUISSANCE;
                        if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", Mode="+mode+", config="+config+", marquage="+marquage);
                    }
                    else if (type == 7) {
                        int mode = (val & 0x0040) >> 6;
                        int config = (val & 0x0E00) >> 9;
                        int marquage = (val & 0x003F);
                        state = STATE_PUISSANCE;
                        if (DEBUG>=2) System.out.println("GN_DEBUG> "+i+", Mode="+mode+", config="+config+", marquage="+marquage);
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
                it.remove();
            }
            
        } // while(count<getValues().length)  
        
        getProfileData().setMeterEvents(meterEvents);
        getProfileData().getIntervalDatas().addAll(intervalDatas);
        getProfileData().sort();
        if (DEBUG >= 2) System.out.println(getProfileData());
        
    } // public void doParse() throws IOException
    

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
    
    static public void main(String[] args) {
        try {
            CourbeCharge cc = new CourbeCharge(new TrimaranObjectFactory(null));
            
			FileInputStream fis;
			File file = new File("c://TEST_FILES/Object_Values_0406_long0.bin");
			fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			int[] values = (int[])ois.readObject();
			ois.close();
			fis.close();    
			cc.initCollections();
            cc.addValues(values);
            cc.now = new Date();
            cc.doParse();
            
			file = new File("c://TEST_FILES/Object_Values_0406_long1.bin");
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			values = (int[])ois.readObject();
			ois.close();
			fis.close(); 
            cc.addValues(values);
            cc.doParse();
            
        }
        catch(IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
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

    
}
