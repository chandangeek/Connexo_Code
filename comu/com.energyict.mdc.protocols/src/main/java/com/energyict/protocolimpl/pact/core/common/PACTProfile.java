/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PACTProfile.java
 *
 * Created on 29 maart 2004, 11:07
 */

package com.energyict.protocolimpl.pact.core.common;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import com.energyict.protocolimpl.pact.core.log.LogInterpreter;
import com.energyict.protocolimpl.pact.core.survey.LoadSurveyInterpreter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class PACTProfile {

    private static final int DEBUG=0;

    ProtocolLink protocolLink;
    PACTRegisterFactory pactRegisterFactory;
    LoadSurveyInterpreter loadSurveyInterpreter=null;
    int nrOfBlocks=-1;
    int nrOfDays=-1;
    byte[] loadSurveyData=null;

    /** Creates a new instance of PACTProfile */
    public PACTProfile(ProtocolLink protocolLink, PACTRegisterFactory pactRegisterFactory) {
        this.protocolLink=protocolLink;
        this.pactRegisterFactory=pactRegisterFactory;
    } // public PACTProfile(ProtocolLink protocolLink)

    /*
     *   retrieve 1 day of profiledata
     */
    public void initChannelInfo() throws IOException {
        if (nrOfBlocks == -1) {
            nrOfBlocks = pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getBlocks()*
                         pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getNrOfChannels();
            nrOfDays = 1;
            if (DEBUG >= 1) {
				System.out.println("initChannelInfo, nrOfBlocks = "+nrOfBlocks);
			}
            setLoadSurveyData(getLoadSurveyRawData());
            authenticateData();
            getLoadSurveyInterpreter().parse(getLoadSurveyData(), protocolLink.getChannelMap(), protocolLink.isStatusFlagChannel());
        }
    }

    private byte[] getLoadSurveyRawData() throws NestedIOException,ConnectionException {
        byte[] data=null;
        if (protocolLink.getPACTMode().isPACTStandard()) {
			data = protocolLink.getPactConnection().getLoadSurveyData(nrOfBlocks);
		} else if (protocolLink.getPACTMode().isPAKNET()) {
			data = protocolLink.getPactConnection().getLoadSurveyDataStream(nrOfBlocks, nrOfDays);
		}
        return data;
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        // f(survey type), calculate nr of blocks to return...
        int tempNrOfBlocks = pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getDays()*
                   pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getBlocks()*
                   pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getNrOfChannels();
        nrOfDays = pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getDays();
        return doGetProfileData(tempNrOfBlocks,includeEvents);
    } // public ProfileData getProfileData(boolean includeEvents)

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        // f(survey type), calculate nr of blocks to return...
        int maxNrOfDaysToRetrieve = pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getDays();
        protocolLink.getLogger().info("Max nr of days to retrieve="+maxNrOfDaysToRetrieve);
        nrOfDays = getLoadSurveyInterpreter().getNrOfDays(from, to) + protocolLink.getForcedRequestExtraDays();
        //int tempNrOfBlocks = getLoadSurveyInterpreter().getNrOfBlocks(from,to);
        int tempNrOfBlocks = nrOfDays*pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getBlocks()*
                        pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getNrOfChannels();
        if (nrOfDays > maxNrOfDaysToRetrieve) {
			nrOfDays = maxNrOfDaysToRetrieve;
		}
        protocolLink.getLogger().info("Retrieving "+nrOfDays+" days, "+tempNrOfBlocks+" blocks");
        ProfileData profileData = doGetProfileData(tempNrOfBlocks,includeEvents);


        if (protocolLink.getForcedRequestExtraDays() == 0) {
            // KV 25082004 Ask profile for N extra days if not all data
            //             received because of day splits...
            // KV 13012005 Limit to max nr of days a load survey can contain AND do not perform a new read if already all days were requested!
            if ((profileData.getIntervalDatas().size()>0) && (nrOfDays<maxNrOfDaysToRetrieve)) {
               Date oldestTimestamp = ((IntervalData)profileData.getIntervalDatas().get(0)).getEndTime();

               if (oldestTimestamp.after(from)) {
                    int nrOfDaysRetrieved = getLoadSurveyInterpreter().getNrOfDays(oldestTimestamp, to);
                    nrOfDays = ((nrOfDays*nrOfDays)/nrOfDaysRetrieved)+5; // calc new nr of days to retrieve based on the already retrieved days...
                    if (nrOfDays > maxNrOfDaysToRetrieve) {
						nrOfDays = maxNrOfDaysToRetrieve;
					}
                    tempNrOfBlocks = nrOfDays*pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getBlocks()*
                                    pactRegisterFactory.getMeterReadingsInterpreter().getSurveyInfo().getNrOfChannels();
                    protocolLink.getLogger().info("Oldest timestamp = "+oldestTimestamp+" is still after "+from+", "+nrOfDaysRetrieved+" days retrieved, retrieve again for "+nrOfDays+" days, "+tempNrOfBlocks+" blocks");
                    profileData = doGetProfileData(tempNrOfBlocks,includeEvents);
               }
            }
        }
        return profileData;

    } // public ProfileData getProfileData(Date from, Date to, boolean includeEvents)

    private ProfileData doGetProfileData(int tempNrOfBlocks, boolean  includeEvents) throws IOException {
        if (tempNrOfBlocks > nrOfBlocks) {
           nrOfBlocks=tempNrOfBlocks;
           loadSurveyInterpreter=null;
           if (DEBUG >= 1) {
			System.out.println("KV_DEBUG>  getProfileData, nrOfBlocks = "+nrOfBlocks);
		}
           setLoadSurveyData(getLoadSurveyRawData());
           authenticateData();
           getLoadSurveyInterpreter().parse(getLoadSurveyData(), protocolLink.getChannelMap(),protocolLink.isStatusFlagChannel());
        }


        if (includeEvents) {
            List meterEvents = getMeterEvents();
            if (meterEvents != null) {
                getLoadSurveyInterpreter().getProfileData().setMeterEvents(meterEvents);
                getLoadSurveyInterpreter().getProfileData().applyEvents(protocolLink.getProfileInterval()/60);
            }
        }
        return getLoadSurveyInterpreter().getProfileData();
    }

    private void authenticateData() throws NestedIOException {
        if ((getLoadSurveyData() != null) && (protocolLink.getPACTToolkit() != null)) {
            pactRegisterFactory.getFileTransfer().appendData(getLoadSurveyData());
            try {
                int encrypted = protocolLink.getPACTToolkit().validateData(pactRegisterFactory.getFileTransfer().getFileName());
                if (encrypted == 1) {
                   setLoadSurveyData(pactRegisterFactory.getFileTransfer().getDecryptedSurveyData());
                }
            }
            catch(IOException e) {
                throw new NestedIOException(e);
            }
        } // if (getProtocolLink().getPACTToolkit() != null)
    } // private void authenticate()

    private List getMeterEvents() throws IOException {
        List meterEvents = null;
        try {
           byte[] data = protocolLink.getPactConnection().getLogData();
           if (data.length != 0) {
              LogInterpreter li = new LogInterpreter(data,protocolLink.getTimeZone());
              meterEvents = li.getMeterEvents();
           }
           else if (DEBUG>=1) {
			System.out.println("KV_DEBUG> Logbook not supported");
		}
        }
        catch(NestedIOException e) {
            if (DEBUG>=1) {
				System.out.println("KV_DEBUG> Logbook not supported");
			}
        }
        return meterEvents;
    }

    public LoadSurveyInterpreter getLoadSurveyInterpreter() throws NestedIOException, ConnectionException {
        if (loadSurveyInterpreter==null) {
            loadSurveyInterpreter = pactRegisterFactory.getMeterReadingsInterpreter().getLoadSurveyInterpreter();
        }
        return loadSurveyInterpreter;
    }

    /** Getter for property loadSurveyData.
     * @return Value of property loadSurveyData.
     *
     */
    private byte[] getLoadSurveyData() {
        return this.loadSurveyData;
    }

    /** Setter for property loadSurveyData.
     * @param loadSurveyData New value of property loadSurveyData.
     *
     */
    private void setLoadSurveyData(byte[] loadSurveyData) {
    	if(loadSurveyData != null){
    		this.loadSurveyData = loadSurveyData;
    	}
    }

} // public class PACTProfile
