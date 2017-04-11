package com.energyict.protocolimpl.edmi.mk6.loadsurvey;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.edmi.common.command.GeniusFileAccessReadCommand;
import com.energyict.protocolimpl.edmi.common.command.GeniusFileAccessSearchCommand;
import com.energyict.protocolimpl.edmi.common.core.AbstractRegisterType;
import com.energyict.protocolimpl.edmi.common.core.RegisterTypeParser;
import com.energyict.protocolimpl.edmi.mk6.registermapping.MK6RegisterInformation;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author koen
 */
public class LoadSurveyData implements Serializable {

    private static final int MAX_DATA_PACKET_SIZE = 2048;

    private LoadSurvey loadSurvey;

    private byte[] data;
    private Date firstTimeStamp;
    private int numberOfRecords;

    /**
     * Creates a new instance of LoadSurveyData
     */
    public LoadSurveyData(LoadSurvey loadSurvey, Date from) throws ProtocolException {
        this.setLoadSurvey(loadSurvey);
        init(from);
    }

    public void init(Date from) throws ProtocolException {
        ByteArrayOutputStream byteArrayOutputStream;
        GeniusFileAccessReadCommand farc;
        GeniusFileAccessSearchCommand fasc;
        int records = 0;
        byteArrayOutputStream = new ByteArrayOutputStream();

        fasc = getLoadSurvey().getCommandFactory().getGeniusFileAccessSearchForwardCommand((getLoadSurvey().getRegisterId() << 16) | MK6RegisterInformation.LOAD_SURVEY_FILE_ACCESS_POINT.getRegisterId(), from);
        long startRecord = fasc.getStartRecord();
        int nrOfRecords2Request = MAX_DATA_PACKET_SIZE / getLoadSurvey().getEntryWidth();

        do {
            farc = getLoadSurvey().getCommandFactory().getGeniusFileAccessReadCommand(
                    (getLoadSurvey().getRegisterId() << 16) | MK6RegisterInformation.LOAD_SURVEY_FILE_ACCESS_POINT.getRegisterId(),
                    startRecord, nrOfRecords2Request, 0,
                    getLoadSurvey().getEntryWidth());
            records += farc.getNumberOfRecords();
            startRecord = farc.getStartRecord() + farc.getNumberOfRecords();
            byteArrayOutputStream.write(farc.getData(), 0, farc.getData().length);

        } while ((getLoadSurvey().getStoredEntries() - (farc.getStartRecord() + farc.getNumberOfRecords())) > 0);

        setNumberOfRecords(records);
        setData(byteArrayOutputStream.toByteArray());
        if (getNumberOfRecords() == 0) {
            return;
        }

        // set first record timestamp...
        Calendar cal = ProtocolUtils.getCleanCalendar(getLoadSurvey().getCommandFactory().getProtocol().getTimeZone());
        if (getLoadSurvey().isEventLog()) {
            setFirstTimeStamp(getChannelValues(0)[1].getDate());
        } else {
            cal.setTime(getLoadSurvey().getStartTime());
            cal.add(Calendar.SECOND, (int) (getLoadSurvey().getStoredEntries() - getNumberOfRecords()) * getLoadSurvey().getProfileInterval());
            
            
            /*
             * There is no reason why the fasc date is used. It's even incorrect if the buffer overflows.
             * That is why the cal.getTime is again used as default.
             * 
             * 26012010 -> By now I probably know why the fasc date was used. If an interval is added in the meter while you 
             * are reading profileData, then the data will be shifted. With the fasc date is will probably not be shifted, 
             * but if you collect data larger then the buffer, then you get incorrect intervals!!!
             */
            if (getLoadSurvey().getCommandFactory().getProtocol().useOldProfileFromDate()) {
                setFirstTimeStamp(fasc.getDate());
            } else {
                setFirstTimeStamp(cal.getTime());
            }
        }
    }

    public int getStatus(int intervalIndex) {
        int offset = intervalIndex * loadSurvey.getEntryWidth();
        return ProtocolTools.getIntFromBytes(getData(), offset, 2);
    }

    private byte[] getData(int intervalIndex, int channelIndex) {
        int offset = intervalIndex * loadSurvey.getEntryWidth() + loadSurvey.getLoadSurveyChannels()[channelIndex].getOffset();
        return ProtocolUtils.getSubArray2(getData(), offset, loadSurvey.getLoadSurveyChannels()[channelIndex].getWidth());
    }

    public AbstractRegisterType[] getChannelValues(int intervalIndex) throws ProtocolException {
        AbstractRegisterType[] channelValues = new AbstractRegisterType[loadSurvey.getNrOfChannels()];
        RegisterTypeParser rtp = new RegisterTypeParser(loadSurvey.getCommandFactory().getProtocol().getTimeZone());
        for (int channel = 0; channel < loadSurvey.getNrOfChannels(); channel++) {
            AbstractRegisterType channelValue = rtp.parse2Internal((char) loadSurvey.getLoadSurveyChannels()[channel].getType(), getData(intervalIndex, channel));
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

    public Date getFirstTimeStamp() {
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