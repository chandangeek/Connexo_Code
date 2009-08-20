/*
 * PACTLoadProfile.java
 *
 * Created on 11 maart 2004, 14:16
 */

package com.energyict.protocolimpl.pact.core.survey;

import java.io.IOException;
import java.util.Date;

import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.pact.core.common.ChannelMap;

/**
 * 
 * @author Koen
 */
public interface LoadSurveyInterpreter {

	ProfileData getProfileData();

	void parse(byte[] loadSurveyData, ChannelMap channelMap, boolean statusFlagchannel) throws IOException;

	String toString();

	int[] getEnergyTypeCodes();

	int getEnergyTypeCode(int channel);

	int getNrOfSurveyChannels();

	int getNrOfBlocks(Date from, Date to) throws IOException;

	int getNrOfDays(Date from, Date to) throws IOException;

}
