/*
 * PACTLoadProfile.java
 *
 * Created on 11 maart 2004, 14:16
 */

package com.energyict.protocolimpl.pact.core.survey;

import java.io.IOException;
import java.util.*;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.pact.core.common.*;
/**
 *
 * @author  Koen 
 */
public interface LoadSurveyInterpreter {
    public ProfileData getProfileData();
    public void parse(byte[] loadSurveyData, ChannelMap channelMap, boolean statusFlagchannel) throws IOException;
    public String toString();
    public int[] getEnergyTypeCodes();
    public int getEnergyTypeCode(int channel);
    public int getNrOfSurveyChannels();
    public int getNrOfBlocks(Date from, Date to) throws IOException;
    public int getNrOfDays(Date from, Date to) throws IOException;
}
