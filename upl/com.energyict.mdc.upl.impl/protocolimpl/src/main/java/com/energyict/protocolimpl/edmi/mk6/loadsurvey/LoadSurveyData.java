/*
 * LoadSurveyData.java
 *
 * Created on 3 april 2006, 11:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.loadsurvey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edmi.mk6.command.FileAccessReadCommand;
import com.energyict.protocolimpl.edmi.mk6.command.FileAccessSearchCommand;
import com.energyict.protocolimpl.edmi.mk6.core.AbstractRegisterType;
import com.energyict.protocolimpl.edmi.mk6.core.RegisterTypeParser;

/**
 *
 * @author koen
 */
public class LoadSurveyData implements Serializable{
    
    /** Generated SerialVersionUID */
	private static final long serialVersionUID = -1983477633172525763L;

	private final int DEBUG=0;
    
    private LoadSurvey loadSurvey;
    
    private byte[] data;
    private Date firstTimeStamp;
    private int numberOfRecords;
    
    /** Creates a new instance of LoadSurveyData */
    public LoadSurveyData(LoadSurvey loadSurvey,Date from) throws IOException {
        this.setLoadSurvey(loadSurvey);
        init(from);
    }
    
    
    public void init(Date from) throws IOException {
        
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> LoadSurveyData, init() getLoadSurvey()="+getLoadSurvey());
		}
        
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.reset();
        
        FileAccessSearchCommand fasc = getLoadSurvey().getCommandFactory().getFileAccessSearchForwardCommand((getLoadSurvey().getRegisterId()<<16)|0x5F008, from);
        long startRecord = fasc.getStartRecord();
        int records = 0;
        
        FileAccessReadCommand farc=null;
         
        // KV_TO_DO Int  The EDMI meter crashes when ist calculates to high buffer.
        int nrOfRecords2Request = 2013 / getLoadSurvey().getEntryWidth();
        do {
//            if (DEBUG>=1) System.out.println("KV_DEBUG> FR startRecord "+startRecord+", getLoadSurvey().getNrOfEntries() "+getLoadSurvey().getNrOfEntries()+" actual "+nrOfRecords2Request+", 0, getLoadSurvey().getEntryWidth() "+getLoadSurvey().getEntryWidth());
//            farc = getLoadSurvey().getCommandFactory().getFileAccessReadCommand((getLoadSurvey().getRegisterId()<<16)|0x5F008, startRecord, getLoadSurvey().getNrOfEntries(), 0, getLoadSurvey().getEntryWidth());
            farc = getLoadSurvey().getCommandFactory().getFileAccessReadCommand((getLoadSurvey().getRegisterId()<<16)|0x5F008, startRecord, nrOfRecords2Request, 0, getLoadSurvey().getEntryWidth());
            if (DEBUG>=1) {
				System.out.println("KV_DEBUG> FR response startRecord "+farc.getStartRecord()+", farc.getNumberOfRecords() "+farc.getNumberOfRecords()+", farc.getRecordOffset() "+farc.getRecordOffset()+", farc.getRecordSize() "+farc.getRecordSize());
			}
            records += farc.getNumberOfRecords();
            startRecord = farc.getStartRecord()+farc.getNumberOfRecords();
            byteArrayOutputStream.write(farc.getData(),0,farc.getData().length);
            
        }  while((getLoadSurvey().getStoredEntries()  - (farc.getStartRecord() + farc.getNumberOfRecords())) > 0);
        
        
        setNumberOfRecords(records);
        setData(byteArrayOutputStream.toByteArray());

        // set first record timestamp...
        
        Calendar cal = ProtocolUtils.getCleanCalendar(getLoadSurvey().getCommandFactory().getMk6().getTimeZone());
        if (getLoadSurvey().isEventLog()) {
            setFirstTimeStamp(getChannelValues(0)[1].getDate());
        }
        else {
            cal.setTime(getLoadSurvey().getStartTime());
            if (DEBUG>=1) {
				System.out.println("KV_DEBUG> getLoadSurvey().getStoredEntries()="+getLoadSurvey().getStoredEntries()+", getNumberOfRecords()="+getNumberOfRecords());
			}
            if (DEBUG>=1) {
				System.out.println("KV_DEBUG> load survey start timestamp ="+cal.getTime());
			}
            cal.add(Calendar.SECOND,(int)(getLoadSurvey().getStoredEntries() - getNumberOfRecords())*getLoadSurvey().getProfileInterval());
            /*
             * There is no reason why the fasc date is used. It's even incorrect if the buffer overflows.
             * Thats why the cal.getTime is again used as default.
             */
            if(getLoadSurvey().getCommandFactory().getMk6().useOldProfileFromDate()){
            	setFirstTimeStamp(fasc.getDate());
            } else {
            	setFirstTimeStamp(cal.getTime());
            }
            
        }
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> first entry timestamp cal ="+getFirstTimeStamp());
			System.out.println("KV_DEBUG> first entry timestamp fasc ="+fasc.getDate());
		}
    } // private void init(Date from) throws IOException

    public String toString() {
        try {
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("LoadSurveyData:\n");
            strBuff.append("    file search:\n");
            strBuff.append("        first record TimeStamp="+getFirstTimeStamp()+"\n");
            strBuff.append("    file read:\n");
            strBuff.append("        actual nr of records read:"+getNumberOfRecords()+"\n");
            for (int interval=0;interval<getNumberOfRecords();interval++) {
               strBuff.append("        interval "+interval+": ");
               for (int channel=0;channel<loadSurvey.getNrOfChannels();channel++) {
                   if (getLoadSurvey().isEventLog()) {
					strBuff.append(getChannelValues(interval)[channel].getString()+" ");
				} else {
					strBuff.append(getChannelValues(interval)[channel].getBigDecimal().multiply(loadSurvey.getLoadSurveyChannels()[channel].getScalingFactor())+"("+loadSurvey.getLoadSurveyChannels()[channel].getUnit()+") ");
				}   
               }
               strBuff.append("\n");
            }
            return strBuff.toString();
        }
        catch(IOException e) {
            return e.toString();
        }
        
    } // public String toString()
    
    // loadSurvey.getProfileInterval() == 0 --> event log!, timestamp as separate field
    
    public int getStatus(int intervalIndex) throws IOException {
        int offset = intervalIndex * loadSurvey.getEntryWidth();
        return ProtocolUtils.getInt(getData(),offset,2);
    }
    
    private byte[] getData(int intervalIndex, int channelIndex) throws IOException {
        int offset = intervalIndex * loadSurvey.getEntryWidth() + loadSurvey.getLoadSurveyChannels()[channelIndex].getOffset();
        return ProtocolUtils.getSubArray2(getData(), offset, loadSurvey.getLoadSurveyChannels()[channelIndex].getWidth());
    }
    
    public AbstractRegisterType[] getChannelValues(int intervalIndex) throws IOException {
        AbstractRegisterType[] channelValues = new AbstractRegisterType[loadSurvey.getNrOfChannels()];
        RegisterTypeParser rtp = new RegisterTypeParser(loadSurvey.getCommandFactory().getMk6().getTimeZone());
        for (int channel=0;channel<loadSurvey.getNrOfChannels();channel++) {
            AbstractRegisterType channelValue = rtp.parse2Internal((char)loadSurvey.getLoadSurveyChannels()[channel].getType(), getData(intervalIndex, channel));
            channelValues[channel] = channelValue;
        }
        return channelValues;
    }
    
    public LoadSurvey getLoadSurvey() {
        return loadSurvey;
    }

    public void setLoadSurvey(LoadSurvey loadSurvey) {
        this.loadSurvey = loadSurvey;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Date getFirstTimeStamp(){
        return this.firstTimeStamp;
    }

    public void setFirstTimeStamp(Date firstTimeStamp) {
        this.firstTimeStamp = firstTimeStamp;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }
}
