/*
 * Quantum1000Profile.java
 *
 * Created on 8 januari 2007, 8:56
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.EventLogUpload;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.EventRecordType;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.MassMemory;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.MassMemoryConfiguration;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.MassMemoryRecord;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.ReplyException;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.ViewableFileId;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class Quantum1000Profile {
    final int DEBUG=0;

    Quantum1000 quantum1000=null;

    /** Creates a new instance of Quantum1000Profile */
    public Quantum1000Profile(Quantum1000 quantum1000) {
        this.quantum1000=quantum1000;
    }


    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileData profileData=new ProfileData();

        profileData.setChannelInfos(buildChannelInfos());
        profileData.setIntervalDatas(buildIntervalData(lastReading));
        if (includeEvents) {
            profileData.setMeterEvents(buildMeterEvents(lastReading));
            profileData.applyEvents(quantum1000.getProfileInterval()/60);
        }
        return profileData;

    } // public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException

    private List buildMeterEvents(Date lastReading) throws IOException {
        List meterEvents = new ArrayList();

        EventLogUpload elu = quantum1000.getDataDefinitionFactory().getEventLogUpload();

        if (DEBUG>=2) System.out.println("KV_DEBUG> "+elu);

        Iterator it = elu.getEventRecordTypes().iterator();
        Date now = new Date();

        while(it.hasNext()) {
            EventRecordType ert = (EventRecordType)it.next();

            if (ert.getEventTimeStamp().after(lastReading) && ert.getEventTimeStamp().before(now)) {
                if (ert.getEventId().getMeterEvent() != -1) {
                    MeterEvent me = new MeterEvent(ert.getEventTimeStamp(),ert.getEventId().getMeterEvent(),ert.getId(), ert.getEventId().getDescription());
                    meterEvents.add(me);
                }
            }

        } // while(it.hasNext())


        return meterEvents;
    }

    private List buildChannelInfos() throws IOException {
        List channelInfos = new ArrayList();
        MassMemoryConfiguration massMemoryConfiguration=quantum1000.getDataDefinitionFactory().getMassMemoryConfiguration(quantum1000.getMassMemoryId());

        for (int channel = 0; channel < massMemoryConfiguration.getMassMemoryConfigType().getNumberOfChannels();channel++) {
            ChannelInfo chi = new ChannelInfo(channel,"Q1000_"+channel,massMemoryConfiguration.getMassMemoryConfigType().getChannelConfigs()[channel].getQid().getUnit().getVolumeUnit());
            chi.setMultiplier(new BigDecimal(""+massMemoryConfiguration.getMassMemoryConfigType().getChannelConfigs()[channel].getKe()));
            channelInfos.add(chi);


        } // for (int channel = 0; channel < massMemoryConfiguration.getMassMemoryConfigType().getNumberOfChannels();channel++)

        return channelInfos;
    } // private List buildChannelInfos()

    private List buildIntervalData(Date lastReading) throws IOException {
        List intervalDatas = new ArrayList();
        MassMemoryConfiguration massMemoryConfiguration=quantum1000.getDataDefinitionFactory().getMassMemoryConfiguration(quantum1000.getMassMemoryId());
        List massMemories = retrieveMassMemory(lastReading);

        Iterator it = massMemories.iterator();
        while(it.hasNext()) {
            MassMemory massMemory = (MassMemory)it.next();

            for (int record=0;record<massMemory.getNumberRecords();record++) {

                MassMemoryRecord mmr = massMemory.getMassMemoryRecords()[record];

                IntervalData intervalData = new IntervalData(mmr.getDate(),mmr.getEIStatus(),mmr.getStatusBits());

                for (int channel = 0; channel < massMemoryConfiguration.getMassMemoryConfigType().getNumberOfChannels();channel++) {
                    int pulseCount = mmr.getPulseCount()[channel];

                    intervalData.addValue(new Integer(pulseCount));

                } // for (int channel = 0; channel < massMemoryConfiguration.getMassMemoryConfigType().getNumberOfChannels();channel++)

                intervalDatas.add(intervalData);

            } // for (int record=0;record<massMemory.getNumberRecords();record++)

        } // while(it.hasNext())

        return intervalDatas;

    } // private List buildIntervalData(Date lastReading) throws IOException


    private List retrieveMassMemory(Date lastReading) throws IOException {

        List massMemories = new ArrayList();

        MassMemoryConfiguration massMemoryConfiguration=quantum1000.getDataDefinitionFactory().getMassMemoryConfiguration(quantum1000.getMassMemoryId());

        // select the view for the mass memory
        quantum1000.getRemoteProcedureCallFactory().setSourceId();
        //quantum.getRemoteProcedureCallFactory().selectViewId();
        quantum1000.getRemoteProcedureCallFactory().selectDefaultViewId();
        quantum1000.getRemoteProcedureCallFactory().startExclusiveViewSession();

        if (quantum1000.getMassMemoryId()==0)
            quantum1000.getRemoteProcedureCallFactory().selectViewableFileId(ViewableFileId.getMASS_MEMORY_1());
        else if (quantum1000.getMassMemoryId()==1)
            quantum1000.getRemoteProcedureCallFactory().selectViewableFileId(ViewableFileId.getMASS_MEMORY_2());

        quantum1000.getRemoteProcedureCallFactory().setRestrictions();
        quantum1000.getRemoteProcedureCallFactory().maximizeRecsPerRead();

        Date from = lastReading;
        while(true) {
            try {

                if (DEBUG>=1) System.out.println("KV_DEBUG> get massMemory for GreaterThanOrEqualToTime "+from);

                quantum1000.getRemoteProcedureCallFactory().setRecordGreaterThanOrEqualToTime(from);
                MassMemory massMemory = quantum1000.getDataDefinitionFactory().getMassMemory();
                massMemories.add(massMemory);
                if (DEBUG>=2) System.out.println("KV_DEBUG> "+massMemory);
                from = massMemory.getMassMemoryRecords()[(int)massMemory.getNumberRecords()-1].getDate();
                from = new Date(from.getTime()+1000); // add 1 second... massMemoryConfiguration.getMassMemoryConfigType().getIntervalLength()*1000);
            } catch(ReplyException e) {
                if (e.getAbstractReplyDataError().isInvalidObject())
                    return massMemories;
                throw e;
            }
        } // while(true)

    } // private void retrieveMassMemory(Date lastReading) throws IOException


} // public class Quantum1000Profile
