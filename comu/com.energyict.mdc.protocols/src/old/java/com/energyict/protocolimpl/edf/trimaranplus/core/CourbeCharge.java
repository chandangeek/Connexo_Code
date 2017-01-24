/*
 * CourbeCharge.java
 *
 * Created on 2 maart 2007, 9:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.IntervalValue;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.MagicNumberConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private static final int fileOperation=0;
    private static final int debug=0;
    private static final int defaultProfileInterval = 600;
    private static final int blockSize = 1250;
    private static final String debugStartString = "KV_DEBUG> ";
    private static final String saisonMobileString = "saisonMobile=";
    private static final String posteHoraireString = "posteHoraire=";
    private static final String modeString = "mode=";
    private static final String marquageString = "marquage=";

    private TrimaranObjectFactory trimaranObjectFactory;
    private List elements;

    public Date now;
    private ProfileData profileData=null;
    private int elementId,previousElementId;
    private long currentMillis;

    /**
     * This is a shiftable years table. The meter will not contain more then ten years of data,
     * but because the ProfileTimestamp has only a unit from the year in it, we have to be able to
     * loop over a decennium.
     */
    private int[] decenniumYears = new int[10];
    private boolean once = false;

    /** Creates a new instance of CourbeCharge */
    public CourbeCharge(TrimaranObjectFactory trimaranObjectFactory) {
    	this.trimaranObjectFactory = trimaranObjectFactory;
    	setCurrentTime(Calendar.getInstance(getTimeZone()).getTimeInMillis());
    	constructDecenniumTable();
    }

    public String toString() {
         StringBuffer strBuff = new StringBuffer();
         for(int i=0;i<getElements().size();i++) {
             strBuff.append("value "+i+" = 0x"+Integer.toHexString(((Integer)getElements().get(i)).intValue()));
             if (i<(getElements().size()-1)){
                 strBuff.append(", ");
             }
         }
         return strBuff.toString();
    }

    private int getProfileInterval() throws IOException {
       if (getTrimaranObjectFactory() == null) {
		return defaultProfileInterval;
	} else {
		return getTrimaranObjectFactory().getTrimaran().getProfileInterval();
	}
    }

    private TimeZone getTimeZone() {
       if (getTrimaranObjectFactory() == null) {
		return TimeZone.getTimeZone("ECT");
	} else {
		return getTrimaranObjectFactory().getTrimaran().getTimeZone();
	}
    }


    private void initCollections() {
        elementId = blockSize;
        previousElementId = blockSize;
        setElements(new ArrayList());
        setProfileData(new ProfileData());
        getProfileData().addChannel(new ChannelInfo(0,"Trimeran ICE kW channel",Unit.get("kW")));
    }

    private void waitUntilCopied() throws IOException { // max 20 sec
        int retry=0;
        while(retry++<MagicNumberConstants.fifteen.getValue()) {
            try {
                Thread.sleep(MagicNumberConstants.thousand.getValue());
                AccessPartiel ap = getTrimaranObjectFactory().readAccessPartiel();
                if (debug>=2) {
					System.out.println(debugStartString +ap);
				}
                if (ap.getNomAccess() == 0) {
					return;
				}
            }
            catch(InterruptedException e) {
                // absorb
            }
        }
        throw new IOException("CourbeCharge, Error! Already waiting 20 sec for copy of the load profile data!");
    }

    public void collect(Date from) throws IOException {
        Date previousEndTime=null;
        initCollections();
        do {
            retrieve();


            // TODO to test:
            // the last interval endtime wasn't correct so the loop keept looping.
            // hope it changes after we sort the data ...

//            this.profileData.sort();




            // if earliest interval is before the from, leave loop
            if (getProfileData().getIntervalData(0).getEndTime().before(from)) {
				break;
			}
            // safety, if earliest interval is after previous interval, that means we wrap around in the buffer
            if ((previousEndTime != null) && (getProfileData().getIntervalData(0).getEndTime().after(previousEndTime))) {
				break;
			}

            previousEndTime = getProfileData().getIntervalData(0).getEndTime();

            if (debug>=1) {
				System.out.println(debugStartString + "do retrieve() ... while ("+elementId+" <= ("+(MagicNumberConstants.thirty.getValue()*blockSize)+") ?");
			}

            if (elementId==previousElementId) {
                if (debug >= 1) {
					System.out.println("elementId==previousElementId --> break");
				}
                break;
            }
            previousElementId = elementId;

        }  while(elementId<=(MagicNumberConstants.thirty.getValue()*blockSize)); // safety margin of 30 blocks to avoid looping!

        // if the connection of data takes more then the profileinterval, a duplicate interval will occur
        aggregateAndRemoveDuplicates();

        if (debug >= 1) {
			System.out.println(getProfileData());
		}

        if (fileOperation==1) {
            FileOutputStream fos = new FileOutputStream(new File("trimeranplus.bin"));
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeLong(now.getTime());
            for (int i=0;i<getElements().size();i++) {
                dos.writeInt(((Integer)getElements().get(i)).intValue());
            }
            fos.close();
        } // if (FILE_OPERATION==1)
    } //  public void collect(Date from) throws IOException


    private void aggregateAndRemoveDuplicates() {
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

    /**
     * @deprecated
     * @param from
     * @throws IOException
     */
    private void retrieve(Date from) throws IOException {
        now = new Date();
        getTrimaranObjectFactory().writeAccessPartiel(from);
        waitUntilCopied();
        int[] values = getTrimaranObjectFactory().getCourbeChargePartielle().getValues();
        addValues(values);
        doParse(false);
    }

    private void retrieve() throws IOException {
        now = new Date();
        if (debug>=1) {
			System.out.println(debugStartString + "retrieve elementId "+elementId);
		}
        getTrimaranObjectFactory().writeAccessPartiel(elementId);
        waitUntilCopied();
        int[] values = getTrimaranObjectFactory().getCourbeChargePartielle().getValues();
        addValues(values);
        doParse(false);
    } // public void collect(int range) throws IOException


    protected void addValues(int[] values) throws IOException {
        List temp = new ArrayList();
        for (int i = 0; i< values.length; i++) {
            temp.add(new Integer(values[i]));
        }
        getElements().addAll(0, temp);

//        for (int i = 0; i< values.length; i++) {
//            getElements().add(new Integer(values[i]));
//        }
    } // private void addValues(int[] values) throws IOException


    private static final int elementBegin=-1;
    private static final int elementPuissance=0;
    private static final int elementPuissanceTronque=1;
    private static final int elementDatationHeure=2;
    private static final int elementDatationDate=3;
    private static final int elementDatationMinuteSeconde=4;
    private static final int elementDatationPoste=5;


    private static final int statePuissance=0;
    private static final int stateOldTime=1;
    private static final int stateNewTime=2;

    public void doParse(boolean file) throws IOException {
        int previousElement=elementBegin;
        int currentElement=elementBegin;
        int type=0;
        int state=statePuissance;
        Calendar calSetClock=null;
        IntervalData intervalData=null;
        int tariff=0;
        int elementOffset=0;
        Calendar cal=null;
        List meterEvents=new ArrayList();
        List intervalDatas=new ArrayList();
        int i=0;

        if ((fileOperation==1) && (file)) {
            try {
                initCollections();
                File f = new File("trimeranplus.bin");
                FileInputStream fis = new FileInputStream(f);
                DataInputStream dis = new DataInputStream(fis);
                int length = (int)(f.length()-MagicNumberConstants.eight.getValue())/MagicNumberConstants.four.getValue();
                now = new Date(dis.readLong());
                for (int t=0;t<length;t++) {
                    getElements().add(new Integer(dis.readInt()));
                }
                fis.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        } // if (FILE_OPERATION==1)

        // parse values
        cal = null;
        elementOffset = 0;

        if (debug>=2) {
			System.out.println(debugStartString + "load profile up to now="+now);
		}

        Iterator it = getElements().iterator();
        while(it.hasNext()) {
            int val = ((Integer)it.next()).intValue();

            i++;

            if ((val & MagicNumberConstants.h8000.getValue()) == 0) {
                if (debug>=2) {
					System.out.println(debugStartString + +i+", val="+val);
				}
                if (cal != null) {
                    if (isDSTGreyZone(intervalDatas)) {
                        cal.add(Calendar.SECOND, 3600);
                    }

                    cal.add(Calendar.SECOND,getProfileInterval());
                    currentElement=elementPuissance;
                    state = statePuissance;
                    // bit 14..0 Valeur de la puissance sans coupure

                    if (now.after(cal.getTime())) {
                        intervalData = new IntervalData(new Date(cal.getTime().getTime()),0,0,tariff);
                        intervalData.addValue(new Integer(val));
                        intervalDatas.add(intervalData);
                    }
                }
            }
            else if ((val & 0xC000) == MagicNumberConstants.h8000.getValue()) {
                // bit 13..0 Valeur de la puissance avec coupure (e tronquee e)
                val &= 0x3FFF;
                if (debug>=2) {
					System.out.println(debugStartString +i+", shortlong, val="+val);
				}
                if (cal != null) {
                    if (isDSTGreyZone(intervalDatas)) {
                        cal.add(Calendar.SECOND, 3600);
                    }

                    cal.add(Calendar.SECOND,getProfileInterval());
                    currentElement=elementPuissanceTronque;
                    state = statePuissance;

                    if (now.after(cal.getTime())) {
                        intervalData = new IntervalData(new Date(cal.getTime().getTime()),0,0,tariff);
                        intervalData.addValue(new Integer(val));
                        intervalData.addEiStatus(IntervalStateBits.SHORTLONG);
                        if (type == MagicNumberConstants.six.getValue()) {
							intervalData.addEiStatus(IntervalStateBits.POWERUP);
						}
                        intervalDatas.add(intervalData);
                    }
                }
            }
            else if ((val & 0xE000) == 0xC000) {

                currentElement=elementDatationDate;
                // element date
                // bit 12..9 chiffre des unites de l'annee bit 8..5 mois bit 4..0 jour
                int year = (val & 0x1E00) >> MagicNumberConstants.nine.getValue();
                int month = (val & 0x01E0) >> MagicNumberConstants.five.getValue();
                int day = (val & 0x001F);

                if ((elementOffset>0) && (cal == null)) {
                    if (debug>=2) {
						System.out.println(debugStartString + "set calendar date");
					}
                }

                cal = ProtocolUtils.getCleanCalendar(getTimeZone());
                cal.set(Calendar.YEAR, getDecenniumYearTable()[year]);
                cal.set(Calendar.MONTH,month-1);
                cal.set(Calendar.DAY_OF_MONTH,day);

                if (debug>=2) {
					System.out.println(debugStartString + "********************************************* "+i+", cal="+cal.getTime() + ", yearUnit was : " + year);
				}

            } // else if ((val & 0xE000) == 0xC000)
            // ************************************************************************************************************************
            // ************************************************ ELEMENT_DATATION_HEURE ************************************************
            // ************************************************************************************************************************
            else if ((val & 0xF000) == 0xE000) {
                currentElement=elementDatationHeure;
                // element heure
                // bit 11..9 type 8..4 heure bit 3..0 minutes en multiples de Tc
                type = (val & 0x0E00) >> MagicNumberConstants.nine.getValue();
                int hour = (val & 0x01F0) >> MagicNumberConstants.four.getValue();
                int minutes = (val & 0x000F)*(getProfileInterval()/MagicNumberConstants.sixty.getValue());
                //if (((previousElement == ELEMENT_BEGIN) || (previousElement == ELEMENT_DATATION_DATE)) && (cal != null)) {
                if (cal != null) {
                    long temp = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND);
                    long temp2 = hour * 3600 + minutes * 60;
                    if (temp != temp2) {
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.add(Calendar.HOUR_OF_DAY, hour);
                        cal.add(Calendar.MINUTE, minutes);
                    }
                }

                if (debug>=2) {
					System.out.println(debugStartString +i+", type=0x"+Integer.toHexString(type)+", cal="+(cal!=null?""+cal.getTime():"no start calendar"));
				}

/* All empty If - statments
                if (type == 0) { // every hour
                    // heure ronde ou changement de jour tarifaire ; dans le cas d'une heure ronde seule, l'element-date n'est pas
                    // insere ; ce type de marquage n'est fait que s'il n'y a pas d'autre marquage e faire e la meme date
                }
                else if (type == 1) { // timeset
                    // remise e l'heure ou changement d'heure legale ; dans ce cas, deux marquages sont effectues, un avec
                    // l'ancienne heure et un avec la nouvelle heure (element-date et element-heure e chaque fois) ; pour chacun
                    // un enregistrement complementaire est effectue pour donner la valeur des minutes et des secondes de la date
                    // marquee (element-minute/seconde)

                }
                else if (type == 2) {
                    // prise d'effet de changement des valeurs d'une table journaliere (element-date et element-heure)
                }
                else if (type == 3) {
                    // changement de structure annuelle ou de poste horaire (en option Base ou EJP), entree ou sortie de la
                    // periode tarifaire pointe mobile ou changement de saison mobile (en option MODULABLE), changement
                    // de mode (toutes options) ; l'element-date n'est insere que dans le cas de changement de structure annuelle
                    // ou de mode ; un enregistrement complementaire est effectue pour preciser la saison, le poste, la structure
                    // ou le mode suivant le cas (element-poste/structure/mode)
                }
                else if (type == 4) {
                    // prise d'effet de nouvelles valeurs de puissances souscrites (element-date et element-heure)
                }
                else if (type == 5) {
                    // changement de la valeur de la duree de la periode deintegration Tc (element-date et element-heure)
                }
                */
                if (type == MagicNumberConstants.six.getValue()) {

                    intervalData.addEiStatus(IntervalStateBits.POWERDOWN);
                    meterEvents.add(new MeterEvent(cal.getTime(),MeterEvent.POWERUP));
                    // retour de lealimentation reseau apres une coupure ; si la duree de la coupure excede la reserve de marche, la
                    // date enregistree correspond au 1er Janvier 1992, et l'heure enregistree est 00h00
                }
//                else if (type == MagicNumberConstants.seven.getValue()) {
                    // multi-marquage. Dans ce cas, un enregistrement complementaire est effectue pour preciser les marquages.
                    // Le multi-marquage ne concerne pas le marquage de e remise e l'heure e ou de e changement d'heure
                    // legale e qui est effectue independamment du reste.
//                }
            }
            // ************************************************************************************************************************
            // ****************************** ELEMENT_DATATION_MINUTE_SECONDE or ELEMENT_DATATION_POSTE *******************************
            // ************************************************************************************************************************
            else if ((val & 0xF000) == 0xF000) {
                if (previousElement == elementDatationHeure) {
//                    if (type == 0) {
                        // heure ronde ou changement de jour tarifaire ; dans le cas d'une heure ronde seule, l'element-date n'est pas
                        // insere ; ce type de marquage n'est fait que s'il n'y a pas d'autre marquage e faire e la meme date ;
//                    }
                    if (type == 1) {

                        currentElement=elementDatationMinuteSeconde;

                        if ((previousElement == elementDatationHeure) && (state == statePuissance)) {
                             currentElement=elementDatationMinuteSeconde;
                             int minute = (val & 0x0FC0)>>6;
                             int seconde = (val & 0x003F);
                             calSetClock = (Calendar)cal.clone();
                             calSetClock.set(Calendar.MINUTE,minute);
                             calSetClock.set(Calendar.SECOND,seconde);
                             meterEvents.add(new MeterEvent(calSetClock.getTime(),MeterEvent.SETCLOCK_BEFORE));
                             state = stateOldTime;
                             if (debug>=2) {
								System.out.println(debugStartString +i+", minute="+minute+", seconde="+seconde);
							}
                        }
                        else if ((previousElement == elementDatationHeure) && (state == stateOldTime)) {
                             currentElement=elementDatationMinuteSeconde;
                             int minute = (val & 0x0FC0)>>6;
                             int seconde = (val & 0x003F);
                             calSetClock = (Calendar)cal.clone();
                             calSetClock.set(Calendar.MINUTE,minute);
                             calSetClock.set(Calendar.SECOND,seconde);
                             meterEvents.add(new MeterEvent(calSetClock.getTime(),MeterEvent.SETCLOCK_AFTER));
                             state = stateNewTime;
                             if (debug>=2) {
								System.out.println(debugStartString +i+", minute="+minute+", seconde="+seconde);
							}
                        }
                        else if ((previousElement == elementDatationMinuteSeconde) && (state == stateNewTime)) {
                             currentElement=elementDatationPoste;
                             int saisonMobile = (val & 0x0C00)>>10;
                             int posteHoraire = (val & 0x0300)>>8;
                             int a = (val & 0x0080)>>7;
                             int mode = (val & 0x0040)>>6;
                             int marquage = (val & 0x003F);
                             state = statePuissance;
                             if (debug>=2) {
								System.out.println(debugStartString +i+", " + saisonMobileString +saisonMobile+", " + posteHoraireString +posteHoraire+", a="+a+", " + modeString +mode+", " + marquageString +marquage);
							}
                             tariff=val&0xFFF;
                             //getMeterEvents().add(new MeterEvent(calSetClock.getTime(),MeterEvent.OTHER,val&0xFFF));
                        }
                    }
                    else if (type == 2) {
                        currentElement=elementDatationPoste;
                        int saisonMobile = (val & 0x0C00)>>10;
                        int posteHoraire = (val & 0x0300)>>8;
                        int a = (val & 0x0080)>>7;
                        int mode = (val & 0x0040)>>6;
                        int marquage = (val & 0x003F);
                        tariff=val&0xFFF;
                        if (debug>=2) {
							System.out.println(debugStartString +i+", " + saisonMobileString +saisonMobile+", " + posteHoraireString +posteHoraire+", a="+a+", " + modeString +mode+", " + marquageString +marquage);
						}
                    }
                    else if (type == MagicNumberConstants.three.getValue()) {
                        currentElement=elementDatationPoste;
                        int saisonMobile = (val & 0x0C00)>>10;
                        int posteHoraire = (val & 0x0300)>>8;
                        int a = (val & 0x0080)>>7;
                        int mode = (val & 0x0040)>>6;
                        int marquage = (val & 0x003F);
                        tariff=val&0xFFF;
                        if (debug>=2) {
							System.out.println(debugStartString +i+", " + saisonMobileString +saisonMobile+", " + posteHoraireString +posteHoraire+", a="+a+", " + modeString +mode+", " + marquageString +marquage);
						}
                    }
                    else if (type == MagicNumberConstants.four.getValue()) {
                        currentElement=elementDatationPoste;
                        int saisonMobile = (val & 0x0C00)>>10;
                        int posteHoraire = (val & 0x0300)>>8;
                        int a = (val & 0x0080)>>7;
                        int mode = (val & 0x0040)>>6;
                        int marquage = (val & 0x003F);
                        tariff=val&0xFFF;
                        if (debug>=2) {
							System.out.println(debugStartString +i+", " + saisonMobileString +saisonMobile+", " + posteHoraireString +posteHoraire+", a="+a+", " + modeString +mode+", " + marquageString +marquage);
						}
                    }
                    else if (type == MagicNumberConstants.five.getValue()) {
                        currentElement=elementDatationPoste;
                        int saisonMobile = (val & 0x0C00)>>10;
                        int posteHoraire = (val & 0x0300)>>8;
                        int a = (val & 0x0080)>>7;
                        int mode = (val & 0x0040)>>6;
                        int marquage = (val & 0x003F);
                        tariff=val&0xFFF;
                        if (debug>=2) {
							System.out.println(debugStartString +i+", " + saisonMobileString +saisonMobile+", " + posteHoraireString +posteHoraire+", a="+a+", " + modeString +mode+", " + marquageString +marquage);
						}
                    }
                    else if (type == MagicNumberConstants.six.getValue()) {
                        currentElement=elementDatationPoste;
                        int saisonMobile = (val & 0x0C00)>>10;
                        int posteHoraire = (val & 0x0300)>>8;
                        int a = (val & 0x0080)>>7;
                        int mode = (val & 0x0040)>>6;
                        int marquage = (val & 0x003F);
                        tariff=val&0xFFF;
                        if (debug>=2) {
							System.out.println(debugStartString +i+", " + saisonMobileString +saisonMobile+", " + posteHoraireString +posteHoraire+", a="+a+", " + modeString +mode+", " + marquageString +marquage);
						}
                    }
                    else if (type == MagicNumberConstants.seven.getValue()) {
                        currentElement=elementDatationPoste;
                        int saisonMobile = (val & 0x0C00)>>10;
                        int posteHoraire = (val & 0x0300)>>8;
                        int a = (val & 0x0080)>>7;
                        int mode = (val & 0x0040)>>6;
                        int marquage = (val & 0x003F);
                        tariff=val&0xFFF;
                        if (debug>=2) {
							System.out.println(debugStartString +i+", " + saisonMobileString +saisonMobile+", " + posteHoraireString +posteHoraire+", a="+a+", " + modeString +mode+", " + marquageString +marquage);
						}
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

        if (debug>=1) {
			System.out.println("doParse(), elementId="+elementId+" elementOffset="+elementOffset);
		}

        elementId = elementId + (blockSize-elementOffset);

        if (debug>=1) {
			System.out.println("doParse(), elementId="+elementId+" --> elementId + (" + blockSize + "-elementOffset)");
		}

        getProfileData().setMeterEvents(meterEvents);
        getProfileData().setIntervalDatas(intervalDatas);
        getProfileData().sort();

        if ((fileOperation==1) && (file)) {
			aggregateAndRemoveDuplicates();
		}

        if (debug >= 1) {
			System.out.println(getProfileData());
		}

    } // public void doParse() throws IOException

    private boolean isDSTGreyZone(List intervalDatas) {
        int index1 = intervalDatas.size() - 1;
        int index2 = intervalDatas.size() - 2;
        if (index1 < 0 || index2 < 0) {
            return false;
        }

        IntervalData intervalDataNew = (IntervalData) (intervalDatas.get(index1));
        IntervalData intervalDataOld = (IntervalData) (intervalDatas.get(index2));

        boolean newDst = getTimeZone().inDaylightTime(intervalDataNew.getEndTime());
        boolean oldDst = getTimeZone().inDaylightTime(intervalDataOld.getEndTime());

        boolean result = (oldDst) && (!newDst);
        if (result && !once) {
            once = true;
            return result;
        }
        return result && !once;           //True when new interval is no longer in DST
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

    public ProfileData getProfileData() {
        return profileData;
    }

    public void setProfileData(ProfileData profileData) {
        this.profileData = profileData;
    }

	/**
	 * Setter for the currentMillis
	 *
	 * @param currentTimeInMillis
	 */
	protected void setCurrentTime(long currentTimeInMillis) {
		this.currentMillis = currentTimeInMillis;
	}

	/**
	 * Getter for the currentMillis
	 * @return the current millis
	 */
	private long getCurrentMillis(){
		return this.currentMillis;
	}

	/**
	 * Constructs a table of the last TEN years.
	 */
	protected void constructDecenniumTable(){
		Calendar cal = Calendar.getInstance(getTimeZone());
		cal.setTimeInMillis(getCurrentMillis());
		int year = cal.get(Calendar.YEAR);
		int yearUnit = year%10;
		for(int i = 0; i < 10; i++){
			this.decenniumYears[yearUnit] = year--;
			if(yearUnit == 0){
				yearUnit = 9;
			} else {
				yearUnit--;
			}
		}
	}

	/**
	 * Getter for the decenniumYear table
	 * @return the current decenniumYears table
	 */
	protected int[] getDecenniumYearTable(){
		return this.decenniumYears;
	}
}
