/*
 * EZ7CommandFactory.java
 *
 * Created on 13 mei 2005, 13:57
 */

package com.energyict.protocolimpl.emon.ez7.core;

import java.io.*;
import java.util.*;

import com.energyict.cbo.NestedIOException;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.emon.ez7.EZ7;
import com.energyict.protocolimpl.emon.ez7.core.command.*;
import com.energyict.dialer.connection.ConnectionException;

/**
 *
 * @author  Koen
 */
public class EZ7CommandFactory {
    
    
    EZ7 ez7=null;
    
    // cached objects
    ProfileStatus profileStatus = null;
    AllEnergy allEnergy = null;
    AllMaximumDemand allMaximumDemand = null;
    Version version=null;
    RGLInfo rglInfo=null;
    HookUp hookUp=null;
    PowerQuality powerQuality=null;
    EventGeneral eventGeneral=null;
    EventLoad eventLoad=null;
    FlagsStatus flagsStatus=null;
    SlidingKWDemands slidingKWDemands=null;
    ProfileHeader profileHeader=null;
    MeterInformation meterInformation=null;
    IMONInformation imonInformation=null;
            
    // not cached
    RTC rtc=null;
    //ProfileDataCompressed profileDataCompressed=null;
    VerifyKey verifyKey=null;
    SetKey setKey=null;
    /** Creates a new instance of EZ7CommandFactory */
    public EZ7CommandFactory(EZ7 ez7) {
        this.ez7=ez7;
    }

    
    /*****************************************************************************************
     * Generic command methods 
     *****************************************************************************************/
    public GenericValue getGenericValue(int index) throws ConnectionException, IOException {
        switch(index) {
            case 0:
                return getEventGeneral(); // REG
            case 1:
                return getEventLoad(); // REL
            case 2:
                return getFlagsStatus(); // RF
            case 3:
                return getRGLInfo(); // RGL
            default:
                return null;
        } // switch(index)
    } // public GenericValue getGenericValue(int index) throws ConnectionException, IOException
    
    /*****************************************************************************************
     * C A C H E D  O B J E C T S  F R O M  M E T E R
     *****************************************************************************************/
    public ProfileStatus getProfileStatus() throws ConnectionException, IOException {
        if (profileStatus == null) {
           profileStatus = new ProfileStatus(this);
           profileStatus.build();
        }
        return profileStatus;
    }
    
    public AllEnergy getAllEnergy() throws ConnectionException, IOException {
        if (allEnergy == null) {
           allEnergy = new AllEnergy(this);
           allEnergy.build();
        }
        return allEnergy;
    }
    
    public AllMaximumDemand getAllMaximumDemand() throws ConnectionException, IOException {
        if (allMaximumDemand == null) {
           allMaximumDemand = new AllMaximumDemand(this);
           allMaximumDemand.build();
        }
        return allMaximumDemand;
    }
    
    public Version getVersion() throws ConnectionException, IOException {
        if (version == null) {
           version = new Version(this);
           version.build();
        }
        return version;
    }
    
    public RGLInfo getRGLInfo() throws ConnectionException, IOException {
        if (rglInfo == null) {
           rglInfo = new RGLInfo(this);
           rglInfo.build();
        }
        return rglInfo;
    }
    
    public HookUp getHookUp() throws ConnectionException, IOException {
        if (hookUp == null) {
           hookUp = new HookUp(this);
           hookUp.build();
        }
        return hookUp;
    }
    
    public PowerQuality getPowerQuality() throws ConnectionException, IOException {
        if (powerQuality == null) {
           powerQuality = new PowerQuality(this);
           powerQuality.build();
        }
        return powerQuality;
    }
    
    public EventGeneral getEventGeneral() throws ConnectionException, IOException {
        if (eventGeneral == null) {
           eventGeneral = new EventGeneral(this);
           eventGeneral.build();
        }
        return eventGeneral;
    }
    
    public EventLoad getEventLoad() throws ConnectionException, IOException {
        if (eventLoad == null) {
           eventLoad = new EventLoad(this);
           eventLoad.build();
        }
        return eventLoad;
    }
    
    public FlagsStatus getFlagsStatus() throws ConnectionException, IOException {
        if (flagsStatus == null) {
           flagsStatus = new FlagsStatus(this);
           flagsStatus.build();
        }
        return flagsStatus;
    }
    
    public SlidingKWDemands getSlidingKWDemands() throws ConnectionException, IOException {
        if (slidingKWDemands == null) {
           slidingKWDemands = new SlidingKWDemands(this);
           slidingKWDemands.build();
        }
        return slidingKWDemands;
    }
    
    public ProfileHeader getProfileHeader() throws ConnectionException, IOException {
        if (profileHeader == null) {
           profileHeader = new ProfileHeader(this);
           profileHeader.build();
        }
        return profileHeader;
    }
    
    public MeterInformation getMeterInformation() throws ConnectionException, IOException {
        if (meterInformation == null) {
           meterInformation = new MeterInformation(this);
           meterInformation.build();
        }
        return meterInformation;
    }
    
    public IMONInformation getImonInformation() throws ConnectionException, IOException {
        if (imonInformation == null) {
           imonInformation = new IMONInformation(this);
           imonInformation.build();
        }
        return imonInformation;
    }
    
    /*****************************************************************************************
     * U P D A T E D  O B J E C T S  F R O M  M E T E R
     *****************************************************************************************/
    public RTC getRTC() throws ConnectionException, IOException {
        rtc = new RTC(this);
        rtc.build();
        return rtc;
    }
    
    public void setRTC() throws ConnectionException, IOException {
        rtc = new RTC(this);
        rtc.write(ez7.getInfoTypeRoundtripCorrection());
    }
    
    public ProfileDataCompressed getProfileDataCompressed(int dayBlockNr) throws ConnectionException, IOException {
        ProfileDataCompressed profileDataCompressed = new ProfileDataCompressed(this,dayBlockNr);
        profileDataCompressed.build();
        return profileDataCompressed;
    }
    
    public VerifyKey getVerifyKey() throws ConnectionException, IOException {
        verifyKey = new VerifyKey(this);
        verifyKey.build();
        return verifyKey;
    }
    
    public SetKey getSetKey() {
        setKey = new SetKey(this);
        return setKey;
    }
    
    /**
     * Getter for property ez7.
     * @return Value of property ez7.
     */
    public com.energyict.protocolimpl.emon.ez7.EZ7 getEz7() {
        return ez7;
    }
    
    
    
    
}
